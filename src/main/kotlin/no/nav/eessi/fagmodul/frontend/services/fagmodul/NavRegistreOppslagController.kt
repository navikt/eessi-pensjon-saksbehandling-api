package no.nav.eessi.fagmodul.frontend.services.fagmodul

import no.nav.eessi.fagmodul.frontend.services.aktoerregister.AktoerregisterException
import no.nav.eessi.fagmodul.frontend.services.aktoerregister.AktoerregisterService
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Protected
@RestController
class NavRegistreOppslagController(val navRegistreOppslagService: NavRegistreOppslagService,
                                   val aktoerregisterService: AktoerregisterService) {

    private val logger = LoggerFactory.getLogger(NavRegistreOppslagController::class.java)

    @GetMapping(value = ["/person/{aktoerId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
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

    @GetMapping(value = ["/person/hentgjeldendeaktoerid/{fnr}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentGjeldendeAktoerIdForNorskident(@PathVariable(required = true) fnr: String): ResponseEntity<String> {
        logger.info("Henter norskidenter")
        return try{
            ResponseEntity.ok().body(aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(fnr))
        }catch(ae: AktoerregisterException){
            logger.error("Klarte ikke å hente gjeldende aktørId, ${ae.message}")
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorBody("Klarte ikke å hente gjeldende aktørId, ${ae.message}"))
        }
    }
}