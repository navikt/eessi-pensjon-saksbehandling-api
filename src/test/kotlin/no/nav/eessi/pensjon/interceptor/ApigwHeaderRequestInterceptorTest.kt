package no.nav.eessi.pensjon.interceptor

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.mock.http.client.MockClientHttpResponse

@ExtendWith(MockKExtension::class)
class ApigwHeaderRequestInterceptorTest {

    val apigwHeaderRequestInterceptor = ApigwHeaderRequestInterceptor("MockApiKey")

    @Test
    fun `intercept adds x-nav-apiKey to header`() {
        val request = mockk<HttpRequest>()
        val body = byteArrayOf()
        val execution = mockk<ClientHttpRequestExecution>()

        every { request.headers } returns HttpHeaders()
        every { execution.execute(any(), any()) } returns MockClientHttpResponse(body, HttpStatus.OK)

        apigwHeaderRequestInterceptor.intercept(request, body, execution)

        assert(request.headers["x-nav-apiKey"]!!.contains("MockApiKey"))
    }
}