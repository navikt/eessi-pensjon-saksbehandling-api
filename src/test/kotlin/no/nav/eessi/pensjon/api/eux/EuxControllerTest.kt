package no.nav.eessi.pensjon.api.eux

import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.eessi.pensjon.eux.EuxService
import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.eux.model.buc.Institusjon
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class EuxControllerTest {

    private val mockService = mockk<EuxService>()

    private val euxController = EuxController(mockService)

    @AfterEach
    fun afterEach() {
        confirmVerified(mockService)
        clearMocks(mockService)
    }

    @Test
    fun `Calling euxController|getRinaURL handler returns correct URL`() {
        euxController.environmentName = "q2"
        euxController.rinaUrl = "localhost"
        val generatedResponse = euxController.getRinaURL()
        assertEquals(generatedResponse.body?.get("rinaUrl") as String, "https://localhost/portal/#/caseManagement/")

        euxController.environmentName = "q1"
        val generatedResponseQ1 = euxController.getRinaURL()
        assertEquals(generatedResponseQ1.body?.get("rinaUrl") as String, "https://localhost/portal_new/case-management/")

        verify(exactly = 0) { mockService.hentInstitusjoner(any(), any()) }
    }

    @Test
    fun `Calling euxController|getInstitutions returns institution list`() {

        val buctype = BucType.P_BUC_01
        val list = listOf(
            Institusjon("1", landkode = "NO"),
            Institusjon("2", landkode = "PL"),
            Institusjon("3", landkode = "IT")
        )

        every { mockService.hentInstitusjoner(buctype, any()) } returns list

        val result = euxController.getInstitutionsWithCountry(BucType.P_BUC_01)

        assertEquals(list.toJson(), result.body)

        verify(exactly = 1) { mockService.hentInstitusjoner(any(), any()) }
    }

    @Test
    fun `getPaakobledeland skal returnere liste med land`() {

        val bucType = BucType.P_BUC_01
        val list = listOf(
            Institusjon("1", landkode = "NO"),
            Institusjon("2", landkode = "PL"),
            Institusjon("3", landkode = "IT")
        )

        every { mockService.hentInstitusjoner(bucType) } returns list

        val json = euxController.getPaakobledeland(bucType).body!!
        val result = mapJsonToAny(json, typeRefs<List<String>>())

        assertEquals(listOf("NO", "PL", "IT"), result)

        verify(exactly = 1) { mockService.hentInstitusjoner(any(), any()) }
    }

    @Test
    fun `Calling euxController|getSubjectArea returns subject areas`() {

        val expectedResponse = listOf("Pensjon")
        val generatedResponse = euxController.getSubjectArea()
        assertEquals(generatedResponse, expectedResponse)

        verify(exactly = 0) { mockService.hentInstitusjoner(any(), any()) }
    }
}
