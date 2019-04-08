package no.nav.eessi.fagmodul.frontend.services.fagmodul

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import org.codehaus.jackson.map.ObjectMapper
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals

class PensjonControllerTest: FagmodulBaseTest() {

    @After
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }


    @Test
    fun `Calling PensjonController | hentPensjonSakType returns mocked response`() {

        val sakId = "12345678"
        val aktoerId = "10000001231345"
        val expectedResponse = "{\n" +
                "    \"sakId\": \"21337890\",\n" +
                "    \"sakType\": \"ALDER\"\n" +
                "}"

        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/pensjon/saktype/$sakId/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        var generatedResponse = pensjonController.hentPensjonSakType(sakId, aktoerId)

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(generatedResponse.body, expectedResponse)

        Assert.assertTrue(generatedResponse.body!!.contains("ALDER"))
    }

    @Test
    fun `Calling PensjonController | hentPensjonSakType returns error`() {

        val sakId = "12345678"
        val aktoerId = "10000001231345"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/pensjon/saktype/$sakId/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val response = pensjonController.hentPensjonSakType(sakId, aktoerId)
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

    }

    @Test
    fun `Calling PensjonController | hentPensjonSakType returns Exception`() {
        val sakId = "12345678"
        val aktoerId = "10000001231345"

        val mockbody = "Feiler ved kontakt med PESYS"
        val mockException = RuntimeException(mockbody)
        val mockError = "ved henting av saktype fra PESYS, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/pensjon/saktype/$sakId/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = pensjonController.hentPensjonSakType(sakId, aktoerId)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

    }


}