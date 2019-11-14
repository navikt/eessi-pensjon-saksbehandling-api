package no.nav.eessi.pensjon.services.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.utils.typeRef
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Description
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

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
                euxRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, typeRef<String>())
            } catch (cex: HttpClientErrorException) {
                logger.error("Fikk 4xx respons ved henting av institusjoner, ex: ${cex.message} body: ${cex.responseBodyAsString}")
                throw cex
            } catch (sex: HttpServerErrorException) {
                logger.error("Fikk 5xx respons ved henting av institusjoner, ex: ${sex.message} body: ${sex.responseBodyAsString}")
                throw sex
            } catch (ex: Exception) {
                logger.error("Noe gikk galt ved henting av institusjoner, ex: ${ex.message}")
                throw ex
            }
        }
    }
}
