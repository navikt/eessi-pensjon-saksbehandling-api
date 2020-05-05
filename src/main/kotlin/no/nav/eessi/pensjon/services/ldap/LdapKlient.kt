package no.nav.eessi.pensjon.services.ldap

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct
import javax.naming.Context
import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.InitialLdapContext
import javax.naming.ldap.LdapContext

@Component
@Profile("!integrationtest")
class LdapKlient(@Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    @Value("\${ldap.basedn}")
    private val ldapBasedn: String? = null

    @Value("\${ldap.url}")
    private val ldapUrl: String? = null

    @Value("\${ldap.domain}")
    private val ldapDomain: String? = null

    @Value("\${srvusername}")
    private val ldapUsername: String? = null

    @Value("\${srvpassword}")
    private val ldapPassword: String? = null


    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)

    private lateinit var ldapInnlogging: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        ldapInnlogging = metricsHelper.init("ldapInnlogging")
    }

    fun ldapSearch(ident: String): SearchResult? {
        val searchBase = "OU=Users,OU=NAV,OU=BusinessUnits,$ldapBasedn"

        return ldapInnlogging.measure {
            logger.info("ldapSearch: $ident")
            try {
                val ldapContext = ldapContext()
                val controls = SearchControls()
                controls.searchScope = SearchControls.SUBTREE_SCOPE
                controls.countLimit = 1
                val soekestreng = String.format("(cn=%s)", ident)
                val result = ldapContext.search(searchBase, soekestreng, controls)
                if (result.hasMoreElements()) {
                    result.nextElement()
                }
                logger.warn("Ident: $ident ikke funnet")
                null
            } catch (lee: LimitExceededException) {
                logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", lee)
                throw lee
            } catch (ne: NamingException) {
                logger.error("En teknisk feil oppstod ved søk etter: $ident i LDAP", ne)
                throw ne
            }
        }
    }

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
