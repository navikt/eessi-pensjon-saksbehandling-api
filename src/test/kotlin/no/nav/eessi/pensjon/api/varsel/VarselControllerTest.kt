package no.nav.eessi.pensjon.api.varsel

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.varsel.VarselBaseTest
import no.nav.eessi.pensjon.services.varsel.VarselServiceException
import no.nav.eessi.pensjon.utils.successBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class VarselControllerTest : VarselBaseTest() {

    @AfterEach
    fun cleanUpTest() {
        Mockito.reset(varselService)
    }

    @Test
    fun `Calling VarselController|sendVarsel returns OK`() {

        val aktoerId = "1234567891012"
        val saksId = "123"
        doReturn(aktoerId).`when`(aktoerregisterService).hentGjeldendeNorskIdentForAktorId(aktoerId)
        doNothing().whenever(varselService).sendVarsel(aktoerId, saksId, "EessiPenVarsleBrukerUfore")
        val expectedResponse = ResponseEntity(successBody(), HttpStatus.OK)
        val generatedResponse = varselController.sendVarsel(aktoerId, saksId)
        assertEquals(expectedResponse, generatedResponse)
    }

    @Test
    fun `Calling VarselController|sendVarsel returns error`() {

        val aktoerId = "1234567891012"
        val fnr = "12345678912"
        val saksId = "123"
        val e = VarselServiceException("Kunne ikke sende varselet til varseltjenesten: message")

        doReturn(fnr).`when`(aktoerregisterService).hentGjeldendeNorskIdentForAktorId(aktoerId)
        doThrow(e).whenever(varselService).sendVarsel(fnr, saksId, "EessiPenVarsleBrukerUfore")

        val generatedResponse = varselController.sendVarsel(aktoerId, saksId)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, generatedResponse.statusCode)
        assertEquals(false, generatedBody.get("success").booleanValue())
        assertEquals(e.message, generatedBody.get("error").textValue())
        assertTrue(generatedBody.get("uuid").textValue().matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
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
