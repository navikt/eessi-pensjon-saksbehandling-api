package no.nav.eessi.pensjon.websocket

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.*

private val logger = LoggerFactory.getLogger(WebSocketConfigurer::class.java)

@Configuration
@EnableWebSocket
class WebsocketConfiguration(private val tokenValidationContextHolder: TokenValidationContextHolder) : WebSocketConfigurer {

    @Value("\${cors.allowed}")
    lateinit var allowed: String

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(SocketTextHandler(), "/bucUpdate").setAllowedOrigins(allowed).addInterceptors(
            WebSocketHandShakeInterceptor(
                tokenValidationContextHolder
            )
        )
        logger.info("Added websocket endpoint /bucUpdate, for ${allowed} access only")
    }
}
