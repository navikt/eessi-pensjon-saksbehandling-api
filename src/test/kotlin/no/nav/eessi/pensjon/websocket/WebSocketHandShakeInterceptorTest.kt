package no.nav.eessi.pensjon.websocket

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.spyk
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler

@ExtendWith(MockKExtension::class)
class WebSocketHandShakeInterceptorTest {

    private lateinit var webSocketHandShakeInterceptor : WebSocketHandShakeInterceptor

    @BeforeEach
    fun setUp() {
        webSocketHandShakeInterceptor = spyk(WebSocketHandShakeInterceptor(SpringTokenValidationContextHolder()))
    }

    @Test
    fun `beforeHandshake returns true when given correct request`() {

        val request = mockk<ServletServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val wsHandler = mockk<WebSocketHandler>()
        val attributes = mutableMapOf<String, Any>()

        every { request.method } returns HttpMethod.GET
        every { request.headers } returns HttpHeaders()
        every { webSocketHandShakeInterceptor.hasValidToken() } returns true
        every { webSocketHandShakeInterceptor.getSubjectFromToken() } returns "12345678910"

        assert(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }

    @Test
    fun `beforeHandshake sets subject if successful`() {

        val request = mockk<ServletServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val wsHandler = mockk<WebSocketHandler>()
        val attributes = mutableMapOf<String, Any>()

        every { request.method } returns HttpMethod.GET
        every { request.headers } returns HttpHeaders()
        every { webSocketHandShakeInterceptor.hasValidToken() } returns true
        every { webSocketHandShakeInterceptor.getSubjectFromToken() } returns "12345678910"

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals("12345678910", attributes["subject"])
    }

    @Test
    fun `beforeHandshake sets response sub-protocol if present`() {

        val request = mockk<ServletServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val wsHandler = mockk<WebSocketHandler>()
        val attributes = mutableMapOf<String, Any>()

        val requestHeaders = HttpHeaders()
        requestHeaders["sec-websocket-protocol"] = "v0.buc"
        val responseHeaders = HttpHeaders()

        every { request.method } returns HttpMethod.GET
        every { request.headers } returns requestHeaders
        every { response.headers} returns responseHeaders
        every { webSocketHandShakeInterceptor.hasValidToken() } returns true
        every { webSocketHandShakeInterceptor.getSubjectFromToken() } returns "12345678910"

        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
        assertEquals(listOf("v0.buc"), responseHeaders["sec-websocket-protocol"])
    }

    @Test
    fun `beforeHandshake fails if request method is not GET`() {

        val request = mockk<ServletServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val wsHandler = mockk<WebSocketHandler>()
        val attributes = mutableMapOf<String, Any>()

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.DELETE
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))

        every { request.method } returns HttpMethod.POST
        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }

    @Test
    fun `beforeHandshake fails if OIDCValidationContext has no valid token`() {

        val request = mockk<ServletServerHttpRequest>()
        val response = mockk<ServerHttpResponse>()
        val wsHandler = mockk<WebSocketHandler>()
        val attributes = mutableMapOf<String, Any>()

        every { request.method } returns null

        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
    }
}