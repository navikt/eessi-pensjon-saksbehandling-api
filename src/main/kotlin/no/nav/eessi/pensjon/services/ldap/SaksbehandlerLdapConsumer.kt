package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory

class SaksbehandlerLdapConsumer(private val ldapBrukeroppslag: LdapBrukeroppslag) {

    private val logger = LoggerFactory.getLogger(LdapInnlogging::class.java)

    fun hentSaksbehandler(saksbehandlerIdent: String): SaksbehandlerDto {
        logger.info("Henter saksbehandler informasjon fra LDAP")
        val saksbehandlerDto = SaksbehandlerDto()
        val saksbehandlerNavn = ldapBrukeroppslag.hentBrukerinformasjon(saksbehandlerIdent)
        saksbehandlerDto.ident = saksbehandlerIdent
        saksbehandlerDto.navn = saksbehandlerNavn
        return saksbehandlerDto
    }
}