package no.nav.eessi.pensjon.api.eux

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.eux.EuxBaseTest
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class EuxControllerTest : EuxBaseTest() {

    @AfterEach fun cleanUpTest() {
        Mockito.reset(euxService)
    }

    @Test
    fun `Calling euxController|getRinaURL handler returns correct URL`() {
        euxController.rinaUrl = "localhost"
        val generatedResponse = euxController.getRinaURL()
        assertEquals(generatedResponse.body?.get("rinaUrl") as String, "https://localhost/portal/#/caseManagement/")
    }

    @Test
    fun `Calling euxController|getInstitutions returns institution list`() {

        val buctype = "P_BUC_01"
        val list = listOf("INST1", "INST2", "INST3")
        val mockResponse = ResponseEntity.status(HttpStatus.OK).body(mapAnyToJson(list))

        doReturn(mockResponse).whenever(euxService).getInstitusjoner(buctype,"")

        val generatedResponse = euxController.getInstitutionsWithCountry("P_BUC_01")
        assertEquals(generatedResponse, mockResponse)
    }

    @Test
    fun `Calling euxController|getInstitutionsWithCountry returns filtered institution list`() {

        val bucType = "P_BUC_01"
        val expectedResponse = ResponseEntity.ok().body(mapAnyToJson(listOf("INST1", "INST2")))

        doReturn(expectedResponse).whenever(euxService).getInstitusjoner(bucType, "NO")

        val generatedResponse = euxController.getInstitutionsWithCountry(bucType, "NO")

        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling euxController|getCountryCode returns country codes`() {

        val expectedResponse = listOf("NO", "SE", "DK", "FI")
        val generatedResponse = euxController.getCountryCode()
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling euxController|getSubjectArea returns subject areas`() {

        val expectedResponse = listOf("Pensjon")
        val generatedResponse = euxController.getSubjectArea()
        assertEquals(generatedResponse, expectedResponse)
    }
}
