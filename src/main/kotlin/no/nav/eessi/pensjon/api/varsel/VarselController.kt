package no.nav.eessi.pensjon.api.varsel

import no.nav.eessi.pensjon.utils.errorBody
import no.nav.security.oidc.api.Protected
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Protected
@RestController
@RequestMapping("/api/varsel")
class VarselController {

    @PostMapping("/{saksId}/{aktoerId}")
    fun sendVarsel(@PathVariable(required = true) aktoerId: String,
                   @PathVariable(required = true) saksId: String): ResponseEntity<String> {
        val uuid = UUID.randomUUID().toString()
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(errorBody("Varsel funksjonaliteten er skrudd av, kontakt EESSI Pensjon for spørsmål", uuid))
    }
}
