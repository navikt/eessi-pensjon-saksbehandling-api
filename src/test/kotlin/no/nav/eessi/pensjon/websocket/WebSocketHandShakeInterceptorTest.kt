//package no.nav.eessi.pensjon.websocket
//
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertFalse
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.mockito.Mockito.*
//import org.mockito.junit.jupiter.MockitoExtension
//import org.springframework.http.HttpHeaders
//import org.springframework.http.HttpMethod
//import org.springframework.http.server.ServerHttpResponse
//import org.springframework.http.server.ServletServerHttpRequest
//import org.springframework.web.socket.WebSocketHandler
//
//@ExtendWith(MockitoExtension::class)
//class WebSocketHandShakeInterceptorTest {
//
//    val oidcRequestContextHolder = generateMockContextHolder() // mock(OIDCRequestContextHolder::class.java)
//    val webSocketHandShakeInterceptor = WebSocketHandShakeInterceptor(oidcRequestContextHolder)
//
//    fun generateMockContextHolder() = mockContextHolder("jwtExample.json")
//
//    fun mockContextHolder(fileName: String, issuer: String = "testIssuer"): OIDCRequestContextHolder {
//
//        val issuer = issuer
//        val idToken = "MockSubject"
//        val oidcContextHolder = MockOIDCRequestContextHolder()
//        val oidcContext = OIDCValidationContext()
//        val tokenContext = TokenContext(issuer, idToken)
//        val claimSet = JWTClaimsSet
//            .parse(FileUtils.readFileToString(File("src/test/resources/json/$fileName"), Charset.forName("UTF-8")))
//        val jwt = PlainJWT(claimSet)
//
//        oidcContext.addValidatedToken(issuer, tokenContext, OIDCClaims(jwt))
//        oidcContextHolder.setOIDCValidationContext(oidcContext)
//        return oidcContextHolder
//    }
//
//
//    @Test
//    fun `beforeHandshake returns true when given correct request`() {
//
//        val request = mock(ServletServerHttpRequest::class.java)
//        val response = mock(ServerHttpResponse::class.java)
//        val wsHandler = mock(WebSocketHandler::class.java)
//        val attributes = mutableMapOf<String, Any>()
//
//        doReturn(HttpMethod.GET).`when`(request).method
//        doReturn(HttpHeaders()).`when`(request).headers
//
//        assert(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//    }
//
//    @Test
//    fun `beforeHandshake sets subject if successful`() {
//
//        val request = mock(ServletServerHttpRequest::class.java)
//        val response = mock(ServerHttpResponse::class.java)
//        val wsHandler = mock(WebSocketHandler::class.java)
//        val attributes = mutableMapOf<String, Any>()
//
//        doReturn(HttpMethod.GET).`when`(request).method
//        doReturn(HttpHeaders()).`when`(request).headers
//
//        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
//        assertEquals("12345678910", attributes["subject"])
//    }
//
//
//    @Test
//    fun `beforeHandshake sets response sub-protocol if present`() {
//
//        val request = mock(ServletServerHttpRequest::class.java)
//        val response = mock(ServerHttpResponse::class.java)
//        val wsHandler = mock(WebSocketHandler::class.java)
//        val attributes = mutableMapOf<String, Any>()
//
//        val requestHeaders = HttpHeaders()
//        requestHeaders["sec-websocket-protocol"] = "v0.buc"
//        val responseHeaders = HttpHeaders()
//
//        doReturn(HttpMethod.GET).`when`(request).method
//        doReturn(requestHeaders).`when`(request).headers
//        doReturn(responseHeaders).`when`(response).headers
//
//        webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes)
//        assertEquals(listOf("v0.buc"), responseHeaders["sec-websocket-protocol"])
//    }
//
//    @Test
//    fun `beforeHandshake fails if request method is not GET`() {
//
//        val request = mock(ServletServerHttpRequest::class.java)
//        val response = mock(ServerHttpResponse::class.java)
//        val wsHandler = mock(WebSocketHandler::class.java)
//        val attributes = mutableMapOf<String, Any>()
//
//        doReturn(HttpMethod.POST).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.DELETE).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.HEAD).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.OPTIONS).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.PATCH).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.PUT).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//
//        doReturn(HttpMethod.TRACE).`when`(request).method
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//    }
//
//    @Test
//    fun `beforeHandshake fails if OIDCValidationContext has no valid token`() {
//
//        val request = mock(ServletServerHttpRequest::class.java)
//        val response = mock(ServerHttpResponse::class.java)
//        val wsHandler = mock(WebSocketHandler::class.java)
//        val attributes = mutableMapOf<String, Any>()
//
//        assertFalse(webSocketHandShakeInterceptor.beforeHandshake(request, response, wsHandler, attributes))
//    }
//}