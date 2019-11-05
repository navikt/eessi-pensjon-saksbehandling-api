package no.nav.eessi.pensjon.services.kafka

import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.slf4j.MDC
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.messaging.Message

class KafkaServiceTest() {

    private val mockKafkaTemplate: KafkaTemplate<String, String> = Mockito.mock(KafkaTemplate::class.java) as KafkaTemplate<String, String>
    private val kafkaService = KafkaService(mockKafkaTemplate)

    @AfterEach
    fun cleanup(){
        MDC.clear()
    }

    @Test
    fun `KafkaService sets topic based on submissionReceivedTopicPrefix and topicPostfix`() {
        val argumentCaptor = argumentCaptor<String>()
        doNothing().`when`(mockKafkaTemplate).defaultTopic = argumentCaptor.capture()
        assertEquals(0, argumentCaptor.allValues.size)

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        kafkaService.publishSubmissionReceivedEvent("Payload")

        assertEquals("privat-eessipensjon-selvbetjeningsinfoMottatt-q2", argumentCaptor.firstValue)
    }

    @Test
    fun `KafkaService adds x_request_id header to sent message if present in MDC`() {
        val argumentCaptor = argumentCaptor<Message<String>>()

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        MDC.put(kafkaService.X_REQUEST_ID, "mockRequestId")
        doReturn(null).`when`(mockKafkaTemplate).send(argumentCaptor.capture())
        kafkaService.publishSubmissionReceivedEvent("Payload")

        assertEquals("mockRequestId", argumentCaptor.firstValue.headers["x_request_id"])
        assertEquals("Payload", argumentCaptor.firstValue.payload)
    }

    @Test
    fun `KafkaService does not add x_request_id header to sent message if not present in MDC`() {
        val argumentCaptor = argumentCaptor<Message<String>>()

        kafkaService.submissionReceivedTopicPrefix = "privat-eessipensjon-selvbetjeningsinfoMottatt"
        kafkaService.topicPostfix = "q2"
        doReturn(null).`when`(mockKafkaTemplate).send(argumentCaptor.capture())
        kafkaService.publishSubmissionReceivedEvent("Payload")

        assertEquals(null, argumentCaptor.firstValue.headers["x_request_id"])
        assertEquals("Payload", argumentCaptor.firstValue.payload)
    }

}