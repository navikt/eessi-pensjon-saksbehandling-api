package no.nav.eessi.pensjon.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.metrics.RequestCountInterceptor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.client.support.BasicAuthenticationInterceptor
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class STSRestTemplate {

    @Value("\${security-token-service-token.url}")
    lateinit var baseUrl: String

    @Value("\${srvusername}")
    lateinit var username: String

    @Value("\${srvpassword}")
    lateinit var password: String

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Qualifier("stsRestTemplate")
    @Bean
    fun securityTokenExchangeBasicAuthRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
                .rootUri(baseUrl)
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        RequestCountInterceptor(meterRegistry),
                        RequestResponseLoggerInterceptor(),
                        BasicAuthenticationInterceptor(username, password)
                )
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }
}
