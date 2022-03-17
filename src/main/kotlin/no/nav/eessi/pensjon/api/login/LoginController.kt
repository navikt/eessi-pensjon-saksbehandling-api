package no.nav.eessi.pensjon.api.login

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
//@Controller
//class LoginController {
//
//    val logger: Logger = LoggerFactory.getLogger(LoginController::class.java)
////
////    @Value("\${ENV}")
////    lateinit var fasitEnvironmentName: String
//
//    @Value("\${NAIS_APP_NAME}")
//    lateinit var appName: String
//
//    @Value("\${NAV_DOMAIN_URL}")
//    lateinit var navDomain: String
//
//    @Unprotected
//    @GetMapping("/login")
//    fun login(httpServletRequest: HttpServletRequest,
//              httpServletResponse: HttpServletResponse,
//              @RequestParam("redirect") redirectTo: String,
//              @RequestParam("context", required = false) context: String) {
//
////        var environmentPostfix = "-$fasitEnvironmentName"
////
////        // Det settes nå kun dfault i prod, namespace brukes i alle andre miljø
////        if (fasitEnvironmentName.contains("p", true)) {
////            environmentPostfix = ""
////        }
//
//        val encodedContext = URLEncoder.encode(context, "UTF-8")
//        logger.debug("Redirecting to: https://$appName.$navDomain/openamlogin?redirect=$redirectTo&context=$encodedContext")
//        httpServletResponse.sendRedirect("https://$appName.$navDomain/openamlogin?redirect=$redirectTo&context=$encodedContext")
//    }
//
//    @Unprotected
//    @GetMapping("/openamlogin")
//    fun openamlogin(httpServletResponse: HttpServletResponse, @RequestParam("redirect") redirectTo: String, @RequestParam("context", required = false) context: String) {
//        logger.debug("Redirecting back to frontend: $redirectTo$context")
//        httpServletResponse.setHeader(HttpHeaders.LOCATION, "$redirectTo$context")
//        httpServletResponse.status = HttpStatus.FOUND.value()
//    }
//}
//
