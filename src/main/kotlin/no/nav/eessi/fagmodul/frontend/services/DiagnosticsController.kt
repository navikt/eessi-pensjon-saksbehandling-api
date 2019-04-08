package no.nav.eessi.fagmodul.frontend.services

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@CrossOrigin
@RestController
class DiagnosticsController {

    private val logger: Logger by lazy { LoggerFactory.getLogger(DiagnosticsController::class.java) }

    @GetMapping("/ping")
    fun ping(): ResponseEntity<Unit> {
        return ResponseEntity.ok().build()
    }

    @GetMapping("/internal/selftest")
    fun selftest(): SelftestResult {
        logger.debug("selftest passed")
        return SelftestResult(aggregateResult = 0, checks = null)
    }

    @GetMapping("/internal/isalive")
    fun isalive(): ResponseEntity<String> {
        return ResponseEntity.ok("Is alive")
    }

    @GetMapping("/internal/isready")
    fun isready(): ResponseEntity<String> {
        return ResponseEntity.ok("Is ready")
    }
}

data class SelftestResult(
        val name: String = "eessi-fagmodul",
        val version: String = "0.0.1-SNAPSHOT",
        val timestamp: Instant = Instant.now(),
        val aggregateResult: Int,
        val checks: List<Check>?
)

data class Check(
        val endpoint: String,
        val description: String,
        val errorMessage: String,
        val result: Int
)
