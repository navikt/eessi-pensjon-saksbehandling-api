package no.nav.eessi.pensjon.services.ldap

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapContext

@Component
@Profile("!integrationtest")
class LdapKlient(
    private var ldapContext: LdapContext,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    @Value("\${ldap.basedn}")
    private val ldapBasedn: String? = null

    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)

    fun ldapSearch(ident: String): SearchResult? {
        val searchBase = "OU=Users,OU=NAV,OU=BusinessUnits,$ldapBasedn"

        return metricsHelper.measure("ldapInnlogging") {
            logger.info("ldapSearch: $ident")
            try {
                val controls = SearchControls()
                controls.searchScope = SearchControls.SUBTREE_SCOPE
                controls.countLimit = 1
                val soekestreng = String.format("(cn=%s)", ident)
                val result = ldapContext!!.search(searchBase, soekestreng, controls)
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
}
