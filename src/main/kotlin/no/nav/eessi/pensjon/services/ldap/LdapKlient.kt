package no.nav.eessi.pensjon.services.ldap

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.lang.IllegalArgumentException

import javax.naming.LimitExceededException
import javax.naming.NamingException
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import javax.naming.ldap.LdapContext
import java.util.Hashtable
import javax.naming.ldap.InitialLdapContext

open class LdapKlient(
    private val environment: Hashtable<String, Any>,
    private var context: LdapContext?,
    private val searchBase: String?,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(LdapKlient::class.java)

    open fun ldapSearch(ident: String): SearchResult? {
        return metricsHelper.measure("ldapInnlogging") {
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
