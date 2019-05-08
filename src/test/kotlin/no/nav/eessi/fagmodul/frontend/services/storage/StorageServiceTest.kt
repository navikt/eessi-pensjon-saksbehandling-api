package no.nav.eessi.fagmodul.frontend.services.storage

import com.amazonaws.AmazonClientException
import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.model.AmazonS3Exception
import com.amazonaws.services.s3.model.ListObjectsV2Request
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import java.time.LocalDateTime
import kotlin.test.assertEquals

class StorageServiceTest : S3StorageBaseTest() {

    @After
    fun cleanUpTest() {
        Mockito.reset(s3storageService)
    }

    @Test
    fun `add files in different directories and list them`() {
        val aktoerId1 = "12345678910"
        val p2000Directory = "P2000"
        val p4000Directory = "P4000"

        val p2000value = "Final P2000-document"
        val p4000value = "Final P4000-document"

        s3storageService.put(aktoerId1 + "___" + "$p2000Directory/${LocalDateTime.now()}/document.txt", p2000value)
        s3storageService.put(aktoerId1 + "___" + "$p4000Directory/${LocalDateTime.now()}/document.txt", p4000value)

        val fileListAktoer1 = s3storageService.list(aktoerId1)
        assertEquals(2, fileListAktoer1.size)
    }

    @Test
    fun `add multiple files and list them`() {
        val directory = "P4000"
        val aktoerId = "12345678910"

        val value1 = "First draft"
        val timestamp1 = LocalDateTime.now().minusHours(5)
        s3storageService.put(aktoerId + "___" + "$directory/$timestamp1/document.txt", value1)

        val value2 = "Second draft"
        val timestamp2 = LocalDateTime.now().minusHours(2)
        s3storageService.put(aktoerId + "___" + "$directory/$timestamp2/document.txt", value2)

        val value3 = "Final document"
        val timestamp3 = LocalDateTime.now()
        s3storageService.put(aktoerId + "___" + "$directory/$timestamp3/document.txt", value3)

        val fileList = s3storageService.list(aktoerId + "___" + directory)
        assertEquals(3, fileList.size)

        val fetchtedValue1 = s3storageService.get(fileList[0])
        val fetchtedValue2 = s3storageService.get(fileList[1])
        val fetchtedValue3 = s3storageService.get(fileList[2])

        assertEquals(value1, fetchtedValue1)
        assertEquals(value2, fetchtedValue2)
        assertEquals(value3, fetchtedValue3)
    }

    @Test
    fun `put file into bucket, list it, read it back and finally delete it`() {
        val directory = "P2000"
        val aktoerId = "12345678910"
        val value = "A string that has to be persisted.\nAnd this line too."

        s3storageService.put(aktoerId + "___" + "$directory/testfile.txt", value)

        val fileList = s3storageService.list(aktoerId + "___" + directory)
        assertEquals(1, fileList.size, "Expect that 1 entry is returned")

        val fetchedValue = s3storageService.get(fileList[0])
        assertEquals(value, fetchedValue, "The stored and fetched values should be equal")

        s3storageService.delete(fileList[0])

        val fileListAfterDelete = s3storageService.list(aktoerId + "___" + directory)
        assertEquals(0, fileListAfterDelete.size, "Expect that 0 entries are returned")
    }

    @Test
    fun `Calling valid S3StorageService|list with staged storage and prefix returns valid list`() {

        val path1 = "12345678910___path___123"
        val path2 = "12345678910___path___123"
        val prefix = "12345678910___path"
        val content = "something"

        // stage
        s3storageService.put(path1, content)
        s3storageService.put(path2, content)

        val generatedResponse = s3storageService.list(prefix)
        Assert.assertEquals(listOf(path1), generatedResponse)
    }

    @Test
    fun `Calling valid S3StorageService|list with empty storage and prefix returns empty list`() {

        val prefix = "12345678910___path"

        val generatedResponse = s3storageService.list(prefix)
        Assert.assertEquals(listOf<String>(), generatedResponse)
    }

    @Test(expected = AmazonClientException::class)
    fun `Calling valid S3StorageService|list with storage error rethrows error`() {

        val prefix = "12345678910___path"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3MockClient).listObjectsV2(ArgumentMatchers.any<ListObjectsV2Request>())

        s3storageService.list(prefix)
    }

    @Test
    fun `Calling valid S3StorageService|get with staged storage returns valid item`() {

        val path1 = "12345678910___path___123"
        val content = "something"

        // stage
        s3storageService.put(path1, content)

        val generatedResponse = s3storageService.get(path1)
        Assert.assertEquals(content, generatedResponse)
    }

    @Test(expected = AmazonS3Exception::class)
    fun `Calling valid S3StorageService|get with empty storage throwes AmazonS3Exception`() {

        val path1 = "12345678910___path___123"

        val generatedResponse = s3storageService.get(path1)
        Assert.assertEquals(null, generatedResponse)
    }

    @Test(expected = AmazonClientException::class)
    fun `Calling valid S3StorageService|get with storage error rethrows error`() {

        val path1 = "12345678910___path___123"
        val content = "something"

        // stage
        s3storageService.put(path1, content)

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3MockClient).getObject(anyString(), anyString())
        s3storageService.get(path1)
    }

    @Test
    fun `Calling valid S3StorageService|delete with staged storage is OK`() {

        val prefix = "12345678910"
        val path1 = "12345678910___path___123"
        val content = "something"

        // stage
        s3storageService.put(path1, content)

        val generatedResponse = s3storageService.delete(path1)
        Assert.assertEquals(Unit, generatedResponse)

        val fileList = s3storageService.list(prefix)
        Assert.assertFalse(fileList.contains(path1))
    }

    @Test(expected = AmazonS3Exception::class)
    fun `Calling valid S3StorageService|delete with empty storage throws error`() {

        val path1 = "12345678910___path___123"

        s3storageService.delete(path1)
    }

    @Test(expected = AmazonClientException::class)
    fun `Calling valid S3StorageService|delete with storage error rethrows error`() {

        val path1 = "12345678910___path___123"
        val content = "something"

        // stage
        s3storageService.put(path1, content)

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500

        doThrow(expectedError).whenever(s3MockClient).deleteObject(anyString(), anyString())
        s3storageService.delete(path1)
    }

    @Test
    fun `Calling valid S3StorageService|put is OK`() {

        val path1 = "12345678910___path___123"
        val content = "something"

        // stage
        s3storageService.put(path1, content)

        val generatedResponse = s3storageService.get(path1)
        Assert.assertEquals(content, generatedResponse)
    }

    @Test
    fun `Calling valid S3StorageService|put overwriting returns OK`() {

        val path1 = "12345678910___path___123"
        val content1 = "something"
        val content2 = "something2"

        // stage
        s3storageService.put(path1, content1)
        s3storageService.put(path1, content2)

        val generatedResponse = s3storageService.get(path1)
        Assert.assertEquals(content2, generatedResponse)
    }

    @Test(expected = AmazonClientException::class)
    fun `Calling valid S3StorageService|put with storage error rethrows error`() {

        val path1 = "12345678910___path___123"
        val content = "something"

        val expectedError = AmazonServiceException("errorMessage")
        expectedError.statusCode = 500


        doThrow(expectedError).whenever(s3MockClient).putObject(anyString(), anyString(), anyString())

        s3storageService.put(path1, content)
    }

    @Test
    fun `Tests function getBucketName | returns namespace specific bucketname`() {

        val bucket = "eessipensjon"

//        Sjekker bucketname i q1
        Assert.assertEquals( "$bucket-q1", s3storageService.getBucketName())

//      Sjekker bucketname i t8
        s3storageService.fasitEnvironmentName = "t8"
        Assert.assertEquals( "$bucket-t8", s3storageService.getBucketName())

//      Sjekker bucketname i q2
        s3storageService.fasitEnvironmentName = "q2"
        Assert.assertEquals( "$bucket-q2", s3storageService.getBucketName())

        //Sjekker bucketname i p
        s3storageService.fasitEnvironmentName = "p"
        Assert.assertEquals( "$bucket", s3storageService.getBucketName())
    }
}