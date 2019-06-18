package no.nav.eessi.fagmodul.frontend.services.eux

import no.nav.eessi.fagmodul.frontend.utils.counter
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.eessi.fagmodul.frontend.utils.typeRef
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException


@Service
@Description("Service class for EuxBasis - EuxCpiServiceController.java")
class EuxService(private val euxRestTemplate: RestTemplate) {

    private val logger = LoggerFactory.getLogger(EuxService::class.java)

    private final val hentInstitusjonerTellerNavn = "eessipensjon_frontend-api.hentinstitusjoner"
    private val hentInstitusjonerVellykkede = counter(hentInstitusjonerTellerNavn, "vellykkede")
    private val hentInstitusjonerFeilede = counter(hentInstitusjonerTellerNavn, "feilede")

    private final val hentRinaSakerTellerNavn = "eessipensjon_frontend-api.hentrinasaker"
    private val hentRinaSakerVellykkede = counter(hentRinaSakerTellerNavn, "vellykkede")
    private val hentRinaSakerFeilede = counter(hentRinaSakerTellerNavn, "feilede")


    /**
     * Henter ut en liste over registrerte institusjoner innenfor spesifiserte EU-land
     *
     * @param bucType
     * @param landKode
     */
    fun getInstitusjoner(bucType: String, landKode: String = ""): ResponseEntity<String> {

        //https://eux-app.nais.preprod.local/cpi/institusjoner?BuCType=P_BUC_01
        val builder = UriComponentsBuilder.fromPath("/institusjoner")
                .queryParam("BuCType", bucType)
                .queryParam("LandKode", landKode)

        val httpEntity = HttpEntity("")

        return try {
            logger.info("Henter registrerte institusjoner")

            val response = euxRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, typeRef<String>())

            if (response.statusCode.isError) {
                logger.warn("Henting av registrerte institusjoner feilet med feilkode: ${response.statusCode}")
                hentInstitusjonerFeilede.increment()
                ResponseEntity.status(response.statusCode).body(errorBody("ved henting av institusjoner mot eux server"))

            } else {
                hentInstitusjonerVellykkede.increment()
                response
            }
        } catch (ex: IOException) {
            logger.error("Henting av registrerte institusjoner feilet", ex)
            hentInstitusjonerFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("ved henting av institusjoner mot eux server, Melding: ${ex.message}"))

        }
    }

    /**
     * Henter Rinasaker
     * @param rinaNummer
     * @param fnr
     */
    fun getRinaSaker(rinaNummer: String = "", fnr: String = ""): ResponseEntity<String?> {
        val builder = UriComponentsBuilder.fromPath("/rinasaker")
                .queryParam("BuCType", "")
                .queryParam("Fødselsnummer", fnr)
                .queryParam("RINASaksnummer", rinaNummer)
                .queryParam("Status","open")

        return try {
            val httpEntity = HttpEntity("")
            logger.debug("Henter rina saker fra EUX mot path: ${builder.toUriString()}")
            val response = euxRestTemplate.exchange(builder.toUriString(), HttpMethod.GET, httpEntity, String::class.java)

            if (response.statusCode.isError) {
                logger.error("Henting av rinsa saker feilet med feilkode: ${response.statusCode}")
                hentRinaSakerFeilede.increment()
                ResponseEntity.status(response.statusCode).body(errorBody("Feiler ved henting av rinasaker mot EUX, melding: ${response.body}"))
            } else {
                hentRinaSakerVellykkede.increment()
                response
            }
        } catch (ex: Exception) {
            hentRinaSakerFeilede.increment()
            logger.error("Henting av rinas saker feilet", ex)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feiler ved henting av rinasaker mot EUX"))

        }
    }

    /**
     * Own impl. no list from eux that contains list of SED to a speific BUC
     * @param bucType
     */
    fun getAvailableSEDonBuc(bucType: String?): List<String> {
        val buc01 = listOf("P2000")
        val buc02 = listOf("P2100")
        val buc03 = listOf("P2200")
        val buc05 = listOf("P5000","P6000","P7000","P8000","P9000")
        val buc06 = listOf("P5000","P6000","P7000","P10000")

        val map: Map<String, List<String>> = mapOf(
            "P_BUC_01" to buc01,
            "P_BUC_02" to buc02,
            "P_BUC_03" to buc03,
            "P_BUC_05" to buc05,
            "P_BUC_06" to buc06
        )

        if (bucType.isNullOrEmpty()) {
            val set = mutableSetOf<String>()
            set.addAll(buc01)
            set.addAll(buc02)
            set.addAll(buc03)
            set.addAll(buc05)
            set.addAll(buc06)
            return set.toList()
        }
        return map[bucType].orEmpty()
    }
}
