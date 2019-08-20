package no.nav.eessi.pensjon.api.login

import no.nav.security.oidc.api.Unprotected
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.codehaus.jackson.map.ObjectMapper
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

@Profile("local")
@Controller
@Import(TokenGeneratorConfiguration::class)
class LocalLoginController {

    val logger: Logger = LoggerFactory.getLogger(LocalLoginController::class.java)
    var localRestTemplate : RestTemplate? = null

    @Value("\${server.port}")
    lateinit var port: String

    @Unprotected
    @GetMapping("/locallogin")
    fun login(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, @RequestParam("redirect") redirectTo: String) {
        if (localRestTemplate == null) {
            localRestTemplate = RestTemplateBuilder()
                    .rootUri("http://localhost:$port")
                    .build()
        }
        val cookieResult = localRestTemplate!!.exchange(
                "/local/cookie",
                HttpMethod.GET,
                HttpEntity("", HttpHeaders()),
                String::class.java)

        val body = ObjectMapper().readTree(cookieResult.body)
        val cookie = Cookie(body.get("name").textValue, body.get("value").textValue)

        logger.debug("Redirecting back to frontend: $redirectTo")
        httpServletResponse.addCookie(cookie)
        httpServletResponse.sendRedirect(redirectTo)
    }
}

@Controller
class LoginController {

    val logger: Logger = LoggerFactory.getLogger(LoginController::class.java)

    @Value("\${FASIT_ENVIRONMENT_NAME}")
    lateinit var fasitEnvironmentName: String

    @Value("\${APP_NAME}")
    lateinit var appName: String

    @Value("\${NAV_DOMAIN_URL}")
    lateinit var navDomain: String

    @Unprotected
    @GetMapping("/login")
    fun login(httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse,
              @RequestParam("redirect") redirectTo: String,
              @RequestParam("context", required = false) context: String) {

        var environmentPostfix = "-$fasitEnvironmentName"

        // Det settes nå kun dfault i prod, namespace brukes i alle andre miljø
        if (fasitEnvironmentName.contains("p", true)) {
            environmentPostfix = ""
        }

        val encodedContext = URLEncoder.encode(context, "UTF-8")
        logger.debug("Redirecting to: https://$appName$environmentPostfix.$navDomain/openamlogin?redirect=$redirectTo&context=$encodedContext")
        httpServletResponse.sendRedirect("https://$appName$environmentPostfix.$navDomain/openamlogin?redirect=$redirectTo&context=$encodedContext")
    }

    @Unprotected
    @GetMapping("/openamlogin")
    fun openamlogin(httpServletResponse: HttpServletResponse, @RequestParam("redirect") redirectTo: String, @RequestParam("context", required = false) context: String) {
        logger.debug("Redirecting back to frontend: $redirectTo$context")
        httpServletResponse.setHeader(HttpHeaders.LOCATION, "$redirectTo$context")
        httpServletResponse.status = HttpStatus.FOUND.value()
    }
}

