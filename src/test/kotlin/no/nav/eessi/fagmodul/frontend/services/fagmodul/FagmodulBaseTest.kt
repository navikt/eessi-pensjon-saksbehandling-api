package no.nav.eessi.fagmodul.frontend.services.fagmodul

import no.nav.eessi.fagmodul.frontend.services.BaseTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.web.client.RestTemplate

open class FagmodulBaseTest : BaseTest() {

    lateinit var mockFagmodulRestTemplate: RestTemplate
    lateinit var sedController : SedController
    lateinit var navRegistreOppslagService: NavRegistreOppslagService
    lateinit var bucController: BucController
    lateinit var pensjonController: PensjonController


    @Before
    fun _init() {

        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()
        sedController = Mockito.spy(SedController(mockFagmodulRestTemplate))
        navRegistreOppslagService = Mockito.spy(NavRegistreOppslagService(mockFagmodulRestTemplate))
        bucController = Mockito.spy(BucController(mockFagmodulRestTemplate))
        pensjonController = Mockito.spy(PensjonController(mockFagmodulRestTemplate))
    }

    @Test
    fun _dummy() {}
}