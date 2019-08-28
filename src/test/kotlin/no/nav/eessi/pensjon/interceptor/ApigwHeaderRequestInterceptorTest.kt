package no.nav.eessi.pensjon.interceptor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpResponse

@ExtendWith(MockitoExtension::class)
class ApigwHeaderRequestInterceptorTest {

    val apigwHeaderRequestInterceptor = ApigwHeaderRequestInterceptor("MockApiKey")

    @Test
    fun `intercept adds x-nav-apiKey to header`() {
        val request = mock(HttpRequest::class.java)
        val body = byteArrayOf()
        val execution = mock(ClientHttpRequestExecution::class.java)

        doReturn(HttpHeaders()).`when`(request).headers
        doReturn(MockClientHttpResponse(body, HttpStatus.OK)).`when`(execution).execute(any(), any())

        apigwHeaderRequestInterceptor.intercept(request, body, execution)

        assert(request.headers["x-nav-apiKey"]!!.contains("MockApiKey"))
    }
}