package no.nav.eessi.pensjon.config

import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class ApiMvcConfig(private val authInterceptor: AuthInterceptor): WebMvcConfigurer {

    private val logger = LoggerFactory.getLogger(ApiMvcConfig::class.java)

    override fun addInterceptors(registry: InterceptorRegistry) {
        logger.debug("legger til TokenInterceptor")
        registry.addInterceptor(authInterceptor)
    }
}