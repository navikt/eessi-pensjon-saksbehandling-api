package no.nav.eessi.pensjon.config

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.eessi.pensjon.api.userinfo.UserInfoController
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class FeatureToggleService(
    @param:Value("\${UNLEASH_URL}") private val unleashUrl: String,
    @param:Value("\${UNLEASH_SERVER_API_TOKEN}") private val unleashToken: String,
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

    fun getAllFeaturesForProject(): String? {
        try {
            val url = "$unleashUrl/admin/projects/default/features"
            val headers = HttpHeaders().apply {
                set("Accept", "application/json")
                set("Authorization", unleashToken)
            }
            val entity = org.springframework.http.HttpEntity<String>(headers)
            val response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                String::class.java
            )
            return response.body
        } catch (e: Exception) {
            throw RuntimeException("Feil ved henting av features for project", e)
        }
    }
}