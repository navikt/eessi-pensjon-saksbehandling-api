package no.nav.eessi.fagmodul.frontend

import no.nav.security.spring.oidc.api.EnableOIDCTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableOIDCTokenValidation(ignore = ["org.springframework", "springfox.documentation", "org.pac4j.springframework.web.CallbackController", "no.nav.eessi.fagmodul.frontend.services.DiagnosticsController"])
@SpringBootApplication
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
