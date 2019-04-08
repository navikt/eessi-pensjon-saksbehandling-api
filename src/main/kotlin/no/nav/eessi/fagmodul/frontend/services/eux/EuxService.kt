package no.nav.eessi.fagmodul.frontend.services.eux

import com.google.common.collect.Sets
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

private val logger = LoggerFactory.getLogger(EuxService::class.java)

@Service
@Description("Service class for EuxBasis - EuxCpiServiceController.java")
class EuxService(private val euxRestTemplate: RestTemplate) {

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
     * @param buc
     */
    fun getAvailableSEDonBuc(buc: String?): List<String> {
        val list1 = listOf("P2000")
        val list2 = listOf("P2100")
        val list3 = listOf("P2200")
        val list4 = listOf("")
        val list5 = listOf("P4000", "P5000", "P6000", "P3000_NO")

        val map: Map<String, List<String>> =
                mapOf(
                        "P_BUC_01" to list1,
                        "P_BUC_02" to list2,
                        "P_BUC_03" to list3,
                        "P_BUC_05" to list4,
                        "P_BUC_06" to list5
                )
        if (buc.isNullOrEmpty()) {
            val set: MutableSet<String> = Sets.newHashSet()
            set.addAll(list1)
            set.addAll(list2)
            set.addAll(list3)
            set.addAll(list4)
            set.addAll(list5)
            return set.toList()
        }
        return map[buc].orEmpty()
    }
}
