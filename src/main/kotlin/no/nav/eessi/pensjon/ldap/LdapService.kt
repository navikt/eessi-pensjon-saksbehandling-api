package no.nav.eessi.pensjon.ldap

import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.util.regex.Pattern
import javax.naming.directory.Attribute
import javax.naming.directory.SearchResult

@Profile("!integrationtest", "!test")
@Service
class LdapService(private val ldapKlient: LdapKlient,
                  @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper.ForTest()) :
    BrukerInformasjonService {

    // Pattern for NAV brukerident, f.eks Z123456
    private val IDENT_PATTERN = Pattern.compile("^[a-zA-Z][0-9]*")
    private val logger = LoggerFactory.getLogger(LdapService::class.java)

    private lateinit var hentBrukerInformasjon: MetricsHelper.Metric

    init {
        hentBrukerInformasjon = metricsHelper.init("hentBrukerInformasjon")
    }

    override fun hentBrukerInformasjon(ident: String): BrukerInformasjon {
        return hentBrukerInformasjon.measure {
            logger.info("Henter bruker-informasjon fra LDAP")
            if (ident.isEmpty()) {
                logger.warn("Brukerident mangler")
                throw IllegalArgumentException("Brukerident mangler")
            }

            // Unngår å søke etter fødselsnummer i AD
            val matcher = IDENT_PATTERN.matcher(ident)
            if (!matcher.matches()) {
                logger.error("Identen: $ident er ikke i et format vi kan søke etter")
                return@measure BrukerInformasjon(ident, emptyList())
            }

            val result = ldapKlient.ldapSearch(ident)
            if (result == null) {
                logger.warn("Fant ingen oppslag i AD for ident: $ident")
                return@measure BrukerInformasjon(ident, emptyList())
            }

            val medlemAv = getMemberOf(result, ident)
            return@measure BrukerInformasjon(ident, medlemAv)
        }
    }

    private fun getMemberOf(result: SearchResult, ident: String): List<String> {
        val attributeName = "memberOf"
        val memberOf = find(result, attributeName, ident) ?: return listOf()
        val groups = mutableListOf<String>()

        try {
            memberOf.all.iterator().forEach { groupEntry ->
                (groupEntry as String).split(",").forEach { attributePart ->
                    if (attributePart.startsWith("CN")) {
                        groups.add(attributePart.substringAfter("="))
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("En feil oppstod under henting av medlemskap for bruker: $ident i AD grupper", e)
            return emptyList()
        }
        logger.debug("Brukeren: $ident er medlem av grupper: $groups")
        return groups
    }

    private fun find(element: SearchResult, attributeName: String, ident: String): Attribute? {
        val attribute = element.attributes.get(attributeName)
        if (attribute == null) {
            logger.warn("Attributtet: $attributeName finnes ikke på brukeren: $ident i AD")
            return null
        }
        return attribute
    }
}