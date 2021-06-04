package no.nav.eessi.pensjon.listeners

import com.fasterxml.jackson.core.JsonParseException
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.spyk
import io.mockk.verify
import no.nav.eessi.pensjon.websocket.SocketTextHandler
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
class SedListenerTest {

    val socketTextHandler = spyk<SocketTextHandler>()
    val sedListener = SedListener(socketTextHandler)

    @Test
    fun `consumeSedSendt calls SocketTextHandler-alertSubScribers on valid json`() {
        sedListener.consumeSedSendt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json"))))
        verify { socketTextHandler.alertSubscribers("147729", "12378945601") }
    }

    @Test
    fun `consumeSedSendt throws JsonParseException on invalid json`() {
        assertThrows<JsonParseException> { sedListener.consumeSedSendt("]\\{)))notValidJson:::.") }
    }

    @Test
    fun `consumeSedSendt throws exception if it gets exception`() {
        every { socketTextHandler.alertSubscribers(any(), any()) } throws Exception("Boom!")
        assertThrows<Exception> { sedListener.consumeSedSendt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json")))) }
    }

    @Test
    fun `consumeSedMottatt calls SocketTextHandler-alertSubScribers on valid json`() {
        sedListener.consumeSedMottatt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json"))))
        verify { socketTextHandler.alertSubscribers("147729", "12378945601") }
    }

    @Test
    fun `consumeSedMottatt throws JsonParseException on invalid json`() {
        assertThrows<JsonParseException> { sedListener.consumeSedMottatt("]\\{)))notValidJson:::.") }
    }

    @Test
    fun `consumeSedMottatt throws exception if it gets exception`() {
        every { socketTextHandler.alertSubscribers(any(), any()) }  throws Exception("Boom!")
        assertThrows<Exception> { sedListener.consumeSedMottatt(String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json")))) }
    }
}