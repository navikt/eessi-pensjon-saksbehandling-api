package no.nav.eessi.pensjon.ldap

interface BrukerInformasjonService {

    fun hentBrukerInformasjon(ident: String): BrukerInformasjon

}