package no.nav.eessi.pensjon.services.fagmodul

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.lang.RuntimeException

@Service
class NavRegistreOppslagService(private val fagmodulUntToRestTemplate: RestTemplate,
                                @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(NavRegistreOppslagService::class.java) }

    /**
     * Henter ut personinformasjon fra TPS via PersonV3
     * @param aktoerId
     * @return Personinformasjon
     */
    fun hentPersoninformasjon(aktoerId: String): Personinformasjon? {
        val path = "/personinfo/$aktoerId"
        val uriParams = mapOf("aktoerid" to aktoerId)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)
        val httpEntity = HttpEntity("")

        return metricsHelper.measure("hentpersoninformasjon") {
            try {
                logger.info("Kaller fagmodulen for å hente personinformasjon")
                val resp = fagmodulUntToRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)
                jacksonObjectMapper().readValue(resp.body!!, Personinformasjon::class.java)
            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under henting av personinformasjon ex: $ex body: ${ex.responseBodyAsString}")
                throw RuntimeException("En feil oppstod under henting av personinformasjon ex: ${ex.message} body: ${ex.responseBodyAsString}")
            } catch (ex: Exception) {
                logger.error("En feil oppstod under henting av personinformasjon ex: $ex")
                throw RuntimeException("En feil oppstod under henting av personinformasjon ex: ${ex.message}")
            }
        }
    }

    fun hentPersoninformasjonNavn(aktoerId: String): String {
        return hentPersoninformasjon(aktoerId)?.fulltNavn ?: "N/A"
    }
}
