package no.nav.eessi.pensjon.config

import no.nav.eessi.pensjon.interceptor.WebSocketHandShakeInterceptor
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.*

private val logger = LoggerFactory.getLogger(WebSocketConfigurer::class.java)

@Configuration
@EnableWebSocket
class WebsocketConfiguration(private val oidcRequestContextHolder: OIDCRequestContextHolder) : WebSocketConfigurer {

    @Value("\${cors.allowed}")
    lateinit var allowed: String

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(SocketTextHandler(), "/bucUpdate").setAllowedOrigins("*").addInterceptors(WebSocketHandShakeInterceptor(oidcRequestContextHolder))
        logger.info("Added websocket endpoint /bucUpdate, for ${allowed} access only")
    }
}
