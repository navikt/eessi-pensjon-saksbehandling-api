package no.nav.eessi.pensjon.api.submit

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.spyk
import io.mockk.verify
import no.nav.eessi.pensjon.services.kafka.KafkaService
import no.nav.eessi.pensjon.services.pdf.TemplateService
import no.nav.eessi.pensjon.services.storage.StorageService
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpStatus

@ExtendWith(MockKExtension::class)
internal class ReceiveSubmissionControllerTest {

    @MockK(relaxed = true)
    lateinit var kafkaService  : KafkaService

    @MockK(relaxed = true)
    lateinit var storageService : StorageService

    @MockK(relaxed = true)
    lateinit var templateService : TemplateService

    private lateinit var controller : ReceiveSubmissionController

    @BeforeEach
    fun setUp() {
        controller = spyk(ReceiveSubmissionController(kafkaService,
            storageService,
            SpringTokenValidationContextHolder(),
            templateService))
        controller.initMetrics()
    }

    @Test
    fun`Gitt en skjema innsending så lagre på s3 og send til kafka `() {

        // Gitt
        justRun {storageService.put(any(), any())  }

        val submissionRequestJson = DefaultResourceLoader().getResource(
            "classpath:json/submissionE207.json").file.readText()
        val submissionRequest = jacksonObjectMapper().readValue(submissionRequestJson, SubmissionRequest::class.java)
        every { controller.getSubjectFromToken() } returns "12345678910"

        // Når
        controller.receiveSubmission("somepage", submissionRequest)

        // Så
        verify(exactly = 1) { storageService.put(any(), any()) }
        verify(exactly = 1) { kafkaService.publishSubmissionReceivedEvent(any()) }
    }

    @Test
    fun`Gitt en skjema re innsending så send til kafka `() {
        // Når
       val resp = controller.resendSubmission("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")

        // Så
        assertEquals(HttpStatus.OK, resp.statusCode)
        verify(exactly = 0) { storageService.put(any(), any()) }
        verify(exactly = 1) { kafkaService.publishSubmissionReceivedEvent(any()) }
    }

    @Test
    fun`Gitt en forespørsel om kvittering generer og returner kvittering `() {

        // Gitt
        every { storageService.list("12345678910___PinfoSubmission___") } returns listOf("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")

        val e207 = DefaultResourceLoader().getResource("classpath:json/submissionE207.json").file.readText()
        every { storageService.get(any()) } returns e207
        justRun { storageService.put(any(), any()) }
        every { controller.getSubjectFromToken() } returns "12345678910"

        // Når
        val resp = controller.sendReceipt("somepage")

        // Så
        assertEquals(resp, "{}")
        verify (exactly = 1) { storageService.put(any(), any()) }
    }

    @Test
    fun`Gitt en hent forespørsel om innsendt 207 så hent innsending fra s3`() {

        // Gitt
        every { storageService.list("12345678910___PinfoSubmission___")  } returns listOf("12345678910___PinfoSubmission___2019-11-08T13:41:44.177.json")

        val e207 = DefaultResourceLoader().getResource("classpath:json/submissionE207.json").file.readText()
        every { storageService.get(any()) } returns e207

        // Når
        val resp = controller.getSubmissionAsJson("12345678910")

        // Så
        assertEquals(resp.statusCode, HttpStatus.OK)

        verify(exactly = 1) { storageService.list(any()) }
        verify(exactly = 1) { storageService.get(any()) }
    }
}

