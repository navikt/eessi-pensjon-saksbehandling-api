package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SaksbehandlerLdapService(private val ldapBrukeroppslag: LdapBrukeroppslag) {

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