package no.nav.eessi.fagmodul.frontend.services


import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.security.oidc.context.TokenContext
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.apache.commons.io.FileUtils
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate
import java.io.File
import java.nio.charset.Charset

@ActiveProfiles("test")
@RunWith(SpringRunner::class)
@SpringBootTest
@Import(TokenGeneratorConfiguration::class)
class BaseTest {

    @Value("\${fagmodul.url:http://localhost:8081}")
    lateinit var fagmodulUrl: String

    @Value("\${aktoerregister.api.v1.url}")
    lateinit var aktoerregisterUrl: String

    @Test fun dummy() {}

    fun generateMockContextHolder(): OIDCRequestContextHolder {

        val issuer = "testIssuer"
        val idToken = "testIdToken"
        val oidcContextHolder = MockOIDCRequestContextHolder()
        val oidcContext = OIDCValidationContext()
        val tokenContext = TokenContext(issuer, idToken)
        val claimSet = JWTClaimsSet
                .parse(FileUtils.readFileToString(File("src/test/resources/json/jwtExample.json"), Charset.forName("UTF-8")))
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
}

class MockOIDCRequestContextHolder : OIDCRequestContextHolder {

    private lateinit var oidcValidationContext : OIDCValidationContext

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