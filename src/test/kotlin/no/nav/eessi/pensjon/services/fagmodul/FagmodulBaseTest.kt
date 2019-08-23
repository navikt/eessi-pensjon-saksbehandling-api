package no.nav.eessi.pensjon.services.fagmodul

import no.nav.eessi.pensjon.api.fagmodul.BucController
import no.nav.eessi.pensjon.api.fagmodul.SedController
import no.nav.eessi.pensjon.api.pensjon.PensjonController
import no.nav.eessi.pensjon.services.BaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.client.RestTemplate

open class FagmodulBaseTest : BaseTest() {

    lateinit var mockFagmodulRestTemplate: RestTemplate
    lateinit var sedController : SedController
    lateinit var navRegistreOppslagService: NavRegistreOppslagService
    lateinit var bucController: BucController
    lateinit var pensjonController: PensjonController


    @BeforeEach
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
