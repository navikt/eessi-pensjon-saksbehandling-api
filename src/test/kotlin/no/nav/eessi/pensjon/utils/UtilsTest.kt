package no.nav.eessi.pensjon.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class UtilsTest {
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
