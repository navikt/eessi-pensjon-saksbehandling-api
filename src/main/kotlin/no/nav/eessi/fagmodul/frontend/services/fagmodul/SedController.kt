package no.nav.eessi.fagmodul.frontend.services.fagmodul

import io.swagger.annotations.ApiOperation
import no.nav.eessi.fagmodul.frontend.utils.counter
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.eessi.fagmodul.frontend.utils.successBody
import no.nav.eessi.fagmodul.frontend.utils.typeRef
import no.nav.security.oidc.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@Protected
@RestController
@RequestMapping("/sed")
class SedController(private val fagmodulRestTemplate: RestTemplate) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(SedController::class.java) }

    private final val opprettBucTellerNavn = "eessipensjon_frontend-api.opprettbuc"
    private val opprettBucVellykkede = counter(opprettBucTellerNavn, "vellykkede")
    private val opprettBucFeilede = counter(opprettBucTellerNavn, "feilede")
    private final val leggTilSedTellerNavn = "eessipensjon_frontend-api.leggtilsed"
    private val leggTilSedVellykkede = counter(leggTilSedTellerNavn, "vellykkede")
    private val leggTilSedFeilede = counter(leggTilSedTellerNavn, "feilede")
    private final val confirmSedTellerNavn = "eessipensjon_frontend-api.confirmsed"
    private val confirmSedVellykkede = counter(confirmSedTellerNavn, "vellykkede")
    private val confirmSedFeilede = counter(confirmSedTellerNavn, "feilede")
    private final val hentSedFraRinaCaseTellerNavn = "eessipensjon_frontend-api.hentsedfrarinacase"
    private val hentSedFraRinaCaseVellykkede = counter(hentSedFraRinaCaseTellerNavn, "vellykkede")
    private val hentSedFraRinaCaseFeilede = counter(hentSedFraRinaCaseTellerNavn, "feilede")
    private final val slettSedFraRinaCaseTellerNavn = "eessipensjon_frontend-api.slettsedfrarinacase"
    private val slettSedFraRinaCaseVellykkede = counter(slettSedFraRinaCaseTellerNavn, "vellykkede")
    private val slettSedFraRinaCaseFeilede = counter(slettSedFraRinaCaseTellerNavn, "feilede")
    private final val sendSedFraRinaCaseTellerNavn = "eessipensjon_frontend-api.sendsedfrarinacase"
    private val sendSedFraRinaCaseVellykkede = counter(sendSedFraRinaCaseTellerNavn, "vellykkede")
    private val sendSedFraRinaCaseFeilede = counter(sendSedFraRinaCaseTellerNavn, "feilede")

    /**
     * Kjører prosess OpprettBuCogSED på EUX for å få dokuemt opprett i Rina
     * @param request informasjon om SED
     * @return EUXCaseID
     */
    @ApiOperation("kjører prosess OpprettBuCogSED på EUX for å få dokuemt opprett i Rina")
    @PostMapping("/buc/create", consumes = ["application/json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createDocument(@RequestBody request: SedRequest): ResponseEntity<String> {
        val path = "/sed/buc/create"
        logger.info("Kaller fagmodulen: $path med SedRquest: $request")

        val builder = UriComponentsBuilder.fromPath(path)
        val httpEntity = HttpEntity(request, HttpHeaders())

        return try {
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.POST,
                    httpEntity,
                    String::class.java)

            if (response.statusCode.isError || response.body.isNullOrEmpty()) {
                logger.error("Feil ved opprettelse av BUC og SED på RINA")
                opprettBucFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Opprettelse av BUC og SED på RINA feilet"))
            } else {
                logger.info("Alt ok, SED created")
                opprettBucVellykkede.increment()
                ResponseEntity.ok().body(response.body)
            }
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul, ${ex}, ${uuid}")
            opprettBucFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Opprettelse av BUC og SED på RINA feilet, Melding: ${ex.message}", uuid))
        }
    }

    /**
     * Kjører prosess Confirm for valgt SED før create. kan så validere korrekt data før Create.
     * @param request informasjon om SED
     * @return Rinanummer
     */
    @ApiOperation("Genereren en Nav-Sed (SED), viser en oppsumering av SED. Før evt. innsending til EUX/Rina")
    @PostMapping("/preview", consumes = ["application/json"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun confirmDocument(@RequestBody request: SedRequest): ResponseEntity<String> {
        logger.info("nav-sed Confirmer SED : $request")

        val path = "/sed/preview"
        val builder = UriComponentsBuilder.fromPath(path)
        val httpEntity = HttpEntity(request, HttpHeaders())

        return try {
            logger.info("Kaller fagmodulen: $path med SedRquest: $request")
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.POST,
                    httpEntity,
                    String::class.java)
            if (response.statusCode.isError || response.body.isNullOrEmpty()) {
                logger.error("Feil ved preview SED,  ${response.statusCode}")
                confirmSedFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Forhåndsvisning og preutfylling av SED"))
            } else {
                logger.info("OK, preview SED")
                confirmSedVellykkede.increment()
                ResponseEntity.ok().body(response.body)
            }
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul, ${ex}, ${uuid}")
            confirmSedFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Forhåndsvisning og preutfylling SED, Melding: ${ex.message}", uuid))
        }
    }

    /**
     * Legger til SED på et eksisterende Rina document. kjører preutfylling
     * @param request informasjon om SED
     * @return EUXCaseId
     */
    @ApiOperation("legge til SED på et eksisterende Rina document. kjører preutfylling")
    @PostMapping("/add")
    fun addDocument(@RequestBody request: SedRequest): ResponseEntity<String> {
        val path = "/sed/add"

        logger.info("Kaller fagmodulen: $path med SedRquest: $request")
        val builder = UriComponentsBuilder.fromPath(path)
        val httpEntity = HttpEntity(request, HttpHeaders())

        return try {
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.POST,
                    httpEntity,
                    typeRef<String>())

            if (response.statusCode.is2xxSuccessful) {
                logger.info("OK, added SED")
                leggTilSedVellykkede.increment()
                response
            } else {
                val uuid = UUID.randomUUID().toString()
                logger.error("Feil ved leggetil SED på BUC, ${request.buc}, ${uuid}")
                leggTilSedFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("ved leggetil SED på BUC", uuid))
            }
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul: ${ex}, ${uuid}")
            leggTilSedFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("ved leggetil SED på BUC, Melding: ${ex.message}", uuid))
        }
    }

    /**
     * Sender eksisterende midlertidig lagret SED
     * @param request informasjon om SED
     * @response Rinanummer
     */
    @ApiOperation("sendSed send current sed")
    @GetMapping("/send/{euxcaseId}/{documentId}")
    fun sendSed(@PathVariable("euxcaseId", required = true) euxcaseId: String,
                @PathVariable("documentId", required = true) documentId: String): ResponseEntity<String> {

        val path = "/sed/send/{euxcaseId}/{documentId}"

        val uriParams = mapOf("euxcaseId" to euxcaseId, "documentId" to documentId)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        val httpEntity = HttpEntity("", HttpHeaders())

        return try {
            logger.info("Kaller fagmodulen: $path")
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)

            if (response.statusCode.isError || response.body.isNullOrEmpty()) {
                logger.error("Sending av SED fra RINA feilet.")
                sendSedFraRinaCaseFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("ved sending av SED fra RINA"))
            } else {
                sendSedFraRinaCaseVellykkede.increment()
                ResponseEntity.ok().body(successBody())
            }
        }catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul: ${ex}, ${uuid}")
            sendSedFraRinaCaseFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("ved sending av SED fra RINA, Melding: ${ex.message}", uuid))
        }
    }

    /**
     * Henter ut en SED fra et eksisterende Rina document. krever unik dokumentid fra valgt SED
     * @param rinanr
     * @param documentid
     * @return SED
     */
    @ApiOperation("henter ut en SED fra et eksisterende Rina document. krever unik dokumentid fra valgt SED")
    @GetMapping("/{euxcaseid}/{documentid}")
    fun getDocument(@PathVariable("euxcaseid", required = true) euxCaseid: String,
                    @PathVariable("documentid", required = true) documentid: String): ResponseEntity<String> {

        val path = "/sed/{euxcaseid}/{documentid}"
        val uriParams = mapOf("euxcaseid" to euxCaseid, "documentid" to documentid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)
        val httpEntity = HttpEntity("")

        return try {
            logger.info("Kaller fagmodulen: $path med ${builder.toUriString()}")
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)
            if (response.statusCode.is2xxSuccessful) {
                hentSedFraRinaCaseVellykkede.increment()
                response
            } else {
                hentSedFraRinaCaseFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody(response.body ?: "Error ved henting av SED ut fra RINA"))
            }
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul: ${ex}, ${uuid}")
            hentSedFraRinaCaseFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("henting av SED, Melding: ${ex.message}", uuid))
        }
    }

    /**
     * Sletter SED fra et eksisterende Rina document. Krever unik dokumentid fra valgt SED
     * @param rinanr
     * @param documentid
     */
    @ApiOperation("sletter SED fra et eksisterende Rina document. krever unik dokumentid fra valgt SED")
    @DeleteMapping("/{euxcaseid}/{documentid}")
    fun deleteDocument(@PathVariable("euxcaseid", required = true) euxcaseid: String,
                       @PathVariable("documentid", required = true) documentid: String): ResponseEntity<String> {

        val path = "/sed/{euxcaseid}/{documentid}"
        val uriParams = mapOf("euxcaseid" to euxcaseid, "documentid" to documentid)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)
        val httpEntity = HttpEntity("")

        return try {
            logger.info("Kaller fagmodulen: $path med ${builder.toUriString()}")
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.DELETE,
                    httpEntity,
                    Unit::class.java)

            if (!response.statusCode.isError) {
                logger.info("Sletting av SED $documentid fra Rina OK")
                slettSedFraRinaCaseVellykkede.increment()
                ResponseEntity.ok().body(successBody())
            } else {
                logger.error("Sletting av SED $documentid fra Rina Feilet")
                slettSedFraRinaCaseFeilede.increment()
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Sletting av SED fra Rina dokument feilet"))
            }
        } catch(ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul, ${ex}, ${uuid}")
            slettSedFraRinaCaseFeilede.increment()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Sletting av SED fra Rina dokument feilet, Melding: ${ex.message}", uuid))
        }
    }
}
