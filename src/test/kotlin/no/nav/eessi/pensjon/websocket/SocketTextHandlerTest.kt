package no.nav.eessi.pensjon.websocket

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession


@ExtendWith(MockKExtension::class)
class SocketTextHandlerTest {
    private val mapper = ObjectMapper()

    val socketTextHandler = SocketTextHandler()

    @MockK(relaxed = true)
    lateinit var mockWebSocketSession: WebSocketSession

    @Test
    fun `handleTextMessage sets subscriptions in session attributes when it gets valid message`() {

        class MockJsonObject(val subscriptions: List<String>)
        val subscribers = listOf("FirstSubject", "SecondSubject")
        val mockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(mockJson)

        every { mockWebSocketSession.attributes } returns mutableMapOf<String, Any>()
        justRun { mockWebSocketSession.sendMessage(any()) }

        socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage)
        assertEquals(subscribers, mockWebSocketSession.attributes["subscriptions"])
    }

    @Test
    fun `handleTextMessage does not set subscriptions in session attributes when it gets invalid parameter in message`() {

        class MockJsonObject(val notSubscriptions: List<String>)
        val subscribers = listOf("FirstSubject", "SecondSubject")
        val mockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(mockJson)

        every { mockWebSocketSession.attributes } returns mutableMapOf<String, Any>()

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

        every { mockWebSocketSession.attributes } returns mutableMapOf<String, Any>()
        every { mockWebSocketSession.sendMessage(any()) } throws RuntimeException("")

        assertThrows<Exception> { socketTextHandler.handleTextMessage(mockWebSocketSession, textMessage) }
    }

    @Test
    fun `alertSubscribers alerts subscribed sessions on event`() {

        // Setup
        class MockJsonObject(val subscriptions: List<String>)

        val firstSessionsubscribers = listOf("01516634630", "02526634630")
        val secondSessionsubscribers = listOf("01516634630")
        val thirdSessionsubscribers = listOf("02526634630")

        val firstSessionMockJson = mapper.writeValueAsString(MockJsonObject(firstSessionsubscribers))
        val secondSessionMockJson = mapper.writeValueAsString(MockJsonObject(secondSessionsubscribers))
        val thirdSessionMockJson = mapper.writeValueAsString(MockJsonObject(thirdSessionsubscribers))

        val firstSessionTextMessage = TextMessage(firstSessionMockJson)
        val secondSessionTextMessage = TextMessage(secondSessionMockJson)
        val thirdSessionTextMessage = TextMessage(thirdSessionMockJson)

        socketTextHandler.fasitEnvironmentName = "p"

        val firstSession = mockk<WebSocketSession>()
        val secondSession = mockk<WebSocketSession>()
        val thirdSession = mockk<WebSocketSession>()

        every { firstSession.attributes } returns mutableMapOf<String, Any>()
        every { firstSession.id } returns "firstSessionId"
        justRun { firstSession.sendMessage(any()) }

        socketTextHandler.afterConnectionEstablished(firstSession)
        socketTextHandler.handleTextMessage(firstSession, firstSessionTextMessage)

        every { secondSession.attributes } returns mutableMapOf<String, Any>()
        every { secondSession.id } returns "secondSessionId"
        justRun { secondSession.sendMessage(any()) }


        socketTextHandler.afterConnectionEstablished(secondSession)
        socketTextHandler.handleTextMessage(secondSession, secondSessionTextMessage)

        every { thirdSession.attributes } returns mutableMapOf<String, Any>()
        every { thirdSession.id } returns "thirdSessionId"
        justRun { thirdSession.sendMessage(any()) }

        socketTextHandler.afterConnectionEstablished(thirdSession)
        socketTextHandler.handleTextMessage(thirdSession, thirdSessionTextMessage)

        // Test
        socketTextHandler.alertSubscribers("mockCaseNumber1", Fodselsnummer.fra("01516634630"))
        socketTextHandler.alertSubscribers("mockCaseNumber2", Fodselsnummer.fra("02526634630"))

        verify() { firstSession.sendMessage(TextMessage("{ \"subscriptions\" : true }")) }
        verify() { firstSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber1\"}}")) }
//        verify() { firstSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber2\"}}")) }
//
//        verify() { secondSession.sendMessage(TextMessage("{ \"subscriptions\" : true }")) }
//        verify() { secondSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber1\"}}")) }
//        verify(exactly = 0) { secondSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber2\"}}")) }
//
//        verify() { thirdSession.sendMessage(TextMessage("{ \"subscriptions\" : true }")) }
//        verify(exactly = 0) { thirdSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber1\"}}")) }
//        verify() { thirdSession.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber2\"}}")) }
//


    }

    @Test
    fun `alertSubscribers does not alert disconnected sessions on event` () {

        // Setup
        class MockJsonObject(val subscriptions: List<String>)

        val subscribers = listOf("FirstSubject", "SecondSubject")

        val MockJson = mapper.writeValueAsString(MockJsonObject(subscribers))

        val textMessage = TextMessage(MockJson)

        socketTextHandler.fasitEnvironmentName = "p"

        val session = mockk<WebSocketSession>()

        every { session.attributes } returns mutableMapOf<String, Any>()
        every { session.id } returns "sessionId"
        justRun { session.sendMessage(any()) }

        socketTextHandler.afterConnectionEstablished(session)
        socketTextHandler.handleTextMessage(session, textMessage)
        socketTextHandler.afterConnectionClosed(session, CloseStatus.NORMAL)

        // Test
        socketTextHandler.alertSubscribers("mockCaseNumber1", Fodselsnummer.fra("01011234856"))
        socketTextHandler.alertSubscribers("mockCaseNumber2", Fodselsnummer.fra("02021236845"))

        verify { session.sendMessage(TextMessage("{ \"subscriptions\" : true }")) }
        verify(exactly = 0) { session.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber1\"}}")) }
        verify(exactly = 0) { session.sendMessage(TextMessage("{\"bucUpdated\": {\"caseId\": \"mockCaseNumber2\"}}")) }

    }

}