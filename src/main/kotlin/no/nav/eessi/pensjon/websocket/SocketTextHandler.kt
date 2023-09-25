package no.nav.eessi.pensjon.websocket

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
class SocketTextHandler : TextWebSocketHandler() {

    @Value("\${ENV}")
    lateinit var fasitEnvironmentName: String

    private val logger = LoggerFactory.getLogger(SocketTextHandler::class.java)
    private val mapper = ObjectMapper()

    companion object {
        private var sessions: ConcurrentHashMap<String, WebSocketSession> = ConcurrentHashMap()
    }

    fun handleSubscription (session: WebSocketSession, message: JsonNode) {
        if (message.isArray) {
            session.attributes["subscriptions"] = message.map { it.textValue() }
            logger.info("Accepting request of session " + session + " to subscribe  " + message.map { it.textValue() })
            session.sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
        }
    }

    @Throws(InterruptedException::class, IOException::class)
    public override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        try {
            logger.info("Received a message from session $session")
            val jsonRoot = mapper.readTree(message.payload)
            if (jsonRoot.isObject && jsonRoot.has("subscriptions")) {
                handleSubscription(session, jsonRoot["subscriptions"])
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

    fun filterSessionsByBruker (navBruker: Fodselsnummer): MutableList<WebSocketSession> {
        logger.debug("Filtering " + sessions.size + "sessions for navBruker " + navBruker)
        return sessions
            .filter { it.value.attributes["subscriptions"] != null }
            .filter { it.value.attributes["subscriptions"] is List<*> }
            .filter { (it.value.attributes["subscriptions"] as List<*>).contains(navBruker.value) }
            .values.toMutableList()
    }

    @Throws(Exception::class)
    override fun afterConnectionEstablished(session: WebSocketSession) {
        sessions[session.id] = session
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        sessions.remove(session.id)
    }

    fun alertSubscribers(caseId: String, subject: Fodselsnummer? = null) {
        try {
            if(subject != null){
                val subscribers = filterSessionsByBruker(subject)
                logger.info("Alerting " + subscribers.size + " subscribers that caseId " + caseId)
                subscribers.map { session ->
                    session.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"$caseId\"}}"))
                }
            }
        } catch(exception: Exception) {
            logger.error("alertSubscribers Exception", exception)
            throw exception
        }
    }
}
