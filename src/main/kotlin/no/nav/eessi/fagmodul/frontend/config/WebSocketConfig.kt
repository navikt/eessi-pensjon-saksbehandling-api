package no.nav.eessi.fagmodul.frontend.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

private val logger = LoggerFactory.getLogger(WebSocketMessageBrokerConfigurer::class.java)

@Configuration
@EnableWebSocketMessageBroker

class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    @Value("\${cors.allowed}")
    lateinit var allowed: String

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/websocket").setAllowedOrigins(allowed).withSockJS()
        logger.info("Added STOMP endpoint /websocket, for ${allowed} access only")
    }
}