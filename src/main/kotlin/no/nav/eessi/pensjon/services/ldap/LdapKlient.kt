package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory

import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapContext
import java.util.Hashtable
import java.util.regex.Pattern

class LdapKlient(
    private val environment: Hashtable<String, Any>,
    private val ldapInnlogging: LdapInnlogging,
    private var context: LdapContext?,
    private val searchBase: String?
) {

    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)
    private val IDENT_PATTERN = Pattern.compile("^\\p{LD}+$")

    fun ldapSearch(ident: String): SearchResult? {
        context = ldapInnlogging.lagLdapContext(environment)

        if (context == null || searchBase == null) {
            logger.error("Context eller searchbase må settes")
            return null
        }

        val matcher = IDENT_PATTERN.matcher(ident)
        if (!matcher.matches()) {
            logger.error("Ident: $ident er ikke i et format vi kan søke på i LDAP")
            return null
        }

        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.countLimit = 1
        val soekestreng = String.format("(cn=%s)", ident)
        try {
            val result = context!!.search(searchBase, soekestreng, controls) // NOSONAR
            if (result.hasMoreElements()) {
                return result.nextElement()
            }
            logger.warn("Ident: $ident ikke funnet")
            return null
        } catch (lee: LimitExceededException) {
            logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", lee)
            return null
        } catch (ne: NamingException) {
            logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", ne)
            return null
        }

    }
}
