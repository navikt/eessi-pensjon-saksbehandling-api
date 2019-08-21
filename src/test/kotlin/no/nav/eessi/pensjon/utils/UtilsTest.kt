package no.nav.eessi.pensjon.utils

import org.codehaus.jackson.map.ObjectMapper
import org.jetbrains.annotations.TestOnly
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.lang.Exception


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

    @TestOnly
    fun isValidJson(json: String): Boolean {
        val mapper = ObjectMapper()
        return try{
            mapper.readTree(json)
            true
        } catch (ex: Exception) {
            false
        }
    }

    @Test
    fun `Test liste med SED kun PensjonSED skal returneres`() {
        val list  = listOf("X005","P2000","P4000","H02","X06","P9000", "")

        val result = filterPensionSedAndSort(list)

        assertEquals(3, result.size)
        assertEquals("[P2000, P4000, P9000]", result.toString())
    }

    @Test
    fun `Test av liste med SEDer der kun PensjonSEDer skal returneres`() {
        val list  = listOf("X005","P2000","P4000","H02","X06","P9000", "")

        val result = filterPensionSedAndSort(list)

        assertEquals(3, result.size)
        assertEquals("[ \"P2000\", \"P4000\", \"P9000\" ]", result.toJson())
    }


    @Test
    fun `Test listMapToJson`() {
        val list = listOf(mapOf("Name" to "Johnnyboy", "place" to "dummy"), mapOf("Name" to "Kjent dorull", "place" to "Q2"))

        val actualjson = "[ {\n" +
                "  \"Name\" : \"Johnnyboy\",\n" +
                "  \"place\" : \"dummy\"\n" +
                "}, {\n" +
                "  \"Name\" : \"Kjent dorull\",\n" +
                "  \"place\" : \"Q2\"\n" +
                "} ]"


        println(list.toJson())
        assertEquals(actualjson.replace("\n", "") , list.toJson().replace(System.lineSeparator(), ""))

    }
}
