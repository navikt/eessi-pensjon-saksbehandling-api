package no.nav.eessi.pensjon.api.fagmodul

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.fagmodul.FagmodulBaseTest
import no.nav.eessi.pensjon.services.fagmodul.SedRequest
import no.nav.eessi.pensjon.utils.successBody
import org.codehaus.jackson.map.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SedControllerTest : FagmodulBaseTest() {

    @AfterEach
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }

    @Test
    fun `Calling fagmodulController|createDocument returns mocked response`() {

        //val mockRequest = SedRequest()
        val expectedResponse = "sampleOK"

        //doReturn(expectedResponse).whenever(fagmodulService).create(mockRequest)
        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)
        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/buc/create"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.createDocument(mockRequest)
        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|createDocument returns error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)
        val mockRequest = SedRequest()

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/buc/create"),
                any(),
                any(),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.createDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode) // TODO consider if the code is correct
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals("Opprettelse av BUC og SED på RINA feilet", generatedBody.get("error").textValue)
        assertEquals("no-uuid", generatedBody.get("uuid").textValue)
    }

    @Test
    fun `Calling fagmodulController|createDocument returns Exception`() {
        val mockRequest = SedRequest()

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError ="Opprettelse av BUC og SED på RINA feilet, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/buc/create"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.createDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals(mockError, generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns mocked response`() {
        val expectedResponse = "expectedResponse"
        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/preview"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.confirmDocument(mockRequest)
        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns error`() {

        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/preview"),
                any(),
                any(),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.confirmDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode) // TODO consider if the code is correct
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals("Forhåndsvisning og preutfylling av SED", generatedBody.get("error").textValue)
        assertEquals("no-uuid", generatedBody.get("uuid").textValue)
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns Exception`() {
        val mockRequest = SedRequest()

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError = "Forhåndsvisning og preutfylling SED, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/preview"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.confirmDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals(mockError, generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling fagmodulController|addDocument returns mocked response`() {

        val expectedResponse = "expectedResponse"
        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity.ok().body(expectedResponse)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/add"),
                any(),
                any(),
                any<ParameterizedTypeReference<*>>())

        val generatedResponse = sedController.addDocument(mockRequest)
        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|addDocument returns error`() {

        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/add"),
                any(),
                any(),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        val generatedResponse = sedController.addDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode) // TODO consider if the code is correct
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals("ved leggetil SED på BUC", generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling fagmodulController|addDocument returns exception`() {

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError = "ved leggetil SED på BUC, Melding: ${mockException.message}"

        val mockRequest = SedRequest()
        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/add"),
                any(),
                any(),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        val generatedResponse = sedController.addDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals(mockError, generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

    }


    @Test
    fun `Calling fagmodulController|getDocument returns OK`() {

        val expectedResponse = "expectedResponse"
        val euxcaseid = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/$euxcaseid/$documentid"),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.getDocument(euxcaseid, documentid)
        assertEquals(generatedResponse, mockResponse)
        assertEquals(expectedResponse, generatedResponse.body)
    }

    @Test
    fun `Calling fagmodulController|getDocument returns error`() {

        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/$euxCaseId/$documentid"),
                eq(HttpMethod.GET),
                any(),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.getDocument(euxCaseId, documentid)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode) // TODO consider if the code is correct
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals("", generatedBody.get("error").textValue)
        assertEquals("no-uuid", generatedBody.get("uuid").textValue)
    }

    @Test
    fun `Calling fagmodulController|deleteDocument returns OK`() {

        val expectedResponse = ResponseEntity.ok().body(successBody())
        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/$euxCaseId/$documentid"),
                eq(HttpMethod.DELETE),
                any<HttpEntity<*>>(),
                eq(Unit::class.java))

        val generatedResponse = sedController.deleteDocument(euxCaseId, documentid)
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|deleteDocument returns error`() {

        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/$euxCaseId/$documentid"),
                eq(HttpMethod.DELETE),
                any(),
                ArgumentMatchers.eq(Unit::class.java))

        val generatedResponse = sedController.deleteDocument(euxCaseId, documentid)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode) // TODO consider if the code is correct
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals("Sletting av SED fra Rina dokument feilet", generatedBody.get("error").textValue)
        assertEquals("no-uuid", generatedBody.get("uuid").textValue)
    }


    @Test
    fun `Calling fagmodulController|deleteDocument returns Exception`() {
        val euxCaseId = "123"
        val documentid = "123456"

        doThrow(RuntimeException("error")).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/$euxCaseId/$documentid"),
                eq(HttpMethod.DELETE),
                any(),
                eq(Unit::class.java))

        sedController.deleteDocument(euxCaseId, documentid)
    }

    @Test
    fun `Calling fagmodulController|sendsed returns OK`() {
        val expectedResponse = ResponseEntity.ok().body(successBody())
        val mockData = "something"
        val euxcaseid = "12231234"
        val documentid = "123123sdas"

        val mockResponse = ResponseEntity(mockData, HttpStatus.OK)

        ///buc/{euxcaseid}/sed/{documentid}/send
        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/send/$euxcaseid/$documentid"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse = sedController.sendSed(euxcaseid,documentid)
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController| sendsed returns 404 error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)
        val euxcaseid = "12231234"
        val documentid = "123123sdas"

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/sed/send/$euxcaseid/$documentid"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedresponse = sedController.sendSed(euxcaseid, documentid)
        assertEquals(HttpStatus.NOT_FOUND, generatedresponse.statusCode) // TODO consider if the code is correct
    }

    @Test
    fun `Calling fagmodulController|sendsed returns Exception`() {

        val euxcaseid = "12231234"
        val documentid = "123123sdas"

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError ="ved sending av SED fra RINA, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/send/$euxcaseid/$documentid"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.sendSed(euxcaseid, documentid)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue)
        assertEquals(mockError, generatedBody.get("error").textValue)
        assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }
}
