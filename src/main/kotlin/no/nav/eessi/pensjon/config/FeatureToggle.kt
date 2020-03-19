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
        return environmentName.contains("q1", true)
    }

    private fun isProductionEnv(): Boolean {
        return environmentName.contains("p", true)
    }

    private fun isOnEnv() = true

    fun getUIFeatures(): Map<String, Boolean> {
        return mapOf(
            FeatureName.P5000_VISIBLE.name to isProductionEnv().not()
        )
    }

    fun getAPIFeatures(): Map<String, Boolean> {
        return mapOf(
            FeatureName.ENABLE_AUTH.name to isQ1Env(),
            FeatureName.WHITELISTING.name to isOnEnv())
    }

}

enum class FeatureName {
    P5000_VISIBLE,
    ENABLE_AUTH,
    WHITELISTING,
}


