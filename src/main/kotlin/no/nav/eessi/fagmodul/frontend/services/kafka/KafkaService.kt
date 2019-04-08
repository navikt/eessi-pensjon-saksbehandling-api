package no.nav.eessi.fagmodul.frontend.services.kafka

import no.nav.eessi.fagmodul.frontend.utils.counter
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class KafkaService(val kafkaTemplate: KafkaTemplate<String, String>) {

    @Value("\${submitTopic:privat-eessipensjon-selvbetjeningsinfoMottatt-v1}")
    lateinit var submissionReceivedTopicPrefix: String

    private final val kafkaSelvbetjeningsinfoMottattProdusertTellerNavn = "eessipensjon_frontend-api.kafka_selvbetjeningsinfomottatt_produsert"
    private val kafkaSelvbetjeningsinfoMottattProdusertVellykkede = counter(kafkaSelvbetjeningsinfoMottattProdusertTellerNavn, "vellykkede")

    @Value("\${FASIT_ENVIRONMENT_NAME:t8}")
    lateinit var topicPostfix: String // = "t8"

    fun publishSubmissionReceivedEvent(message: String) {

        val topic = "$submissionReceivedTopicPrefix-$topicPostfix"
        kafkaTemplate.send(topic, message)
        kafkaSelvbetjeningsinfoMottattProdusertVellykkede.increment()
    }
}