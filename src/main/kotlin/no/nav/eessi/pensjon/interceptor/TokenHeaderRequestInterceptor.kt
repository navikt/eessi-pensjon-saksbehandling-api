package no.nav.eessi.pensjon.interceptor

import no.nav.eessi.pensjon.utils.getToken
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class TokenHeaderRequestInterceptor(private val tokenValidationContextHolder: TokenValidationContextHolder) : ClientHttpRequestInterceptor {

    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        if (request.headers[HttpHeaders.AUTHORIZATION] == null) {
            val oidcToken = getToken(tokenValidationContextHolder).tokenAsString
            request.headers[HttpHeaders.AUTHORIZATION] = "Bearer $oidcToken"
        }
        return execution.execute(request, body)
    }
}
