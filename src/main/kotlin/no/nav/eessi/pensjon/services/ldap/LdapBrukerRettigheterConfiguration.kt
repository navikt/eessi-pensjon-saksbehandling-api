package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.naming.Context
import javax.naming.NamingException
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext
import java.util.Hashtable

@Configuration
class LdapBrukerRettigheterConfiguration {

    @Value("\${ldap.url}")
    private val ldapUrl: String? = null
    @Value("\${ldap.username}")
    private val ldapUsername: String? = null
    @Value("\${ldap.domain}")
    private val ldapDomain: String? = null
    @Value("\${ldap.password}")
    private val ldapPassword: String? = null
    @Value("\${ldap.basedn}")
    private val ldapBasedn: String? = null

    private val logger = LoggerFactory.getLogger(LdapBrukerRettigheterConfiguration::class.java)

    @Bean
    fun saksbehandlerConsumer(ldapBrukerOppslag: LdapBrukeroppslag): SaksbehandlerLdapConsumer {
        return SaksbehandlerLdapConsumer(ldapBrukerOppslag)
    }

    @Bean
    fun ldapBrukeroppslag(ldapInnlogging: LdapInnlogging): LdapBrukeroppslag {
        logger.info("Setter opp LDAP config for brukeroppslag")
        val environment = Hashtable<String, Any>() // NOSONAR //metodeparameter krever Hashtable
        environment[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
        environment[Context.PROVIDER_URL] = ldapUrl!!
        environment[Context.SECURITY_AUTHENTICATION] = "simple"
        environment[Context.SECURITY_CREDENTIALS] = ldapPassword!!
        environment[Context.SECURITY_PRINCIPAL] = "$ldapUsername@$ldapDomain"

        val searchBase = "OU=Users,OU=NAV,OU=BusinessUnits," + ldapBasedn!!
        var context: LdapContext? = null
        try {
            context = InitialLdapContext()
        } catch (e: NamingException) {
        }

        return LdapBrukeroppslag(environment, ldapInnlogging, context, searchBase)
    }

    @Bean
    fun ldapInnlogging(): LdapInnlogging {
        return LdapInnlogging()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(LdapBrukerRettigheterConfiguration::class.java)
    }
}

