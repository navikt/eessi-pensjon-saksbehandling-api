package no.nav.eessi.pensjon.api.eux

import no.nav.eessi.pensjon.services.eux.EuxService
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


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
        return euxService.getInstitusjoner(bucType, landkode)
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
        return listOf("Pensjon", "Andre")

    }
}
