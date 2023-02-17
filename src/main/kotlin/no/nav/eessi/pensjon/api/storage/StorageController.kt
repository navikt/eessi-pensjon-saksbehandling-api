package no.nav.eessi.pensjon.api.storage

import com.google.cloud.storage.StorageException
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.annotation.PostConstruct
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.utils.errorBody
import no.nav.eessi.pensjon.utils.maskerPersonIdentifier
import no.nav.eessi.pensjon.utils.successBody
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@Protected
@RestController
@RequestMapping("/api/storage")
class StorageController(private val storage: GcpStorageService,
                        @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(StorageController::class.java)

    private lateinit var storeDocument: MetricsHelper.Metric
    private lateinit var getDocument: MetricsHelper.Metric
    private lateinit var listDocuments: MetricsHelper.Metric
    private lateinit var deleteDocument: MetricsHelper.Metric
    private lateinit var deleteMultipleDocuments: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        storeDocument = metricsHelper.init("storeDocument")
        getDocument = metricsHelper.init("getDocument")
        listDocuments = metricsHelper.init("listDocuments")
        deleteDocument = metricsHelper.init("deleteDocument")
        deleteMultipleDocuments = metricsHelper.init("deleteMultipleDocuments")
    }


    @EessiPensjonTilgang
    @Timed("s3.put")
    @PostMapping("/{path}")
    fun storeDocument(@PathVariable(required = true) path: String,
                      @RequestBody(required = true) document: String): ResponseEntity<String>{
        return storeDocument.measure {
            return@measure try {
                validerPath(path)
                logger.info("Lagrer S3 dokument")
                storage.lagre(path, document)
                ResponseEntity.ok().body(successBody())
            } catch (gcpEx: StorageException) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.valueOf(gcpEx.code)).body(errorBody(gcpEx.message, uuid))
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å lagre s3 dokumenter", uuid))
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.get")
    @GetMapping(value = ["/get/{path}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return getDocument.measure {
            return@measure try {
                validerPath(path)
                logger.info("Henter S3 dokument")
                ResponseEntity.ok().body(storage.hent(path))
            } catch (gcpEx: StorageException) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.valueOf(gcpEx.code)).body(errorBody(gcpEx.message, uuid))
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å hente s3 dokument", uuid))
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.list")
    @GetMapping("/list")
    fun listAll(): ResponseEntity<List<String>> {
        return listDocuments("")
    }

    @EessiPensjonTilgang
    @Timed("s3.list")
    @GetMapping("/list/{prefix}")
    fun listDocuments(@PathVariable(required = true) prefix: String): ResponseEntity<List<String>> {
        return listDocuments.measure {
            return@measure try {
                //validerPath(prefix)
                logger.info("Lister S3 dokumenter")
                ResponseEntity.ok().body(storage.list(prefix))
            } catch (gcpEx: StorageException) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.valueOf(gcpEx.code)).body(listOf(errorBody(gcpEx.message, uuid)))
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(listOf(errorBody("Klarte ikke å liste s3 dokumenter", uuid)))
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.delete")
    @DeleteMapping("/{path}")
    fun deleteDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return deleteDocument.measure {
            return@measure try {
                validerPath(path)
                storage.slett(path)
                ResponseEntity.ok().body(successBody())
            } catch (gcpEx: StorageException) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.valueOf(gcpEx.code)).body(errorBody(gcpEx.message, uuid))
            } catch (ex: Exception) {
                val uuid = UUID.randomUUID().toString()
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å slette s3 dokument", uuid))
            }
        }
    }

    private fun validerPath(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("s3 path kan ikke være tom")
        }
        if (!path.matches(Regex("^.+___.+"))) {
            throw IllegalArgumentException("s3 path må følge mønsteret")
        }
        logger.info("S3 path til ${maskerPersonIdentifier(path)} validert")
    }
}
