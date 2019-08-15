package no.nav.eessi.pensjon.config

import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import java.io.IOException
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


@Component

class SocketTextHandler : TextWebSocketHandler() {

    private val logger = LoggerFactory.getLogger(TextWebSocketHandler::class.java)

    companion object {
        private var sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()
    }

    @Throws(InterruptedException::class, IOException::class)
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        logger.info("$session sent message")
        sessions.forEach { (id: String, session: WebSocketSession) ->  session.sendMessage(TextMessage("$id echo >> ${message.payload} "))}
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        sessions.remove(session.id)
    }
}