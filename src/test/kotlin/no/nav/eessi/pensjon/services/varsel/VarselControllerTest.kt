package no.nav.eessi.pensjon.services.varsel

import com.nhaarman.mockitokotlin2.*
import no.nav.eessi.pensjon.utils.successBody
import org.codehaus.jackson.map.ObjectMapper
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class VarselControllerTest : VarselBaseTest() {

    @After
    fun cleanUpTest() {
        Mockito.reset(varselService)
    }

    @Test
    fun `Calling VarselController|sendVarsel returns OK`() {

        val aktoerId = "12345678910"
        val saksId = "123"

        doNothing().whenever(varselService).sendVarsel(aktoerId, saksId, "EessiPenVarsleBrukerUfore")
        val expectedResponse = ResponseEntity(successBody(), HttpStatus.OK)
        val generatedResponse = varselController.sendVarsel(aktoerId, saksId)
        Assert.assertEquals(expectedResponse, generatedResponse)
    }

    @Test
    fun `Calling VarselController|sendVarsel returns error`() {

        val aktoerId = "12345678910"
        val saksId = "123"
        val e = VarselServiceException("Kunne ikke sende varselet til varseltjenesten: message")

        doThrow(e).whenever(varselService).sendVarsel(aktoerId, saksId, "EessiPenVarsleBrukerUfore")

        val generatedResponse = varselController.sendVarsel(aktoerId, saksId)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(e.message, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Gitt et fnr i aktoerId feltet saa send varsel direkte`() {
        val fnr = "12345678912"
        val sakId = "123"

        varselController.sendVarsel(fnr, sakId)

        verify(varselService).sendVarsel(fnr, sakId,"EessiPenVarsleBrukerUfore")
        verify(aktoerregisterService, times(0)).hentGjeldendeNorskIdentForAktorId(fnr)
    }

    @Test
    fun `Gitt et aktoerId i aktoerId feltet saa hent fnr deretter send varsel`() {
        val fnr = "12345678912"
        val aktoerId = "123456789123"
        val sakId = "123"

        doReturn(fnr).`when`(aktoerregisterService).hentGjeldendeNorskIdentForAktorId(aktoerId)

        varselController.sendVarsel(aktoerId, sakId)

        verify(varselService).sendVarsel(fnr, sakId, "EessiPenVarsleBrukerUfore")
        verify(aktoerregisterService, times(1)).hentGjeldendeNorskIdentForAktorId(aktoerId)
    }
}
