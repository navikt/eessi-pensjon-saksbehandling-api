package no.nav.eessi.fagmodul.frontend.services.submit

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SubmitControllerTest : SubmitBaseTest() {

    @After
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
        Mockito.reset(kafkaService)
    }

    @Test
    fun `Calling receiveSubmissionController|receiveSubmission returns OK`() {

        val mockRequest = SubmissionRequest(
                periodeInfo = PeriodeInfo(),
                personInfo = Personinfo(),
                bankinfo = Bankinfo(),
                comment = "comment"
        )

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(
                ArgumentMatchers.anyString()
        )

        val generatedResponse = submitController.receiveSubmission(mockRequest)
        val filename = generatedResponse.get("filename")!!

        Assert.assertTrue(filename.startsWith("12345678910___PinfoSubmission___"))
        Assert.assertTrue(filename.endsWith(".json"))

        val content = storageController.getDocument(filename)

        val json = mapper.readTree(content.body!!)
        Assert.assertTrue(json.has("periodeInfo"))
        Assert.assertTrue(json.has("personInfo"))
        Assert.assertTrue(json.has("bankinfo"))
        Assert.assertTrue(json.has("comment"))
    }

    @Test
    fun `Calling receiveSubmissionController|sendReceipt returns OK`() {

        val subject = "12345678910"
        val mockResponse = DefaultResourceLoader().getResource("classpath:json/submissions.json").file.readText()

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(
                ArgumentMatchers.anyString()
        )

        doReturn(mockResponse).whenever(submitController).getSubmission(subject)

        val generatedResponse = submitController.sendReceipt()

        val responseJson =  mapper.readTree(generatedResponse)
        Assert.assertEquals("kvittering.pdf", responseJson.get("name").textValue())
        Assert.assertEquals("application/pdf", responseJson.get("mimetype").textValue())
        Assert.assertTrue(responseJson.get("size").intValue() > 1000)
        Assert.assertTrue(responseJson.has("content"))
    }

    @Test
   fun `Calling receiveSubmissionController|getSubmission does sort and returns most recent item`() {

        val subject = "12345678910"

        val mockResponse = listOf(
                "12345678910___PinfoSubmission___2018-09-15T15:53:00.json",
                "12345678910___PinfoSubmission___2028-09-15T15:53:00.json",
                "12345678910___PinfoSubmission___1998-09-15T15:53:00.json",
                "12345678910___PinfoSubmission___2008-09-15T15:53:00.json"
        )
        val expectedSelectedFile = "12345678910___PinfoSubmission___2028-09-15T15:53:00.json"
        val expectedResponse = "something"
        val searchPattern = "${subject}___${submitController.PINFO_SUBMISSION}___"

        doReturn(mockResponse).whenever(s3storageService).list(searchPattern)
        doReturn(expectedResponse).whenever(s3storageService).get(expectedSelectedFile)

        val generatedResponse = submitController.getSubmission(subject)
        Assert.assertEquals(expectedResponse, generatedResponse)
    }
}