package no.nav.eessi.pensjon.services.fagmodul

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

class PersonInformasjonException(message: String): Exception(message)
