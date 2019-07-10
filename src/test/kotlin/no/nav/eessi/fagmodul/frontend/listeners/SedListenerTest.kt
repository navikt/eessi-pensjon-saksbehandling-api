package no.nav.eessi.fagmodul.frontend.listeners

import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.messaging.simp.SimpMessagingTemplate

@RunWith(MockitoJUnitRunner.Silent::class)
class SedListenerTest {

    private lateinit var sedListener: SedListener

    @Mock
    lateinit var brokerMessagingTemplate: SimpMessagingTemplate

    @Before
    fun setup() {
        sedListener = SedListener(brokerMessagingTemplate)
    }

    @After
    fun takedown() {
        Mockito.reset(brokerMessagingTemplate)
    }

    @Test
    fun `consumeSedSendt sender en websocket melding `() {
        val mockMessage = "mockMessage"
        sedListener.consumeSedSendt(mockMessage)
        verify(brokerMessagingTemplate, times(1)).convertAndSend(
            "/sed",
            mapAnyToJson(mapOf("action" to "Sent", "payload" to mockMessage)))
    }

    @Test
    fun `consumeSedMottatt sender en websocket melding `() {
            val mockMessage = "mockMessage"
            sedListener.consumeSedMottatt(mockMessage)
            verify(brokerMessagingTemplate, times(1)).convertAndSend(
                        "/sed",
                        mapAnyToJson(mapOf("action" to "Received", "payload" to mockMessage)))
        }
}
