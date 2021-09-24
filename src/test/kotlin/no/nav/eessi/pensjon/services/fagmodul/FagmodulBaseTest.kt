package no.nav.eessi.pensjon.services.fagmodul

import no.nav.eessi.pensjon.services.BaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate

open class FagmodulBaseTest : BaseTest() {

    lateinit var mockFagmodulRestTemplate: RestTemplate


    @BeforeEach
    fun _init() {
        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()
    }

    @Test
    fun _dummy() {}
}
