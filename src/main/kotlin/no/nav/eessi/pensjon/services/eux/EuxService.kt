package no.nav.eessi.pensjon.services.eux

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.utils.toJson
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
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.ArrayNode
import org.springframework.util.LinkedMultiValueMap


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
    fun getInstitusjoner(bucType: String, landKode: String?): ResponseEntity<String> {

        val params: LinkedMultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("BuCType", bucType)
        landKode?.let { params.add("LandKode", landKode) }

        val builder = UriComponentsBuilder.fromPath("/institusjoner").queryParams(params)

        val httpEntity = HttpEntity("")

        return metricsHelper.measure("hentinstitusjoner") {
            try {
                logger.info("Henter registrerte institusjoner")
                euxRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String::class.java)
            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under henting av institusjoner ex: $ex body: ${ex.responseBodyAsString}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under henting av institusjoner ex: $ex")
                throw ex
            }
        }
    }

    /**
     * Henter en liste av påkoblede landkoder for en konkret buctype
     */
    fun getPaakobledeLand(buctype : String): String {
        val resp = getInstitusjoner(buctype, null)
        return ObjectMapper().readValue(resp.body, ArrayNode::class.java).map { land ->
            land.get("landkode").textValue
        }.distinct().toJson()
    }
}
