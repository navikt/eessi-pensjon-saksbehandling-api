package no.nav.eessi.pensjon.services


import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import no.nav.security.token.support.test.spring.TokenGeneratorConfiguration
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@ActiveProfiles("test")
@Import(TokenGeneratorConfiguration::class)
@DirtiesContext
@ExtendWith(SpringExtension::class)
open class BaseTest {

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

}
