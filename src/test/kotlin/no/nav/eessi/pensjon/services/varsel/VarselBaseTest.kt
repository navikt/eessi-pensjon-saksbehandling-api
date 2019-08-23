package no.nav.eessi.pensjon.services.varsel

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.eessi.pensjon.api.varsel.VarselController
import no.nav.eessi.pensjon.services.aktoerregister.AktoerregisterService
import no.nav.eessi.pensjon.services.fagmodul.NavRegistreOppslagService
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.springframework.jms.core.JmsTemplate
import org.springframework.web.client.RestTemplate

open class VarselBaseTest : S3StorageBaseTest() {

    lateinit var varselController : VarselController
    lateinit var varselService : VarselService
    lateinit var jmsTemplate : JmsTemplate
    lateinit var navRegistreService: NavRegistreOppslagService
    lateinit var mockFagmodulRestTemplate : RestTemplate
    lateinit var aktoerregisterService: AktoerregisterService

    @BeforeEach
    fun _setup() {

        mockFagmodulRestTemplate = generateMockFagmodulRestTemplate()
        navRegistreService = Mockito.spy(NavRegistreOppslagService(mockFagmodulRestTemplate))
        jmsTemplate = Mockito.spy(JmsTemplate())
        aktoerregisterService = mock(AktoerregisterService::class.java)


        varselService = Mockito.spy(VarselService(
                jmsTemplate,
                s3storageService,
                navRegistreService,
                whitelistService,
                ObjectMapper()
        ))

        varselService.frontendUrl = "http://localhost:3000"
        varselService.varselQueue = "TEST.SEND_VARSEL"
        varselService.varselTittel = "Ny elektronisk løsning fra NAV"

        varselController = Mockito.spy(VarselController(varselService, aktoerregisterService))
    }

    @Test
    fun __dummy() {}
}
