package no.nav.eessi.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle {

    private val listOfUsers = listOf(
    "K157734",
    "I123103",
    "S122793",
    "R107597",
    "I135684",
    "B151417",
    "L124884",
    "H145594",
    "K105134",
    "B101331",
    "S128848")

    @Value("\${ENV}")
    private lateinit var environmentName: String

    //kun til testing
    fun setCurrentEnv(env: String) {
        environmentName = env
    }

    private fun isProductionEnv(): Boolean {
        return environmentName.contains("p", true)
    }

    fun getUIFeatures(fnr: String): Map<String, Boolean> {
        return mapOf(
            FeatureName.P5000_SUMMER_VISIBLE.name to true,
            FeatureName.X010_X009_VISIBLE.name to true
        )
    }

    fun fetureToggleP5000(fnr: String) : Boolean {
        return if (isProductionEnv() && fnr.uppercase() in listOfUsers) {
            true
        } else !isProductionEnv()
    }
}

enum class FeatureName {
    P5000_SUMMER_VISIBLE,
    X010_X009_VISIBLE,
    ENABLE_AUTH,
    WHITELISTING,
}



