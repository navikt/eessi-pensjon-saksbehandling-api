package no.nav.eessi.pensjon.services.eux

import no.nav.eessi.pensjon.api.eux.EuxController
import no.nav.eessi.pensjon.services.BaseTest
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.DefaultResponseErrorHandler
import org.springframework.web.client.RestTemplate


open class EuxBaseTest : BaseTest() {
    @Value("\${EUXBASIS_V1_URL:http://localhost:9090/cpi}")
    lateinit var euxUrl: String

    lateinit var mockEuxRestTemplate: RestTemplate
    lateinit var mockFagmodulRestTemplate: RestTemplate
    lateinit var euxController : EuxController
    lateinit var euxService: EuxService
    lateinit var navRegistreService: NavRegistreOppslagService

    @BeforeEach
    fun _init() {

        fun generateMockEuxRestTemplate(): RestTemplate {
            val euxRestTemplate = RestTemplateBuilder()
                    .rootUri(euxUrl)
                    .errorHandler(DefaultResponseErrorHandler())
                    .additionalInterceptors()
                    .build()
            return Mockito.spy(euxRestTemplate)
        }

        mockEuxRestTemplate = generateMockEuxRestTemplate()

        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()
        navRegistreService = Mockito.spy(NavRegistreOppslagService(mockFagmodulRestTemplate))
        euxService = Mockito.spy(EuxService(mockEuxRestTemplate))
        euxController = Mockito.spy(EuxController(euxService, navRegistreService))
    }
}
