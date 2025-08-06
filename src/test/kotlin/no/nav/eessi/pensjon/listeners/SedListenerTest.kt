package no.nav.eessi.pensjon.listeners

import io.mockk.junit5.MockKExtension
import no.nav.eessi.pensjon.utils.JsonException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockKExtension::class)
class SedListenerTest {

    val sedListener = SedListener()

    @Test
    fun `consumeSedSendt throws JsonParseException on invalid json`() {
        assertThrows<JsonException> { sedListener.consumeSedSendt("]\\{)))notValidJson:::.") }
    }
    @Test
    fun `consumeSedMottatt throws JsonParseException on invalid json`() {
        assertThrows<JsonException> { sedListener.consumeSedMottatt("]\\{)))notValidJson:::.") }
    }

}