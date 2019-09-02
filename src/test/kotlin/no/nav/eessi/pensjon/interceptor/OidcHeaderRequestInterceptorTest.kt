package no.nav.eessi.pensjon.interceptor

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import no.nav.security.oidc.context.TokenContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpResponse

@ExtendWith(MockitoExtension::class)
class OidcHeaderRequestInterceptorTest {

    val oidcRequestContextHolder = mock(OIDCRequestContextHolder::class.java)
    val oidcHeaderRequestInterceptor = OidcHeaderRequestInterceptor(oidcRequestContextHolder)

    @Test
    fun `intercept adds token to header`() {
        val request = mock(HttpRequest::class.java)
        val body = byteArrayOf()
        val execution = mock(ClientHttpRequestExecution::class.java)

        val oidcValidationContext = mock(OIDCValidationContext::class.java)
        val tokenContext = mock(TokenContext::class.java)

        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext
        doReturn(listOf("MockIssuer")).`when`(oidcValidationContext).getIssuers()
        doReturn(tokenContext).`when`(oidcValidationContext).getToken(anyString())
        doReturn("MockIdToken").`when`(tokenContext).idToken

        doReturn(HttpHeaders()).`when`(request).headers
        doReturn(MockClientHttpResponse(body, HttpStatus.OK)).`when`(execution).execute(
            any(),
            any()
        )

        oidcHeaderRequestInterceptor.intercept(request, body, execution)

        assert(request.headers[HttpHeaders.AUTHORIZATION]!!.contains("Bearer MockIdToken"))

    }
}