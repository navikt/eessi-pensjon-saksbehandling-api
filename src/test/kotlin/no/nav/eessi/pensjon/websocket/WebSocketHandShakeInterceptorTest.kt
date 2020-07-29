package no.nav.eessi.pensjon.websocket

import com.nhaarman.mockitokotlin2.whenever
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler

@ExtendWith(MockitoExtension::class)
class WebSocketHandShakeInterceptorTest {

    private lateinit var webSocketHandShakeInterceptor : WebSocketHandShakeInterceptor

    @BeforeEach
    fun setUp() {
        webSocketHandShakeInterceptor = Mockito.spy(WebSocketHandShakeInterceptor(SpringTokenValidationContextHolder()))
    }

    @Test
    fun `beforeHandshake returns true when given correct request`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(HttpHeaders()).`when`(request).headers
        doReturn(true).whenever(webSocketHandShakeInterceptor).hasValidToken()
        doReturn("12345678910").`when`(webSocketHandShakeInterceptor).getSubjectFromToken()

        assert(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }

    @Test
    fun `beforeHandshake sets subject if successful`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(HttpHeaders()).`when`(request).headers
        doReturn(true).whenever(webSocketHandShakeInterceptor).hasValidToken()
        doReturn("12345678910").`when`(webSocketHandShakeInterceptor).getSubjectFromToken()

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals("12345678910", attributes["subject"])
    }


    @Test
    fun `beforeHandshake sets response sub-protocol if present`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

        val requestHeaders = HttpHeaders()
        requestHeaders["sec-websocket-protocol"] = "v0.buc"
        val responseHeaders = HttpHeaders()

        doReturn(HttpMethod.GET).`when`(request).method
        doReturn(requestHeaders).`when`(request).headers
        doReturn(responseHeaders).`when`(response).headers
        doReturn(true).whenever(webSocketHandShakeInterceptor).hasValidToken()
        doReturn("12345678910").`when`(webSocketHandShakeInterceptor).getSubjectFromToken()

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals(listOf("v0.buc"), responseHeaders["sec-websocket-protocol"])
    }

    @Test
    fun `beforeHandshake fails if request method is not GET`() {

        val request = mock(ServletServerHttpRequest::class.java)
        val response = mock(ServerHttpResponse::class.java)
        val wsHandler = mock(WebSocketHandler::class.java)
        val attributes = mutableMapOf<String, Any>()

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

        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }
}