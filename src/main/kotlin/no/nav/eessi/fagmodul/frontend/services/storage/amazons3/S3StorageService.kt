package no.nav.eessi.fagmodul.frontend.services.storage.amazons3

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import no.nav.eessi.fagmodul.frontend.utils.counter
import no.nav.eessi.fagmodul.frontend.services.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors.joining
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import java.lang.RuntimeException

private val logger = LoggerFactory.getLogger(S3Storage::class.java)

@Component
class S3Storage(val s3: AmazonS3) : StorageService {

    @Value("\${eessi_pensjon_s3_crypto_password}")
    lateinit var passphrase: String

    @Value("\${eessi.pensjon.frontend.api.s3.bucket.name}")
    lateinit var bucketname: String

    @Value("\${FASIT_ENVIRONMENT_NAME}")
    lateinit var fasitEnvironmentName: String

    fun getBucketName(): String {
        return bucketname+postfixFasitEnv()
    }

    fun postfixFasitEnv(): String {
        var environmentPostfix = "-$fasitEnvironmentName"

        // Det settes nå kun dfault i prod, namespace brukes i alle andre miljø
        if (fasitEnvironmentName.contains("p", true)) {
            environmentPostfix = ""
        }
        return environmentPostfix
    }

    private final val lists3objectsTellerNavn = "eessipensjon_frontend-api.lists3objects"
    private val lists3objectsVellykkede = counter(lists3objectsTellerNavn, "vellykkede")
    private val lists3objectsFeilede = counter(lists3objectsTellerNavn, "feilede")
    private final val hents3objectTellerNavn = "eessipensjon_frontend-api.hents3object"
    private val hents3objectVellykkede = counter(hents3objectTellerNavn, "vellykkede")
    private val hents3objectFeilede = counter(hents3objectTellerNavn, "feilede")
    private final val sletts3objectTellerNavn = "eessipensjon_frontend-api.sletts3object"
    private val sletts3objectVellykkede = counter(sletts3objectTellerNavn, "vellykkede")
    private val sletts3objectFeilede = counter(sletts3objectTellerNavn, "feilede")
    private final val oppretts3objectTellerNavn = "eessipensjon_frontend-api.oppretts3object"
    private val oppretts3objectVellykkede = counter(oppretts3objectTellerNavn, "vellykkede")
    private val oppretts3objectFeilede = counter(oppretts3objectTellerNavn, "feilede")

    @EventListener(ApplicationReadyEvent::class)
    fun init() {
        try {
            ensureBucketExists()
            ensureVersioningIsEnabled()
            logger.debug("S3-storage ready with bucket: ${getBucketName()}")
        } catch (e: Exception) {
            logger.warn("Failed to connect to or create bucket ${getBucketName()}", e)
        }
    }

    private fun ensureVersioningIsEnabled() {
        val versionConfig = s3.getBucketVersioningConfiguration(getBucketName())
        logger.debug("Bucket versioning configuration status: ${versionConfig.status}")

        if (versionConfig.status == "Enabled")
            return

        logger.debug("Enabling versioning on bucket ${getBucketName()}")
        try {
            val versioningConfiguration = BucketVersioningConfiguration().withStatus("Enabled")
            val setBucketVersioningConfigurationRequest = SetBucketVersioningConfigurationRequest(getBucketName(), versioningConfiguration)
            s3.setBucketVersioningConfiguration(setBucketVersioningConfigurationRequest)
        } catch (e: Exception) {
            logger.error("Failed to create versioned S3 bucket: ${e.message}")
            throw e
        }
    }

    private fun ensureBucketExists() {
        logger.debug("Checking if bucket exists")
        val bucketExists = s3.listBuckets().stream()
                .anyMatch { it.name == getBucketName() }
        if (!bucketExists) {
            logger.debug("Bucket does not exist, creating new bucket")
            s3.createBucket(CreateBucketRequest(getBucketName()).withCannedAcl(CannedAccessControlList.Private))
        }
    }

    /**
     * Lister objekter med prefix $path, path må begynne med fnr/dnr dersom innlogget bruker er borger
     *
     * @param path
     */
    override fun list(path: String): List<String> {
        try {
            val list = mutableListOf<String>()
            val listObjectsRequest = populerListObjectRequest(path)
            val objectListing = s3.listObjectsV2(listObjectsRequest)
            objectListing.objectSummaries.mapTo(list) { it.key }
            lists3objectsVellykkede.increment()
            return list
        } catch (ex: Exception) {
            lists3objectsFeilede.increment()
            throw ex
        }
    }

    override fun get(path: String): String? {
        return try {
            val content: String
            logger.info("Getting plaintext path")
            val s3Object = s3.getObject(getBucketName(), path)
            content = readS3Stream(s3Object)
            hents3objectVellykkede.increment()
            content
        } catch(awsEx: AmazonServiceException) {
            if(awsEx.statusCode != HttpStatus.NOT_FOUND.value()){
                hents3objectFeilede.increment()
            }
            throw awsEx
        }
    }

    override fun delete(path: String) {
        try {
            s3.deleteObject(getBucketName(), path)
            sletts3objectVellykkede.increment()
        } catch (ex: Exception) {
            sletts3objectFeilede.increment()
            throw ex
        }
    }

    /**
     * Lagrer nytt S3 objekt.
     *
     * @param path <fnr/dnr/ad-bruker>___<valgfri filending>
     * @param content innholdet i objektet
     */
    override fun put(path: String, content: String) {
        try {
            s3.putObject(getBucketName(), path, content)
            oppretts3objectVellykkede.increment()
        } catch (ex: Exception) {
            oppretts3objectFeilede.increment()
            throw ex
        }
    }

    override fun multipleDelete(paths: List<String>) {
        throw RuntimeException("Not implemented")
    }

    private fun populerListObjectRequest(cipherPath: String?): ListObjectsV2Request? {
        return ListObjectsV2Request()
                .withBucketName(getBucketName())
                .withPrefix(cipherPath)
    }

    private fun readS3Stream(s3Object: S3Object): String {
        val inputStreamReader = InputStreamReader(s3Object.objectContent)
        val content = BufferedReader(inputStreamReader)
                .lines()
                .collect(joining("\n"))
        inputStreamReader.close()
        return content
    }
}