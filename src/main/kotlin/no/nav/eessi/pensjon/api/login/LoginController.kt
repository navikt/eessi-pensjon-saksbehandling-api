package no.nav.eessi.pensjon.api.login

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


//    @Unprotected
//    @GetMapping("/login")
//    fun login(httpServletRequest: HttpServletRequest,
//              httpServletResponse: HttpServletResponse,
//              @RequestParam("redirect", required = true) redirectTo: String) {
//
//        httpServletRequest.headerNames
////        @RequestParam("context", required = false) context: String?
////        val encodedContext = URLEncoder.encode(context, "UTF-8")
//
//        val apppath = "$appName.$navDomain"
//        val redir = "https://$apppath/oauth2/login?redirect=$redirectTo"
//
////        https://pensjon-utland-q2.nais.preprod.local?aktoerId=2953297351855&sakId=22955439&kravId=42501017&vedtakId=42791092
//
//        logger.debug("Redirecter til: $redir")
//
//        httpServletResponse.sendRedirect(redir)
//
//
//    }

//    @Unprotected
//    @GetMapping("/callback")
//    fun callback(httpServletRequest: HttpServletRequest,
//                 httpServletResponse: HttpServletResponse,
//                 @RequestParam("redirect", required = true) redirectTo: String) {
//
//        val callbackurl = "https://pensjon-utland-q2.nais.preprod.local?$redirectTo"
//        httpServletResponse.sendRedirect(callbackurl)
//
//
//    }

    @Unprotected
    @GetMapping("/login")
    fun login(httpServletRequest: HttpServletRequest,
              httpServletResponse: HttpServletResponse,
              @RequestParam("redirect") redirectTo: String,
              @RequestParam("context", required = false) context: String) {

     //        https://pensjon-utland-q2.nais.preprod.local?aktoerId=2953297351855&sakId=22955439&kravId=42501017&vedtakId=42791092


        val callbackUrl = "https://${appName}.${navDomain}/logincallback"//?redirect=" + redirectTo + context
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


