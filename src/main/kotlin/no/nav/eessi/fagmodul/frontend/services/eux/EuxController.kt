package no.nav.eessi.fagmodul.frontend.services.eux

import io.swagger.annotations.ApiOperation
import no.nav.eessi.fagmodul.frontend.services.fagmodul.BucController
import no.nav.eessi.fagmodul.frontend.services.fagmodul.NavRegistreOppslagService
import no.nav.eessi.fagmodul.frontend.utils.*
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.regex.Pattern.matches


@Protected
@RestController
@RequestMapping("/eux")
class EuxController(private val euxService: EuxService, private val navRegistreService: NavRegistreOppslagService, private val bucController: BucController) {

    private val logger = LoggerFactory.getLogger(EuxController::class.java)

    @Value("\${rina_host.url}")
    lateinit var rinaUrl: String

    @GetMapping("/rinaurl")
    fun getRinaURL(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("rinaUrl" to "https://$rinaUrl/portal/#/caseManagement/"))
    }

    @GetMapping("/case/{caseid}/{actorid}/{rinaid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun validateCaseNumberWithRinaID(@PathVariable caseid: String,
                                     @PathVariable actorid: String,
                                     @PathVariable rinaid: String): ResponseEntity<Map<String, String>> {
        if (matches("\\d+", caseid) && matches("\\d+", actorid) && matches("\\d+", rinaid)) {
            return ResponseEntity.ok(mapOf("casenumber" to caseid, "pinid" to actorid, "rinaid" to rinaid))
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("serverMessage" to "invalidCase"))
    }

    @GetMapping("/case/{caseid}/{actorid}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun validateCaseNumber(@PathVariable caseid: String,
                           @PathVariable actorid: String): ResponseEntity<Map<String, String>> {
        if (matches("\\d+", caseid) && matches("\\d+", actorid)) {
            return ResponseEntity.ok(mapOf("casenumber" to caseid, "pinid" to actorid))
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(mapOf("serverMessage" to "invalidCase"))
    }

    @ApiOperation("henter liste av alle tilgjengelige BuC-typer")
    @GetMapping("/bucs", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getBucs(): List<String> {
        return listOf(
                "P_BUC_01",
                "P_BUC_02",
                "P_BUC_03",
                "P_BUC_05",
                "P_BUC_06"
        )
    }

    @ApiOperation("henter liste over seds, seds til valgt buc eller seds til valgt rinasak")
    @GetMapping("/seds", "/seds/{buctype}", "/seds/{buctype}/{rinanr}")
    fun getSeds(@PathVariable(value = "buctype", required = false) bucType: String?,
                @PathVariable(value = "rinanr", required = false) euxCaseId: String?): ResponseEntity<String?> {

        if (euxCaseId != null) {
            val result = getSedActionFromRina(euxCaseId)

            if (result.hasBody()) {
                val resultlist = mapJsonToAny(result.body!!, typeRefs<List<String>>())
                if (resultlist.isEmpty()) {
                    return euxService.getAvailableSEDonBuc(bucType).toResponse()
                } else {
                    return filterPensionSedAndSort(resultlist).toResponse()
                }
            } else {
                return ResponseEntity.badRequest().body("")
            }
        }
        //seds eller bestem mulige seds på en bucType (hardkoddet liste)
        return euxService.getAvailableSEDonBuc(bucType).toResponse()

    }

    //Return liste av sedType fra fagmodul/BucController
    fun getSedActionFromRina(euxCaseId: String): ResponseEntity<String?> {
        try {
            val response = bucController.getMuligeAksjoner(euxCaseId)
            return if (response.statusCode.is2xxSuccessful) {
                logger.debug("Return liste av tilgjenglige SED som kan opprettes på buc")
                ResponseEntity.ok().body(response.body)
            } else {
                response
            }
        } catch(ex: Exception) {
            logger.error("Error, hente aksjon fra Buc: ${euxCaseId}")
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("Error, hente aksjon fra Buc: ${euxCaseId}")
        }
    }


    @ApiOperation("Henter lisgte over rina mot euxcaseId")
    @GetMapping( "/rinasaker/{euxcaseId}")
    fun getRinaSakerPaaEuxCaseId(@PathVariable(value = "euxcaseId", required = true) euxcaseId: String): ResponseEntity<String?>  {
        return  euxService.getRinaSaker(euxcaseId, "")
    }

    @GetMapping("/institutions/{buctype}/{countrycode}")
    fun getInstitutionsWithCountry(@PathVariable(value = "buctype", required = true) bucType: String,
            @PathVariable(value = "countrycode", required = false) landkode: String = ""): ResponseEntity<String> {
        return euxService.getInstitusjoner(bucType, landkode)
    }

    @GetMapping("/countrycode")
    fun getCountryCode(): List<String> {
        return try {
            navRegistreService.landkoder().filter { s -> s == "NO" } // TODO: Using "NO" temporarily to avoid sending documents to other countries in test by accident
        } catch (ex: Exception) {
            logger.error(ex.message)
            listOf("NO", "SE", "DK", "FI")
        }
    }

    @GetMapping("/subjectarea")
    fun getSubjectArea(): List<String> {
        return listOf("Pensjon", "Andre")
    }
}
