package no.nav.eessi.pensjon.unleash

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle(val featureToggleService: FeatureToggleService){

    private val logger = LoggerFactory.getLogger(FeatureToggle::class.java)

    private val listeOverTestere = listOf("B101331", "K105134", "L137579", "T120898", "K137167", "S137110", "H145594", "E153764", "B170313", "S165198", "O107147", "R107597", "R170375", "N128870", "H103790", "K137167", "H148728","F150681" , "S165969", "A100245")
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
        try {
            when {
                !isProductionEnv() -> {
                    logger.info("Ikke i produksjon, alle features er påslått")
                    return allFeaturesEnabled()
                }
                featureToggleService.isFeatureEnabled("EESSI_ADMIN") -> {
                    logger.info("Feature toggle produksjon, EESSI_ADMIN er påslått i Unleash")
                    return allFeaturesEnabled()
                }
                featureToggleService.isFeatureEnabled("P5000_UPDATES_VISIBLE") -> {
                    logger.info("Feature toggle produksjon, P5000_UPDATES_VISIBLE er påslått i Unleash")
                    return mapOf(
                        FeatureName.TEST_USER.name to true,
                        FeatureName.P5000_UPDATES_VISIBLE.name to true
                    )
                }
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
        }
        logger.info("ingen feature toggle funnet, prøver ordinær vurdering av bruker mot lister")
        return allFeaturesOldSchool(ident)
    }
    fun featureToggle(
        ident: String,
        userList: List<String>
    ) : Boolean = (isProductionEnv() && ident.uppercase() in userList) || !isProductionEnv()

    private fun allFeaturesEnabled() = featureToggleService.getAllFeaturesForProject().associate { it.name to it.enabled }

    private fun allFeaturesOldSchool(ident: String) = mapOf(
        FeatureName.ADMIN_USER.name to featureToggle(ident, listeOverAdmins),
        FeatureName.TEST_USER.name to featureToggle(ident, listeOverTestere),
        FeatureName.P5000_UPDATES_VISIBLE.name to featureToggle(ident, listeOverTestere),
    )
}

enum class FeatureName {
    // New P5000 features visibility
    P5000_UPDATES_VISIBLE,
    ADMIN_USER,
    TEST_USER
}



