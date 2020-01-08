package no.nav.eessi.pensjon.services.storage

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.AnonymousAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.findify.s3mock.S3Mock
import no.nav.eessi.pensjon.api.storage.StorageController
import no.nav.eessi.pensjon.services.BaseTest
import no.nav.eessi.pensjon.services.ldap.LdapService
import no.nav.eessi.pensjon.services.storage.amazons3.S3Storage
import no.nav.eessi.pensjon.services.whitelist.WhitelistService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito
import java.net.ServerSocket

open class S3StorageBaseTest : BaseTest() {

    lateinit var s3storageService: S3Storage
    lateinit var s3MockClient: AmazonS3
    lateinit var s3api: S3Mock
    lateinit var storageController : StorageController
    lateinit var whitelistService: WhitelistService
    lateinit var ldapService: LdapService

    @BeforeEach fun setup() {
        val s3Port = randomOpenPort()
        s3api = S3Mock.Builder().withPort(s3Port).withInMemoryBackend().build()
        s3api.start()

        val endpoint = AwsClientBuilder.EndpointConfiguration("http://localhost:$s3Port", "us-east-1")

        val s3Client = AmazonS3ClientBuilder.standard()
                .withPathStyleAccessEnabled(true)
                .withCredentials(AWSStaticCredentialsProvider(AnonymousAWSCredentials()))
                .withEndpointConfiguration(endpoint)
                .build()
        s3Client.createBucket("eessipensjon")

        s3MockClient = Mockito.spy(s3Client)

        s3storageService =  Mockito.spy(S3Storage(s3MockClient))
        s3storageService.bucketname = "eessipensjon"
        s3storageService.fasitEnvironmentName = "q1"
        s3storageService.passphrase = "something very vey tricky to hack"
        s3storageService.init()

        whitelistService = Mockito.spy(WhitelistService(
                s3storageService, listOf("someUser"),
                "whitelisted",
                "___"))

        storageController = Mockito.spy(StorageController(s3storageService, generateMockSaksbehandlerLdapService(),
            generateMockContextHolder()))
    }

    @AfterEach fun teardown() {
        s3api.stop()
    }

    fun randomOpenPort(): Int = ServerSocket(0).use { it.localPort }

}
