package no.nav.eessi.fagmodul.frontend.services.fagmodul

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import no.nav.eessi.fagmodul.frontend.utils.successBody
import org.codehaus.jackson.map.ObjectMapper
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SedControllerTest : FagmodulBaseTest() {

    @After
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
                ArgumentMatchers.eq("/sed/buc/create"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.createDocument(mockRequest)
        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|createDocument returns error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)
        val mockRequest = SedRequest()

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/buc/create"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        try {
            sedController.createDocument(mockRequest)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(BucOpprettelseException::class.java))
            Assert.assertEquals(e.message, "Feil ved opprettelse av BUC (eux basis), Ingen EUXcaseid mottatt.")
        }
    }

    @Test
    fun `Calling fagmodulController|createDocument returns Exception`() {
        val mockRequest = SedRequest()

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError ="Opprettelse av BUC og SED på RINA feilet, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/buc/create"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.createDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns mocked response`() {
        val expectedResponse = "expectedResponse"
        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/preview"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.confirmDocument(mockRequest)
        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns error`() {

        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/preview"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        try {
            sedController.confirmDocument(mockRequest)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(SedDokumentOpprettelseException::class.java))
            Assert.assertEquals(e.message, "Ingen RINANR mottatt. feil ved opprett ny SED")
        }
    }

    @Test
    fun `Calling fagmodulController|confirmDocument returns Exception`() {
        val mockRequest = SedRequest()

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError = "Forhåndsvisning og preutfylling SED, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/preview"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.confirmDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling fagmodulController|addDocument returns mocked response`() {

        val expectedResponse = "expectedResponse"
        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity.ok().body(expectedResponse)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/add"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        val generatedResponse = sedController.addDocument(mockRequest)
        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(generatedResponse.body, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|addDocument returns error`() {

        val mockRequest = SedRequest()
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/add"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        try {
            sedController.addDocument(mockRequest)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(SedDokumentLeggeTilException::class.java))
            Assert.assertEquals(e.message, "Ingen EUXcaseid mottatt. feil ved leggetil av SED (eux basis)")
        }
    }

    @Test
    fun `Calling fagmodulController|addDocument returns exception`() {

        val mockbody = "Mockresponsebody for Exception av sendsed"
        val mockException = RuntimeException(mockbody)
        val mockError = "ved leggetil SED på BUC, Melding: ${mockException.message}"

        val mockRequest = SedRequest()
        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/add"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        val generatedResponse = sedController.addDocument(mockRequest)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

    }


    @Test
    fun `Calling fagmodulController|getDocument returns OK`() {

        val expectedResponse = "expectedResponse"
        val euxcaseid = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/$euxcaseid/$documentid"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.getDocument(euxcaseid, documentid)
        Assert.assertEquals(generatedResponse, mockResponse)
        Assert.assertEquals(expectedResponse, generatedResponse.body)
    }

    @Test
    fun `Calling fagmodulController|getDocument returns error`() {

        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/$euxCaseId/$documentid"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        try {
            sedController.getDocument(euxCaseId, documentid)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(SedDokumentHentingException::class.java))
            Assert.assertEquals(e.message, "Feil ved henting av SED")
        }
    }

    @Test
    fun `Calling fagmodulController|deleteDocument returns OK`() {

        val expectedResponse = ResponseEntity.ok().body(successBody())
        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/$euxCaseId/$documentid"),
                ArgumentMatchers.eq(HttpMethod.DELETE),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Unit::class.java))

        val generatedResponse = sedController.deleteDocument(euxCaseId, documentid)
        Assert.assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController|deleteDocument returns error`() {

        val euxCaseId = "123"
        val documentid = "123456"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/$euxCaseId/$documentid"),
                ArgumentMatchers.eq(HttpMethod.DELETE),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Unit::class.java))

        try {
            sedController.deleteDocument(euxCaseId, documentid)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(SedDokumentSlettingException::class.java))
            Assert.assertEquals(e.message, "Feil under sletting av SED")
        }
    }


    @Test
    fun `Calling fagmodulController|deleteDocument returns Exception`() {
        val euxCaseId = "123"
        val documentid = "123456"

        doThrow(RuntimeException("error")).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/$euxCaseId/$documentid"),
                ArgumentMatchers.eq(HttpMethod.DELETE),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Unit::class.java))

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
                ArgumentMatchers.eq("/sed/send/$euxcaseid/$documentid"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = sedController.sendSed(euxcaseid,documentid)
        Assert.assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling fagmodulController| sendsed returns 404 error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)
        val euxcaseid = "12231234"
        val documentid = "123123sdas"

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/sed/send/$euxcaseid/$documentid"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedresponse = sedController.sendSed(euxcaseid, documentid)
        Assert.assertEquals(HttpStatus.NOT_FOUND, generatedresponse.statusCode)
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

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }
}
