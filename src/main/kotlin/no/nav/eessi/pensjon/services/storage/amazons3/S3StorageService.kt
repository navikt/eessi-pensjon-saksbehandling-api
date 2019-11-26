package no.nav.eessi.pensjon.services.storage.amazons3

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.*
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.storage.StorageService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors.joining
import org.springframework.stereotype.Component
import java.lang.RuntimeException

private val logger = LoggerFactory.getLogger(S3Storage::class.java)

@Component
class S3Storage(val s3: AmazonS3,
                @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) : StorageService {

    @Value("\${eessi_pensjon_s3_crypto_password}")
    lateinit var passphrase: String

    @Value("\${eessi.pensjon.frontend.api.s3.bucket.name}")
    lateinit var bucketname: String

    @Value("\${FASIT_ENVIRONMENT_NAME}")
    lateinit var fasitEnvironmentName: String

    fun getBucketName(): String {
        return bucketname+postfixFasitEnv()
    }

    private fun postfixFasitEnv(): String {
        var environmentPostfix = "-$fasitEnvironmentName"

        // Det settes nå kun dfault i prod, namespace brukes i alle andre miljø
        if (fasitEnvironmentName.contains("p", true)) {
            environmentPostfix = ""
        }
        return environmentPostfix
    }

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
        return metricsHelper.measure("lists3objects") {
            try {
                val list = mutableListOf<String>()
                val listObjectsRequest = populerListObjectRequest(path)
                val objectListing = s3.listObjectsV2(listObjectsRequest)
                objectListing.objectSummaries.mapTo(list) { it.key }
                list
            } catch (ex: AmazonServiceException) {
                logger.error("En feil oppstod under listing av bucket ex: $ex message: ${ex.errorMessage} errorcode: ${ex.errorCode}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under listing av bucket ex: $ex")
                throw ex
            }
        }
    }

    override fun get(path: String): String? {
        return try {
            val content: String
            logger.info("Getting plaintext path")
            val s3Object = s3.getObject(getBucketName(), path)
            content = readS3Stream(s3Object)
            metricsHelper.increment("hents3objects", "successful")
            content
        } catch (se: AmazonServiceException) {
            if(se.statusCode == 404) {
                logger.info("Objektet som forsøkes å hentes finnes ikke $se")
                metricsHelper.increment("hents3objects", "successful")
                throw se
            } else {
                logger.error("En feil oppstod under henting av objekt ex: $se message: ${se.errorMessage} errorcode: ${se.errorCode}")
                metricsHelper.increment("hents3objects", "failed")
                throw se
            }
        } catch (ex: Exception) {
            logger.error("En feil oppstod under henting av objekt ex: $ex")
            metricsHelper.increment("hents3objects", "failed")
            throw ex
        }
    }

    override fun delete(path: String) {
        return metricsHelper.measure("sletts3object") {
            try {
                s3.deleteObject(getBucketName(), path)
            } catch (ex: AmazonServiceException) {
                logger.error("En feil oppstod under sletting av objekt ex: $ex message: ${ex.errorMessage} errorcode: ${ex.errorCode}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under sletting av objekt ex: $ex")
                throw ex
            }
        }
    }

    /**
     * Lagrer nytt S3 objekt.
     *
     * @param path <fnr/dnr/ad-bruker>___<valgfri filending>
     * @param content innholdet i objektet
     */
    override fun put(path: String, content: String) {
        return metricsHelper.measure("oppretts3object") {
            try {
                s3.putObject(getBucketName(), path, content)
            } catch (ex: AmazonServiceException) {
                logger.error("En feil oppstod under opprettelse av objekt ex: $ex message: ${ex.errorMessage} errorcode: ${ex.errorCode}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under opprettelse av objekt ex: $ex")
                throw ex
            }
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
