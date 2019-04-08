package no.nav.eessi.fagmodul.frontend.services.storage

import com.amazonaws.AmazonServiceException
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import org.codehaus.jackson.map.ObjectMapper
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus

class StorageControllerTest : S3StorageBaseTest() {

    val mapper = ObjectMapper()

    @After
    fun cleanUpTest() {
        Mockito.reset(s3storageService)
    }

    @Test
    fun `Calling storageController|storeDocument returns OK response`() {

        val path = "12345678910___path___123"
        val document = "document.json"
        val expectedResponse = "{\"success\": true}"

        val generatedResponse = storageController.storeDocument(path, document)

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(expectedResponse, generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|storeDocument returns error response`() {

        val path = "12345678910___path___123"
        val document = "document.json"
        val mockError = "Klarte ikke å lagre s3 dokument"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3storageService).put(path, document)
        val generatedResponse = storageController.storeDocument(path, document)
        val generatedBody = mapper.readTree(generatedResponse.body)

        Assert.assertTrue(generatedResponse.statusCode.is5xxServerError)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling storageController|getDocument returns OK response with existing file`() {

        val path = "12345678910___path___123"
        val document = "document.json"

        // stage
        storageController.storeDocument(path, document)

        val generatedResponse = storageController.getDocument(path)

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(document, generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|getDocument returns 404 response with non-existing file`() {

        val path = "12345678910___nopath___123"

        val generatedResponse = storageController.getDocument(path)
        val generatedBody = mapper.readTree(generatedResponse.body)

        Assert.assertTrue(generatedResponse.statusCode == HttpStatus.NOT_FOUND)
        Assert.assertEquals(generatedResponse.body, errorBody("S3 dokumentet eksisterer ikke", generatedBody.get("uuid").textValue))
    }

    @Test
    fun `Calling storageController|getDocument returns error response with storage error`() {

        val path = "12345678910___nopath___123"
        val mockError  ="Klarte ikke å hente s3 dokument"
        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3storageService).get(path)
        val generatedResponse = storageController.getDocument(path)
        val generatedBody = mapper.readTree(generatedResponse.body)

        Assert.assertTrue(generatedResponse.statusCode.is5xxServerError)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

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

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(listOf(path1), generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|listDocuments with prefix on empty storage returns empty list`() {

        val prefix = "12345678910___path"

        // stage
        val generatedResponse = storageController.listDocuments(prefix)

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(listOf<String>(), generatedResponse.body!!)
    }

    @Test
    fun `Calling storageController|listDocument returns error response with storage error`() {

        val path = "12345678910___nopath___123"
        val mockError ="Klarte ikke å liste s3 dokumenter"
        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3storageService).list(path)
        val generatedResponse = storageController.listDocuments(path)
        val generatedBody = mapper.readTree(generatedResponse.body!!.get(0))

        Assert.assertTrue(generatedResponse.statusCode.is5xxServerError)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }

    @Test
    fun `Calling storageController|deleteDocument on staged storage returns OK`() {

        val path1 = "12345678910___path___123"
        val document = "document.json"
        val expectedResponse = mapper.readTree("{\"success\": true}")

        // stage
        storageController.storeDocument(path1, document)

        val generatedResponse = storageController.deleteDocument(path1)

        Assert.assertTrue(generatedResponse.statusCode.is2xxSuccessful)
        Assert.assertEquals(expectedResponse,  mapper.readTree(generatedResponse.body))

        val generatedResponse2 = storageController.listDocuments("12345678910___path")
        Assert.assertEquals(listOf<String>(), generatedResponse2.body!!)
    }

    @Test
    fun `Calling storageController|deleteDocument on empty storage returns OK`() {

        val path1 = "12345678910___path___123"
        val mockError = "Klarte ikke å slette s3 dokument"

        // stage
        val generatedResponse = storageController.deleteDocument(path1)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertTrue(generatedResponse.statusCode.is4xxClientError)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))

        val generatedResponse2 = storageController.listDocuments("12345678910___path")
        Assert.assertEquals(listOf<String>(), generatedResponse2.body!!)
    }

    @Test
    fun `Calling storageController|deleteDocument returns error response with storage error`() {

        val path1 = "12345678910___path___123"
        val mockError = "Klarte ikke å slette s3 dokument"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3storageService).delete(path1)
        val generatedResponse = storageController.deleteDocument(path1)
        val generatedBody = ObjectMapper().readTree(generatedResponse.body)

        Assert.assertTrue(generatedResponse.statusCode.is5xxServerError)
        Assert.assertEquals(false, generatedBody.get("success").booleanValue)
        Assert.assertEquals(mockError, generatedBody.get("error").textValue)
        Assert.assertTrue(generatedBody.get("uuid").textValue.matches(Regex("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}")))
    }
}