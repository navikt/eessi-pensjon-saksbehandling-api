package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.SearchResult

@Service
class LdapService(private val ldapKlient: LdapKlient) {

    private val logger = LoggerFactory.getLogger(LdapInnlogging::class.java)

    fun hentBrukerInformasjon(ident: String): BrukerInformasjon? {
        logger.info("Henter bruker informasjon fra LDAP")
        if (ident.isEmpty()) {
            logger.warn("Bruker ident mangler")
            return null
        }

        val result = ldapKlient.ldapSearch(ident)
        if(result == null) {
            logger.warn("Fant ingen oppslag i ldap for ident: $ident")
            return null
        }

        val navn = getDisplayName(result)
        val medlemAv = getMemberOf(result)
        return BrukerInformasjon(ident, navn, medlemAv)
    }

    private fun getDisplayName(result: SearchResult): String? {
        val attributeName = "displayName"
        val displayName = find(result, attributeName) ?: return null
        return try {
            displayName.get().toString()
        } catch (e: NamingException) {
            logger.error("En feil oppstod under henting av displayName feltet i LDAP", e)
            null
        }
    }

    private fun getMemberOf(result: SearchResult): String? {
        val attributeName = "memberOf"
        val memberOf = find(result, attributeName) ?: return null
        return try {
            memberOf.get().toString()
        } catch (e: NamingException) {
            logger.error("En feil oppstod under henting av memberOf feltet i LDAP", e)
            null
        }
    }

    private fun find(element: SearchResult, attributeName: String): Attribute? {
        val attribute = element.attributes.get(attributeName)
        if (attribute == null) {
            logger.warn("Atributtet: $attributeName finnes ikke på brukeren")
            return null
        }
        return attribute
    }
}