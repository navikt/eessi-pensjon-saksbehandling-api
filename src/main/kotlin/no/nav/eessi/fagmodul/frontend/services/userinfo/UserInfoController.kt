package no.nav.eessi.fagmodul.frontend.services.userinfo

import no.nav.eessi.fagmodul.frontend.services.whitelist.WhitelistService
import no.nav.eessi.fagmodul.frontend.utils.getClaims
import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import no.nav.security.oidc.api.Protected
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@RequestMapping("/api")
class UserInfoController(val oidcRequestContextHolder: OIDCRequestContextHolder,
                         val whitelistService: WhitelistService,
                         @Value("\${no.nav.sone}") var sone: String) {

    private val logger = LoggerFactory.getLogger(UserInfoController::class.java)

    /**
     *  This endpoint is used to get the userinfo about the currently logged in requester.
     *  It will look for a S3 object with key ${subject}___whitelisted,
     *  subject is either fødselsnummer / d-nummer if it is a citizen or a AD username if saksbehandler.
     *  If the object exists the fnummer or AD user is whitelisted, the contents of the S3 object is not used
     *
     *  @return userinfo containing: subject, role and allowed
     */
    @GetMapping("/userinfo")
    fun getUserInfo(): ResponseEntity<String> {
        logger.info("Henter userinfo")
        val fnr = getClaims(oidcRequestContextHolder).subject
        val role = getRole(fnr)
        val allowed = checkWhitelist()

        return ResponseEntity.ok().body(mapAnyToJson(UserInfoResponse(fnr, role, allowed)))
    }

    /**
     * Tries to get the S3 object with key ${subject}___whitelisted,
     * subject is either fødselsnummer / d-nummer if it is a citizen or a AD username if saksbehandler.
     * if the S3 object exists the user is whitelisted, the contents of the S3 object is not read
     *
     * @return whitelisted
     */
    @GetMapping("/whitelisted")
    fun checkWhitelist(): Boolean {
        logger.info("Sjekker om brukeren er whitelistet")
        val personIdentifier = getClaims(oidcRequestContextHolder).subject
        return whitelistService.isPersonWhitelisted(personIdentifier)
    }
}

/**
 * Parses the OIDC token subject and returns a role
 * SAKSBEHANDLER, BRUKER, or UNKNOWN if the two regex has no matches
 *
 * if saksbehandler it will have a letter followed by 6 digits ( eg. A999999 )
 * if citizen bruker it will have a fødselsnummer / dnummer, 11 digits
 *
 * @param subject
 */
fun getRole(subject: String): String {
    return when {
        subject.matches(Regex("^[a-zA-Z]\\d{6}$")) -> "SAKSBEHANDLER"
        subject.matches(Regex("^\\d{11}$")) -> "BRUKER"
        else -> "UNKNOWN"
    }
}

data class UserInfoResponse(
        val subject: String,
        val role: String,
        val allowed: Boolean
)