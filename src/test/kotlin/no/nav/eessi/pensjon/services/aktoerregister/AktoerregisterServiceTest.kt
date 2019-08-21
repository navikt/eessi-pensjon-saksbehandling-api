package no.nav.eessi.pensjon.services.aktoerregister

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@RunWith(MockitoJUnitRunner::class)
class AktoerregisterServiceTest {

    @Mock
    private lateinit var mockrestTemplate: RestTemplate

    lateinit var aktoerregisterService: AktoerregisterService

    @Before
    fun setup() {
        aktoerregisterService = AktoerregisterService(mockrestTemplate)
        aktoerregisterService.appName = "unittests"
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 NorskIdent`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"
        val expectedNorskIdent = "18128126178"

        val response = aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        assertEquals("AktørId 1000101917358 har norskidenten 18128126178", expectedNorskIdent, response)
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 AktoerId`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-NorskIdent.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "18128126178"
        val expectedNorskIdent = "1000101917358"

        val response = aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        assertEquals("NorskIdent 18128126178 skal ha AktoerId 100010191735818128126178", expectedNorskIdent, response)
    }


    @Test(expected = AktoerregisterIkkeFunnetException::class)
    fun `hentGjeldendeNorskIdentForAktorId() should fail if ident is not found in response`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1234"
        try {
            // the mock returns NorskIdent 18128126178, not 1234 as we asked for
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        } catch (rte: RuntimeException) {
            assertTrue("Exception skal si noe om hvilken identen som ikke ble funnet", rte.message!!.contains(testAktoerId))
            throw rte
        }
    }

    @Test(expected = AktoerregisterIkkeFunnetException::class)
    fun `should throw runtimeexception if no ident is found in response`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_0-IdentinfoForAktoer.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "18128126178"
        try {
            // the mock returns a valid response, but has no idents
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        } catch (rte: RuntimeException) {
            assertTrue("Exception skal si noe om hvilken identen som ikke ble funnet", rte.message!!.contains(testAktoerId))
            throw rte
        }
    }


    @Test(expected = AktoerregisterException::class)
    fun `AktoerregisterException should be thrown when response contains a 'feilmelding'`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-errormsg.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "10000609641830456"
        try {
            // the mock returns a valid response, but with a message in 'feilmelding'
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        } catch (are: AktoerregisterException) {
            assertEquals("Feilmeldingen fra aktørregisteret skal være exception-message", "Den angitte personidenten finnes ikke", are.message!!)
            throw are
        }
    }

    @Test(expected = AktoerregisterException::class)
    fun `should throw runtimeexception when multiple idents are returned`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-2-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"
        try {
            // the mock returns a valid response, but has 2 gjeldende AktoerId
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        } catch (rte: RuntimeException) {
            assertEquals("RuntimeException skal kastes dersom mer enn 1 ident returneres", "Forventet 1 ident, fant 2", rte.message!!)
            throw rte
        }
    }

    @Test(expected = AktoerregisterException::class)
    fun `should throw runtimeexception when 403-forbidden is returned`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/403-Forbidden.json", HttpStatus.FORBIDDEN)
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "does-not-matter"
        try {
            // the mock returns 403-forbidden
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        } catch (rte: RuntimeException) {
            assertEquals("RuntimeException skal kastes dersom mer enn 1 ident returneres", "Received 403 Forbidden from aktørregisteret", rte.message!!)
            throw rte
        }
    }

    private fun createResponseEntityFromJsonFile(filePath: String, httpStatus: HttpStatus = HttpStatus.OK): ResponseEntity<String> {
        val mockResponseString = String(Files.readAllBytes(Paths.get(filePath)))
        return ResponseEntity(mockResponseString, httpStatus)
    }
}
