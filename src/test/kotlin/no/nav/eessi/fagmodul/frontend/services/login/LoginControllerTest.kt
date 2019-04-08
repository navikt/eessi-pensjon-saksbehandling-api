package no.nav.eessi.fagmodul.frontend.services.login

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@RunWith(MockitoJUnitRunner::class)
class LoginControllerTest {

    @Spy
    lateinit var resp : MockHttpServletResponse

    @Mock
    lateinit var req : MockHttpServletRequest

    lateinit var loginController: LoginController

    @Before
    fun before(){
        loginController = LoginController()
        loginController.appName = "eessi-pensjon-frontend-api-fss"
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is q1 Then redirect to fss without namespace`() {
        loginController.fasitEnvironmentName = "q1"
        loginController.navDomain = "domain"

        loginController.login(req, resp, "somewhere", "somecontext")


        verify(resp).sendRedirect("https://eessi-pensjon-frontend-api-fss.domain/openamlogin?redirect=somewhere&context=somecontext")
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is p Then redirect to adeo`() {
        loginController.fasitEnvironmentName = "p"
        loginController.navDomain = "nais.adeo.no"

        loginController.login(req, resp, "somewhereelse", "somecontext")

        verify(resp).sendRedirect("https://${loginController.appName}.nais.adeo.no/openamlogin?redirect=somewhereelse&context=somecontext")
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is preprod Then redirect to preprod`() {
        loginController.fasitEnvironmentName = "t8"
        loginController.navDomain = "nais.preprod.local"

        loginController.login(req, resp, "somewhereelse", "somecontext")

        verify(resp).sendRedirect("https://${loginController.appName}-t8.nais.preprod.local/openamlogin?redirect=somewhereelse&context=somecontext")
    }


}