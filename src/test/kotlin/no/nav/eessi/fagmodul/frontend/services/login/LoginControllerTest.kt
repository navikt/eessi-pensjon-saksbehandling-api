package no.nav.eessi.fagmodul.frontend.services.login

import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Spy
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

@RunWith(MockitoJUnitRunner::class)
class FssLoginControllerTest {

    @Spy
    lateinit var resp : MockHttpServletResponse

    @Mock
    lateinit var req : MockHttpServletRequest

    lateinit var fssLoginController: LoginController

    @Before
    fun before(){
        fssLoginController = LoginController()
        fssLoginController.appName = "eessi-pensjon-frontend-api-fss"
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is q1 Then redirect to fss without namespace`() {
        fssLoginController.fasitEnvironmentName = "q1"
        fssLoginController.navDomain = "domain"

        fssLoginController.login(req, resp, "somewhere", "somecontext")


        verify(resp).sendRedirect("https://eessi-pensjon-frontend-api-fss-q1.domain/openamlogin?redirect=somewhere&context=somecontext")
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is q2 Then redirect to fss without namespace`() {
        fssLoginController.fasitEnvironmentName = "q2"
        fssLoginController.navDomain = "domain"

        fssLoginController.login(req, resp, "somewhere", "somecontext")

        verify(resp).sendRedirect("https://eessi-pensjon-frontend-api-fss-q2.domain/openamlogin?redirect=somewhere&context=somecontext")
    }


    @Test
    fun `Given a login attempt in FSS zone When environment is p Then redirect to adeo`() {
        fssLoginController.fasitEnvironmentName = "p"
        fssLoginController.navDomain = "nais.adeo.no"

        fssLoginController.login(req, resp, "somewhereelse", "somecontext")

        verify(resp).sendRedirect("https://${fssLoginController.appName}.nais.adeo.no/openamlogin?redirect=somewhereelse&context=somecontext")
    }

    @Test
    fun `Given a login attempt in FSS zone When environment is preprod Then redirect to preprod`() {
        fssLoginController.fasitEnvironmentName = "t8"
        fssLoginController.navDomain = "nais.preprod.local"

        fssLoginController.login(req, resp, "somewhereelse", "somecontext")

        verify(resp).sendRedirect("https://${fssLoginController.appName}-t8.nais.preprod.local/openamlogin?redirect=somewhereelse&context=somecontext")
    }
}
