package no.nav.eessi.pensjon.services.kafka

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.messaging.support.MessageBuilder

@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>,
                   @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry()))  {
    val X_REQUEST_ID = "x_request_id"

    @Value("\${submitTopic}")
    lateinit var submissionReceivedTopicPrefix: String

    @Value("\${ENV}")
    lateinit var topicPostfix: String


    fun publishSubmissionReceivedEvent(payload: String) {
        val topic = "$submissionReceivedTopicPrefix-$topicPostfix"
        kafkaTemplate.defaultTopic = topic

        val messageBuilder = MessageBuilder.withPayload(payload)

        metricsHelper.measure("selvbetjeningsinfoprodusert") {
            if (MDC.get(X_REQUEST_ID).isNullOrEmpty()) {
                val message = messageBuilder.build()
                kafkaTemplate.send(message)
            } else {
                val message = messageBuilder.setHeader(X_REQUEST_ID, MDC.get(X_REQUEST_ID)).build()
                kafkaTemplate.send(message)
            }
        }
    }
}
