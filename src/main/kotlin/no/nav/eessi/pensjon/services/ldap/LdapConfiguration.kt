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
class LdapConfiguration {

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

    private val logger = LoggerFactory.getLogger(LdapConfiguration::class.java)

    @Bean
    fun saksbehandlerConsumer(ldapBrukerOppslag: LdapKlient): LdapService {
        return LdapService(ldapBrukerOppslag)
    }

    @Bean
    fun ldapKlient(ldapInnlogging: LdapInnlogging): LdapKlient {
        logger.info("Setter opp LDAP klient")
        val environment = Hashtable<String, Any>()
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

        return LdapKlient(environment, ldapInnlogging, context, searchBase)
    }

    @Bean
    fun ldapInnlogging(): LdapInnlogging {
        return LdapInnlogging()
    }

    companion object {

        private val logger = LoggerFactory.getLogger(LdapConfiguration::class.java)
    }
}

