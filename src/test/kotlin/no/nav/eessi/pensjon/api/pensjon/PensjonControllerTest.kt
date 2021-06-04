package no.nav.eessi.pensjon.api.pensjon


import io.mockk.every
import no.nav.eessi.pensjon.services.fagmodul.FagmodulBaseTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException

open class PensjonControllerTest: FagmodulBaseTest() {

    val SAK_ID = "12345678"
    val AKTOER_ID = "10000001231345"

    @AfterEach
    fun cleanUpTest() {
    }

    @Test
    fun `hentPensjonSakType returns result from fagmodul when 200 OK`() {

        val responseFromFagmodul = "{\n" +
                "    \"sakId\": \"" + SAK_ID + "\",\n" +
                "    \"sakType\": \"ALDER\"\n" +
                "}"

        every { (mockFagmodulRestTemplate).exchange(
            eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
            any(),
            any(),
            eq(String::class.java)) } returns ResponseEntity(responseFromFagmodul, HttpStatus.OK)


        val generatedResponse = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body, responseFromFagmodul)

        assertTrue(generatedResponse.body!!.contains("ALDER"))
    }

    @Test
    fun `hentPensjonSakType returns 404 NOT FOUND when 404 from fagmodul`() {

        every { mockFagmodulRestTemplate.exchange(
            eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
            any(),
            any(),
            eq(String::class.java)) } throws HttpClientErrorException.
        create(HttpStatus.NOT_FOUND,
            HttpStatus.NOT_FOUND.reasonPhrase,
            HttpHeaders.EMPTY,
            ByteArray(0),
            null)

        val response = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `gitt 500 feil når kall til hentPensjonSakType så returner 500 feil ut av controlleren `() {

        every { mockFagmodulRestTemplate.exchange(
            eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
            any(),
            any(),
            eq(String::class.java)) } throws HttpClientErrorException.
        create(HttpStatus.BAD_REQUEST,
            HttpStatus.BAD_REQUEST.reasonPhrase,
            HttpHeaders.EMPTY,
            ByteArray(0),
            null)


        val response = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }
}