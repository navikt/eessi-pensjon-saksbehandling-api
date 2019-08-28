package no.nav.eessi.pensjon.listeners

import com.fasterxml.jackson.core.JsonParseException
import no.nav.eessi.pensjon.websocket.SocketTextHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.mockito.exceptions.base.MockitoException
import org.mockito.junit.jupiter.MockitoExtension
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
class SedListenerTest {
    val socketTextHandler = mock(SocketTextHandler::class.java)
    val sedListener = SedListener(socketTextHandler)

    @Test
    fun `consumeSedSendt calls SocketTextHandler-alertSubScribers on valid json`() {
        sedListener.consumeSedSendt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json"))))
        verify(socketTextHandler).alertSubscribers("147729", "12378945601")
    }

    @Test
    fun `consumeSedSendt throws JsonParseException on invalid json`() {
        assertThrows<JsonParseException> { sedListener.consumeSedSendt("]\\{)))notValidJson:::.") }
    }

    @Test
    fun `consumeSedSendt throws exception if it gets exception`() {
        doThrow(MockitoException("Boom!")).`when`(socketTextHandler).alertSubscribers(anyString(), anyString())
        assertThrows<Exception> { sedListener.consumeSedSendt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json")))) }
    }

    @Test
    fun `consumeSedMottatt calls SocketTextHandler-alertSubScribers on valid json`() {
        sedListener.consumeSedMottatt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json"))))
        verify(socketTextHandler).alertSubscribers("147729", "12378945601")
    }

    @Test
    fun `consumeSedMottatt throws JsonParseException on invalid json`() {
        assertThrows<JsonParseException> { sedListener.consumeSedMottatt("]\\{)))notValidJson:::.") }
    }

    @Test
    fun `consumeSedMottatt throws exception if it gets exception`() {
        doThrow(MockitoException("Boom!")).`when`(socketTextHandler).alertSubscribers(anyString(), anyString())
        assertThrows<Exception> { sedListener.consumeSedMottatt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json")))) }
    }
}