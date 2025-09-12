package no.nav.eessi.pensjon.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.eessi.pensjon.utils.toJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class UnleashConfigEessi {

    private val logger = LoggerFactory.getLogger(UnleashConfigEessi::class.java)

    @Bean
    fun unleash(
        @Value("\${APP_NAME}") appName: String,
        @Value("\${UNLEASH_URL}") unleashUrl: String,
        @Value("\${UNLEASH_SERVER_API_TOKEN}") unleashToken: String
    ): Unleash? = try {
        val config = UnleashConfig.builder()
            .appName(appName)
            .apiKey(unleashToken)
            .unleashAPI(unleashUrl)
            .build()

        DefaultUnleash(config).also {
            logger.info("Unleash config: ${config.toJson()}")
        }
    } catch (e: Exception) {
        logger.error("Error in Unleash config: ${e.message}")
        null
    }
}