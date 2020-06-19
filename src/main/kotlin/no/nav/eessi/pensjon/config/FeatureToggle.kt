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

    fun currentEnv() = environmentName

    private fun isQ1Env(): Boolean {
        return environmentName.contains("q1", true)
    }

    private fun isQ2Env(): Boolean {
        return environmentName.contains("q2", true)
    }

    private fun isProductionEnv(): Boolean {
        return environmentName.contains("p", true)
    }

    private fun isOnEnv() = true

    fun getUIFeatures(): Map<String, Boolean> {
        return mapOf(
            FeatureName.P5000_VISIBLE.name to true,
            FeatureName.P_BUC_02_VISIBLE.name to !isProductionEnv()
        )
    }

    fun getAPIFeatures(): Map<String, Boolean> {
        return mapOf(
            FeatureName.ENABLE_AUTH.name to isOnEnv(),
            FeatureName.WHITELISTING.name to isOnEnv()
        )
    }
}

enum class FeatureName {
    P5000_VISIBLE,
    P_BUC_02_VISIBLE,
    ENABLE_AUTH,
    WHITELISTING,
}


