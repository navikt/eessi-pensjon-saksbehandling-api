package no.nav.eessi.pensjon.config

import org.apache.catalina.connector.Connector
import org.apache.catalina.connector.Request
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class TomcatConfigTest {

    @Test
    fun `accepts twenty locales in Accept-Language`() {
        val request = requestWithLocales(20)

        assertEquals(20, request.locales.toList().size)
    }

    @Test
    fun `rejects more than twenty locales in Accept-Language`() {
        val request = requestWithLocales(21)

        assertThrows(IllegalArgumentException::class.java) {
            request.locale
        }
    }

    private fun requestWithLocales(count: Int): Request {
        val connector = Connector()
        TomcatConfig().localeCountCustomizer().customize(connector)
        val coyoteRequest = org.apache.coyote.Request()
        coyoteRequest.mimeHeaders.addValue("Accept-Language")
            .setString((1..count).joinToString(",") { "en-${it.toString().padStart(3, '0')}" })
        return Request(connector, coyoteRequest)
    }
}
