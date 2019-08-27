package no.nav.eessi.pensjon.websocket

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.doNothing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.mockito.exceptions.base.MockitoException
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.socket.*


@ExtendWith(MockitoExtension::class)
class SocketTextHandlerTest {
    private val mapper = ObjectMapper()


    val socketTextHandler = SocketTextHandler()
    lateinit var mockWebSocketSession: WebSocketSession


    @BeforeEach
    fun setup() {
        mockWebSocketSession = mock(WebSocketSession::class.java)
    }

    @Test
    fun `handleTextMessage sets subscriptions in session attributes when it gets valid message`() {

        class MockJsonObject(val subscriptions: List<String>)
        val subscribers = listOf("FirstSubject", "SecondSubject")
        val mockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(mockJson)

        doReturn(mutableMapOf<String, Any>()).`when`(mockWebSocketSession).attributes
        doNothing().`when`(mockWebSocketSession).sendMessage(any(WebSocketMessage::class.java))

        socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage)
        assertEquals(subscribers, mockWebSocketSession.attributes["subscriptions"])
    }

    @Test
    fun `handleTextMessage does not set subscriptions in session attributes when it gets invalid parameter in message`() {

        class MockJsonObject(val notSubscriptions: List<String>)
        val subscribers = listOf("FirstSubject", "SecondSubject")
        val mockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(mockJson)

        doReturn(mutableMapOf<String, Any>()).`when`(mockWebSocketSession).attributes

        socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage)
        assertNull(mockWebSocketSession.attributes["subscriptions"])
    }

    @Test
    fun `handleTextMessage throws JsonParseException when it gets non-json message`() {
        val textMessage = TextMessage("}Not. valid, json:[")

        assertThrows<JsonParseException> { socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage) }
    }

    @Test
    fun `handleTextMessage throws Exception when it gets Exception`() {
        class MockJsonObject(val subscriptions: List<String>)
        val subscribers = listOf("FirstSubject", "SecondSubject")
        val mockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(mockJson)

        doReturn(mutableMapOf<String, Any>()).`when`(mockWebSocketSession).attributes
        doThrow(MockitoException::class.java).`when`(mockWebSocketSession).sendMessage(any(WebSocketMessage::class.java))

        assertThrows<Exception> { socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage) }
    }

    @Test
    fun `alertSubscribers alerts subscribed sessions on event`() {

        // Setup
        class MockJsonObject(val subscriptions: List<String>)

        val firstSessionsubscribers = listOf("FirstSubject", "SecondSubject")
        val secondSessionsubscribers = listOf("FirstSubject")
        val thirdSessionsubscribers = listOf("SecondSubject")

        val firstSessionMockJson = mapper.writeValueAsString(MockJsonObject(firstSessionsubscribers))
        val secondSessionMockJson = mapper.writeValueAsString(MockJsonObject(secondSessionsubscribers))
        val thirdSessionMockJson = mapper.writeValueAsString(MockJsonObject(thirdSessionsubscribers))

        val firstSessionTextMessage = TextMessage(firstSessionMockJson)
        val secondSessionTextMessage = TextMessage(secondSessionMockJson)
        val thirdSessionTextMessage = TextMessage(thirdSessionMockJson)

        socketTextHandler.fasitEnvironmentName = "p"

        val firstSession = mock(WebSocketSession::class.java)
        val secondSession = mock(WebSocketSession::class.java)
        val thirdSession = mock(WebSocketSession::class.java)

        doReturn(mutableMapOf<String, Any>()).`when`(firstSession).attributes
        doReturn("firstSessionId").`when`(firstSession).id
        doNothing().`when`(firstSession).sendMessage(any(WebSocketMessage::class.java))
        socketTextHandler.afterConnectionEstablished(firstSession)
        socketTextHandler.handleTextMessage(firstSession, firstSessionTextMessage)

        doReturn(mutableMapOf<String, Any>()).`when`(secondSession).attributes
        doReturn("secondSessionId").`when`(secondSession).id
        doNothing().`when`(secondSession).sendMessage(any(WebSocketMessage::class.java))
        socketTextHandler.afterConnectionEstablished(secondSession)
        socketTextHandler.handleTextMessage(secondSession, secondSessionTextMessage)

        doReturn(mutableMapOf<String, Any>()).`when`(thirdSession).attributes
        doReturn("thirdSessionId").`when`(thirdSession).id
        doNothing().`when`(thirdSession).sendMessage(any(WebSocketMessage::class.java))
        socketTextHandler.afterConnectionEstablished(thirdSession)
        socketTextHandler.handleTextMessage(thirdSession, thirdSessionTextMessage)

        // Test
        socketTextHandler.alertSubscribers("mockCaseNumber1", "FirstSubject")
        socketTextHandler.alertSubscribers("mockCaseNumber2", "SecondSubject")

        verify(firstSession).sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
        verify(firstSession).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber1\"}"))
        verify(firstSession).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber2\"}"))

        verify(secondSession).sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
        verify(secondSession).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber1\"}"))
        verify(secondSession, times(0)).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber2\"}"))

        verify(thirdSession).sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
        verify(thirdSession, times(0)).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber1\"}"))
        verify(thirdSession).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber2\"}"))
    }

    @Test
    fun `alertSubscribers does not alert disconnected sessions on event` () {

        // Setup
        class MockJsonObject(val subscriptions: List<String>)

        val subscribers = listOf("FirstSubject", "SecondSubject")

        val MockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(MockJson)

        socketTextHandler.fasitEnvironmentName = "p"

        val session = mock(WebSocketSession::class.java)

        doReturn(mutableMapOf<String, Any>()).`when`(session).attributes
        doReturn("sessionId").`when`(session).id
        doNothing().`when`(session).sendMessage(any(WebSocketMessage::class.java))
        socketTextHandler.afterConnectionEstablished(session)
        socketTextHandler.handleTextMessage(session, textMessage)
        socketTextHandler.afterConnectionClosed(session, CloseStatus.NORMAL)

        // Test
        socketTextHandler.alertSubscribers("mockCaseNumber1", "FirstSubject")
        socketTextHandler.alertSubscribers("mockCaseNumber2", "SecondSubject")

        verify(session).sendMessage(TextMessage("{ \"subscriptions\" : true }" ))
        verify(session, times(0)).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber1\"}"))
        verify(session, times(0)).sendMessage(TextMessage("{\"bucUpdated\": \"mockCaseNumber2\"}"))
    }

}