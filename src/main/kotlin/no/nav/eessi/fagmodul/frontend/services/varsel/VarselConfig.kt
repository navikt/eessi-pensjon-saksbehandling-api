package no.nav.eessi.fagmodul.frontend.services.varsel

import com.ibm.mq.constants.MQConstants
import com.ibm.mq.jms.MQConnectionFactory
import com.ibm.msg.client.wmq.WMQConstants
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jms.connection.UserCredentialsConnectionFactoryAdapter
import org.springframework.jms.core.JmsTemplate
import org.springframework.jms.support.destination.DynamicDestinationResolver
import java.net.URI
import javax.jms.ConnectionFactory

@Configuration
class VarselConfig {

    @Value("\${eessi.pensjon.frontend.api.fss.channel.queuemanager}")
    lateinit var queueManagerUrl: String

    @Value("\${eessi.pensjon.frontend.api.fss.channel.name}")
    lateinit var channelName: String

    @Bean
    fun wmqConnectionFactory(): ConnectionFactory {

        val qmgrURI = URI(queueManagerUrl)

        val connectionFactory = MQConnectionFactory().apply {
            hostName = qmgrURI.host
            port = qmgrURI.port
            queueManager = qmgrURI.path.replace("/", "")
            transportType = WMQConstants.WMQ_CM_CLIENT
            channel = channelName.replace("-", "_")
            clientReconnectOptions = WMQConstants.WMQ_CLIENT_RECONNECT
            clientReconnectTimeout = 600
            setIntProperty(WMQConstants.JMS_IBM_ENCODING, MQConstants.MQENC_NATIVE)
            setIntProperty(WMQConstants.JMS_IBM_CHARACTER_SET, 1208)
        }

        return UserCredentialsConnectionFactoryAdapter().apply {
            setTargetConnectionFactory(connectionFactory)
            setUsername("srvappserver")
            setPassword("")
        }
    }

    @Bean
    fun wmqJmsTemplate(wmqConnectionFactory: ConnectionFactory): JmsTemplate {
        val jmsTemplate = JmsTemplate(wmqConnectionFactory)
        jmsTemplate.destinationResolver = DynamicDestinationResolver()
        return jmsTemplate
    }
}