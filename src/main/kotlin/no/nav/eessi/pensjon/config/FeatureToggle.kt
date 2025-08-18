package no.nav.eessi.pensjon.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle {

    private val listeOverTestere = listOf("B101331", "K105134", "L137579", "T120898", "K137167", "S137110", "H145594", "E153764", "B170313", "S165198", "O107147")
    private val listeOverAdmins = listOf("B101331", "K105134")

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
            FeatureName.ADMIN_USER.name to featureToggle(ident, listeOverAdmins),
            FeatureName.TEST_USER.name to featureToggle(ident, listeOverTestere),
            FeatureName.P5000_UPDATES_VISIBLE.name to featureToggle(ident, listeOverTestere),
        )
    }

    fun featureToggle(
        ident: String,
        userList: List<String>
    ) : Boolean = (isProductionEnv() && ident.uppercase() in userList) || !isProductionEnv()

}

enum class FeatureName {
    // New P5000 features visibility
    P5000_UPDATES_VISIBLE,
    ADMIN_USER,
    TEST_USER
}



