package no.nav.eessi.pensjon.api.login

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.client.RestTemplate
import java.net.URLEncoder
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class LoginController {

    val logger: Logger = LoggerFactory.getLogger(LoginController::class.java)

    @Value("\${NAIS_APP_NAME}")
    lateinit var appName: String

    @Value("\${NAV_DOMAIN_URL}")
    lateinit var navDomain: String

    @Unprotected
    @GetMapping("/login")
    fun login(httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse,
              @RequestParam("redirect") redirectTo: String,
              @RequestParam("context", required = false) context: String) {

        var callbackUrl = "https://${appName}.${navDomain}/logincallback?redirect=" + redirectTo + context
        var redirectUrl = "https://${appName}.${navDomain}/oauth2/login?redirect=" + URLEncoder.encode(callbackUrl, "UTF-8")

        logger.debug("Redirecting to login: $redirectUrl")
        httpServletResponse.sendRedirect(redirectUrl)
    }

    @Unprotected
    @GetMapping("/logincallback")
    fun openamlogin(httpServletResponse: HttpServletResponse, @RequestParam("redirect") redirect: String) {
        logger.debug("Redirecting back to frontend: $redirect")
        httpServletResponse.setHeader(HttpHeaders.LOCATION, "$redirect")
        httpServletResponse.status = HttpStatus.FOUND.value()
    }
}

