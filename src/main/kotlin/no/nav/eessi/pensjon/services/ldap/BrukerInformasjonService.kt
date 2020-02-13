package no.nav.eessi.pensjon.services.ldap

interface BrukerInformasjonService {

    fun hentBrukerInformasjon(ident: String): BrukerInformasjon

}