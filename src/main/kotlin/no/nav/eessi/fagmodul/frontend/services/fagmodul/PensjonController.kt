package no.nav.eessi.fagmodul.frontend.services.fagmodul

import io.swagger.annotations.ApiOperation
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.security.oidc.api.Protected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.http.*
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*

@RestController
@Protected
@Profile("fss")
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

        return try {
            logger.info("Kaller fagmodulen: $path")
            val response = fagmodulRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)

            if (response.statusCode.is2xxSuccessful) {
                response
            } else {
                logger.error("Henting av saktype fra PESYS feilet")
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody("Henting av saktype fra PESYS feilet"))
            }
        }catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Feiler med kontakt mot fagmodul, ${ex}, ${uuid}")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("ved henting av saktype fra PESYS, Melding: ${ex.message}", uuid))
        }



    }


}