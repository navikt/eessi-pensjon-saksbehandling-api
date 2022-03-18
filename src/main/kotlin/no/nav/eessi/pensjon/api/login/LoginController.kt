package no.nav.eessi.pensjon.api.login

import no.nav.security.token.support.core.api.Unprotected
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
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
              @RequestParam("redirect", required = true) redirectTo: String) {

//        @RequestParam("context", required = false) context: String?
//        val encodedContext = URLEncoder.encode(context, "UTF-8")

        val apppath = "$appName.$navDomain"
        val redir = "https://$apppath/oauth2/login?redirect=$redirectTo"

        logger.debug("Redirecter til: $redir")

        httpServletResponse.sendRedirect(redir)

    }

}

//@Profile("local")
//@Controller
//@Import(TokenGeneratorConfiguration::class)
//class LocalLoginController {
//
//    val logger: Logger = LoggerFactory.getLogger(LocalLoginController::class.java)
//    var localRestTemplate : RestTemplate? = null
//
//    @Value("\${server.port}")
//    lateinit var port: String
//
//    @Unprotected
//    @GetMapping("/locallogin")
//    fun login(httpServletRequest: HttpServletRequest, httpServletResponse: HttpServletResponse, @RequestParam("redirect") redirectTo: String) {
//        if (localRestTemplate == null) {
//            localRestTemplate = RestTemplateBuilder()
//                    .rootUri("http://localhost:$port")
//                    .build()
//        }
//        val cookieResult = localRestTemplate!!.exchange(
//                "/local/cookie",
//                HttpMethod.GET,
//                HttpEntity("", HttpHeaders()),
//                String::class.java)
//
//        val body = ObjectMapper().readTree(cookieResult.body)
//        val cookie = Cookie(body.get("name").textValue(), body.get("value").textValue())
//
//        logger.debug("Redirecting back to frontend: $redirectTo")
//        httpServletResponse.addCookie(cookie)
//        httpServletResponse.sendRedirect(redirectTo)
//    }
//}
//


