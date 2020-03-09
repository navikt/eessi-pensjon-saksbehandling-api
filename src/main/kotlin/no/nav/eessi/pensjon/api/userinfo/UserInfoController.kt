package no.nav.eessi.pensjon.api.userinfo

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.logging.AuditLogger
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.services.whitelist.WhitelistService
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.security.oidc.api.Protected
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Protected
@RequestMapping("/api")
class UserInfoController(
    val oidcRequestContextHolder: OIDCRequestContextHolder,
    val whitelistService: WhitelistService,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    @Value("\${ENV}")
    lateinit var fasitEnvironmentName: String

    private val logger = LoggerFactory.getLogger(UserInfoController::class.java)
    private val auditLogger = AuditLogger(oidcRequestContextHolder)

    /**
     *  This endpoint is used to get the userinfo about the currently logged in requester.
     *  It will look for a S3 object with key ${subject}___whitelisted,
     *  subject is either fødselsnummer / d-nummer if it is a citizen or a AD username if saksbehandler.
     *  If the object exists the fnummer or AD user is whitelisted, the contents of the S3 object is not used
     *
     *  @return userinfo containing: subject, role and allowed
     */
    //@EessiPensjonTilgang
    @GetMapping("/userinfo")
    fun getUserInfo(): ResponseEntity<String> {
        logger.info("Henter userinfo")
        val fnr = getClaims(oidcRequestContextHolder).subject
        val role = getRole(fnr)
        val allowed = checkWhitelist()
        val features = if (allowed) getFeatures() else mapOf()
        val jwtset =  getClaims(oidcRequestContextHolder).claimSet
        val expirationTime = jwtset.expirationTime.time

        when (role) {
            "SAKSBEHANDLER" -> {
                auditLogger.log("getUserInfo")
                metricsHelper.increment("hentUserinfoSaksbehandler", "successful")
            }
            "BRUKER" -> metricsHelper.increment("hentUserinfoBorger", "successful")

            else -> metricsHelper.increment("hentUserinfoUkjent", "successful")
        }

        return ResponseEntity.ok().body(mapAnyToJson(UserInfoResponse(fnr, role, allowed,expirationTime, features)))
    }

    fun isProductionEnv(): Boolean {
        return fasitEnvironmentName.contains("p", true)
    }

    fun getFeatures(): Map<String, Boolean> {
        return mapOf("P5000_VISIBLE" to !isProductionEnv())
    }

    /**
     * Tries to get the S3 object with key ${subject}___whitelisted,
     * subject is either fødselsnummer / d-nummer if it is a citizen or a AD username if saksbehandler.
     * if the S3 object exists the user is whitelisted, the contents of the S3 object is not read
     *
     * @return whitelisted
     */
    //@EessiPensjonTilgang
    @GetMapping("/whitelisted")
    fun checkWhitelist(): Boolean {
        logger.info("Sjekker om brukeren er whitelistet")
        val personIdentifier = getClaims(oidcRequestContextHolder).subject
        return whitelistService.isPersonWhitelisted(personIdentifier.toUpperCase())
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
        val allowed: Boolean,
        val expirationTime: Long,
        val features: Map<String, Boolean>
)
