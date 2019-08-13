package no.nav.eessi.pensjon.interceptor

import no.nav.eessi.pensjon.utils.getToken
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class OidcHeaderRequestInterceptor(private val oidcRequestContextHolder: OIDCRequestContextHolder) : ClientHttpRequestInterceptor {

    private val logger: Logger by lazy { LoggerFactory.getLogger(OidcHeaderRequestInterceptor::class.java) }

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        if (request.headers[HttpHeaders.AUTHORIZATION] == null) {
            val oidcToken = getToken(oidcRequestContextHolder).idToken
            request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $oidcToken"
        }
        return execution.execute(request, body)
    }
}
