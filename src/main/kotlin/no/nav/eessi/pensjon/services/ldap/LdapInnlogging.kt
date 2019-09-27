package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import javax.naming.NamingException
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext
import java.util.Hashtable

class LdapInnlogging {

    private val logger = LoggerFactory.getLogger(LdapInnlogging::class.java)

    internal fun lagLdapContext(environment: Hashtable<String, Any>): LdapContext? {
        return try {
            InitialLdapContext(environment, null)
        } catch (e: NamingException) {
            logger.error("Klarte ikke å initiere LDAP context", e)
            null
        }
    }
}
