package no.nav.eessi.fagmodul.frontend.services.varsel

import com.nhaarman.mockito_kotlin.doNothing
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import org.codehaus.jackson.map.ObjectMapper
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.jms.JmsSecurityException
import javax.jms.JMSSecurityException

class VarselServiceTest : VarselBaseTest() {

    val mapper = ObjectMapper()

    @After
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

        Assert.assertEquals(Unit, generatedResponse)

        val list = s3storageService.list(personIdentifikator)
        Assert.assertEquals(2, list.size)

        val sentVarsel = s3storageService.get(list[0])

        val response = mapper.readTree(sentVarsel)

        Assert.assertEquals("Ny elektronisk løsning fra NAV", response.get("tittel").textValue)
        Assert.assertEquals(navn, response.get("fulltnavn").textValue)
        Assert.assertEquals(varseltype, response.get("varseltype").textValue)
        Assert.assertNull(response.get("parametere").textValue)
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

        try {
            varselService.sendVarsel(aktoerId, saksId, varseltype)
        } catch (e: Exception) {
            Assert.assertThat(e, instanceOf(VarselServiceException::class.java))
            Assert.assertEquals(e.message, "Kunne ikke sende varselet til varseltjenesten: $reason; nested exception is ${JMSSecurityException::class.java.toString().replace("class ", "")}: $reason")
        }
    }
}