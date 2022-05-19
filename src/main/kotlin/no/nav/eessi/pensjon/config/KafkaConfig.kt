package no.nav.eessi.pensjon.config

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import java.time.Duration

@Configuration
class KafkaConfig {

    @Value("\${kafka.brokers.url}")
    lateinit var brokers: String

    @Value("\${NAIS_APP_NAME}")
    lateinit var appName: String

    @Value("\${srvusername}")
    lateinit var username: String

    @Value("\${srvpassword}")
    lateinit var password: String

    @Bean
    fun sedSendtAuthRetry(registry: KafkaListenerEndpointRegistry): ApplicationRunner? {
        return ApplicationRunner {
            val sedSendtListenerContainer = registry.getListenerContainer("sakSendtListener") ?: throw RuntimeException("Klarte ikke å starte sedSendtListener")
            sedSendtListenerContainer.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(4L))
            sedSendtListenerContainer.start()

            val sedMottattListenerContainer = registry.getListenerContainer("sakMottattListener") ?: throw RuntimeException("Klarte ikke å starte sedMottattListener")
            sedMottattListenerContainer.containerProperties.setAuthExceptionRetryInterval(Duration.ofSeconds(4L))
            sedMottattListenerContainer.start()
        }
    }

    fun producerFactory(): DefaultKafkaProducerFactory<String, String> {
        val properties = mapOf<String, Any>(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to brokers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.CLIENT_ID_CONFIG to appName,
                CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to "SASL_SSL",
                SaslConfigs.SASL_MECHANISM to "PLAIN",
                SaslConfigs.SASL_JAAS_CONFIG to "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"$username\" password=\"$password\";"
        )
        return DefaultKafkaProducerFactory(properties)
    }

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, String> {
        return KafkaTemplate<String, String>(producerFactory())
    }
}