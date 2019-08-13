package no.nav.eessi.pensjon.services.fagmodul

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

data class SedRequest(
        val sakId: String? = null,
        val vedtakId: String? = null,
        val kravId: String? = null,
        val aktoerId: String? = null,
        val fnr: String? = null,
        val payload: String? = null,
        val buc: String? = null,
        val sed : String? = null,
        val documentid: String? = null,
        val euxCaseId: String? = null,
        val skipSEDkey: List<String>? = null,
        val subjectArea: String? = null,
        val institutions: List<InstitusjonItem>? = null
)

data class InstitusjonItem(
        val country: String? = null,
        val institution: String? = null
)

data class Personinformasjon(var fulltNavn: String? = null,
                             var fornavn: String? = null,
                             var mellomnavn: String? = null,
                             var etternavn: String? = null)

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class SedDokumentOpprettelseException(message: String): Exception(message)

class SedDokumentHentingException(message: String): Exception(message)

class SedDokumentSlettingException(message: String): Exception(message)

class SedDokumentSendingException(message: String): Exception(message)

class SedDokumentLeggeTilException(message: String): Exception(message)

class BucOpprettelseException(message: String): Exception(message)

class LandkodeException(message: String): Exception(message)

class PersonInformasjonException(message: String): Exception(message)
