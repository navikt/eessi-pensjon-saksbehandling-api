package no.nav.eessi.pensjon.api.submit

import com.nhaarman.mockitokotlin2.*
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.ResponseEntity

class SubmitControllerTest : SubmitBaseTest() {

    @After fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
        Mockito.reset(kafkaService)
        Mockito.reset(s3storageService)
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

    @Test fun `Calling receiveSubmissionController|receiveSubmission fail on s3`() {
        val mockRequest = SubmissionRequest(
                periodeInfo = PeriodeInfo(),
                personInfo = Personinfo(),
                bankinfo = Bankinfo(),
                comment = "comment"
        )

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())
        doThrow(RuntimeException("Feiler her ved s3")).whenever(s3storageService).put(any(),any())

        try {
            receiveSubmissionController.receiveSubmission(mockRequest)
            Assert.fail("skal ikke komme hit!")
        } catch (ex: Exception) {
            Assert.assertTrue("Skal komme hit!", true)
        }
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
        //Assert.assertEquals(HttpStatus.OK, generatedResponse.statusCode)
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
        //Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        //Assert.assertTrue(generatedResponse.body!!.contains("Resend av submission feilet. "))
    }

    @Test fun `receiveSubmissionController| putOnKafka failed after maxtries kafka Service fails`() {
        val uuid = "1234-1234-1234"
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

        doNothing().whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        doReturn(mockResponse).whenever(s3storageService).list(ArgumentMatchers.anyString())
        doReturn(latestSubmission).whenever(s3storageService).get(ArgumentMatchers.anyString())

        whenever(kafkaService.publishSubmissionReceivedEvent(ArgumentMatchers.anyString())).thenThrow(RuntimeException("This did not work"))
        try {
            receiveSubmissionController.putOnKafka(inputFileName, uuid)
            Assert.fail()
        } catch (ex: Exception) {
            Assert.assertTrue("Hit skal man komme i denne testen!!",true)
        }
    }

    @Test fun `receiveSubmissionController| putOnKafka successful after 2 kafka Service fails`() {
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
            .whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        doReturn(mockResponse).whenever(s3storageService).list(ArgumentMatchers.anyString())
        doReturn(latestSubmission).whenever(s3storageService).get(ArgumentMatchers.anyString())

        val response = receiveSubmissionController.putOnKafka(inputFileName, uuid)

        Assert.assertEquals(uuid, response)
    }

    @Test fun `receiveSubmissionController| putOnKafka successful`() {
        val uuid = "1234-1234-1234"
        val subject = "12345678910"
        val mockResponse = listOf(
            "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2018-09-01T00:00:00.json"
        )
        val latestSubmission = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"
        val inputFileName = "${subject}___${receiveSubmissionController.PINFO_SUBMISSION}___2028-09-01T00:00:00.json"

            doNothing()
            .whenever(kafkaService).publishSubmissionReceivedEvent(ArgumentMatchers.anyString())

        doReturn(mockResponse).whenever(s3storageService).list(ArgumentMatchers.anyString())
        doReturn(latestSubmission).whenever(s3storageService).get(ArgumentMatchers.anyString())

        val response = receiveSubmissionController.putOnKafka(inputFileName, uuid)

        Assert.assertEquals(uuid, response)
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
        Assert.assertEquals(expectedResponse, generatedResponse)
    }
}
