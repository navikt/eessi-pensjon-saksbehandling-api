package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.security.sts.UsernameToOidcInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Configuration
class EuxConfiguration(private val securityTokenExchangeService: STSService) {

    @Value("\${EUX_RINA_API_V1_URL}")
    private lateinit var euxUrl: String

    @Bean
    fun euxOidcRestTemplate(templateBuilder: RestTemplateBuilder): RestTemplate {
        return templateBuilder
            .rootUri(euxUrl)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors(
                RequestIdHeaderInterceptor(),
                RequestResponseLoggerInterceptor(),
                UsernameToOidcInterceptor(securityTokenExchangeService)
            )
            .build()
            .apply { requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory()) }
    }
}
