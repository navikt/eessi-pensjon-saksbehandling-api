package no.nav.eessi.pensjon.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle(val featureToggleService: FeatureToggleService){

    private val listeOverTestere = listOf("B101331", "K105134", "L137579", "T120898", "K137167", "S137110", "H145594", "E153764", "B170313", "S165198", "O107147", "R107597", "R170375", "N128870", "H103790", "K137167", "H148728","F150681" )
    private val listeOverAdmins = listOf("B101331", "K105134")
    private val logger = LoggerFactory.getLogger(FeatureToggle::class.java)

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
        try {
            if(featureToggleService.isFeatureEnabled("EESSI_ADMIN")){
                logger.info("Feature toggle EESSI_ADMIN er påslått i Unleash")
                return mapOf(
                    FeatureName.TEST_USER.name to true,
                    FeatureName.ADMIN_USER.name to true,
                    FeatureName.P5000_UPDATES_VISIBLE.name to true,
                )
            }

            else if(featureToggleService.isFeatureEnabled("P5000_UPDATES_VISIBLE")){
                logger.info("Feature toggle P5000_UPDATES_VISIBLE er påslått i Unleash")
                return mapOf(
                    FeatureName.TEST_USER.name to true,
                    FeatureName.P5000_UPDATES_VISIBLE.name to true,
                )
            } else {
                logger.info("Feature toggle P5000_UPDATES_VISIBLE er avslått i Unleash for alle brukere")
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
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



