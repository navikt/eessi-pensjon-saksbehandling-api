package no.nav.eessi.pensjon.services.kafka

import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.MDC
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.Message

class KafkaServiceTest {

    private var mockKafkaTemplate: KafkaTemplate<String, String> = mockk(relaxed = true)
    private val kafkaService = KafkaService(mockKafkaTemplate)

    @AfterEach
    fun cleanup(){
        MDC.clear()
    }

    @BeforeEach
    fun init() {
        kafkaService.initMetrics()
    }

    @Test
    fun `KafkaService sets topic based on submissionReceivedTopicPrefix and topicPostfix`() {

        val captureSlot  = slot<String>()
        justRun { mockKafkaTemplate.defaultTopic = capture(captureSlot) }

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        kafkaService.publishSubmissionReceivedEvent("Payload")

        assertEquals("privat-eessipensjon-selvbetjeningsinfoMottatt-q2", captureSlot.captured)
    }

    @Test
    fun `KafkaService adds x_request_id header to sent message if present in MDC`() {

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        MDC.put(kafkaService.X_REQUEST_ID, "mockRequestId")

        var captureSlot = slot<Message<String>>()

        every { mockKafkaTemplate.send(capture(captureSlot)) } returns mockk()
        kafkaService.publishSubmissionReceivedEvent("Payload")
        assertEquals("mockRequestId", captureSlot.captured.headers["x_request_id"])
        assertEquals("Payload", captureSlot.captured.payload)

    }

    @Test
    fun `KafkaService does not add x_request_id header to sent message if not present in MDC`() {
        var captor = slot<Message<String>>()

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        every { mockKafkaTemplate.send(capture(captor)) } returns mockk()

        kafkaService.publishSubmissionReceivedEvent("Payload")

        assertEquals(null, captor.captured.headers["x_request_id"])
        assertEquals("Payload", captor.captured.payload)
    }

}
