package no.nav.eessi.pensjon.websocket

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import java.io.IOException
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import java.util.concurrent.ConcurrentHashMap

@Component
class SocketTextHandler : TextWebSocketHandler() {

    @Value("\${FASIT_ENVIRONMENT_NAME}")
    lateinit var fasitEnvironmentName: String

    private val logger = LoggerFactory.getLogger(TextWebSocketHandler::class.java)
    private val mapper = ObjectMapper()

    companion object {
        private var sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()
    }

    @Throws(JsonParseException::class, InterruptedException::class, IOException::class)
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.info("$session sent message")
            val jsonRoot = mapper.readTree(message.payload)
            if (jsonRoot.isObject && jsonRoot.has("subscriptions") && jsonRoot["subscriptions"].isArray) {
                session.attributes["subscriptions"] = jsonRoot["subscriptions"].map { it.textValue() }
                session.sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
            }
        }catch(jsonParseException: JsonParseException) {
            logger.error("handleTextMessage JsonParseException", jsonParseException)
            throw jsonParseException
        }catch(interruptedException: InterruptedException){
            logger.error("handleTextMessage interruptedException", interruptedException)
            throw interruptedException
        }catch(iOException: IOException){
            logger.error("handleTextMessage iOException", iOException)
            throw iOException
        }catch(exception: Exception){
            logger.error("handleTextMessage exception", exception)
            throw exception
        }

    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        sessions.remove(session.id)
    }

    fun alertSubscribers(caseNumber: String, subject: String? = null){
        try {
            if(fasitEnvironmentName == "q2") { // TODO Remove after CT test
                sessions.forEach { (_, session) -> session.sendMessage(TextMessage("{\"bucUpdated\": \"$caseNumber\"}")) }
            } else if(subject != null){
                sessions
                    .filter { it.value.attributes["subscriptions"] != null }
                    .filter { it.value.attributes["subscriptions"] is List<*> }
                    .filter { (it.value.attributes["subscriptions"] as List<*>).contains(subject) }
                    .forEach { (_, session) -> session.sendMessage(TextMessage("{\"bucUpdated\": \"$caseNumber\"}")) }
            }
        }catch(exception: Exception){
            logger.error("alertSubscribers Exception", exception)
            throw exception
        }
    }
}