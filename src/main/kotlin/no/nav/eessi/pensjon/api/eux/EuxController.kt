package no.nav.eessi.pensjon.api.eux

import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.token.support.core.api.Protected
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
class EuxController(private val euxService: EuxService) {

    @Value("\${rina_host.url}")
    lateinit var rinaUrl: String

    @GetMapping("/rinaurl")
    fun getRinaURL(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(mapOf("rinaUrl" to "https://$rinaUrl/portal/#/caseManagement/"))
    }

    @GetMapping("/institutions/{buctype}/{countrycode}")
    fun getInstitutionsWithCountry(
        @PathVariable(value = "buctype") bucType: BucType,
        @PathVariable(value = "countrycode", required = false) landkode: String = ""
    ): ResponseEntity<String> {
        return try {
            val institusjoner = euxService.hentInstitusjoner(bucType, landkode)

            ResponseEntity.ok(institusjoner.toJson())
        } catch (sce: HttpStatusCodeException) {
            ResponseEntity.status(sce.statusCode).body(errorBody(sce.responseBodyAsString))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex.message))
        }
    }

    @GetMapping("/countries/{buctype}")
    fun getPaakobledeland(@PathVariable(value = "buctype") bucType: BucType): ResponseEntity<String> {
        return try {
            val paakobledeLand = euxService.hentInstitusjoner(bucType)
                .map { it.landkode }
                .distinct()

            ResponseEntity.ok(paakobledeLand.toJson())
        } catch (sce: HttpStatusCodeException) {
            ResponseEntity.status(sce.statusCode).body(errorBody(sce.responseBodyAsString))
        } catch (ex: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex.message))
        }
    }

    @GetMapping("/subjectarea")
    fun getSubjectArea(): List<String> {
        return listOf("Pensjon")
    }
}
