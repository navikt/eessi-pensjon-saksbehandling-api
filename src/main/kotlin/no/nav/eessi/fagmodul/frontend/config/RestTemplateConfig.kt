package no.nav.eessi.fagmodul.frontend.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.eessi.fagmodul.frontend.interceptor.ApigwHeaderRequestInterceptor
import no.nav.eessi.fagmodul.frontend.interceptor.OidcHeaderRequestInterceptor
import no.nav.eessi.fagmodul.frontend.interceptor.RequestResponseLoggerInterceptor
import no.nav.eessi.fagmodul.frontend.services.sts.SecurityTokenExchangeService
import no.nav.eessi.fagmodul.frontend.services.sts.UntToOidcInterceptor
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.metrics.web.client.DefaultRestTemplateExchangeTagsProvider
import org.springframework.boot.actuate.metrics.web.client.MetricsRestTemplateCustomizer
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate

@Component
class RestTemplateConfig(val restTemplateBuilder: RestTemplateBuilder,
                         val oidcRequestContextHolder: OIDCRequestContextHolder,
                         val registry: MeterRegistry,
                         val securityTokenExchangeService: SecurityTokenExchangeService) {

    @Value("\${eessifagmodulservice.url:http://localhost}")
    lateinit var fagmodulUrl: String

    @Value("\${eessipen-eux-rina.url:http://localhost}")
    lateinit var euxrinaapi: String

    @Value("\${EessiPensjonFrontendApi_v1.url:http://localhost}")
    lateinit var apiFssUrl: String

    @Value("\${EessiPensjonFrontendApi.v1.apiKey:key}")
    lateinit var apiFssApiKey: String

    @Value("\${aktoerregister.api.v1.url:http://localhost}")
    lateinit var url: String


    @Bean
    fun euxRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(euxrinaapi)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(
                        OidcHeaderRequestInterceptor(oidcRequestContextHolder),
                        RequestResponseLoggerInterceptor())
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }

    @Bean
    //denne skal benytte OidcHeaderRequestInterceptor for kall til fagmodulen osv.. som kan kalle direkte.
    fun fagmodulRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(fagmodulUrl)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(RequestResponseLoggerInterceptor(), OidcHeaderRequestInterceptor(oidcRequestContextHolder))
                .customizers(MetricsRestTemplateCustomizer(registry, DefaultRestTemplateExchangeTagsProvider(), "eessipensjon_frontend-api_fagmodul"))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }

    @Bean
    //denne skal benytte UntToOidcInterceptor for kall til fagmodulen på veiene av noen som ikke kan kalle direkte.
    fun fagmodulUntToRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(fagmodulUrl)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(RequestResponseLoggerInterceptor(), UntToOidcInterceptor(securityTokenExchangeService))
                .customizers(MetricsRestTemplateCustomizer(registry, DefaultRestTemplateExchangeTagsProvider(), "eessipensjon_frontend-api_fagmodulUntTo"))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }



    @Qualifier("apiFssRestTemplate")
    @Bean
    fun apiFssRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(apiFssUrl)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(
                        OidcHeaderRequestInterceptor(oidcRequestContextHolder),
                        ApigwHeaderRequestInterceptor(apiFssApiKey),
                        RequestResponseLoggerInterceptor())
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }

    @Bean
    fun aktoerregisterRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(url)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(RequestResponseLoggerInterceptor(), UntToOidcInterceptor(securityTokenExchangeService))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }
}
