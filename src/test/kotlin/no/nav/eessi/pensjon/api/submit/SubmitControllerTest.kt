package no.nav.eessi.pensjon.api.submit

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class SubmitControllerTest : SubmitBaseTest() {

    @AfterEach fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
        Mockito.reset(kafkaService)
        Mockito.reset(s3storageService)
    }

    @Test fun `Gitt en skjema innsending når en feil oppstår under s3 lagring så returner Internal server error`() {
        val mockRequest = SubmissionRequest(
                periodeInfo = PeriodeInfo(),
                personInfo = Personinfo(),
                bankInfo = Bankinfo(),
                comment = "comment"
        )

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(any())
        doThrow(RuntimeException("Feiler her ved s3")).whenever(s3storageService).put(any(),any())

        val resp = receiveSubmissionController.receiveSubmission("p4000", mockRequest)
        assertEquals(resp.statusCode, HttpStatus.INTERNAL_SERVER_ERROR)
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

        doReturn(mockResponse).whenever(s3storageService).list(any())
        doReturn(latestSubmission).whenever(s3storageService).get(any())
        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(any())
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

        doReturn(mockResponse).whenever(s3storageService).list(any())
        doReturn(latestSubmission).whenever(s3storageService).get(any())
        doThrow(RuntimeException("This did not work")).whenever(kafkaService).publishSubmissionReceivedEvent(any())
    }

    @Test fun `receiveSubmissionController|putOnKafka failed after maxtries kafka Service fails`() {
        val uuid = "1234-1234-1234"
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(any())

        doReturn(mockResponse).whenever(s3storageService).list(any())
        doReturn(latestSubmission).whenever(s3storageService).get(any())

        doThrow(RuntimeException("This did not work")).whenever(kafkaService).publishSubmissionReceivedEvent(any())
        assertThrows<Exception> {
            receiveSubmissionController.putOnKafka(inputFileName, uuid)
        }
    }

    @Test fun `receiveSubmissionController|putOnKafka successful after 2 kafka Service fails`() {
        val uuid = "1234-1234-1234"
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

        doThrow(RuntimeException("This did not work"))
            .doThrow(RuntimeException("This did not work"))
            .doNothing()
            .whenever(kafkaService).publishSubmissionReceivedEvent(any())

        doReturn(mockResponse).whenever(s3storageService).list(any())
        doReturn(latestSubmission).whenever(s3storageService).get(any())

        val response = receiveSubmissionController.putOnKafka(inputFileName, uuid)

        assertEquals(uuid, response)
    }

    @Test fun `receiveSubmissionController|putOnKafka successful`() {
        val uuid = "1234-1234-1234"
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

            doNothing()
            .whenever(kafkaService).publishSubmissionReceivedEvent(any())

        doReturn(mockResponse).whenever(s3storageService).list(any())
        doReturn(latestSubmission).whenever(s3storageService).get(any())

        val response = receiveSubmissionController.putOnKafka(inputFileName, uuid)

        assertEquals(uuid, response)
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
        assertEquals(expectedResponse, generatedResponse)
    }

    @Test fun `Calling receiveSubmissionController|getSubmissionJson gets getSubmission in JSON`() {
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___1998-09-01T00:00:00.json",
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2008-09-01T00:00:00.json"
        )
        val expectedSelectedFile = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val mockContent = "something"
        val expectedResponse =  ResponseEntity.ok().body(mapAnyToJson(mapOf("content" to mockContent)))

        val searchPattern = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___"

        doReturn(mockResponse).whenever(s3storageService).list(searchPattern)
        doReturn(mockContent).whenever(s3storageService).get(expectedSelectedFile)

        val generatedResponse = receiveSubmissionController.getSubmissionAsJson(subject)
        assertEquals(expectedResponse, generatedResponse)
    }
}
