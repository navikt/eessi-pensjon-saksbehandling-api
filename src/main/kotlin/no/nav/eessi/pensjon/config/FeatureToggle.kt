package no.nav.eessi.pensjon.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class FeatureToggle(val featureToggleService: FeatureToggleService){

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
        return try {
            when {
                !isProductionEnv() -> {
                    logger.info("Ikke i produksjon, alle features er påslått")
                    allFeaturesEnabled()
                }
                featureToggleService.isFeatureEnabled("EESSI_ADMIN") -> {
                    logger.info("Feature toggle produksjon, EESSI_ADMIN er påslått i Unleash")
                    allFeaturesEnabled()
                }
                featureToggleService.isFeatureEnabled("P5000_UPDATES_VISIBLE") -> {
                    logger.info("Feature toggle produksjon, P5000_UPDATES_VISIBLE er påslått i Unleash")
                    mapOf(
                        FeatureName.TEST_USER.name to true,
                        FeatureName.P5000_UPDATES_VISIBLE.name to true
                    )
                }
                else -> {
                    logger.info("Feature toggle P5000_UPDATES_VISIBLE er avslått i Unleash for alle brukere")
                    allFeaturesDisabled()
                }
            }
        } catch (e: Exception) {
            logger.error(e.message, e)
            allFeaturesDisabled()
        }
    }

    private fun allFeaturesEnabled() = mapOf(
        FeatureName.TEST_USER.name to true,
        FeatureName.ADMIN_USER.name to true,
        FeatureName.P5000_UPDATES_VISIBLE.name to true
    )

    private fun allFeaturesDisabled() = mapOf(
        FeatureName.TEST_USER.name to false,
        FeatureName.ADMIN_USER.name to false,
        FeatureName.P5000_UPDATES_VISIBLE.name to false
    )
}

enum class FeatureName {
    // New P5000 features visibility
    P5000_UPDATES_VISIBLE,
    ADMIN_USER,
    TEST_USER
}



