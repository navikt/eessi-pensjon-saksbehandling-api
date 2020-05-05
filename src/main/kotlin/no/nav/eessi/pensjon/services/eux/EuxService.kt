package no.nav.eessi.pensjon.services.eux

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
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
     * prøver å sende et SED doument på RINA ut til eu/mottaker.
     * @param euxCaseId  er iden til den aktuelle Buc/Rina sak
     * @param documentId er iden til det unike dokuement/Sed som skal sendes.
     * true hvis alt ok, og sed sendt. Exception error hvis feil.
     */
    fun sendDocumentById(euxCaseId: String, documentId: String) {

        val path = "/buc/{RinaSakId}/sed/{DokumentId}/send"
        val uriParams = mapOf("RinaSakId" to euxCaseId, "DokumentId" to documentId)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return metricsHelper.measure("SendSED") {
            try {
                logger.info("Sender SED euxCaseId: $euxCaseId documentId: $documentId")
                euxRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.POST,
                    null,
                    String::class.java
                )
            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under sending av SED ex: $ex body: ${ex.responseBodyAsString}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under sending av SED ex: $ex")
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
            land.get("landkode").textValue()
        }.distinct().toJson()
    }
}
