package no.nav.eessi.pensjon.config

import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import org.pac4j.springframework.web.SecurityInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ApiMvcConfig(private val toggle: FeatureToggle,
               private val authInterceptor: AuthInterceptor,
               private val securityInterceptor: SecurityInterceptor): WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(ApiMvcConfig::class.java)


    override fun addInterceptors(registry: InterceptorRegistry) {
        logger.debug("legger til OIDCInterceptor i: ${toggle.currentEnv()}")
        registry.addInterceptor(securityInterceptor).addPathPatterns("/openamlogin")

        if (toggle.getAPIFeatures().getValue(FeatureName.ENABLE_AUTH.name)) {
            logger.debug("legger til AuthInterceptor i: ${toggle.currentEnv()}")
            registry.addInterceptor(authInterceptor)
        }

    }



}