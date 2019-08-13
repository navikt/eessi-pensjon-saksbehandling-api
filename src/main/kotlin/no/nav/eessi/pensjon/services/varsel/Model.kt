package no.nav.eessi.pensjon.services.varsel

import java.time.LocalDateTime

data class SendtVarsel(
        val tittel: String,
        val fulltnavn: String,
        val timestamp: LocalDateTime,
        val varseltype: String,
        val parametere: String? = null
)

class VarselServiceException(message: String) : RuntimeException(message)
