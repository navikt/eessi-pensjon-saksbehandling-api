package no.nav.eessi.pensjon.services


import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import io.mockk.spyk
import no.nav.eessi.pensjon.services.ldap.LdapKlient
import no.nav.eessi.pensjon.services.ldap.LdapService
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.util.*
import javax.naming.ldap.InitialLdapContext


@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
@DirtiesContext
@ExtendWith(SpringExtension::class)
open class BaseTest {

    @Value("\${EUXBASIS_V1.URL}")
   lateinit var euxUrl: String

    @Value("\${fagmodul.url}")
    lateinit var fagmodulUrl: String

    @Value("\${aktoerregister.api.v1.url}")
    lateinit var aktoerregisterUrl: String

    @Value("\${ldapServerPort}")
    lateinit var ldapServerPort: String

    @Test
    fun dummy() {
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun before() {
            val config = InMemoryDirectoryServerConfig("dc=test,dc=local")
            config.schema = null
            val server = InMemoryDirectoryServer(config)
            server.startListening()

            server.connection.use { connection ->
                System.setProperty("ldapServerPort", connection.connectedPort.toString())
            }

        }
    }

    fun generateMockFagmodulRestTemplate(): RestTemplate {

        val fagmodulRestTemplate = RestTemplateBuilder()
            .rootUri(fagmodulUrl)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors()
            .build()
        return spyk(fagmodulRestTemplate)
    }

    fun generateMockSaksbehandlerLdapService(): LdapService {
        val ldapContext = InitialLdapContext()
        val ldapBrukeroppslag = LdapKlient(
            Hashtable(),
            ldapContext,
            "OU=Users,OU=NAV,OU=BusinessUnits,"
        )

        return LdapService(ldapBrukeroppslag)
    }
}
