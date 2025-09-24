package no.nav.eessi.pensjon.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.utils.EnvUtil.NAIS_CLUSTER_NAME
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class UnleashConfigEessi(
    @param:Value("\${UNLEASH_URL}") private val unleashUrl: String,
    @param:Value("\${UNLEASH_APP_NAME}") private val appName: String,
    @param:Value("\${UNLEASH_SERVER_API_TOKEN}") private val unleashToken: String
) {
    private val logger = LoggerFactory.getLogger(UnleashConfigEessi::class.java)

    @Bean
    fun unleash(): Unleash? = try {
        logger.info("Initialiserer Unleash med url $unleashUrl og appName $appName")

        val config = UnleashConfig.builder()
            .appName(appName)
            .apiKey(unleashToken)
            .unleashAPI(unleashUrl)
            .environment(
                when (System.getenv("NAIS_CLUSTER_NAME").orEmpty()) {
                    "prod-gcp" -> "production"
                    else -> "development"
                },
            ).build()
        DefaultUnleash(
            config,
        ).also {
            logger.info("Unleash  naisClusterName: $NAIS_CLUSTER_NAME")
        }
    } catch (e: Exception) {
        logger.error("Error in Unleash config: ${e.message}")
        null
    }

}

