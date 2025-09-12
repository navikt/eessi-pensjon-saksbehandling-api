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
    fun unleash(@Value("\${UNLEASH_SERVER_API_TOKEN}") token: String): Unleash {
        val unleashConfig = UnleashConfig.builder()
            .apiKey(token)
            .appName(APP_NAME)
            .unleashAPI(UNLEASH_URL)
            .build()

        return DefaultUnleash(
            unleashConfig
        ).also {
            logger.info("Unleash is enabled at $UNLEASH_URL with appName $APP_NAME")
            logger.info("Unleash config: ${unleashConfig.toJson()}")
        }
    }

    companion object {
        const val UNLEASH_URL = "https://eessipensjon-unleash-api.nav.cloud.nais.io/api"
        const val APP_NAME = "saksbehandlingapi-q2"
    }
}