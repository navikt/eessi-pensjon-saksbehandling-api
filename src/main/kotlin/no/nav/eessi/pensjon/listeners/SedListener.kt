package no.nav.eessi.pensjon.listeners

import com.fasterxml.jackson.core.JsonParseException
import no.nav.eessi.pensjon.models.SedHendelseModel
import no.nav.eessi.pensjon.websocket.SocketTextHandler
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@Description("Listener on kafka messages to send websocket notifications")
class SedListener (private val socketTextHandler: SocketTextHandler) {

    private val logger = LoggerFactory.getLogger(SedListener::class.java)

    @KafkaListener(topics = ["\${kafka.sedSendt.topic}"], groupId = "\${kafka.sedSendt.groupid}")
    fun consumeSedSendt(hendelse: String) {
        try {
            val sedHendelse = SedHendelseModel.fromJson(hendelse)
            if(sedHendelse.sektorKode == "P") {
                logger.info("Innkommet sedSendt hendelse med rinaSakId " + sedHendelse.rinaSakId + ", navBruker " + sedHendelse.navBruker)
                socketTextHandler.alertSubscribers(sedHendelse.rinaSakId, sedHendelse.navBruker)
            }
        } catch (jsonParseException: JsonParseException) {
            logger.error("Error when parsing outgoing sedSendt Json", jsonParseException)
            throw jsonParseException
        } catch(exception: Exception){
            logger.error("Error when handling outgoing sedSendt event", exception)
            throw exception
        }
    }

    @KafkaListener(topics = ["\${kafka.sedMottatt.topic}"], groupId = "\${kafka.sedMottatt.groupid}")
    fun consumeSedMottatt(hendelse: String) {
        try {
            val sedHendelse = SedHendelseModel.fromJson(hendelse)
            if(sedHendelse.sektorKode == "P") {
                logger.info("Innkommet sedMottatt hendelse")
                socketTextHandler.alertSubscribers(sedHendelse.rinaSakId, sedHendelse.navBruker)
            }
        } catch (jsonParseException: JsonParseException) {
            logger.error("Error when parsing outgoing sedMottatt Json", jsonParseException)
            throw jsonParseException
        } catch(exception: Exception){
            logger.error("Error when handling incoming sedMottatt event", exception)
            throw exception
        }
    }
}
