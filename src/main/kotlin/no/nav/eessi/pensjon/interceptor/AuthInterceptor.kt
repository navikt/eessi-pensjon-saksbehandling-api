package no.nav.eessi.pensjon.interceptor

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.logging.AuditLogger
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.auth.AdRolle
import no.nav.eessi.pensjon.services.auth.AuthorisationService
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.services.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.services.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.services.whitelist.WhitelistService
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.getToken
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.Ordered
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthInterceptor(private val ldapService: BrukerInformasjonService,
    private val authorisationService: AuthorisationService,
    private val oidcRequestContextHolder: OIDCRequestContextHolder,
    private val auditLogger: AuditLogger,
    private val whitelistService: WhitelistService,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) : HandlerInterceptor, Ordered {

    private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)

    private val userinfoPath = "/api/userinfo"

    private val regexNavident  = Regex("^[a-zA-Z]\\d{6}$")
    private val regexBorger = Regex("^\\d{11}$")

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (handler is HandlerMethod) {
            // Vi sjekker om det er en annotasjon av typen EessiPensjonTilgang
            // Hvis den er der så skal vi sjekke om pålogget saksbehandler
            // har tilgang til tjenesten som blir kalt
            val eessiPensjonTilgang = handler.getMethodAnnotation(EessiPensjonTilgang::class.java)
            if (eessiPensjonTilgang != null) {
                // Skal sjekke tilgang til tjenesten som kalles
                sjekkTilgangTilUserinfo(request)
                return sjekkTilgangTilEessiPensjonTjeneste()
            }
        }
        return true

    }

    fun sjekkTilgangTilUserinfo(request: HttpServletRequest) {
        logger.debug("reqestURI: ${request.requestURI}  localvar: $userinfoPath")
        if (request.requestURI == userinfoPath) {
            try {
                getToken(oidcRequestContextHolder)
            } catch (rx: RuntimeException) {
                logger.warn("Bruker har ingen token, kaster 401 så vi får logget bruker inn via forgeRock oidctoken")
                throw AuthorisationIkkeTilgangTilEeessiPensjonException("Ingen gyldig token")
            }
        }
    }

    /**
     * Tilgangen til en tjenesten skal kalles hvis den er annotert med "EessiPensjonTilgang". Ved annotering skal
     * denne funksjonen kalles. Funksjonen har ansvar for å finne frem til kontekst og så kalle de enkelte
     * tilgangskontrollene for tilgang til
     *      o EESSI-Pensjon
     *      o PESYS sak
     *      o Brukere
     *      o BUC
     */
    fun sjekkTilgangTilEessiPensjonTjeneste(): Boolean{
        return metricsHelper.measure("authInterceptor") {

            // Er bruker det samme som saksbehandler eller er det en borger? Jeg ønsker saksbehandler
            val ident = getClaims(oidcRequestContextHolder).subject
            val expirationTime = getClaims(oidcRequestContextHolder).claimSet.expirationTime

            logger.debug("Ident: $ident  Token Expire: $expirationTime Role: ${getRole(ident)}")
            logger.debug("Sjekke tilgang 1")

            // Bare saksbehandlere skal sjekkes om de har tilgang.
            // Brukere med fødselsnummer har tilgang til seg selv. Det er håndtert ved pålogging.

            if (ident.matches(regexNavident)) {

                logger.debug("Hente ut brukerinformasjon fra AD '$ident'")

                val brukerInformasjon: BrukerInformasjon
                try {
                    brukerInformasjon = ldapService.hentBrukerInformasjon(ident)
                    logger.info("Ldap brukerinformasjon hentet")
                    logger.debug("Ldap brukerinfo: $brukerInformasjon")
                } catch (ex: Exception) {
                    logger.error("Feil ved henthing av ldpap brukerinformasjon", ex)

                    //det feiler ved ldap oppsalg benytter witheliste for å sjekke ident
                    return@measure sjekkWhitelisting(ident)
                }

                val adRoller = AdRolle.konverterAdRollerTilEnum(brukerInformasjon.medlemAv)
                    // Sjekk tilgang til EESSI-Pensjon
                    if( authorisationService.harTilgangTilEessiPensjon(adRoller).not() ){

                        // Ikke tilgang til EESSI-Pensjon
                        logger.warn("Bruker har ikke korrekt tilganger vi avviser med UNAUTHORIZED")
                        auditLogger.log("sjekkTilgangTilEessiPensjonTjeneste, INGEN TILGANG")
                        throw AuthorisationIkkeTilgangTilEeessiPensjonException("Ikke tilgang til EESSI-Pensjon")

                    }
                    logger.info("Saksbehandler tilgang til EESSI-Pensjon er i orden")
                    // Sjekk tilgang til PESYS-SAK?
                    // Hvordan får jeg tak i sakstypen?
                    // Sjekk tilgang til BUC?
                    // Sjekk tilgang til alle brukere i SED eller andre data
                    return@measure true

            } else {
                logger.info("Borger/systembruker tilgang til EESSI-Pensjon alltid i orden")
                return@measure true
            }
        }
    }

    private fun sjekkWhitelisting(ident: String): Boolean {
        logger.warn("Prøver å slå opp ident i whitelisting")
        if (whitelistService.isPersonWhitelisted(ident)) {
            logger.info("Godkjenner følgende saksbehandler fra whitelisting : $ident")
            return true
        }
        logger.warn("Følgende ident er ikke whitelisted : $ident  INGEN TILGANG")
        throw AuthorisationIkkeTilgangTilEeessiPensjonException("Ikke tilgang til EESSI-Pensjon")
    }

    fun getRole(subject: String): String {
        return when {
            subject.matches(regexNavident) -> "SAKSBEHANDLER"
            subject.matches(regexBorger) -> "BRUKER"
            else -> "UNKNOWN"
        }
    }

    /**
     * Feil som kan kastes: Ikke tilgang til EESSI-Pensjon
     */
    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    class AuthorisationIkkeTilgangTilEeessiPensjonException(message: String?) : Exception(message)

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }

}


