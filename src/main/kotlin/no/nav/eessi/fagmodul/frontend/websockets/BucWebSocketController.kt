package no.nav.eessi.fagmodul.frontend.websockets

import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Description
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Controller

@EnableScheduling
@Controller
@Description("This controller's purpose is to verify the websocket status during development, and should be delete when development is finished")
class BucWebSocketController {

    @Autowired
    lateinit var brokerMessagingTemplate: SimpMessagingTemplate

    @MessageMapping("/buc/increment")
    @SendTo("/topic/10")
    fun greeting(message: String): String {
        return mapAnyToJson(mapOf("message" to "" + (Integer.parseInt(message) + 10)))
    }

    @Scheduled(fixedRate = 2000)
    fun broadcastMessage() {
        brokerMessagingTemplate.convertAndSend("/topic/1",
            mapAnyToJson(mapOf("message" to 1))
        )
    }
}