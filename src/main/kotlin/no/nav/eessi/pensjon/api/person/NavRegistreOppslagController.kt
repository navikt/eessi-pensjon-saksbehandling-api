package no.nav.eessi.pensjon.api.person

import no.nav.eessi.pensjon.personoppslag.aktoerregister.AktoerregisterService
import no.nav.eessi.pensjon.personoppslag.aktoerregister.IdentGruppe
import no.nav.eessi.pensjon.personoppslag.aktoerregister.NorskIdent
import no.nav.eessi.pensjon.personoppslag.aktoerregister.Result
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import no.nav.eessi.pensjon.services.fagmodul.PersonInformasjonException
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException

@Protected
@RestController
class NavRegistreOppslagController(val navRegistreOppslagService: NavRegistreOppslagService,
                                   val aktoerregisterService: AktoerregisterService) {

    private val logger = LoggerFactory.getLogger(NavRegistreOppslagController::class.java)

    @GetMapping(value = ["/personinfo/{aktoerId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) aktoerId: String): ResponseEntity<String> {
        logger.info("Henter personinformasjon for $aktoerId")
        return try{
            ResponseEntity.ok().body(mapAnyToJson(navRegistreOppslagService.hentPersoninformasjon(aktoerId)!!))
        }catch(pe: PersonInformasjonException){
            logger.error("Klarte ikke å hente personinformasjon, ${pe.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Klarte ikke å hente personinformasjon, ${pe.message}"))
        }
    }

    @GetMapping(value = ["/personinfo/hentgjeldendeaktoerid/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentGjeldendeAktoerIdForNorskident(@PathVariable(required = true) fnr: String): ResponseEntity<String> =
            if (fnr.isBlank()) {
                logger.warn("Kall har blankt fnr")
                ResponseEntity.badRequest().body("blankt fnr")
            } else {
                when (val result = aktoerregisterService.hentGjeldendeIdentFraGruppe(IdentGruppe.AktoerId, NorskIdent(fnr))) {
                    is Result.Found -> {
                        logger.info("Returnerer gjeldende aktoerId for fnr")
                        ResponseEntity.ok().body(result.value.id)
                    }
                    is Result.NotFound -> {
                        logger.info("Fant ikke aktoerId for fnr")
                        ResponseEntity.notFound().build<String>()
                    }
                    is Result.Failure -> {
                        logger.error("Feil ved henting av AktoerId med ${result.cause} cause: ${result.cause.cause}", result.cause)
                        if (result.cause.cause != null && result.cause.cause is HttpStatusCodeException) {
                            val rootCause = result.cause.cause as HttpStatusCodeException
                            ResponseEntity.status(rootCause.statusCode).body(rootCause.responseBodyAsString)
                        } else {
                            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(result.cause.message)
                        }
                    }
                }
            }
}
