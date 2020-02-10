package no.nav.eessi.pensjon.config

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import org.pac4j.core.client.Clients
import org.pac4j.core.config.Config
import org.pac4j.core.context.Cookie
import org.pac4j.core.context.WebContext
import org.pac4j.core.http.callback.NoParameterCallbackUrlResolver
import org.pac4j.core.http.url.DefaultUrlResolver
import org.pac4j.oidc.client.OidcClient
import org.pac4j.oidc.config.OidcConfiguration
import org.pac4j.oidc.profile.OidcProfile
import org.pac4j.springframework.web.SecurityInterceptor
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.util.UriComponentsBuilder

private const val CALLBACK_URI = "/callback"

private val logger = LoggerFactory.getLogger(OidcConfig::class.java)

@Configuration
@ComponentScan(basePackages = ["org.pac4j.springframework.web"])
class OidcConfig : WebMvcConfigurer {

    @Value("\${isso.agentname}")
    private lateinit var clientId: String

    @Value("\${isso.agent.password}")
    private lateinit var clientSecret: String

    @Value("\${no.nav.security.oidc.issuer.isso.discoveryurl}")
    private lateinit var discoveryUrl: String

    @Value("\${no.nav.security.oidc.issuer.isso.cookiename}")
    private lateinit var cookiename: String

    @Value("\${redirectscheme}")
    private lateinit var redirectScheme: String

    @Value("\${ENVCLASS}")
    private lateinit var fasitEnvironmentName: String

    @Autowired
    private lateinit var authInterceptor: AuthInterceptor

    var cookieDomain: String = "localhost"

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(securityInterceptor()).addPathPatterns("/openamlogin")
        registry.addInterceptor(authInterceptor)

    }

    @Bean
    fun securityInterceptor(): SecurityInterceptor {
        return SecurityInterceptor(config(), "OidcClient")
    }

    @Bean
    fun config(): Config {

        val oidcConfiguration = OidcConfiguration().apply {
            clientId = this@OidcConfig.clientId
            secret = clientSecret
            isUseNonce = true
            discoveryURI = discoveryUrl
            scope = "openid"
            clientAuthenticationMethod = ClientAuthenticationMethod.CLIENT_SECRET_BASIC
            maxClockSkew = 30
            isExpireSessionWithToken = true
            isWithState = false
            preferredJwsAlgorithm = JWSAlgorithm.RS256
        }

        val oidcClient = OidcClient<OidcProfile, OidcConfiguration>(oidcConfiguration).apply {
            callbackUrlResolver = NoParameterCallbackUrlResolver()
            name = "OidcClient"
            urlResolver = object : DefaultUrlResolver(true) {
                override fun compute(url: String, context: WebContext): String {
                    var computed = super.compute(url, context)

                    // TODO: This is dumb. But it works. Better hope loginservice comes to FSS soon so we can get rid of this whole pile of shit

                    if(!computed.contains("http://localhost")) {
                        if (discoveryUrl.contains("isso-t") || discoveryUrl.contains("isso-q")) {
                            // We are in a test-environment
                            var environmentPostfix = "-$fasitEnvironmentName"
                            if (fasitEnvironmentName == "p") {
                                environmentPostfix = ""
                            }
                            computed = "https://eessi-pensjon-frontend-api-fss$environmentPostfix.nais.preprod.local/callback"
                            cookieDomain = "nais.preprod.local"
                        } else {
                            // We are in production
                            computed = "https://eessi-pensjon-frontend-api-fss.nais.adeo.no/callback"
                            cookieDomain = "nais.adeo.no"
                        }
                    } else {
                        // We are in a local dev-environment. Keep the computed url, and set cookiedomain to localhost
                        cookieDomain = "localhost"
                    }

                    val uri = UriComponentsBuilder.fromUriString(computed).scheme(redirectScheme).build().toUriString()
                    logger.info("Computed callbackuri: $uri")
                    return uri
                }
            }
            setAuthorizationGenerator { context, profile ->
                context.addResponseCookie(createCookie(cookieDomain, cookiename, profile.idTokenString))
                profile
            }
        }
        logger.debug("Created OidcClient: $oidcClient")
        val clients = Clients(CALLBACK_URI, oidcClient)
        oidcClient.callbackUrl = CALLBACK_URI
        return Config(clients)
    }

    private fun createCookie(domain: String, cookieName: String, content: String?, secureCookie: Boolean = true): Cookie {
        val cookie = Cookie(cookieName, content).apply {
            maxAge = 60 * 60  // 3600 seconds = 1 hour
            this.domain = domain
            if (secureCookie) {
                isHttpOnly = true
                isSecure = redirectScheme == "https"
            } else {
                isHttpOnly = false
                isSecure = false
            }
        }
        logger.debug("Created cookie ${cookie.name} for domain ${cookie.domain}")
        return cookie
    }
}

