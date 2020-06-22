package no.nav.eessi.pensjon.api.pensjon

import io.swagger.annotations.ApiOperation
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.security.token.support.core.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

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

        } catch (sce: HttpStatusCodeException) {
            return ResponseEntity.status(sce.statusCode).body(errorBody(sce.responseBodyAsString))
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex.message))
        }
        return response
    }
}
