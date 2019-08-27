package no.nav.eessi.pensjon.websocket

import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.OIDCValidationContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler

@ExtendWith(MockitoExtension::class)
class WebSocketHandShakeInterceptorTest {

    val oidcRequestContextHolder = mock(OIDCRequestContextHolder::class.java)
    val webSocketHandShakeInterceptor = WebSocketHandShakeInterceptor(oidcRequestContextHolder)

    @Test
    fun `beforeHandshake returns true when given correct request`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val oidcValidationContext = mock(OIDCValidationContext::class.java)
        val oidcClaims = mock(OIDCClaims::class.java)

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(HttpHeaders()).`when`(request).headers

        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext
        doReturn(true).`when`(oidcValidationContext).hasValidToken()
        doReturn(listOf("MockIssuer")).`when`(oidcValidationContext).issuers
        doReturn(oidcClaims).`when`(oidcValidationContext).getClaims(anyString())
        doReturn("MockSubject").`when`(oidcClaims).subject

        assert(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }

    @Test
    fun `beforeHandshake sets subject if successful`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val oidcValidationContext = mock(OIDCValidationContext::class.java)
        val oidcClaims = mock(OIDCClaims::class.java)

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(HttpHeaders()).`when`(request).headers

        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext
        doReturn(true).`when`(oidcValidationContext).hasValidToken()
        doReturn(listOf("MockIssuer")).`when`(oidcValidationContext).issuers
        doReturn(oidcClaims).`when`(oidcValidationContext).getClaims(anyString())
        doReturn("MockSubject").`when`(oidcClaims).subject

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals("MockSubject", attributes["subject"])
    }


    @Test
    fun `beforeHandshake sets response sub-protocol if present`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val oidcValidationContext = mock(OIDCValidationContext::class.java)
        val oidcClaims = mock(OIDCClaims::class.java)

        val requestHeaders = HttpHeaders()
        requestHeaders["sec-websocket-protocol"] = "v0.buc"
        val responseHeaders = HttpHeaders()

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(requestHeaders).`when`(request).headers
        doReturn(responseHeaders).`when`(response).headers

        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext
        doReturn(true).`when`(oidcValidationContext).hasValidToken()
        doReturn(listOf("MockIssuer")).`when`(oidcValidationContext).issuers
        doReturn(oidcClaims).`when`(oidcValidationContext).getClaims(anyString())
        doReturn("MockSubject").`when`(oidcClaims).subject

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals(listOf("v0.buc"), responseHeaders["sec-websocket-protocol"])
    }

    @Test
    fun `beforeHandshake fails if request method is not GET`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val oidcValidationContext = mock(OIDCValidationContext::class.java)

        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext

        doReturn(HttpMethod.POST).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.DELETE).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.HEAD).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.OPTIONS).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.PATCH).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.PUT).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        doReturn(HttpMethod.TRACE).`when`(request).method
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }

    @Test
    fun `beforeHandshake fails if OIDCValidationContext has no valid token`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val oidcValidationContext = mock(OIDCValidationContext::class.java)

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(oidcValidationContext).`when`(oidcRequestContextHolder).oidcValidationContext
        doReturn(false).`when`(oidcValidationContext).hasValidToken()

        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }
}