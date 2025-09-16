package no.nav.eessi.pensjon.config

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("test")
class UnleashConfigEessi(
    @param:Value("\${UNLEASH_URL}") private val unleashUrl: String,
    @param:Value("\${UNLEASH_APP_NAME}") private val appName: String,
    @param:Value("\${UNLEASH_SERVER_API_TOKEN}") private val unleashToken: String,
    private val tokenValidationContextHolder: TokenValidationContextHolder,
) {
    private val logger = LoggerFactory.getLogger(UnleashConfigEessi::class.java)

    @Bean
    fun unleash(
    ): Unleash? = try {
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
        val ident = getClaims(tokenValidationContextHolder).get("NAVident")?.toString() ?: throw IllegalStateException("Fant ikke NAVident i token")
        DefaultUnleash(
            config,
            ByUserIdStrategy(ident)
        ).also {
            logger.info("Unleash config: $config")
        }
    } catch (e: Exception) {
        logger.error("Error in Unleash config: ${e.message}")
        null
    }


}

