package no.nav.eessi.pensjon.services.fagmodul

import io.swagger.annotations.ApiOperation
import no.nav.eessi.pensjon.services.eux.RinaAksjon
import no.nav.eessi.pensjon.utils.counter
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import no.nav.security.oidc.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@Protected
@RequestMapping("/buc/")
class BucController(private val fagmodulRestTemplate: RestTemplate) {

    private val hentMuligeAksjonerVellykkede = counter("eessipensjon_frontend-api.hentmuligeaksjoner", "vellykkede")
    private val hentMuligeAksjonerFeilede = counter("eessipensjon_frontend-api.hentmuligeaksjoner", "feilede")

    private val logger: Logger by lazy { LoggerFactory.getLogger(BucController::class.java) }
    private var muligeAksjoner: String = ""

    init {
        logger.debug("leser aksjoner hvor euxCaseId/RINAnr er ukjent")
        muligeAksjoner = this.javaClass.getResource("/aksjoner/aksjoner.json").readText()
    }

    @ApiOperation("Henter opp hele BUC på valgt caseid")
    @GetMapping("/{euxcaseid}")
    fun getBuc(@PathVariable(value = "rinanr", required = true) euxcaseid: String): ResponseEntity<String?> {

        val path = "/buc/{euxcaseid}"
        logger.info("Kaller fagmodulen buccontroller: $path med euxcaseid : $euxcaseid")

        val uriParams = mapOf("euxcaseid" to euxcaseid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return try {
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    String::class.java)

            if (response.statusCode.isError || response.body.isNullOrEmpty()) {
                logger.error("Feil ved henting av Buc")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Feil ved henting av Buc"))
            } else {
               response
            }
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feil ved henting av Buc: ${ex}, ${uuid}")
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Feil ved henting av Buc, Melding: ${ex.message}", uuid))
        }
    }

    @ApiOperation("Henter opp navn på valgt buc (P_BUC_01, P_BUC_05....)")
    @GetMapping("/{euxcaseid}/name")
    fun getProcessDefinitionName(@PathVariable(value = "euxcaseid", required = true) euxcaseid: String): ResponseEntity<String?> {

        val path = "/buc/{euxcaseid}/name"
        logger.info("Kaller fagmodulen buccontroller: $path med euxcaseid : $euxcaseid")

        val uriParams = mapOf("euxcaseid" to euxcaseid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return try {
            val bucNameResponse = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    String::class.java)

            if (bucNameResponse.statusCode.is2xxSuccessful) {
                bucNameResponse
            } else {
                logger.error("Fagmodul feiler med følgende melding: ${bucNameResponse.statusCode} og body: ${bucNameResponse.body}")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Uthenting av Buc navn eller type feiler! "))
            }

        } catch (exception: Exception){
            val uuid = UUID.randomUUID().toString()
            logger.error("Kontakt mot fagmodulen feiler ${exception.message}, ${uuid}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feil ved henting navn, Melding: ${exception.message}", uuid))
        }
    }

    @ApiOperation("Lager aksjon for buc/sed. Dersom vi ikke har euxCaseId.")
    @GetMapping("/aksjoner")
    fun getDefaultMuligeAksjoner() : List<RinaAksjon> {
        return mapJsonToAny(muligeAksjoner, typeRefs())
    }

    @ApiOperation("Henter opp mulige aksjon som kan utføres på valgt buc")
    @GetMapping("/{euxcaseid}/aksjoner")
    fun getMuligeAksjoner(@PathVariable(value = "euxcaseid", required = true) euxcaseid: String): ResponseEntity<String?> {

        val path = "/buc/{euxcaseid}/aksjoner"

        logger.info("Kaller fagmodulen buccontroller: $path med euxcaseid : $euxcaseid")

        val uriParams = mapOf("euxcaseid" to euxcaseid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return try {
            val bucResponse = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    String::class.java)

            if (bucResponse.statusCode.is2xxSuccessful) {
                hentMuligeAksjonerVellykkede.increment()
                bucResponse
            } else {
                logger.error("Fagmodul feiler med følgende melding: ${bucResponse.statusCode} og body: ${bucResponse.body}")
                hentMuligeAksjonerFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Uthenting av aksjoner på buc feiler "))
            }

        } catch (exception: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Kontakt mot fagmodulen feiler ${exception.message}, ${uuid}")
            hentMuligeAksjonerFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feil ved henting aksjon, Melding: ${exception.message}", uuid))
        }
    }

    @ApiOperation("Henter opp mulige aksjon som kan utføres på valgt buc, filtert på sed starter med 'P'")
    @GetMapping("/{euxcaseid}/aksjoner/{filter}")
    fun getMuligeAksjonerFilter(@PathVariable(value = "euxcaseid", required = true) euxcaseid: String,
                                @PathVariable(value = "filter", required = true) filter: String? = null): ResponseEntity<String?> {

        val path = "/buc/{euxcaseid}/aksjoner/{filter}"

        logger.info("Kaller fagmodulen buccontroller: $path med riannr : $euxcaseid og filter: $filter")

        val uriParams = mapOf("euxcaseid" to euxcaseid, "filter" to filter)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return try {
            val bucResponse = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    String::class.java)

            if (bucResponse.statusCode.is2xxSuccessful) {
                hentMuligeAksjonerVellykkede.increment()
                bucResponse
            } else {
                logger.error("Fagmodul feiler med følgende melding: ${bucResponse.statusCode} og body: ${bucResponse.body}")
                hentMuligeAksjonerFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Uthenting av aksjoner med filter på buc feiler. "))
            }

        } catch (exception: Exception){
            val uuid = UUID.randomUUID().toString()
            logger.error("Kontakt mot fagmodulen feiler ${exception.message}, ${uuid}")
            hentMuligeAksjonerFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feil ved henting aksjon filter, Melding: ${exception.message}", uuid))
        }
    }

    @ApiOperation("Henter alle dokumenter knyttet til valgt buc")
    @GetMapping("/{euxcaseid}/allDocuments")
    fun getAllDocuments(@PathVariable(value = "euxcaseid", required = true) euxcaseid: String): ResponseEntity<String?> {
        val path = "/buc/{euxcaseid}/allDocuments"

        logger.info("Kaller fagmodulen buccontroller: $path med euxcaseid : $euxcaseid")

        val uriParams = mapOf("euxcaseid" to euxcaseid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        return try {
            val bucResponse = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    null,
                    String::class.java)

            if (bucResponse.statusCode.is2xxSuccessful) {
                bucResponse
            } else {
                logger.error("Fagmodul feiler med følgende melding: ${bucResponse.statusCode} og body: ${bucResponse.body}")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Uthenting av alle dokumenter i buc feiler."))
            }

        } catch (exception: Exception){
            val uuid = UUID.randomUUID().toString()
            logger.error("Kontakt mot fagmodulen feiler ${exception.message}, ${uuid}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feil ved henting alle dokumenter på valgt buc, Melding: ${exception.message}", uuid))
        }
    }
}
