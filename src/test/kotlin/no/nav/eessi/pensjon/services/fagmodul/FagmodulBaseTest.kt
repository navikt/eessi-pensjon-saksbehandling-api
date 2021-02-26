package no.nav.eessi.pensjon.services.fagmodul

import no.nav.eessi.pensjon.api.pensjon.PensjonController
import no.nav.eessi.pensjon.services.BaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.web.client.RestTemplate

open class FagmodulBaseTest : BaseTest() {

    lateinit var mockFagmodulRestTemplate: RestTemplate
    lateinit var pensjonController: PensjonController


    @BeforeEach
    fun _init() {
        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()
        pensjonController = Mockito.spy(PensjonController(mockFagmodulRestTemplate))
    }

    @Test
    fun _dummy() {}
}
