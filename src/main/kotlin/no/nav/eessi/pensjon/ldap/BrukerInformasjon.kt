package no.nav.eessi.pensjon.ldap


data class BrukerInformasjon(
        var ident: String = "",
        var medlemAv: List<String>
)