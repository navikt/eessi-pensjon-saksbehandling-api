package no.nav.eessi.pensjon.config

import io.micrometer.core.instrument.MeterRegistry
import no.nav.eessi.pensjon.interceptor.OidcHeaderRequestInterceptor
import no.nav.eessi.pensjon.logging.RequestIdHeaderInterceptor
import no.nav.eessi.pensjon.logging.RequestResponseLoggerInterceptor
import no.nav.eessi.pensjon.metrics.RequestCountInterceptor
import no.nav.eessi.pensjon.security.sts.STSService
import no.nav.eessi.pensjon.security.sts.UsernameToOidcInterceptor
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.metrics.AutoTimer
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
                         val securityTokenExchangeService: STSService
) {

    @Value("\${eessi_pensjon_fagmodul_url}")
    lateinit var fagmodulUrl: String

    @Value("\${eessipen-eux-rina.url}")
    lateinit var euxrinaapi: String

    @Value("\${aktoerregister.api.v1.url}")
    lateinit var url: String

    @Autowired
    lateinit var meterRegistry: MeterRegistry

    @Bean
    fun euxRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(euxrinaapi)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        RequestCountInterceptor(meterRegistry),
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
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        RequestCountInterceptor(meterRegistry),
                        RequestResponseLoggerInterceptor(),
                        OidcHeaderRequestInterceptor(oidcRequestContextHolder))
                .customizers(MetricsRestTemplateCustomizer(registry, DefaultRestTemplateExchangeTagsProvider(), "eessipensjon_frontend-api_fagmodul",  AutoTimer.ENABLED))
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
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        RequestCountInterceptor(meterRegistry),
                        RequestResponseLoggerInterceptor(),
                        UsernameToOidcInterceptor(securityTokenExchangeService))
                .customizers(MetricsRestTemplateCustomizer(registry, DefaultRestTemplateExchangeTagsProvider(), "eessipensjon_frontend-api_fagmodulUntTo",  AutoTimer.ENABLED))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }

    @Bean
    fun aktoerregisterRestTemplate(): RestTemplate {
        return restTemplateBuilder
                .rootUri(url)
                .errorHandler(DefaultResponseErrorHandler())
                .additionalInterceptors(
                        RequestIdHeaderInterceptor(),
                        RequestCountInterceptor(meterRegistry),
                        RequestResponseLoggerInterceptor(),
                        UsernameToOidcInterceptor(securityTokenExchangeService))
                .build().apply {
                    requestFactory = BufferingClientHttpRequestFactory(SimpleClientHttpRequestFactory())
                }
    }
}
