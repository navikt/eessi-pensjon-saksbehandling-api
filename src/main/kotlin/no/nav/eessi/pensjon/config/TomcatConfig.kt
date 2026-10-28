package no.nav.eessi.pensjon.config

import org.springframework.boot.tomcat.TomcatConnectorCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TomcatConfig {

    @Bean
    fun localeCountCustomizer() = TomcatConnectorCustomizer { connector ->
        connector.maxLocaleCount = 20
    }
}
