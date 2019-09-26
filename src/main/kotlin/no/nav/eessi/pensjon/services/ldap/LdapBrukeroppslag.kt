package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory

import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.Attribute
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapContext
import java.util.Hashtable
import java.util.regex.Pattern

class LdapBrukeroppslag(
    private val environment: Hashtable<String, Any>,
    private val ldapInnlogging: LdapInnlogging,
    private var context: LdapContext?,
    private val searchBase: String?
) {

    private val logger = LoggerFactory.getLogger(LdapBrukeroppslag::class.java)
    private val UKJENT_NAVN = ""
    private val IDENT_PATTERN = Pattern.compile("^\\p{LD}+$")

    internal fun hentBrukerinformasjon(ident: String?): String? {
        if (ident == null || ident.isEmpty()) {
            logger.warn("Saksbehandler ident mangler")
            return UKJENT_NAVN
        }

        context = ldapInnlogging.lagLdapContext(environment)

        if (context == null || searchBase == null) {
            return UKJENT_NAVN
        }
        val result = ldapSearch(ident) ?: return UKJENT_NAVN
        return if (getDisplayName(result) == null) UKJENT_NAVN else getDisplayName(result)
    }

    private fun ldapSearch(ident: String): SearchResult? {
        val matcher = IDENT_PATTERN.matcher(ident)
        if (!matcher.matches()) {
            logger.warn("Navn på saksbehandler for ident $ident ikke funnet (1)")
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
            logger.warn("Navn på saksbehandler for ident $ident ikke funnet (2)")
            return null
        } catch (lee: LimitExceededException) {
            logger.warn("Navn på saksbehandler for ident $ident ikke funnet (LimitExceededException)")
            return null
        } catch (ne: NamingException) {
            logger.warn("Navn på saksbehandler for ident $ident ikke funnet (NamingException)")
            return null
        }

    }

    private fun getDisplayName(result: SearchResult): String? {
        val attributeName = "displayName"
        val displayName = find(result, attributeName) ?: return null
        return try {
            displayName.get().toString()
        } catch (e: NamingException) {
            logger.warn("Navn på saksbehandler ikke funnet (NamingException)")
            null
        }

    }

    private fun find(element: SearchResult, attributeName: String): Attribute? {
        val attribute = element.attributes.get(attributeName)
        if (attribute == null) {
            logger.warn("Navn på saksbehandler ikke funnet (attribute == null)")
            return null
        }
        return attribute
    }
}
