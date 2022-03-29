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

    @Test
    fun `Calling loginController|login on FSS env returns redirect response`() {

        val request = MockHttpServletRequest("GET", "/login")

        loginController.appName = "eessi-pensjon-frontend-api-fss-q2"
        loginController.navDomain = "nais.preprod.local"

        var response = MockHttpServletResponse()
        val redirectTo = "http://pensjon-utland-t.nav.no"
        val context = "/_/"
        loginController.login(request, response, redirectTo, context)

        val generatedResponse = response.getHeader("Location")
        val expectedResponse = "https://eessi-pensjon-frontend-api-fss-q2.nais.preprod.local/oauth2/login?redirect=https%3A%2F%2Feessi-pensjon-frontend-api-fss-q2.nais.preprod.local%2Flogincallback%3Fredirect%3Dhttp%3A%2F%2Fpensjon-utland-t.nav.no%2F_%2F"

        assertEquals(response.status, 302)
        assertEquals(expectedResponse, generatedResponse)
    }
}
