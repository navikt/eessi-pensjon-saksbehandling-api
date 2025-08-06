package no.nav.eessi.pensjon.services

import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import okio.use
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@Suppress("DEPRECATION")
@ActiveProfiles("test")
@DirtiesContext
@ExtendWith(SpringExtension::class)
open class BaseTest {

    @Value("\${aktoerregister.api.v1.url}")
    lateinit var aktoerregisterUrl: String

    @Value("\${ldapServerPort}")
    lateinit var ldapServerPort: String

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
