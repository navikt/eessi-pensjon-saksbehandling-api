package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException

import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapContext
import java.util.Hashtable

open class LdapKlient(
    private val environment: Hashtable<String, Any>,
    private val ldapInnlogging: LdapInnlogging,
    private var context: LdapContext?,
    private val searchBase: String?
) {

    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)

   open fun ldapSearch(ident: String): SearchResult? {
        context = ldapInnlogging.lagLdapContext(environment)

        if (context == null || searchBase == null) {
            logger.error("Context eller searchbase må angis")
            throw IllegalArgumentException("Context eller searchbase må angis")
        }

        val controls = SearchControls()
        controls.searchScope = SearchControls.SUBTREE_SCOPE
        controls.countLimit = 1
        val soekestreng = String.format("(cn=%s)", ident)
        try {
            val result = context!!.search(searchBase, soekestreng, controls)
            if (result.hasMoreElements()) {
                return result.nextElement()
            }
            logger.warn("Ident: $ident ikke funnet")
            return null
        } catch (lee: LimitExceededException) {
            logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", lee)
            throw lee
        } catch (ne: NamingException) {
            logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", ne)
            throw ne
        }
    }
}
