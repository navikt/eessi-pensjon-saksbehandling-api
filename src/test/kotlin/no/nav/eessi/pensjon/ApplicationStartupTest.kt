package no.nav.eessi.pensjon

import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import no.nav.eessi.pensjon.config.ApiMvcConfig
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import no.nav.eessi.pensjon.listeners.SedListener
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest


@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@MockkBeans(
    MockkBean(classes = [GcpStorageService::class], relaxed = true),
    MockkBean(classes = [ApiMvcConfig::class], relaxed = true),
    MockkBean(classes = [AuthInterceptor::class], relaxed = true),
    MockkBean(classes = [SedListener::class], relaxed = true)
)
internal class ApplicationStartupTest {

    @Test
    fun `sanityCheck`() {
        // se at denne starter application uten error
    }
}