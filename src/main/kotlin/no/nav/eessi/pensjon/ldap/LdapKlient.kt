package no.nav.eessi.pensjon.ldap

import org.slf4j.LoggerFactory
import java.util.*
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

open class LdapKlient(
    private val environment: Hashtable<String, Any>,
    private var context: LdapContext?,
    private val searchBase: String?) {

    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)

    open fun ldapSearch(ident: String): SearchResult? {
        logger.info("ldapSearch: $ident")
        try {
            context = InitialLdapContext(environment, null)

            if (context == null || searchBase == null) {
                logger.error("Context eller searchbase må angis")
                throw IllegalArgumentException("Context eller searchbase må angis")
            }

            val controls = SearchControls()
            controls.searchScope = SearchControls.SUBTREE_SCOPE
            controls.countLimit = 1
            val soekestreng = String.format("(cn=%s)", ident)
            val result = context!!.search(searchBase, soekestreng, controls)
            if (result.hasMoreElements()) {
                return result.nextElement()
            }
            logger.warn("Ident: $ident ikke funnet")
            return null
        }  catch (ex: Exception) {
            logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", ex)
            throw ex
        }
    }
}
