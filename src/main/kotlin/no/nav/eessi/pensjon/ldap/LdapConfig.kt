package no.nav.eessi.pensjon.ldap

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

@Configuration
class LdapConfig {

    private val logger = LoggerFactory.getLogger(LdapConfig::class.java)

    @Value("\${ldap.url}")
    private val ldapUrl: String? = null
    @Value("\${ldap.domain}")
    private val ldapDomain: String? = null
    @Value("\${ldap.basedn}")
    private val ldapBasedn: String? = null

    @Value("\${srvfagmodulusername}")
    private val ldapUsername: String? = null
    @Value("\${srvfagmodulpassword}")
    private val ldapPassword: String? = null

    @Bean
    fun ldapKlient(): LdapKlient {
        logger.info("Setter opp LDAP klient")
        val environment = Hashtable<String, Any>()
        environment[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        environment[Context.PROVIDER_URL] = ldapUrl!!
        environment[Context.SECURITY_AUTHENTICATION] = "simple"
        environment[Context.SECURITY_CREDENTIALS] = ldapPassword
        environment[Context.SECURITY_PRINCIPAL] = "$ldapUsername@$ldapDomain"

        val searchBase = "OU=Users,OU=NAV,OU=BusinessUnits," + ldapBasedn!!
        var context: LdapContext? = null
        try {
            context = InitialLdapContext()
        } catch (e: NamingException) {
        }

        return LdapKlient(environment, context, searchBase)
    }
}

