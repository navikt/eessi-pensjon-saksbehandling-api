package no.nav.eessi.pensjon.api.storage

import com.amazonaws.AmazonServiceException
import com.fasterxml.jackson.databind.ObjectMapper
import io.mockk.every
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.utils.errorBody
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class StorageControllerTest : S3StorageBaseTest() {

    private val mapper = ObjectMapper()

    @AfterEach
    fun cleanUpTest() {
    }

    @Test
    fun `Calling storageController|storeDocument returns OK response`() {

        val path = "12345678910___path___123"
        val document = "document.json"
        val expectedResponse = "{\"success\": true}"

        val generatedResponse = storageController.storeDocument(path, document)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(expectedResponse, generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|storeDocument returns error response`() {

        val path = "12345678910___path___123"
        val document = "document.json"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        every { s3storageService.put(path, document) } throws expectedError
        val generatedResponse = storageController.storeDocument(path, document)
        val generatedBody = mapper.readTree(generatedResponse.body)

        assertTrue(generatedResponse.statusCode.is5xxServerError)
        assertEquals(false, generatedBody.get("success").booleanValue())
        assertEquals("errorMessage", generatedBody.get("error").textValue())
        assertTrue(generatedBody.get("uuid").textValue().matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling storageController|getDocument returns OK response with existing file`() {

        val path = "12345678910___path___123"
        val document = "document.json"

        // stage
        storageController.storeDocument(path, document)

        val generatedResponse = storageController.getDocument(path)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(document, generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|getDocument returns 404 response with non-existing file`() {

        val path = "12345678910___nopath___123"

        val generatedResponse = storageController.getDocument(path)
        val generatedBody = mapper.readTree(generatedResponse.body)

        assertTrue(generatedResponse.statusCode == HttpStatus.NOT_FOUND)
        assertEquals(generatedResponse.body, errorBody("The resource you requested does not exist", generatedBody.get("uuid").textValue()))
    }

    @Test
    fun `Calling storageController|getDocument returns error response with storage error`() {

        val path = "12345678910___nopath___123"
        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        every {s3storageService.get(path)  } throws expectedError
        val generatedResponse = storageController.getDocument(path)
        val generatedBody = mapper.readTree(generatedResponse.body)

        assertTrue(generatedResponse.statusCode.is5xxServerError)
        assertEquals(false, generatedBody.get("success").booleanValue())
        assertEquals("errorMessage", generatedBody.get("error").textValue())
        assertTrue(generatedBody.get("uuid").textValue().matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

    }

    @Test
    fun `Calling storageController|listDocuments with matching prefix on staged storage returns non-empty list`() {

        val path1 = "12345678910___path___123"
        val path2 = "12345678910___path___123"
        val document = "document.json"
        val prefix = "12345678910___path"

        // stage
        storageController.storeDocument(path1, document)
        storageController.storeDocument(path2, document)

        val generatedResponse = storageController.listDocuments(prefix)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(listOf(path1), generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|listDocuments with prefix on empty storage returns empty list`() {

        val prefix = "12345678910___path"

        // stage
        val generatedResponse = storageController.listDocuments(prefix)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(listOf<String>(), generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|listDocument returns error response with storage error`() {

        val path = "12345678910___nopath___123"
        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        every {s3storageService.list(path)  } throws expectedError
        val generatedResponse = storageController.listDocuments(path)
        val generatedBody = mapper.readTree(generatedResponse.body!!.get(0))

        assertTrue(generatedResponse.statusCode.is5xxServerError)
        assertEquals(false, generatedBody.get("success").booleanValue())
        assertEquals("errorMessage", generatedBody.get("error").textValue())
        assertTrue(generatedBody.get("uuid").textValue().matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling storageController|deleteDocument on staged storage returns OK`() {

        val path1 = "12345678910___path___123"
        val document = "document.json"
        val expectedResponse = mapper.readTree("{\"success\": true}")

        // stage
        storageController.storeDocument(path1, document)

        val generatedResponse = storageController.deleteDocument(path1)

        assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        assertEquals(expectedResponse,  mapper.readTree(generatedResponse.body))

        val generatedResponse2 = storageController.listDocuments("12345678910___path")
        assertEquals(listOf<String>(), generatedResponse2.body!!)
    }

    @Test
    fun `Calling storageController|deleteDocument on empty storage returns OK`() {

        val path1 = "12345678910___path___123"

        // stage
        val generatedResponse = storageController.deleteDocument(path1)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertTrue(generatedResponse.statusCode.is4xxClientError)
        assertEquals(false, generatedBody.get("success").booleanValue())

        val generatedResponse2 = storageController.listDocuments("12345678910___path")
        assertEquals(listOf<String>(), generatedResponse2.body!!)
    }

    @Test
    fun `Calling storageController|deleteDocument returns error response with storage error`() {

        val path1 = "12345678910___path___123"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        every {s3storageService.delete(path1)  } throws expectedError
        val generatedResponse = storageController.deleteDocument(path1)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        assertTrue(generatedResponse.statusCode.is5xxServerError)
        assertEquals(false, generatedBody.get("success").booleanValue())
        assertEquals("errorMessage", generatedBody.get("error").textValue())
        assertTrue(generatedBody.get("uuid").textValue().matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }
}
