package no.nav.eessi.pensjon.unleash

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    private val unleash: Unleash,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) {

    private val logger = LoggerFactory.getLogger(FeatureToggleService::class.java)

    fun isFeatureEnabled(featureName: String): Boolean {
        val claims = getClaims(tokenValidationContextHolder)
            .also { logger.debug("Claims: ${it.toJson()}") }
        val userId = claims.get("NAVident")?.toString() ?: "Unknown"
        val context = UnleashContext.builder()
            .userId(userId)
            .build()
        return unleash.isEnabled(featureName, context).also {
            logger.info("Sjekker feature toggle for feature: $featureName for user: $userId, unleash: $it")
        }
    }

    fun getAllFeaturesForProject(): List<FeatureToggleStatus> {
        val featureNames = unleash.more().featureToggleNames
        return featureNames.map { name ->
            FeatureToggleStatus(
                name = name,
                enabled = isFeatureEnabled(name)
            )
        }
    }
}