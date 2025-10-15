package no.nav.eessi.pensjon.api.userinfo

import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.getToken
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@RestController
@Protected
@RequestMapping("/api")
class UserInfoController(
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()
) {

    private val logger = LoggerFactory.getLogger(UserInfoController::class.java)

    /**
     *  This endpoint is used to get the userinfo about the currently logged in requester.
     *  It will look for a S3 object with key ${subject}___whitelisted,
     *  subject is either fødselsnummer / d-nummer if it is a citizen or a AD username if saksbehandler.
     *  If the object exists the fnummer or AD user is whitelisted, the contents of the S3 object is not used
     *
     *  @return userinfo containing: subject, role and allowed
     */
    @EessiPensjonTilgang
    @GetMapping("/userinfo")
    fun getUserInfo(): ResponseEntity<String> {
        val userInfo = UserInfoResponse(
            subject = getSubjectFromToken(),
            role = getRole(getSubjectFromToken()),
            expirationTime = getClaims().expirationTime.time
        )
        logger.debug("Henter featureInfo: ${userInfo.toJson()}")
        return ResponseEntity.ok(mapAnyToJson(userInfo))
    }

    fun getTokens(): String =
        URLDecoder.decode(getToken(tokenValidationContextHolder)?.encodedToken, StandardCharsets.UTF_8) ?: "Unknown"

    fun getSubjectFromToken() = getClaims(tokenValidationContextHolder).get("NAVident")?.toString() ?: "Unknown"

    fun getClaims(): JwtTokenClaims = getClaims(tokenValidationContextHolder)
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
    val expirationTime: Long
)

