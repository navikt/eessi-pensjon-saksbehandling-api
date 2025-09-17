package no.nav.eessi.pensjon.config

import io.getunleash.Unleash
import io.getunleash.UnleashContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.stereotype.Service

@Service
class FeatureToggleService(
    private val unleash: Unleash,
    private val tokenValidationContextHolder: TokenValidationContextHolder
) {
    fun isFeatureEnabled(featureName: String): Boolean {
        val claims = no.nav.eessi.pensjon.utils.getClaims(tokenValidationContextHolder)
        val userId = claims.get("NAVident")?.toString() ?: "Unknown"
        val context = UnleashContext.builder()
            .userId(userId)
            .build()
        return unleash.isEnabled(featureName, context)
    }
}