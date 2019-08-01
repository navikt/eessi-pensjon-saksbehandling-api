package no.nav.eessi.fagmodul.frontend.services.login

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.fagmodul.frontend.services.BaseTest
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
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

        loginController.fasitEnvironmentName = "t8"
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
        val expectedResponse = "https://eessi-pensjon-frontend-api-fss-t8.nais.preprod.local/openamlogin?redirect=$redirectTo&context=$encodedContext"

        Assert.assertEquals(response.status, 302)
        Assert.assertEquals(expectedResponse, generatedResponse)
    }

    @Test
    fun `Calling loginController|login on LOCAL env returns redirect response`() {

        val request = MockHttpServletRequest("GET", "/locallogin")
        request.serverName = "localhost"
        request.scheme = "http"

        localLoginController.port = "8888"
        localLoginController.localRestTemplate =  Mockito.spy(
                RestTemplateBuilder()
                .rootUri("http://localhost:8888")
                .build())

        var response = MockHttpServletResponse()
        val redirectTo = "http://localhost:3000"

        val mockCookieResponse = ResponseEntity("{\"name\":\"localhost-idtoken\",\"value\":\"eyJraWQiOiJsb2NhbGhvc3Qtc2lnbmVyIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiIxMjM0NTY3ODkxMCIsImF1ZCI6ImF1ZC1sb2NhbGhvc3QiLCJhY3IiOiJMZXZlbDQiLCJ2ZXIiOiIxLjAiLCJuYmYiOjE1NDk2Mjg1OTksImF1dGhfdGltZSI6MTU0OTYyODU5OSwiaXNzIjoiaXNzLWxvY2FsaG9zdCIsImV4cCI6MjMyNzIyODU5OSwibm9uY2UiOiJteU5vbmNlIiwiaWF0IjoxNTQ5NjI4NTk5LCJqdGkiOiI1MjBmOTI4Ny1lZjI5LTRhMWEtOWUwZC02OTExYzdjMTY0YzQifQ.GmOIJoYsSNvoOa1OQVtNeaSts3AjW4fE6rRae3lB2xNEZMhsRJIHtJqU3QxBRDqiGidKYkIGKFjaueqyPiPf1vZFmPQnu0Ul8yXzKJz4TvtiAfqW4fwEgvPXHAgrlVji-zAwpXa_QNmTN_xXJnFzbqAqC5K2r5hP6BvxkmdAWsOmVSDrUooHIqcO6GB0BtDv1xZ1yI1AZuFb8U5WmRWRVlxuTCTdLWgK_BRpclmfF4oGILomkLQaCEY0BD2PvdOwv73UIPZR_tkNec6FAjjLsEOFbmEZR5esYN8pyT94LqV6YJsjwzyCpT0bVZe0-BHWq2d3xKcrzuCsYJemkxk9IQ\",\"version\":0,\"comment\":null,\"domain\":\"localhost\",\"maxAge\":-1,\"path\":\"/\",\"secure\":false,\"httpOnly\":false}", HttpStatus.OK)

        doReturn(mockCookieResponse).whenever(localLoginController.localRestTemplate)!!.exchange(
                ArgumentMatchers.eq("/local/cookie"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        localLoginController.login(request, response, redirectTo)

        Assert.assertEquals(response.status, 302)
        Assert.assertEquals("http://localhost:3000", response.getHeader("Location"))
        Assert.assertTrue(response.getHeader("Set-Cookie")!!.startsWith("localhost-idtoken"))
        Assert.assertNotNull(response.getCookie("localhost-idtoken"))
    }
}
