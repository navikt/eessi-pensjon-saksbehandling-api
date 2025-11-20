import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import no.nav.eessi.pensjon.ldap.BrukerInformasjon
import no.nav.eessi.pensjon.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.services.auth.AuthorisationService
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate

class AuthInterceptorTest {

    private val ldapService: BrukerInformasjonService = mockk()
    private val proxyOAuthRestTemplate: RestTemplate = mockk()
    private val authorisationService: AuthorisationService = mockk()
    private val tokenValidationContextHolder: TokenValidationContextHolder = mockk()
    private val authInterceptor =
        AuthInterceptor(ldapService, authorisationService, tokenValidationContextHolder)

    @Test
    @Disabled
    fun `hentBrukerinformasjon gir tomt svar fra GET`() {
        val navident = "testNavident"
        val uri = "/brukerinfo/$navident"
        val responseEntity: ResponseEntity<String> = mockk()

        every {
            proxyOAuthRestTemplate.exchange(uri, HttpMethod.GET, null, String::class.java)
        } returns responseEntity

        every { responseEntity.body } returns null

        val exception = assertThrows(IllegalStateException::class.java) {
//            authInterceptor.hentBrukerInformasjon(navident)
        }

        assertEquals("Mangler innhold for navident: $navident", exception.message.toString())

        verify(exactly = 1) {
            proxyOAuthRestTemplate.exchange(uri, HttpMethod.GET, null, String::class.java)
        }
    }

    @Test
    fun `brukerInformasjon kaster IllegalArgumentException ved tom ident`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            BrukerInformasjon(ident = "", medlemAv = listOf("gruppe1"))
        }
        assertEquals("Ident kan ikke være tom", exception.message.toString())
    }
}