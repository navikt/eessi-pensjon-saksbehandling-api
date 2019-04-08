package no.nav.eessi.fagmodul.frontend.services.sts

import no.nav.eessi.fagmodul.frontend.interceptor.RequestResponseLoggerInterceptor
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
class StsRestTemplate {

    @Value("\${security-token-service-token.url:http://localhost}")
    lateinit var baseUrl: String

    @Value("\${srveessipensjon.username:user}")
    lateinit var username: String

    @Value("\${srveessipensjon.password:pass}")
    lateinit var password: String

    @Qualifier("stsRestTemplate")
    @Bean
    fun securityTokenExchangeBasicAuthRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
                .rootUri(baseUrl)
                .additionalInterceptors(
                        RequestResponseLoggerInterceptor(),
                        BasicAuthenticationInterceptor(username, password)
                )
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }
}