package no.nav.eessi.pensjon.services.ldap


data class BrukerInformasjon(
        var ident: String = "",
        var navn: String? = null,
        var medlemAv: String? = null
)