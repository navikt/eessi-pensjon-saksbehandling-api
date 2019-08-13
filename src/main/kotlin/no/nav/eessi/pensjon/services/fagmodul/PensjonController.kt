package no.nav.eessi.pensjon.services.fagmodul

import io.swagger.annotations.ApiOperation
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.security.oidc.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@Protected
@RequestMapping("/pensjon/")
class PensjonController(private val fagmodulRestTemplate: RestTemplate) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PensjonController::class.java) }

    @ApiOperation("Henter ut saktype knyttet til den valgte sakId og aktoerId")
    @GetMapping("/saktype/{sakId}/{aktoerId}")
    fun hentPensjonSakType(@PathVariable("sakId", required = true) sakId: String, @PathVariable("aktoerId", required = true) aktoerId: String): ResponseEntity<String> {

        val path = "/pensjon/saktype/{sakId}/{aktoerId}"

        val uriParams = mapOf("sakId" to sakId, "aktoerId" to aktoerId)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)

        val httpEntity = HttpEntity("", HttpHeaders())

        val response : ResponseEntity<String>
        try {
            logger.info("Kaller fagmodulen: $path")
            response = fagmodulRestTemplate.exchange(builder.toUriString(),
                HttpMethod.GET,
                httpEntity,
                String::class.java)
        } catch (ex: HttpClientErrorException.NotFound) {
            logger.info("Fikk ikke saktype fra fagmodul")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Henting av saktype feilet - ikke funnet"))
        } catch (ex: HttpClientErrorException) {
            logger.error("Henting av saktype fra PESYS feilet", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Henting av saktype fra PESYS feilet"))
        } catch (ex: HttpServerErrorException) {
            logger.error("Henting av saktype fra PESYS feilet", ex)
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorBody("Henting av saktype fra PESYS feilet"))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul, ${ex}, ${uuid}", ex)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("ved henting av saktype fra PESYS, Melding: ${ex.message}", uuid))
        }
        return response
    }
}
