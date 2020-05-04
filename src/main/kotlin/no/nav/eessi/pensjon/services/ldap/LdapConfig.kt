package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.*
import javax.naming.Context
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

@Configuration
@Profile("!integrationtest")
class LdapConfig {
        private val logger = LoggerFactory.getLogger(LdapConfig::class.java)

        @Value("\${ldap.url}")
        private val ldapUrl: String? = null

        @Value("\${ldap.domain}")
        private val ldapDomain: String? = null

        @Value("\${srvusername}")
        private val ldapUsername: String? = null

        @Value("\${srvpassword}")
        private val ldapPassword: String? = null

        @Bean
        fun ldapContext(): LdapContext {
            logger.info("Setter opp LDAP klient")
            val environment = Hashtable<String, Any>()
            environment[Context.INITIAL_CONTEXT_FACTORY] = "com.sun.jndi.ldap.LdapCtxFactory"
            environment[Context.PROVIDER_URL] = ldapUrl!!
            environment[Context.SECURITY_AUTHENTICATION] = "simple"
            environment[Context.SECURITY_CREDENTIALS] = ldapPassword
            environment[Context.SECURITY_PRINCIPAL] = "$ldapUsername@$ldapDomain"

            return InitialLdapContext(environment, null)
        }
}

