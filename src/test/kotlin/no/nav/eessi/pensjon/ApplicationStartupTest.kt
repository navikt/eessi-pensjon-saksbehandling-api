package no.nav.eessi.pensjon

import com.ninjasquad.springmockk.MockkBean
import io.getunleash.Unleash
import no.nav.eessi.pensjon.config.ApiMvcConfig
import no.nav.eessi.pensjon.config.UnleashConfigEessi
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.interceptor.AuthInterceptor
import no.nav.eessi.pensjon.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.unleash.FeatureToggleService
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc


@AutoConfigureMockMvc
@SpringBootTest(classes = [Application::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableMockOAuth2Server
@MockkBean(types = [Unleash::class], relaxed = true)
@MockkBean(types = [ApiMvcConfig::class], relaxed = true)
@MockkBean(types = [AuthInterceptor::class], relaxed = true)
@MockkBean(types = [GcpStorageService::class], relaxed = true)
@MockkBean(types = [UnleashConfigEessi::class], relaxed = true)
@MockkBean(types = [BrukerInformasjonService::class], relaxed = true)
@MockkBean(types = [FeatureToggleService::class], relaxed = true)
internal class ApplicationStartupTest {

    @Test
    fun `sanityCheck`() {
        // se at denne starter application uten error
    }
}