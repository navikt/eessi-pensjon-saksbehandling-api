package no.nav.eessi.pensjon.api.submit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockitokotlin2.*
import no.nav.eessi.pensjon.services.kafka.KafkaService
import no.nav.eessi.pensjon.services.pdf.TemplateService
import no.nav.eessi.pensjon.services.storage.StorageService
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpStatus

@ExtendWith(MockitoExtension::class)
internal class ReceiveSubmissionControllerTest {

    @Mock
    lateinit var kafkaService : KafkaService

    @Mock
    lateinit var storageService: StorageService

    @Mock
    lateinit var templateService : TemplateService


    private lateinit var controller : ReceiveSubmissionController

    @BeforeEach
    fun setUp() {
        controller = Mockito.spy(ReceiveSubmissionController(kafkaService,
            storageService,
            SpringTokenValidationContextHolder(),
            templateService))
        controller.initMetrics()
    }

    @Test
    fun`Gitt en skjema innsending så lagre på s3 og send til kafka `() {

        // Gitt
        doNothing().`when`(storageService).put(any(), any())

        val submissionRequestJson = DefaultResourceLoader().getResource(
            "classpath:json/submissionE207.json").file.readText()
        val submissionRequest = jacksonObjectMapper().readValue(submissionRequestJson, SubmissionRequest::class.java)
        doReturn("12345678910").`when`(controller).getSubjectFromToken()

        // Når
        controller.receiveSubmission("somepage", submissionRequest)

        // Så
        verify(storageService, times(1)).put(any(), any())
        verify(kafkaService, times(1)).publishSubmissionReceivedEvent(any())
    }

    @Test
    fun`Gitt en skjema re innsending så send til kafka `() {
        // Når
       val resp = controller.resendSubmission("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")

        // Så
        assertEquals(resp.statusCode, HttpStatus.OK)
        verify(storageService, times(0)).put(any(), any())
        verify(kafkaService, times(1)).publishSubmissionReceivedEvent(any())
    }

    @Test
    fun`Gitt en forespørsel om kvittering generer og returner kvittering `() {

        // Gitt
        doReturn(listOf("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")).`when`(storageService).list("12345678910___PinfoSubmission___")

         val e207 = DefaultResourceLoader().getResource(
            "classpath:json/submissionE207.json").file.readText()
        doReturn(e207).`when`(storageService).get(any())
        doNothing().`when`(storageService).put(any(), any())
        doReturn("12345678910").`when`(controller).getSubjectFromToken()

        // Når
        val resp = controller.sendReceipt("somepage")

        // Så
        assertEquals(resp, "{}")
        verify(storageService, times(1)).put(any(), any())
    }

    @Test
    fun`Gitt en hent forespørsel om innsendt 207 så hent innsending fra s3`() {

        // Gitt
        doReturn(listOf("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")).`when`(storageService).list("12345678910___PinfoSubmission___")

        val e207 = DefaultResourceLoader().getResource(
            "classpath:json/submissionE207.json").file.readText()
        doReturn(e207).`when`(storageService).get(any())

        // Når
        val resp = controller.getSubmissionAsJson("12345678910")

        // Så
        assertEquals(resp.statusCode, HttpStatus.OK)

        verify(storageService, times(1)).list(any())
        verify(storageService, times(1)).get(any())
    }
}

