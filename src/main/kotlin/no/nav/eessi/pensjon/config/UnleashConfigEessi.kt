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

    private val logger = LoggerFactory.getLogger(ApiMvcConfig::class.java)

    @Bean
    fun unleash(
        @Value("\${APP_NAME}") app_name: String,
        @Value("\${UNLEASH_URL}") unleash_url: String,
        @Value("\${UNLEASH_SERVER_API_TOKEN}") unleash_token: String
    ){
        try {
            val unleashConfig = UnleashConfig.builder()
                .appName(app_name)
                .apiKey(unleash_token)
                .unleashAPI(unleash_url)
                .build()

            DefaultUnleash(
                unleashConfig,
            ).also {
                logger.info("Unleash config: ${unleashConfig.toJson()}")
            }
        } catch (e: Exception) {
           logger.error("Feil ved unleash config ${e.message}")
        }
    }
}