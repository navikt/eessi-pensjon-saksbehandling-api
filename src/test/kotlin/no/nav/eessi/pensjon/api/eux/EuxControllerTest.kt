package no.nav.eessi.pensjon.api.eux

import com.fasterxml.jackson.core.type.TypeReference
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.eux.EuxBaseTest
import no.nav.eessi.pensjon.services.eux.RinaAksjon
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toResponse
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class EuxControllerTest : EuxBaseTest() {

    val rinaAksjonTypeRef = object : TypeReference<List<RinaAksjon>>() {}

    @After fun cleanUpTest() {
        Mockito.reset(euxService);
    }

    @Test
    fun `Calling euxController|getRinaURL handler returns correct URL`() {
        euxController.rinaUrl = "localhost"
        var generatedResponse = euxController.getRinaURL()
        assertEquals(generatedResponse.body?.get("rinaUrl") as String, "https://localhost/portal/#/caseManagement/")
    }

    @Test
    fun `Calling euxController|validateCaseNumberWithRinaID handler with valid params returns 2XX response`() {

        val caseid = "123"
        val aktorid = "456"
        val rinaid = "789"

        var generatedResponse = euxController.validateCaseNumberWithRinaID(caseid, aktorid, rinaid)
        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body!!.get("casenumber"), caseid)
        assertEquals(generatedResponse.body!!.get("pinid"), aktorid)
        assertEquals(generatedResponse.body!!.get("rinaid"), rinaid)
    }

    @Test
    fun `Calling euxController|validateCaseNumberWithRinaID handler with invalid params returns 4XX response`() {
        val caseid = "not"
        val aktorid = "valid"
        val rinaid = "values"

        var generatedResponse = euxController.validateCaseNumberWithRinaID(caseid, aktorid, rinaid)
        assertTrue(generatedResponse.statusCode.is4xxClientError)
        assertEquals(generatedResponse.body?.get("serverMessage") as String, "invalidCase")
    }

    @Test
    fun `Calling euxController|validateCaseNumber handler with valid params returns 2XX response`() {
        val caseid = "123"
        val aktorid = "456"

        val generatedResponse = euxController.validateCaseNumber(caseid, aktorid)
        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(generatedResponse.body!!.get("casenumber"), caseid)
        assertEquals(generatedResponse.body!!.get("pinid"), aktorid)
    }

    @Test
    fun `Calling euxController|validateCaseNumber handler with invalid params returns 4XX response`() {
        val caseid = "not"
        val aktorid = "valid"

        val generatedResponse = euxController.validateCaseNumber(caseid, aktorid)
        assertTrue(generatedResponse.statusCode.is4xxClientError)
        assertEquals(generatedResponse.body?.get("serverMessage") as String, "invalidCase")
    }

    @Test
    fun `Calling euxController|getBucs returns list of BUCs`() {

        val expectedResponse = listOf("P_BUC_01", "P_BUC_02", "P_BUC_03", "P_BUC_05", "P_BUC_06")
        val generatedResponse = euxController.getBucs()
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling euxController|getSeds returns SEDs for a given BUC`() {

        val buc = "P_BUC_01"
        val rinanr = null

        val expectedResponse = ResponseEntity.ok().body(mapAnyToJson(listOf("P2000")))
        val generatedResponse = euxController.getSeds(buc, rinanr)

        assertEquals(generatedResponse, expectedResponse)
    }

      @Test
      fun euxController_getSeds_returnsSEDsgivenBUC() {
          val buc = "P_BUC_01"
          val rinanr = "1000101"

          val bucResponse =  ResponseEntity.ok().body(mapAnyToJson(listOf("P6000","X6000")))
          doReturn(bucResponse).whenever(bucController).getMuligeAksjoner(rinanr)

          val expectedResponse = listOf("P6000").toResponse()

          val generatedResponse = euxController.getSeds(buc, rinanr)

          assertEquals(expectedResponse, generatedResponse)

          val json = generatedResponse.body!!
          val validSedListforBuc = mapJsonToAny(json, typeRefs<List<String>>())
          assertEquals(1, validSedListforBuc.size)
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
        //val expectedResponse = listOf("NO")
        val generatedResponse = euxController.getCountryCode()
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling euxController|getSubjectArea returns subject areas`() {

        val expectedResponse = listOf("Pensjon", "Andre")
        val generatedResponse = euxController.getSubjectArea()
        assertEquals(generatedResponse, expectedResponse)
    }


    private fun getAksjonlist(): List<RinaAksjon> {
        return listOf(
                RinaAksjon(
                        navn = "Create",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "P6000",
                        dokumentId = "213123123"
                ),
                RinaAksjon(
                        navn = "Create",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "X6000",
                        dokumentId = "213123123"
                ),
                RinaAksjon(
                        navn = "Update",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "X200",
                        dokumentId = "213123123"
                )
        )
    }


}
