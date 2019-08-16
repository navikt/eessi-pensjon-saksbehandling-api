package no.nav.eessi.pensjon.interceptor
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.lang.Nullable
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception

class WebSocketHandShakeInterceptor(private val oidcRequestContextHolder: OIDCRequestContextHolder): HandshakeInterceptor {

    private val logger = LoggerFactory.getLogger(HandshakeInterceptor::class.java)

    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, @Nullable exception: Exception?){}

    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>): Boolean {

        return if(request is ServletServerHttpRequest && request.method == HttpMethod.GET){
            val cookies = request.servletRequest.cookies
            logger.info("WEBSOCKET INTERCEPTOR ${getClaims(oidcRequestContextHolder).subject} VALID TOKEN >> ${oidcRequestContextHolder.oidcValidationContext.hasValidToken()}")
            cookies.forEach { logger.info("COOKIE: ${it.name} ${it.path} ${it.value} ${it.comment} ${it.domain} ${it.isHttpOnly} ${it.maxAge} ${it.secure} ${it.version}")}
            true
        } else {
            false
        }
    }
}