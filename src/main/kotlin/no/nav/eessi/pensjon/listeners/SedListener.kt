package no.nav.eessi.pensjon.listeners

import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Description
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
@Description("Listener on kafka messages to send websocket notifications")
class SedListener {

    private val logger = LoggerFactory.getLogger(SedListener::class.java)
    private val latch = CountDownLatch(1)

    @Autowired
    lateinit var brokerMessagingTemplate: SimpMessagingTemplate

    @KafkaListener(topics = ["\${kafka.sedSendt.topic}"], groupId = "\${kafka.sedSendt.groupid}")
    fun consumeSedSendt(hendelse: String) {
        logger.info("Innkommet sedSendt hendelse")
        logger.debug(hendelse)
        try {
            brokerMessagingTemplate.convertAndSend("/sed",
                mapAnyToJson(mapOf("action" to "Sent", "payload" to hendelse))
            )
        } catch(ex: Exception){
            logger.error(
                "Noe gikk galt under behandling av SED-hendelse:\n $hendelse \n${ex.message}", ex)
            throw RuntimeException(ex.message)
        }
        latch.countDown()
    }

    @KafkaListener(topics = ["\${kafka.sedMottatt.topic}"], groupId = "\${kafka.sedMottatt.groupid}")
    fun consumeSedMottatt(hendelse: String) {
        logger.info("Innkommet sedMottatt hendelse")
        logger.debug(hendelse)
        try {
            brokerMessagingTemplate.convertAndSend("/sed",
                mapAnyToJson(mapOf("action" to "Received", "payload" to hendelse))
            )
        } catch(ex: Exception){
            logger.error(
                "Noe gikk galt under behandling av SED-hendelse:\n $hendelse \n${ex.message}", ex)
            throw RuntimeException(ex.message)
        }
    }
}