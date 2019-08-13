package no.nav.eessi.pensjon.services.varsel

import no.nav.eessi.pensjon.services.aktoerregister.AktoerregisterService
import no.nav.eessi.pensjon.services.fagmodul.PersonInformasjonException
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.successBody
import no.nav.security.oidc.api.Unprotected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

private val logger = LoggerFactory.getLogger(VarselController::class.java)

@Unprotected
@RestController
@RequestMapping("/api/varsel")
class VarselController(val varselService: VarselService,
                       val aktoerregisterService: AktoerregisterService) {

    @PostMapping
    fun sendVarsel(@RequestParam("aktoerId") aktoerId: String,
                   @RequestParam("saksId") saksId: String): ResponseEntity<String> {
        try {
            // FIXME This is a hack because Pesys uses the wrong identifier in some cases
            val fnr = if (isProbablyAFnrSentAsAktoerId(aktoerId)) aktoerId else hentFnrForAktoerId(aktoerId)
            varselService.sendVarsel(fnr, saksId, "EessiPenVarsleBrukerUfore")
        } catch( pie: PersonInformasjonException) {
            val uuid = UUID.randomUUID().toString()
            logger.error(pie.message + ": " + uuid)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(pie.message!!, uuid))
        } catch( vse: VarselServiceException) {
            val uuid = UUID.randomUUID().toString()
            logger.error(vse.message + ": " + uuid)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody(vse.message!!, uuid))
        }
        return ResponseEntity.ok().body(successBody())
    }

    private fun isProbablyAFnrSentAsAktoerId(aktorid: String) = aktorid.length == 11

    fun hentFnrForAktoerId(aktoerId: String): String{
        return aktoerregisterService.hentGjeldendeNorskIdentForAktorId(aktoerId)
    }
}
