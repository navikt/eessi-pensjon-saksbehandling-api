package no.nav.eessi.pensjon.listeners

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eessi.pensjon.websocket.SocketTextHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.concurrent.CountDownLatch

@Component
@Description("Listener on kafka messages to send websocket notifications")
class SedListener {

    private val mapper = ObjectMapper()
    private val logger = LoggerFactory.getLogger(SedListener::class.java)
    private val latch = CountDownLatch(1)

    @KafkaListener(topics = ["\${kafka.sedSendt.topic}"], groupId = "\${kafka.sedSendt.groupid}")
    fun consumeSedSendt(hendelse: String) {
        try {
            logger.info("Innkommet sedSendt hendelse")
            logger.debug(hendelse)
            val jsonNode = mapper.readTree(hendelse)
            if(jsonNode.has("rinaSakId") && jsonNode["rinaSakId"].isTextual){
                SocketTextHandler().alertSubscribers(jsonNode["rinaSakId"].textValue())
            }
            latch.countDown()
        } catch(exception: Exception){
            logger.error("Error when handling outgoing sedSendt event", exception)
            throw exception
        }
    }

    @KafkaListener(topics = ["\${kafka.sedMottatt.topic}"], groupId = "\${kafka.sedMottatt.groupid}")
    fun consumeSedMottatt(hendelse: String) {
        try {
        logger.info("Innkommet sedMottatt hendelse")
        logger.debug(hendelse)
            val jsonNode = mapper.readTree(hendelse)
            if(jsonNode.has("rinaSakId") && jsonNode["rinaSakId"].isTextual){
                SocketTextHandler().alertSubscribers(jsonNode["rinaSakId"].textValue())
            }
        } catch(exception: Exception){
            logger.error("Error when handling incoming sedMottatt event", exception)
            throw exception
        }
    }
}
