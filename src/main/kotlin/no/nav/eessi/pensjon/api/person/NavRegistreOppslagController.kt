package no.nav.eessi.pensjon.api.person

import no.nav.eessi.pensjon.personoppslag.pdl.PersonService
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException

/**
 * Denne kontrolleren brukes av eessi-pensjon-selvbetjening-api
 *
 * Burde flyttes ut av dette prosjektet og over i et eget fss-prosjekt hvor det gir mer mening.
 */
@Protected
@RestController
class NavRegistreOppslagController(private val personService: PersonService) {

    private val logger = LoggerFactory.getLogger(NavRegistreOppslagController::class.java)

    @GetMapping(value = ["/personinfo/{aktoerId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) aktoerId: String): ResponseEntity<String> {
        logger.info("Henter personinformasjon fra PDL for AkterId=$aktoerId")

        return try {
            val navn = personService.hentPerson(AktoerId(aktoerId))!!.navn!!

            val person = Personinformasjon(
                fulltNavn = navn.sammensattNavn,
                fornavn = navn.fornavn,
                mellomnavn = navn.mellomnavn,
                etternavn = navn.etternavn
            )

            ResponseEntity.ok(mapAnyToJson(person))
        } catch(ex: Exception) {
            logger.error("Klarte ikke å hente personinformasjon fra PDL: ", ex)

            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody("Klarte ikke å hente personinformasjon, ${ex.message}"))
        }
    }

    @GetMapping(value = ["/personinfo/hentgjeldendeaktoerid/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentGjeldendeAktoerIdForNorskident(@PathVariable(required = true) fnr: String): ResponseEntity<String> =
            if (fnr.isBlank()) {
                logger.warn("Kall har blankt fnr")
                ResponseEntity.badRequest().body("blankt fnr")
            } else {
                runCatching {
                    personService.hentAktorId(fnr)
                }.fold({ ident ->
                    logger.info("Returnerer gjeldende aktoerId for fnr")
                    ResponseEntity.ok(ident.id)
                }, { exception: Throwable ->
                    logger.error("Feil ved henting av AktoerId med $exception cause: ${exception.cause}", exception)
                    if (exception.cause != null && exception.cause is HttpStatusCodeException) {
                        val rootCause = exception.cause as HttpStatusCodeException
                        ResponseEntity.status(rootCause.statusCode).body(rootCause.responseBodyAsString)
                    } else {
                        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.message)
                    }
                })
            }
}

data class Personinformasjon(
    var fulltNavn: String,
    var fornavn: String,
    var mellomnavn: String? = null,
    var etternavn: String
)
