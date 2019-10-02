package no.nav.eessi.pensjon.services.ldap


data class BrukerInformasjon(
        var ident: String = "",
        var medlemAv: List<String>
)