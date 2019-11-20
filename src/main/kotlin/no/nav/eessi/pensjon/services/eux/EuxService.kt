package no.nav.eessi.pensjon.services.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Description
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.lang.RuntimeException

@Service
@Description("Service class for EuxBasis - EuxCpiServiceController.java")
class EuxService(private val euxRestTemplate: RestTemplate,
                 @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    /**
     * Henter ut en liste over registrerte institusjoner innenfor spesifiserte EU-land
     *
     * @param bucType
     * @param landKode
     */
    fun getInstitusjoner(bucType: String, landKode: String = ""): ResponseEntity<String> {

        val builder = UriComponentsBuilder.fromPath("/institusjoner")
                .queryParam("BuCType", bucType)
                .queryParam("LandKode", landKode)

        val httpEntity = HttpEntity("")

        return metricsHelper.measure("hentinstitusjoner") {
            try {
                logger.info("Henter registrerte institusjoner")
                euxRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String::class.java)
            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under henting av institusjoner ex: $ex body: ${ex.responseBodyAsString}")
                throw RuntimeException("En feil oppstod under henting av institusjoner ex: ${ex.message} body: ${ex.responseBodyAsString}")
            } catch (ex: Exception) {
                logger.error("En feil oppstod under henting av institusjoner ex: $ex")
                throw RuntimeException("En feil oppstod under henting av institusjoner ex: ${ex.message}")
            }
        }
    }
}
