package no.nav.eessi.pensjon.api.fagmodul

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.fagmodul.FagmodulBaseTest
import no.nav.eessi.pensjon.utils.errorBody
import org.codehaus.jackson.map.ObjectMapper
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.junit.Assert.assertEquals

class BucControllerTest : FagmodulBaseTest()  {

    @Test
    fun `Calling BucController| getProcessDefinitionName returns mocked response`() {
        val mockRinaId = "13245"
        val mockbody = "Mockresponse"
        val mockResponse = ResponseEntity(mockbody , HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/name"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getProcessDefinitionName(mockRinaId)
        Assert.assertEquals(mockResponse, generatedResponse)
        Assert.assertEquals(mockbody, generatedResponse.body)

    }

    @Test
    fun `Calling BucController| getProcessDefinitionName returns mocked UNAUTHORIZED error`() {
        val mockRinaId = "13245"
        val mockbody = errorBody("Uthenting av Buc navn eller type feiler! ")
        val mockResponse = ResponseEntity("error error" , HttpStatus.UNAUTHORIZED)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/name"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getProcessDefinitionName(mockRinaId)
        Assert.assertEquals(true, generatedResponse.statusCode.isError)
        Assert.assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode)
        Assert.assertEquals(mockbody, generatedResponse.body)

    }

    @Test
    fun `Calling BucController| getProcessDefinitionName returns 404 error`() {
        val mockRinaId = "98765"
        val mockbody = errorBody("Uthenting av Buc navn eller type feiler! ")
        val mockResponse = ResponseEntity("Ja dette trynte så det sang..." , HttpStatus.NOT_ACCEPTABLE)


        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/name"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getProcessDefinitionName(mockRinaId)
        Assert.assertEquals(true, generatedResponse.statusCode.isError)
        Assert.assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode)
        Assert.assertEquals(mockbody, generatedResponse.body)
    }


    fun `Calling BucController| getProcessDefinitionName returns mocked Exception`() {
        val mockRinaId = "98765"
        val mockbody = "Mockresponsebody for Exception av getBuc"
        val mockException = RuntimeException(mockbody)
        val mockresponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feil ved henting av Buc, Melding: ${mockException.message}"))

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/name"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val response = bucController.getProcessDefinitionName(mockRinaId)
        assertEquals(mockresponse, response)
        assertEquals(mockresponse.statusCode, response.statusCode)
        assertEquals(mockresponse.body, response.body)
    }


    @Test
    fun `Calling BucController| getBuc returns mocked response`() {
        val mockRinaId = "13245"
        val mockbody = "Mocket buc body response"
        val mockResponse = ResponseEntity(mockbody , HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getBuc(mockRinaId)
        Assert.assertEquals(mockResponse, generatedResponse)
        Assert.assertEquals(mockbody, generatedResponse.body)
    }

    @Test
    fun `Calling BucController| getBuc returns 404 error`() {
        val mockRinaId = "98765"
        val mockbody = errorBody("Feil ved henting av Buc")
        val mockResponse = ResponseEntity("Dette tryner!! DOH!..." , HttpStatus.UNAUTHORIZED)


        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getBuc(mockRinaId)
        Assert.assertEquals(true, generatedResponse.statusCode.isError)
        Assert.assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode)
        Assert.assertEquals(mockbody, generatedResponse.body)
    }


    @Test
    fun `Calling BucController| getBuc returns mocked Exception`() {
        val mockRinaId = "98765"
        val mockbody = "Mockresponsebody for Exception av getBuc"
        val mockException = RuntimeException(mockbody)
        val mockError = "Feil ved henting av Buc, Melding: ${mockException.message}"

        doThrow(mockException).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getBuc(mockRinaId)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.NOT_FOUND, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling BucController| allDocuments returns mocked response`() {
        val mockRinaId = "345345345"
        val mockbody = "Mocket buc body response"
        val mockResponse = ResponseEntity(mockbody , HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/allDocuments"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getAllDocuments(mockRinaId)
        Assert.assertEquals(mockResponse, generatedResponse)
        Assert.assertEquals(mockbody, generatedResponse.body)

    }

    @Test
    fun `Calling BucController| MuligeAksjonerFilter returns mocked response`() {
        val mockRinaId = "345345345"
        val mockfilter = "P"
        val mockbody = "Mocket buc body response"
        val mockResponse = ResponseEntity(mockbody , HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/buc/$mockRinaId/aksjoner/$mockfilter"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.eq(null),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = bucController.getMuligeAksjonerFilter(mockRinaId, mockfilter)
        Assert.assertEquals(mockResponse, generatedResponse)
        Assert.assertEquals(mockbody, generatedResponse.body)

    }

        @Test
        fun `Calling BucController| MuligeAksjoner returns mocked response`() {
            val mockRinaId = "345345345"
            val mockbody = "Mocket buc body response"
            val mockResponse = ResponseEntity(mockbody , HttpStatus.OK)

            doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                    ArgumentMatchers.eq("/buc/$mockRinaId/aksjoner"),
                    ArgumentMatchers.any(HttpMethod::class.java),
                    ArgumentMatchers.eq(null),
                    ArgumentMatchers.eq(String::class.java))

            val generatedResponse = bucController.getMuligeAksjoner(mockRinaId)
            Assert.assertEquals(mockResponse, generatedResponse)
            Assert.assertEquals(mockbody, generatedResponse.body)

        }


}
