package no.nav.eessi.pensjon

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableJwtTokenValidation(ignore = ["org.springframework", "springfox.documentation", "org.pac4j.springframework.web.CallbackController", "no.nav.eessi.pensjon.health.DiagnosticsController"])
@SpringBootApplication
@EnableOAuth2Client(cacheEnabled = true)
@EnableCaching
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
