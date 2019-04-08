package no.nav.eessi.fagmodul.frontend.services.storage

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.findify.s3mock.S3Mock
import no.nav.eessi.fagmodul.frontend.services.BaseTest
import no.nav.eessi.fagmodul.frontend.services.storage.amazons3.S3Storage
import no.nav.eessi.fagmodul.frontend.services.whitelist.WhitelistService
import org.junit.After
import org.junit.Before
import org.mockito.Mockito

class S3StorageBaseTest : BaseTest() {

    lateinit var s3storageService: S3Storage
    lateinit var s3MockClient: AmazonS3
    lateinit var s3api: S3Mock
    lateinit var storageController : StorageController
    lateinit var whitelistService: WhitelistService

    @Before fun setup() {
        s3api = S3Mock.Builder().withPort(8001).withInMemoryBackend().build()
        s3api.start()

        val endpoint = AwsClientBuilder.EndpointConfiguration("http://localhost:8001", "us-east-1")

        val s3Client = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
                .withEndpointConfiguration(endpoint)
                .build()
        s3Client.createBucket("eessipensjon")

        s3MockClient = Mockito.spy(s3Client)

        s3storageService =  Mockito.spy(S3Storage(s3MockClient))
        s3storageService.bucketname = "eessipensjon"
        s3storageService.passphrase = "something very vey tricky to hack"
        s3storageService.init()

        whitelistService = Mockito.spy(WhitelistService(
                s3storageService, listOf("someUser"),
                "whitelisted",
                "___"))
        storageController = Mockito.spy(StorageController(s3storageService))
    }

    @After fun teardown() {
        s3api.stop()
    }
}
