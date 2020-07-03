package no.nav.eessi.pensjon.personoppslag.aktoerregister

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doThrow
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(MockitoExtension::class)
class AktoerregisterServiceTest {

    @Mock
    private lateinit var mockrestTemplate: RestTemplate

    lateinit var aktoerregisterService: AktoerregisterService

    @BeforeEach
    fun setup() {
        aktoerregisterService = AktoerregisterService(mockrestTemplate)
        aktoerregisterService.appName = "unittests"
        aktoerregisterService.initMetrics()
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 NorskIdent`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"
        val expectedNorskIdent = "18128126178"

        val response = aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        assertEquals(expectedNorskIdent, response, "AktørId 1000101917358 har norskidenten 18128126178")
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should return 1 AktoerId`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-NorskIdent.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "18128126178"
        val expectedNorskIdent = "1000101917358"

        val response = aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        assertEquals(expectedNorskIdent, response, "NorskIdent 18128126178 skal ha AktoerId 100010191735818128126178")
    }


    @Test
    fun `hentGjeldendeNorskIdentForAktorId() should fail if ident is not found in response`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-1-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1234"

        val are = assertThrows<AktoerregisterIkkeFunnetException> {
            // the mock returns NorskIdent 18128126178, not 1234 as we asked for
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertTrue(are.message!!.contains(testAktoerId), "Exception skal si noe om hvilken identen som ikke ble funnet")
    }

    @Test
    fun `should throw runtimeexception if no ident is found in response`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_0-IdentinfoForAktoer.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "18128126178"

        val rte = assertThrows<AktoerregisterIkkeFunnetException> {
            // the mock returns a valid response, but has no idents
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertTrue(rte.message!!.contains(testAktoerId), "Exception skal si noe om hvilken identen som ikke ble funnet")

    }


    @Test
    fun `AktoerregisterException should be thrown when response contains a 'feilmelding'`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-errormsg.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "10000609641830456"

        val are = assertThrows<AktoerregisterException> {
            // the mock returns a valid response, but with a message in 'feilmelding'
            aktoerregisterService.hentGjeldendeNorskIdentForAktorId(testAktoerId)
        }
        assertEquals("Den angitte personidenten finnes ikke", are.message!!, "Feilmeldingen fra aktørregisteret skal være exception-message")
    }

    @Test
    fun `should throw runtimeexception when multiple idents are returned`() {
        val mockResponseEntity = createResponseEntityFromJsonFile("src/test/resources/json/aktoerregister/200-OK_1-IdentinfoForAktoer-with-2-gjeldende-AktoerId.json")
        whenever(mockrestTemplate.exchange(any<String>(), any(), any<HttpEntity<Unit>>(), ArgumentMatchers.eq(String::class.java))).thenReturn(mockResponseEntity)

        val testAktoerId = "1000101917358"

        val are = assertThrows<AktoerregisterException> {
            // the mock returns a valid response, but has 2 gjeldende AktoerId
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        }

        assertEquals("Forventet 1 ident, fant 2", are.message!!, "RuntimeException skal kastes dersom mer enn 1 ident returneres")
    }

    @Test
    fun `should throw runtimeexception when 403-forbidden is returned`() {
        doThrow(HttpClientErrorException(HttpStatus.FORBIDDEN, "bad request")).whenever(mockrestTemplate).exchange(
            eq("/identer?identgruppe=AktoerId&gjeldende=true"),
            eq(HttpMethod.GET),
            any(),
            eq(String::class.java)
        )
        val testAktoerId = "does-not-matter"

        val are = assertThrows<RuntimeException> {
            aktoerregisterService.hentGjeldendeAktorIdForNorskIdent(testAktoerId)
        }
        assertEquals("403 bad request", are.message!!, "RuntimeException skal kastes dersom mer enn 1 ident returneres")
    }

    private fun createResponseEntityFromJsonFile(filePath: String, httpStatus: HttpStatus = HttpStatus.OK): ResponseEntity<String> {
        val mockResponseString = String(Files.readAllBytes(Paths.get(filePath)))
        return ResponseEntity(mockResponseString, httpStatus)
    }
}
