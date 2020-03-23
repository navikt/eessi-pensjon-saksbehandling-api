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
import no.nav.security.oidc.context.OIDCClaims
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

    private val regexNavident  = Regex("^[a-zA-Z]\\d{6}$")
    private val regexBorger = Regex("^\\d{11}$")

    enum class Roller {
        SAKSBEHANDLER,
        BRUKER,
        UNKNOWN
    }

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {

        if (handler is HandlerMethod) {
            // Vi sjekker om det er en annotasjon av typen EessiPensjonTilgang
            // Hvis den er der så skal vi sjekke om pålogget saksbehandler
            // har tilgang til tjenesten som blir kalt
            val eessiPensjonTilgang = handler.getMethodAnnotation(EessiPensjonTilgang::class.java)
            if (eessiPensjonTilgang != null) {
                // Skal sjekke tilgang til tjenesten som kalles
                return sjekkTilgangTilEessiPensjonTjeneste(sjekkTilgangTilUserinfo())
            }
        }
        return true

    }

    fun sjekkTilgangTilUserinfo(): OIDCClaims {
        try {
            logger.debug("Sjekker om det finnes et token")
            return getClaims(oidcRequestContextHolder)
        } catch (rx: RuntimeException) {
            logger.warn("Det finnes ingen gyldig token, kaster en exception")
            throw TokenIkkeTilgjengeligException("Ingen gyldig token")
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
    fun sjekkTilgangTilEessiPensjonTjeneste(oidcClaims: OIDCClaims): Boolean{
        return metricsHelper.measure("authInterceptor") {

            // Er bruker det samme som saksbehandler eller er det en borger? Jeg ønsker saksbehandler
            val ident = oidcClaims.subject
            val expirationTime = oidcClaims.claimSet.expirationTime
            val brukerRolle = hentRolle(ident)

            logger.debug("Ident: $ident  Token Expire: $expirationTime Role: $brukerRolle")
            logger.debug("Sjekke tilgang 1")

            // Bare saksbehandlere skal sjekkes om de har tilgang.
            // Brukere med fødselsnummer har tilgang til seg selv. Det er håndtert ved pålogging.

            //if (ident.matches(regexNavident)) {
            if (Roller.SAKSBEHANDLER == brukerRolle) {

                logger.debug("Hente ut brukerinformasjon fra AD '$ident'")

                val brukerInformasjon: BrukerInformasjon
                try {
                    brukerInformasjon = ldapService.hentBrukerInformasjon(ident)
                    logger.info("Ldap brukerinformasjon hentet")
                    logger.debug("Ldap brukerinfo: $brukerInformasjon")
                } catch (ex: Exception) {
                    logger.error("Feil ved henthing av ldap brukerinformasjon", ex)

                    //det feiler ved ldap oppsalg benytter witheliste for å sjekke ident
                    return@measure sjekkWhitelisting(ident)
                }

                val adRoller = AdRolle.konverterAdRollerTilEnum(brukerInformasjon.medlemAv)
                    // Sjekk tilgang til EESSI-Pensjon
                    if( authorisationService.harTilgangTilEessiPensjon(adRoller).not() ) {

                        // Ikke tilgang til EESSI-Pensjon
                        logger.warn("Bruker har ikke korrekt tilganger vi avviser med UNAUTHORIZED")
                        auditLogger.log("sjekkTilgangTilEessiPensjonTjeneste, INGEN TILGANG")
                        throw AuthorisationIkkeTilgangTilEeessiPensjonException("Ikke tilgang til EESSI-Pensjon")

                    }
                    logger.info("Saksbehandler tilgang til EESSI-Pensjon er i orden")
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

    fun hentRolle(subject: String): Roller {
        return when {
            subject.matches(regexNavident) -> Roller.SAKSBEHANDLER
            subject.matches(regexBorger) -> Roller.BRUKER
            else -> Roller.UNKNOWN
        }
    }

    /**
     * Feil som kan kastes: Ikke tilgang til EESSI-Pensjon
     */

    @ResponseStatus(value = HttpStatus.UNAUTHORIZED)
    class TokenIkkeTilgjengeligException(message: String?): Exception(message)

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    class AuthorisationIkkeTilgangTilEeessiPensjonException(message: String?): Exception(message)

    override fun getOrder(): Int {
        return Ordered.LOWEST_PRECEDENCE
    }


}


