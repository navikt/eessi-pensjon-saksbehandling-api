package no.nav.eessi.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle {

    private val listeFagfolkEllerSaksbehandlere = listOf(
        "S128848",
        "K105134",
        "B101331"
    )

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
            FeatureName.P5000_UPDATES_VISIBLE.name to featureToggleP5000Updates(fnr),
            FeatureName.X010_X009_VISIBLE.name to true
        )
    }

    fun featureToggleP5000Updates(fnr: String) : Boolean = (isProductionEnv() && fnr.uppercase() in listeFagfolkEllerSaksbehandlere) || !isProductionEnv()

}

enum class FeatureName {
    P5000_SUMMER_VISIBLE,
    X010_X009_VISIBLE,
    P5000_UPDATES_VISIBLE,
    ENABLE_AUTH,
    WHITELISTING,
}



