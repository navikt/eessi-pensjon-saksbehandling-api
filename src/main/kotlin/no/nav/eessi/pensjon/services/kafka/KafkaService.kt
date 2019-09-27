package no.nav.eessi.pensjon.services.kafka

import no.nav.eessi.pensjon.utils.counter
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.messaging.support.MessageBuilder

@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>) {
    val X_REQUEST_ID = "x_request_id"

    @Value("\${submitTopic:privat-eessipensjon-selvbetjeningsinfoMottatt-v1}")
    lateinit var submissionReceivedTopicPrefix: String

    private final val kafkaSelvbetjeningsinfoMottattProdusertTellerNavn = "eessipensjon_frontend-api.kafka_selvbetjeningsinfomottatt_produsert"
    private val kafkaSelvbetjeningsinfoMottattProdusertVellykkede = counter(kafkaSelvbetjeningsinfoMottattProdusertTellerNavn, "vellykkede")

    @Value("\${FASIT_ENVIRONMENT_NAME:t8}")
    lateinit var topicPostfix: String // = "t8"


    fun publishSubmissionReceivedEvent(payload: String) {
        val topic = "$submissionReceivedTopicPrefix-$topicPostfix"
        kafkaTemplate.defaultTopic = topic

        val messageBuilder = MessageBuilder.withPayload(payload)

        if(MDC.get(X_REQUEST_ID).isNullOrEmpty()){
            val message = messageBuilder.build()
            kafkaTemplate.send(message)
        } else {
            val message = messageBuilder.setHeader(X_REQUEST_ID, MDC.get(X_REQUEST_ID)).build()
            kafkaTemplate.send(message)
        }
        kafkaSelvbetjeningsinfoMottattProdusertVellykkede.increment()
    }
}
