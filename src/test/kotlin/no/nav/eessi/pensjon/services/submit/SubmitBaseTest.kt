package no.nav.eessi.pensjon.services.submit

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eessi.pensjon.services.kafka.KafkaService
import no.nav.eessi.pensjon.services.pdf.PdfService
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.services.storage.StorageService
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.client.RestTemplate

open class SubmitBaseTest : S3StorageBaseTest() {

    var mapper = ObjectMapper()

    lateinit var receiveSubmissionController : ReceiveSubmissionController
    lateinit var mockFagmodulRestTemplate : RestTemplate
    lateinit var pdfService: PdfService
    lateinit var kafkaService : KafkaService
    lateinit var kafkaTemplate : KafkaTemplate<String, String>

    fun producerFactory(): DefaultKafkaProducerFactory<String, String> {
        val properties = mapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to "brokers",
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.CLIENT_ID_CONFIG to "appName",
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_SSL",
                SaslConfigs.SASL_MECHANISM to "PLAIN",
                SaslConfigs.SASL_JAAS_CONFIG to "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"username\" password=\"password\";"
        )
        return DefaultKafkaProducerFactory(properties)
    }

    @Before
    fun init() {
        kafkaTemplate = Mockito.spy(KafkaTemplate<String, String>(producerFactory()))
        kafkaService = Mockito.spy(KafkaService(kafkaTemplate))

        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()

        pdfService = Mockito.spy(PdfService())

        receiveSubmissionController = Mockito.spy(ReceiveSubmissionController(
            kafkaService,
            s3storageService,
            ObjectMapper(),
            generateMockContextHolder(),
            pdfService
        ))
    }

    @Test
    fun __dummy() {}
}
