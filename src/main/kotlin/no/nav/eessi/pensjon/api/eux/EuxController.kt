package no.nav.eessi.pensjon.api.eux

import no.nav.eessi.pensjon.services.eux.EuxService
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException


@Protected
@RestController
@RequestMapping("/eux")
class EuxController(private val euxService: EuxService, private val navRegistreService: NavRegistreOppslagService) {

    private val logger = LoggerFactory.getLogger(EuxController::class.java)

    @Value("\${rina_host.url}")
    lateinit var rinaUrl: String

    @GetMapping("/rinaurl")
    fun getRinaURL(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("rinaUrl" to "https://$rinaUrl/portal/#/caseManagement/"))
    }

    @GetMapping("/institutions/{buctype}/{countrycode}")
    fun getInstitutionsWithCountry(@PathVariable(value = "buctype", required = true) bucType: String,
            @PathVariable(value = "countrycode", required = false) landkode: String = ""): ResponseEntity<String> {
        return try {
            euxService.getInstitusjoner(bucType, landkode)
        } catch (sce: HttpStatusCodeException) {
            ResponseEntity.status(sce.statusCode).body(errorBody(sce.responseBodyAsString))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex.message))
        }
    }

    @GetMapping("/countrycode")
    fun getCountryCode(): List<String> {
        return try {
            navRegistreService.landkoder()
        } catch (ex: Exception) {
            logger.error(ex.message)
            listOf("NO", "SE", "DK", "FI")
        }
    }

    @GetMapping("/subjectarea")
    fun getSubjectArea(): List<String> {
        return listOf("Pensjon")

    }
}
