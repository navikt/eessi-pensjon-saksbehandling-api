package no.nav.eessi.pensjon.api.login

import io.mockk.every
import io.mockk.spyk
import no.nav.eessi.pensjon.services.BaseTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import java.net.URLEncoder


class LoginControllerMockTest : BaseTest() {

    var loginController = LoginController()
    var localLoginController = LocalLoginController()

    @Test
    fun `Calling loginController|login on FSS env returns redirect response`() {

        val request = MockHttpServletRequest("GET", "/login")
        request.serverName = "pensjon-utland-t.nav.no"
        request.scheme = "http"

        loginController.fasitEnvironmentName = "q2"
        loginController.appName = "eessi-pensjon-frontend-api-fss"
        loginController.navDomain = "nais.preprod.local"
        request.serverName = "pensjon-utland-t.nav.no"
        request.scheme = "http"

        var response = MockHttpServletResponse()
        val redirectTo = "http://pensjon-utland-t.nav.no"
        val context = "/_/"
        loginController.login(request, response, redirectTo, context)

        val encodedContext = URLEncoder.encode(context, "UTF-8")
        val generatedResponse = response.getHeader("Location")
        val expectedResponse = "https://eessi-pensjon-frontend-api-fss-q2.nais.preprod.local/openamlogin?redirect=$redirectTo&context=$encodedContext"

        assertEquals(response.status, 302)
        assertEquals(expectedResponse, generatedResponse)
    }

    @Test
    fun `Calling loginController|login on LOCAL env returns redirect response`() {

        val request = MockHttpServletRequest("GET", "/locallogin")
        request.serverName = "localhost"
        request.scheme = "http"

        localLoginController.port = "8888"
        localLoginController.localRestTemplate =  spyk(
                RestTemplateBuilder()
                .rootUri("http://localhost:8888")
                .build())

        var response = MockHttpServletResponse()
        val redirectTo = "http://localhost:3000"

        val mockCookieResponse = ResponseEntity("{\"name\":\"localhost-idtoken\",\"value\":\"eyJraWQiOiJsb2NhbGhvc3Qtc2lnbmVyIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIxMjM0NTY3ODkxMCIsImF1ZCI6ImF1ZC1sb2NhbGhvc3QiLCJhY3IiOiJMZXZlbDQiLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1NDk2Mjg1OTksImF1dGhfdGltZSI6MTU0OTYyODU5OSwiaXNzIjoiaXNzLWxvY2FsaG9zdCIsImV4cCI6MjMyNzIyODU5OSwibm9uY2UiOiJteU5vbmNlIiwiaWF0IjoxNTQ5NjI4NTk5LCJqdGkiOiI1MjBmOTI4Ny1lZjI5LTRhMWEtOWUwZC02OTExYzdjMTY0YzQifQ.GmOIJoYsSNvoOa1OQVtNeaSts3AjW4fE6rRae3lB2xNEZMhsRJIHtJqU3QxBRDqiGidKYkIGKFjaueqyPiPf1vZFmPQnu0Ul8yXzKJz4TvtiAfqW4fwEgvPXHAgrlVji-zAwpXa_QNmTN_xXJnFzbqAqC5K2r5hP6BvxkmdAWsOmVSDrUooHIqcO6GB0BtDv1xZ1yI1AZuFb8U5WmRWRVlxuTCTdLWgK_BRpclmfF4oGILomkLQaCEY0BD2PvdOwv73UIPZR_tkNec6FAjjLsEOFbmEZR5esYN8pyT94LqV6YJsjwzyCpT0bVZe0-BHWq2d3xKcrzuCsYJemkxk9IQ\",\"version\":0,\"comment\":null,\"domain\":\"localhost\",\"maxAge\":-1,\"path\":\"/\",\"secure\":false,\"httpOnly\":false}", HttpStatus.OK)

        every {
            localLoginController.localRestTemplate!!.exchange(
                eq("/local/cookie"),
                eq(HttpMethod.GET),
                any<HttpEntity<*>>(),
                eq(String::class.java))
        } returns mockCookieResponse

        localLoginController.login(request, response, redirectTo)

        assertEquals(response.status, 302)
        assertEquals("http://localhost:3000", response.getHeader("Location"))
        assertTrue(response.getHeader("Set-Cookie")!!.startsWith("localhost-idtoken"))
        assertNotNull(response.getCookie("localhost-idtoken"))
    }
}
