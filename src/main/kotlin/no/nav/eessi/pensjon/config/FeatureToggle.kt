package no.nav.eessi.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle {

    @Value("\${ENV}")
    private lateinit var environmentName: String

    //kun til testing
    fun setCurrentEnv(env: String) {
        environmentName = env
    }

    private fun isProductionEnv(): Boolean {
        return environmentName.contains("p", true)
    }

    fun getUIFeatures(): Map<String, Boolean> {
        return mapOf(
            FeatureName.P5000_SUMMER_VISIBLE.name to !isProductionEnv()
        )
    }
}

enum class FeatureName {
    P5000_SUMMER_VISIBLE,
    ENABLE_AUTH,
    WHITELISTING,
}


