package no.nav.eessi.fagmodul.frontend.services.submit

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import java.lang.RuntimeException

class SubmitControllerTest : SubmitBaseTest() {

    @After fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
        Mockito.reset(kafkaService)
    }

    @Test fun `Calling receiveSubmissionController|receiveSubmission returns OK`() {
        val mockRequest = SubmissionRequest(
            periodeInfo = PeriodeInfo(),
            personInfo = Personinfo(),
            bankinfo = Bankinfo(),
            comment = "comment"
        )

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        val generatedResponse = receiveSubmissionController.receiveSubmission(mockRequest)
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

    @Test fun `Resending a failed submission|resendSubmission returns OK`() {
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___1998-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2008-09-01T00:00:00.json"
        )

        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

        doReturn(mockResponse).whenever(s3storageService).list(ArgumentMatchers.anyString())
        doReturn(latestSubmission).whenever(s3storageService).get(ArgumentMatchers.anyString())
        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        val generatedResponse = receiveSubmissionController.resendSubmission(inputFileName)
        Assert.assertEquals(HttpStatus.OK, generatedResponse.statusCode)
    }

    @Test fun `Calling receiveSubmissionController|resendSubmission returns Error because kafka Service fails`() {
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___1998-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2008-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

        doReturn(mockResponse).whenever(s3storageService).list(ArgumentMatchers.anyString())
        doReturn(latestSubmission).whenever(s3storageService).get(ArgumentMatchers.anyString())
        doThrow(RuntimeException("This did not work")).whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        val generatedResponse = receiveSubmissionController.resendSubmission(inputFileName)
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertTrue(generatedResponse.body!!.contains("Resend av submission feilet. "))
    }

    @Test fun `Calling receiveSubmissionController|getSubmission does sort and returns most recent item`() {
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___1998-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2008-09-01T00:00:00.json"
        )
        val expectedSelectedFile = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val expectedResponse = "something"
        val searchPattern = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___"

        doReturn(mockResponse).whenever(s3storageService).list(searchPattern)
        doReturn(expectedResponse).whenever(s3storageService).get(expectedSelectedFile)

        val generatedResponse = receiveSubmissionController.getSubmission(subject)
        Assert.assertEquals(expectedResponse, generatedResponse)
    }
}