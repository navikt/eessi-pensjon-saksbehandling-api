package no.nav.eessi.pensjon.services.varsel

import com.nhaarman.mockitokotlin2.doNothing
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.whenever
import org.codehaus.jackson.map.ObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.jms.JmsSecurityException
import javax.jms.JMSSecurityException

class VarselServiceTest : VarselBaseTest() {

    val mapper = ObjectMapper()

    @AfterEach
    fun cleanUpTest() {
        Mockito.reset(jmsTemplate)
        Mockito.reset(navRegistreService)
    }

    @Test
    fun `Calling varselService|sendVarsel returns OK`() {

        val personIdentifikator = "12345678910"
        val saksId = "123"
        val varseltype = "EessiPenVarsleBrukerUfore"
        val navn = "testNavn"

        doNothing().whenever(jmsTemplate).convertAndSend(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object::class.java))
        doReturn(navn).whenever(navRegistreService).hentPersoninformasjonNavn(personIdentifikator)

        val generatedResponse = varselService.sendVarsel(personIdentifikator, saksId, varseltype)

        assertEquals(Unit, generatedResponse)

        val list = s3storageService.list(personIdentifikator)
        assertEquals(2, list.size)

        val sentVarsel = s3storageService.get(list[0])

        val response = mapper.readTree(sentVarsel)

        assertEquals("Ny elektronisk løsning fra NAV", response.get("tittel").textValue)
        assertEquals(navn, response.get("fulltnavn").textValue)
        assertEquals(varseltype, response.get("varseltype").textValue)
        assertNull(response.get("parametere").textValue)
    }

    @Test
    fun `Calling varselService|sendVarsel returns error`() {

        val aktoerId = "12345678910"
        val saksId = "123"
        val varseltype = "EessiPenVarsleBrukerUfore"

        val reason = "reason"
        val errorcode = "errorcode"
        val e = JmsSecurityException(JMSSecurityException(reason, errorcode))

        doThrow(e).whenever(jmsTemplate).convertAndSend(ArgumentMatchers.anyString(), ArgumentMatchers.any(Object::class.java))

        val exception = assertThrows<VarselServiceException> {
            varselService.sendVarsel(aktoerId, saksId, varseltype)
        }
        assertEquals(exception.message, "Kunne ikke sende varselet til varseltjenesten: $reason; nested exception is ${JMSSecurityException::class.java.toString().replace("class ", "")}: $reason")
    }
}
