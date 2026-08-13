package no.nav.eessi.pensjon.api.storage

import com.google.cloud.storage.StorageException
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.api.FrontEndResponse
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StorageControllerTest {

    private lateinit var gcpStorageService: GcpStorageService
    private lateinit var storageController: StorageController

    @BeforeEach
    fun setUp() {
        gcpStorageService = mockk(relaxed = true)
        storageController = StorageController(gcpStorageService, MetricsHelper.ForTest())
    }

    // storeDocument

    @Test
    fun `storeDocument happy path returns 200 with status OK`() {
        every { gcpStorageService.lagre(any(), any()) } returns Unit

        val response = storageController.storeDocument("12345678901___bucs", """{"key":"value"}""")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(HttpStatus.OK.name, response.body?.status)
        assertEquals(true, response.body?.result)
    }

    @Test
    fun `storeDocument StorageException preserves GCP HTTP status`() {
        val gcpEx = StorageException(403, "Forbidden")
        every { gcpStorageService.lagre(any(), any()) } throws gcpEx

        val response = storageController.storeDocument("12345678901___bucs", """{}""")

        assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        assertEquals(HttpStatus.FORBIDDEN.name, response.body?.status)
        assertEquals("Forbidden", response.body?.message)
    }

    @Test
    fun `storeDocument generic exception returns 500`() {
        every { gcpStorageService.lagre(any(), any()) } throws RuntimeException("boom")

        val response = storageController.storeDocument("12345678901___bucs", """{}""")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name, response.body?.status)
        assertEquals("Klarte ikke å lagre s3 dokumenter", response.body?.message)
    }

    // getDocument

    @Test
    fun `getDocument happy path returns 200 with parsed JSON as result`() {
        every { gcpStorageService.hent(any()) } returns """{"rinaSakId":"123","bucs":[]}"""

        val response = storageController.getDocument("12345678901___bucs")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(HttpStatus.OK.name, response.body?.status)
        val result = response.body?.result
        assertEquals("123", result?.get("rinaSakId")?.asText())
    }

    @Test
    fun `getDocument StorageException preserves GCP HTTP status`() {
        val gcpEx = StorageException(404, "Not Found")
        every { gcpStorageService.hent(any()) } throws gcpEx

        val response = storageController.getDocument("12345678901___bucs")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertEquals(HttpStatus.NOT_FOUND.name, response.body?.status)
        assertEquals("Not Found", response.body?.message)
    }

    @Test
    fun `getDocument generic exception returns 500`() {
        every { gcpStorageService.hent(any()) } throws RuntimeException("oops")

        val response = storageController.getDocument("12345678901___bucs")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name, response.body?.status)
        assertEquals("Klarte ikke å hente s3 dokument", response.body?.message)
    }

    // listDocuments

    @Test
    fun `listDocuments happy path returns 200 with list in result`() {
        every { gcpStorageService.list(any()) } returns listOf("12345678901___bucs", "12345678901___p5000")

        val response = storageController.listDocuments("12345678901")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(HttpStatus.OK.name, response.body?.status)
        assertEquals(listOf("12345678901___bucs", "12345678901___p5000"), response.body?.result)
    }

    @Test
    fun `listDocuments StorageException preserves GCP HTTP status`() {
        val gcpEx = StorageException(503, "Service Unavailable")
        every { gcpStorageService.list(any()) } throws gcpEx

        val response = storageController.listDocuments("12345678901")

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.statusCode)
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE.name, response.body?.status)
        assertNull(response.body?.result)
    }

    @Test
    fun `listDocuments generic exception returns 500`() {
        every { gcpStorageService.list(any()) } throws RuntimeException("fail")

        val response = storageController.listDocuments("12345678901")

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.name, response.body?.status)
        assertEquals("Klarte ikke å liste s3 dokumenter", response.body?.message)
    }

    @Test
    fun `deleteDocument happy path returns deletion result`() {
        every { gcpStorageService.slett(any()) } returns true

        val response = storageController.deleteDocument("12345678901___bucs")

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(HttpStatus.OK.name, response.body?.status)
        assertEquals(true, response.body?.result)
    }
}
