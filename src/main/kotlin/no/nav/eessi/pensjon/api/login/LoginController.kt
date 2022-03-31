package no.nav.eessi.pensjon.api.login

import no.nav.security.token.support.core.api.Protected
import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URLEncoder
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Controller
class LoginController {

    val logger: Logger = LoggerFactory.getLogger(LoginController::class.java)

    @Value("\${NAIS_APP_NAME}")
    lateinit var appName: String

    @Value("\${NAV_DOMAIN_URL}")
    lateinit var navDomain: String

    @GetMapping("/")
    fun login(request: HttpServletRequest): String {

        logger.debug("accesstoken før: ${request.getHeader("AUTHORIZATION")}")

        return request.getHeader("AUTHORIZATION")
    }

    @Unprotected
    @GetMapping("/login2")
    fun login2(httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse,
              @RequestParam("redirect", required = false) redirectTo: String?,
              @RequestParam("context", required = false) context: String?) {

        val redirectUrl = "https://${appName}.${navDomain}/oauth2/login?redirect=/blah"
        logger.debug("Redirecting to login: $redirectUrl")

        httpServletResponse.sendRedirect(redirectUrl)

    }


    @Unprotected
    @GetMapping("/login")
    fun login(httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse,
              @RequestParam("redirect") redirectTo: String,
              @RequestParam("context", required = false) context: String) {

        //        https://pensjon-utland-q2.nais.preprod.local?aktoerId=2953297351855&sakId=22955439&kravId=42501017&vedtakId=42791092
        //        https://pensjon-utland-q2.dev.intern.nav.no?aktoerId=2953297351855&sakId=22955439&kravId=42501017&vedtakId=42791092

        val callbackUrl = "/logincallback"//?redirect=" + redirectTo + context
        val redirectUrl = "https://${appName}.${navDomain}/oauth2/login?redirect=" + URLEncoder.encode(callbackUrl, "UTF-8")

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

