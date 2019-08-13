package no.nav.eessi.pensjon.interceptor

import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

class ApigwHeaderRequestInterceptor(private val apiKey: String) : ClientHttpRequestInterceptor {
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        request.headers.add("x-nav-apiKey", apiKey)
        return execution.execute(request, body)
    }
}
