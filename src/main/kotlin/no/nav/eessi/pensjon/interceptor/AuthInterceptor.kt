package no.nav.eessi.pensjon.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import no.nav.eessi.pensjon.models.BrukerInformasjon
import no.nav.eessi.pensjon.services.auth.AdRolle
import no.nav.eessi.pensjon.services.auth.AuthorisationService
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.getToken
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.client.RestTemplate
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.util.UriComponentsBuilder
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Component
class AuthInterceptor(private val proxyOAuthRestTemplate: RestTemplate,
                      private val authorisationService: AuthorisationService,
                      private val tokenValidationContextHolder: TokenValidationContextHolder
                      ) : HandlerInterceptor, Ordered {

    private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)
    private val ugyldigToken = "UNAUTHORIZED"
    private val avvistIdent = "FORBIDDEN"

    @Throws(Exception::class)
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler is HandlerMethod) {
            // Vi sjekker om det er en annotasjon av typen EessiPensjonTilgang
            // Sjekke om pålogget saksbehandler har tilgang til tjenesten.
            val eessiPensjonTilgang = handler.getMethodAnnotation(EessiPensjonTilgang::class.java)
            if (eessiPensjonTilgang != null) {
                val oidcClaims = sjekkForGyldigToken()
                logger.debug("gcp azureToken: ${getTokens()}")
                return sjekkTilgangTilEessiPensjonTjeneste(oidcClaims)
            }
        }
        return true
    }

    fun getTokens(): String = URLDecoder.decode(getToken(tokenValidationContextHolder)?.encodedToken, StandardCharsets.UTF_8) ?: "Unknown"

    fun getSubjectFromToken() = getClaims(tokenValidationContextHolder).get("NAVident")?.toString() ?: "Unknown"

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
        val ident = getSubjectFromToken()
        val expirationTime = oidcClaims.expirationTime

            logger.info("Ident: $ident,  expire: $expirationTime")
            logger.debug("Henter ut brukerinformasjon fra AD/Ldap")
            return try {
                val brukerInformasjon = hentBrukerinformasjon(ident)
                logger.info("Ldap brukerinformasjon hentet")
                logger.debug("Ldap brukerinfo: $brukerInformasjon")

                val adRoller = AdRolle.konverterAdRollerTilEnum(brukerInformasjon.medlemAv)
                // Sjekk tilgang til EESSI-Pensjon
                if( authorisationService.harTilgangTilEessiPensjon(adRoller).not() ) {
                    // Ikke tilgang til EESSI-Pensjon
                    logger.warn("Bruker har ikke korrekt tilganger vi avviser med $avvistIdent")
                    throw AuthorisationIkkeTilgangTilEeessiPensjonException("Du har ikke tilgang til EESSI-Pensjon")
                }
                logger.debug("Saksbehandler tilgang til EESSI-Pensjon er i orden")
                true
            } catch (ex: Exception) {
                logger.error("Feil ved henting av ldap brukerinformasjon", ex)
                throw AuthorisationIkkeTilgangTilEeessiPensjonException("Ikke tilgang til EESSI-Pensjon")
            }
    }

    fun hentBrukerinformasjon(navident: String) : BrukerInformasjon {
        val path = "/brukerinfo/{navident}"
        val uriParams = mapOf("navident" to navident)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        val response = proxyOAuthRestTemplate.exchange(builder.toUriString(),
            HttpMethod.GET,
            null,
            String::class.java)

        val body = response.body ?: throw Exception("Feiler ved innhenting av navident")
        return mapJsonToAny(body)
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


