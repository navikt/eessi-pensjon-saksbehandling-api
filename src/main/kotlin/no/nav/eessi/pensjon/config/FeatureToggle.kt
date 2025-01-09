package no.nav.eessi.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle {

    private val listeFagfolkEllerSaksbehandlere = listOf("K105134", "B101331", "R107597", "H129072", "S165969", "M159200")

    @Value("\${ENV}")
    private lateinit var environmentName: String

    //kun til testing
    fun setCurrentEnv(env: String) {
        environmentName = env
    }

    private fun isProductionEnv(): Boolean {
        return environmentName.contains("p", true)
    }

    fun getUIFeatures(ident: String): Map<String, Boolean> {
        return mapOf(
            FeatureName.P5000_UPDATES_VISIBLE.name to featureToggleP5000Updates(ident),
            FeatureName.ADMIN_NOTIFICATION_MESSAGE.name to featureToggleP5000Updates(ident)
        )
    }

    fun featureToggleP5000Updates(ident: String) : Boolean = (isProductionEnv() && ident.uppercase() in listeFagfolkEllerSaksbehandlere) || !isProductionEnv()

}

enum class FeatureName {
    // New P5000 features visibility
    P5000_UPDATES_VISIBLE,
    // Administrate the notification message that shows up when EP page loads
    ADMIN_NOTIFICATION_MESSAGE,
    ENABLE_AUTH,
    WHITELISTING,
}



