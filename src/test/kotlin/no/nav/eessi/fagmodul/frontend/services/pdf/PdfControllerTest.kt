package no.nav.eessi.fagmodul.frontend.services.pdf

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import no.nav.eessi.fagmodul.frontend.services.fagmodul.FagmodulBaseTest
import org.codehaus.jackson.map.ObjectMapper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito
import org.springframework.http.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException

class PensjonControllerTest: FagmodulBaseTest() {

    val SAK_ID = "12345678"
    val AKTOER_ID = "10000001231345"

    @After
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }


    @Test
    fun `hentPensjonSakType returns result from fagmodul when 200 OK`() {

        val responseFromFagmodul = "{\n" +
                "    \"sakId\": \"" + SAK_ID + "\",\n" +
                "    \"sakType\": \"ALDER\"\n" +
                "}"

        doReturn(
                ResponseEntity(responseFromFagmodul, HttpStatus.OK))
        .`when`(mockFagmodulRestTemplate).exchange(
                eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java))

        val generatedResponse = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body, responseFromFagmodul)

        assertTrue(generatedResponse.body!!.contains("ALDER"))
    }

    @Test
    fun `hentPensjonSakType returns 404 NOT FOUND when 404 from fagmodul`() {

        doThrow(HttpClientErrorException.
                create(HttpStatus.NOT_FOUND,
                        HttpStatus.NOT_FOUND.reasonPhrase,
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null))
                .`when`(mockFagmodulRestTemplate).exchange(
                eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java))

        val response = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }

    @Test
    fun `hentPensjonSakType returns 500 INTERNAL_SERVER_ERROR if other 4xx error from fagmodul`() {

        doThrow(HttpClientErrorException.
                create(HttpStatus.BAD_REQUEST,
                        HttpStatus.BAD_REQUEST.reasonPhrase,
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null))
                .`when`(mockFagmodulRestTemplate).exchange(
                eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java))

        val response = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
    }

    @Test
    fun `hentPensjonSakType returns 503 BAD_GATEWAY error if 5xx error from fagmodul`() {

        doThrow(HttpServerErrorException.
                create(HttpStatus.INTERNAL_SERVER_ERROR,
                        HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                        HttpHeaders.EMPTY,
                        ByteArray(0),
                        null))
                .`when`(mockFagmodulRestTemplate).exchange(
                        eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
                        any(HttpMethod::class.java),
                        any(HttpEntity::class.java),
                        eq(String::class.java))

        val response = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        assertEquals(HttpStatus.BAD_GATEWAY, response.statusCode)
    }

    @Test
    fun `hentPensjonSakType returns 500 Internal Server Error in case of other exception`() {

        val mockbody = "Feiler ved kontakt med PESYS"
        val mockException = RuntimeException(mockbody)
        val mockError = "ved henting av saktype fra PESYS, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                eq("/pensjon/saktype/$SAK_ID/$AKTOER_ID"),
                any(HttpMethod::class.java),
                any(HttpEntity::class.java),
                eq(String::class.java))

        val generatedResponse = pensjonController.hentPensjonSakType(SAK_ID, AKTOER_ID)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals(mockError, generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

    }


}