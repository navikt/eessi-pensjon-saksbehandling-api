package no.nav.eessi.pensjon.websocket
import no.nav.eessi.pensjon.utils.getClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.lang.Nullable
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeFailureException
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception

class WebSocketHandShakeInterceptor(private val oidcRequestContextHolder: OIDCRequestContextHolder): HandshakeInterceptor {

    private val logger = LoggerFactory.getLogger(HandshakeInterceptor::class.java)

    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, @Nullable exception: Exception?){}

    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>): Boolean {

        return try {
            if (request is ServletServerHttpRequest && request.method == HttpMethod.GET && oidcRequestContextHolder.oidcValidationContext.hasValidToken()) {
                logger.info("WebSocketHandShakeInterceptor >> ${getClaims(oidcRequestContextHolder).subject} VALID TOKEN")
                attributes["subject"] = getClaims(oidcRequestContextHolder).subject

                // For å støtte IE11 er vi nødt til å ha en "sec-websocket-protocol" header.
                // Verdien av denne er nødt til å være det samme som vi mottar i requesten.
                if(request.headers["sec-websocket-protocol"] != null){
                    response.headers["sec-websocket-protocol"] = request.headers["sec-websocket-protocol"]
                }
                true
            } else {
                logger.info("WebSocketHandShakeInterceptor handshake failed", request)
                false
            }
        } catch(exception: HandshakeFailureException){
            logger.error("HANDSHAKE FAILURE EXCEPTION", request, exception)
            false
        }
    }
}