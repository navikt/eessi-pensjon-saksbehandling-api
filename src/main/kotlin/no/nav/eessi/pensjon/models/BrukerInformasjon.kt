package no.nav.eessi.pensjon.models


data class BrukerInformasjon(
        var ident: String = "",
        var medlemAv: List<String>
)