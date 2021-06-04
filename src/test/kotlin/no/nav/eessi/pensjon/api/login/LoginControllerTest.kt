package no.nav.eessi.pensjon.api.login

import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@ExtendWith(MockKExtension::class)
class FssLoginControllerTest {

    @SpyK
    var resp = MockHttpServletResponse()

    @MockK
    lateinit var req: MockHttpServletRequest

    lateinit var fssLoginController: LoginController

    @BeforeEach
    fun before() {
        fssLoginController = LoginController()
        fssLoginController.appName = "eessi-pensjon-frontend-api-fss"
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is q1 Then redirect to fss without namespace`() {
        fssLoginController.fasitEnvironmentName = "q1"
        fssLoginController.navDomain = "domain"

        fssLoginController.login(req, resp, "somewhere", "somecontext")


        verify(atLeast = 1) { resp.sendRedirect("https://eessi-pensjon-frontend-api-fss-q1.domain/openamlogin?redirect=somewhere&context=somecontext") }
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is q2 Then redirect to fss without namespace`() {
        fssLoginController.fasitEnvironmentName = "q2"
        fssLoginController.navDomain = "domain"

        fssLoginController.login(req, resp, "somewhere", "somecontext")

        verify(atLeast = 1) { resp.sendRedirect("https://eessi-pensjon-frontend-api-fss-q2.domain/openamlogin?redirect=somewhere&context=somecontext") }
    }


    @Test
    fun `Given a login attempt in FSS zone When environment is p Then redirect to adeo`() {
        fssLoginController.fasitEnvironmentName = "p"
        fssLoginController.navDomain = "nais.adeo.no"

        fssLoginController.login(req, resp, "somewhereelse", "somecontext")

        verify(atLeast = 1) { resp.sendRedirect("https://${fssLoginController.appName}.nais.adeo.no/openamlogin?redirect=somewhereelse&context=somecontext") }
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is preprod Then redirect to preprod`() {
        fssLoginController.fasitEnvironmentName = "q2"
        fssLoginController.navDomain = "nais.preprod.local"

        fssLoginController.login(req, resp, "somewhereelse", "somecontext")

        verify(atLeast = 1) { resp.sendRedirect("https://${fssLoginController.appName}-q2.nais.preprod.local/openamlogin?redirect=somewhereelse&context=somecontext") }
    }
}
