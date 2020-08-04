package no.nav.eessi.pensjon.interceptor

import no.nav.eessi.pensjon.logging.AuditLogger
import no.nav.eessi.pensjon.services.auth.AdRolle
import no.nav.eessi.pensjon.services.auth.AuthorisationService
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.services.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.services.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.services.whitelist.WhitelistService
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
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
                      private val tokenValidationContextHolder: TokenValidationContextHolder,
                      private val auditLogger: AuditLogger,
                      private val whitelistService: WhitelistService) : HandlerInterceptor, Ordered {

    private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)
    private val regexNavident  = Regex("^[a-zA-Z]\\d{6}$")
    private val regexBorger = Regex("^\\d{11}$")
    private val ugyldigToken = "UNAUTHORIZED"
    private val avvistIdent = "FORBIDDEN"

    enum class Roller {
        SAKSBEHANDLER,
        BRUKER,
        UNKNOWN
    }

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            // Vi sjekker om det er en annotasjon av typen EessiPensjonTilgang
            // Sjekke om pålogget saksbehandler har tilgang til tjenesten.
            val eessiPensjonTilgang = handler.getMethodAnnotation(EessiPensjonTilgang::class.java)
            if (eessiPensjonTilgang != null) {
                val oidcClaims = sjekkForGyldigToken()
                return sjekkTilgangTilEessiPensjonTjeneste(oidcClaims)
            }
        }
        return true
    }

    fun sjekkForGyldigToken(): JwtTokenClaims {
        //kaster en 401 dersom ingen gyldig token finnes så UI kan redirekte til /login
        try {
            return getClaims(tokenValidationContextHolder)
        } catch (rx: RuntimeException) {
            logger.info("Ingen gyldig token, kaster en $ugyldigToken Exception")
            throw TokenIkkeTilgjengeligException("Ingen gyldig token funnet")
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
    fun sjekkTilgangTilEessiPensjonTjeneste(oidcClaims: JwtTokenClaims): Boolean{
        val ident = oidcClaims.subject
        val expirationTime = oidcClaims.expirationTime
        val brukerRolle = hentRolle(ident)

        // Bare saksbehandlere skal sjekkes om de har tilgang.
        // Skal borgere ha tilgang til api-fss?
        return if (Roller.SAKSBEHANDLER == brukerRolle) {
            logger.info("Ident: $ident,  expire: $expirationTime, Rolle: $brukerRolle")
            logger.debug("Henter ut brukerinformasjon fra AD/Ldap")
            val brukerInformasjon: BrukerInformasjon
            try {
                brukerInformasjon = ldapService.hentBrukerInformasjon(ident)
                logger.info("Ldap brukerinformasjon hentet")
                logger.debug("Ldap brukerinfo: $brukerInformasjon")
            } catch (ex: Exception) {
                logger.error("Feil ved henting av ldap brukerinformasjon, prøver whitelistig s3", ex)
                return sjekkWhitelisting(ident)
            }

            val adRoller = AdRolle.konverterAdRollerTilEnum(brukerInformasjon.medlemAv)
                // Sjekk tilgang til EESSI-Pensjon
                if( authorisationService.harTilgangTilEessiPensjon(adRoller).not() ) {
                    // Ikke tilgang til EESSI-Pensjon
                    logger.warn("Bruker har ikke korrekt tilganger vi avviser med $avvistIdent")
                    auditLogger.log("sjekkTilgangTilEessiPensjonTjeneste, INGEN TILGANG")
                    throw AuthorisationIkkeTilgangTilEeessiPensjonException("Du har ikke tilgang til EESSI-Pensjon")
                }
                logger.debug("Saksbehandler tilgang til EESSI-Pensjon er i orden")
                true
        } else {
            logger.debug("Borger/systembruker tilgang til EESSI-Pensjon alltid i orden")
            true
        }
    }

    //TODO Temp. denne skal fjernes etterhvert.
    private fun sjekkWhitelisting(ident: String): Boolean {
        logger.warn("Prøver å slå opp ident i whitelisting")
        if (whitelistService.isPersonWhitelisted(ident)) {
            logger.info("Godkjenner fra whitelisting")
            return true
        }
        logger.warn("Bruker har ikke korrekt tilganger vi avviser med $avvistIdent")
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


