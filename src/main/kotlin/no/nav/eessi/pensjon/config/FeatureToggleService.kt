package no.nav.eessi.pensjon.config

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate


@Service
class FeatureToggleService(
    @param:Value("\${UNLEASH_URL}") private val unleashUrl: String,
    @param:Value("\${UNLEASH_SERVER_API_TOKEN}") private val unleashToken: String,
    @param:Value("\${UNLEASH_SERVER_ADMIN_TOKEN}") private val unleashAdminToken: String,
    private val unleash: Unleash,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
    private val restTemplate: RestTemplate
) {

    private val logger = LoggerFactory.getLogger(FeatureToggleService::class.java)

    fun isFeatureEnabled(featureName: String): Boolean {
        val claims = no.nav.eessi.pensjon.utils.getClaims(tokenValidationContextHolder).also { logger.debug("Claims: ${it.toJson()}") }
        val userId = claims.get("NAVident")?.toString() ?: "Unknown"
        val context = UnleashContext.builder()
            .userId(userId)
            .build()
        return unleash.isEnabled(featureName, context).also {
            logger.info("Sjekker feature toggle for feature: $featureName for user: $userId, unleash: $it")
        }
    }

    fun getAllFeaturesForProject(): List<String>? {
        try {
            return unleash.more().featureToggleNames.also {
                logger.debug("Henter alle features for prosjekt fra unleash: $unleashUrl |  $it")
            }

        } catch (e: Exception) {
            throw RuntimeException("Feil ved henting av features for project", e)
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class FeaturesResponse(
        val version: Int?,
        val features: List<Feature>?
    )

    @JsonInclude(JsonInclude.Include.NON_NULL)
    data class Feature(
        val impressionData: Boolean?,
        val enabled: Boolean?,
        val name: String?,
        val description: String?,
        val project: String?,
        val stale: Boolean?,
        val type: String?,
        val lastSeenAt: String?,
        val variants: List<Any>?,
        val createdAt: String?,
        val environments: List<Environment>?,
        val strategies: List<Any>?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Environment(
        val name: String?,
        val lastSeenAt: String?,
        val enabled: Boolean?
    )

}