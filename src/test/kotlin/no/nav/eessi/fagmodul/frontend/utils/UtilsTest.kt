package no.nav.eessi.fagmodul.frontend.utils

import org.codehaus.jackson.map.ObjectMapper
import org.junit.Test
import java.lang.Exception
import kotlin.test.*


class UtilsTest {

    @Test
    fun `Gitt en feilmelding når kall til errorBody så generer gyldig json`() {
        val feilmelding = "En feilmelding"
        val body = errorBody(feilmelding)

        assertNotNull(body)
        assertNotEquals("", body)
        assertTrue(isValidJson(body))
    }

    @Test
    fun `Gitt en feilmelding med feilmeldingstekst når kall til errorBody så må body inneholde error og success false`() {
        val feilmelding = "En feilmelding"
        val body = errorBody(feilmelding)

        val mapper = ObjectMapper()
        try{
            val tree = mapper.readTree(body)
            assertEquals("En feilmelding" , tree.get("error").textValue)
            assertEquals(false , tree.get("success").booleanValue)
        } catch (ex: Exception) {
            fail("Klarte ikke å parse json")
        }
    }

    @Test
    fun `Gitt en feilmelding med tom feilmeldingstekst når kall til errorBody så må body inneholde error og success false`() {
        val feilmelding = ""
        val body = errorBody(feilmelding)

        val mapper = ObjectMapper()
        try{
            val tree = mapper.readTree(body)
            assertEquals("" , tree.get("error").textValue)
            assertEquals(false , tree.get("success").booleanValue)
        } catch (ex: Exception) {
            fail("Klarte ikke å parse json")
        }
    }

    @Test
    fun `Gitt et kall til successBody så generer gyldig json`() {
        val body = successBody()

        assertNotNull(body)
        assertNotEquals("", body)
        assertTrue(isValidJson(body))
    }

    @Test
    fun `Gitt et kall til successBody så må body inneholde success true`() {
        val body = successBody()

        val mapper = ObjectMapper()
        try{
            val tree = mapper.readTree(body)
            assertEquals(true , tree.get("success").booleanValue)
        } catch (ex: Exception) {
            fail("Klarte ikke å parse json")
        }
    }

    @Test
    fun `Gitt en gyldig path når maskerer så masker kun personIdentifikator`() {
        assertEquals("***********___PINFO", maskerPersonIdentifier("12345678910___PINFO"))
    }

    @Test
    fun `Gitt en gyldig paths når maskerer så masker kun personIdentifikator`() {
        assertEquals(
                "***********___PINFO,***********___PINFO",
                maskerPersonIdentifier(listOf("12345678910___PINFO", "10987654321___PINFO")
        ))
    }

    fun isValidJson(json: String): Boolean {
        val mapper = ObjectMapper()
        return try{
            mapper.readTree(json)
            true
        } catch (ex: Exception) {
            false
        }
    }
}