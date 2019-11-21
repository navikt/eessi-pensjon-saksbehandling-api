package no.nav.eessi.pensjon.api.person

import no.nav.eessi.pensjon.services.aktoerregister.AktoerregisterService
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import no.nav.eessi.pensjon.services.fagmodul.PersonInformasjonException
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.security.oidc.api.Protected
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
        navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        return try{
            ResponseEntity.ok().body(mapAnyToJson(navRegistreOppslagService.hentPersoninformasjon(aktoerId)!!))
        }catch(pe: PersonInformasjonException){
            logger.error("Klarte ikke å hente personinformasjon, ${pe.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Klarte ikke å hente personinformasjon, ${pe.message}"))
        }
    }

    @GetMapping(value = ["/personinfo/hentgjeldendeaktoerid/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentGjeldendeAktoerIdForNorskident(@PathVariable(required = true) fnr: String): ResponseEntity<String> {
        logger.info("Henter norskidenter")
        return try{
            ResponseEntity.ok().body(aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(fnr))
        } catch (sce: HttpStatusCodeException) {
            return ResponseEntity.status(sce.statusCode).body(errorBody(sce.responseBodyAsString))
        } catch (ex: Exception) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(ex.message))
        }
    }
}
