package no.nav.eessi.pensjon.services


import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import com.sun.jndi.ldap.LdapCtx
import com.unboundid.ldap.listener.InMemoryDirectoryServer
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig
import no.nav.eessi.pensjon.logging.AuditLogger
import no.nav.eessi.pensjon.services.ldap.LdapKlient
import no.nav.eessi.pensjon.services.ldap.LdapInnlogging
import no.nav.eessi.pensjon.services.ldap.LdapService
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.security.oidc.context.TokenContext
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.File
import java.nio.charset.Charset
import java.util.*


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

    lateinit var auditLogger: AuditLogger

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

    fun generateMockSaksbehContextHolder() = mockContextHolder("jwtSaksbehandlerEksempel.json", "isso")

    fun generateMockContextHolder() = mockContextHolder("jwtExample.json")

    fun mockContextHolder(fileName: String, issuer: String = "testIssuer"): OIDCRequestContextHolder {

        val issuer = issuer
        val idToken = "testIdToken"
        val oidcContextHolder = MockOIDCRequestContextHolder()
        val oidcContext = OIDCValidationContext()
        val tokenContext = TokenContext(issuer, idToken)
        val claimSet = JWTClaimsSet
            .parse(FileUtils.readFileToString(File("src/test/resources/json/$fileName"), Charset.forName("UTF-8")))
        val jwt = PlainJWT(claimSet)

        oidcContext.addValidatedToken(issuer, tokenContext, OIDCClaims(jwt))
        oidcContextHolder.setOIDCValidationContext(oidcContext)
        return oidcContextHolder
    }

    fun generateMockFagmodulRestTemplate(): RestTemplate {

        val fagmodulRestTemplate = RestTemplateBuilder()
            .rootUri(fagmodulUrl)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors()
            .build()
        return Mockito.spy(fagmodulRestTemplate)
    }


    fun generateMockAktoerregisterRestTemplate(): RestTemplate {

        val aktoerregisterRestTemplate = RestTemplateBuilder()
            .rootUri(aktoerregisterUrl)
            .errorHandler(DefaultResponseErrorHandler())
            .additionalInterceptors()
            .build()
        return Mockito.spy(aktoerregisterRestTemplate)
    }

    fun generateMockSaksbehandlerLdapService(): LdapService {
        val ldapContext = LdapCtx(
            "dc=test,dc=local",
            "localhost",
            ldapServerPort.toInt(),
            Hashtable<String, String>(),
            false
        )
        val ldapInnlogging = LdapInnlogging()
        val ldapBrukeroppslag = LdapKlient(
            Hashtable(),
            ldapInnlogging,
            ldapContext,
            "OU=Users,OU=NAV,OU=BusinessUnits,"
        )

        return LdapService(ldapBrukeroppslag)
    }
}

class MockOIDCRequestContextHolder : OIDCRequestContextHolder {

    private lateinit var oidcValidationContext: OIDCValidationContext

    override fun setOIDCValidationContext(oidcValidationContext: OIDCValidationContext?) {
        this.oidcValidationContext = oidcValidationContext!!
    }

    override fun getRequestAttribute(name: String?): Any {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setRequestAttribute(name: String?, value: Any?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOIDCValidationContext(): OIDCValidationContext {
        return this.oidcValidationContext
    }
}
