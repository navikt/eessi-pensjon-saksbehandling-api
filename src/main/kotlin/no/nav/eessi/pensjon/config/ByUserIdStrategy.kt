package no.nav.eessi.pensjon.config

import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy
import org.slf4j.LoggerFactory

internal class ByUserIdStrategy() : Strategy {
    private val logger = LoggerFactory.getLogger(UnleashConfigEessi::class.java)

    override fun getName(): String = "userId"
    override fun isEnabled(parameters: Map<String?, String?>, p1: UnleashContext): Boolean {

        logger.debug("ByUserIdStrategy: " + parameters.toString())

        val userIDs = parameters["user"]
        if (userIDs.isNullOrBlank()) return false
        logger.debug("ByUserIdStrategy sjekker om saksbehandler userId er i listen $userIDs")

       return false
    }
}

