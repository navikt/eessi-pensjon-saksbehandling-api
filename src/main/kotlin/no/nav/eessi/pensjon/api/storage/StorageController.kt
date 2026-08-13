package no.nav.eessi.pensjon.api.storage

import com.google.cloud.storage.StorageException
import com.fasterxml.jackson.databind.JsonNode
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.api.FrontEndResponse
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.services.auth.EessiPensjonTilgang
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.maskerPersonIdentifier
import no.nav.security.token.support.core.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Protected
@RestController
@RequestMapping("/api/storage")
class StorageController(private val storage: GcpStorageService,
                        @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(StorageController::class.java)

    private var storeDocument: MetricsHelper.Metric
    private var getDocument: MetricsHelper.Metric
    private var listDocuments: MetricsHelper.Metric
    private var deleteDocument: MetricsHelper.Metric
    private var deleteMultipleDocuments: MetricsHelper.Metric

    init {
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
                      @RequestBody(required = true) document: String): ResponseEntity<FrontEndResponse<Boolean>> {
        return storeDocument.measure {
            return@measure try {
                validerPath(path)
                storage.lagre(path, document).also { logger.info("Lagrer dokument for frontend: $path") }
                ResponseEntity.ok(FrontEndResponse(result = true, status = HttpStatus.OK.name))
            } catch (gcpEx: StorageException) {
                val status = HttpStatus.valueOf(gcpEx.code)
                ResponseEntity.status(status).body(
                    FrontEndResponse(status = status.name, message = gcpEx.message)
                )
            } catch (ex: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontEndResponse(status = HttpStatus.INTERNAL_SERVER_ERROR.name, message = "Klarte ikke å lagre s3 dokumenter")
                )
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.get")
    @GetMapping(value = ["/get/{path}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) path: String): ResponseEntity<FrontEndResponse<JsonNode>> {
        return getDocument.measure {
            return@measure try {
                validerPath(path)
                logger.info("Henter S3 dokument")
                ResponseEntity.ok(FrontEndResponse(result = storage.hent(path)?.let { mapJsonToAny<JsonNode>(it) }, status = HttpStatus.OK.name))
            } catch (gcpEx: StorageException) {
                val status = HttpStatus.valueOf(gcpEx.code)
                ResponseEntity.status(status).body(
                    FrontEndResponse(status = status.name, message = gcpEx.message)
                )
            } catch (ex: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontEndResponse(status = HttpStatus.INTERNAL_SERVER_ERROR.name, message = "Klarte ikke å hente s3 dokument")
                )
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.list")
    @GetMapping("/list")
    fun listAll(): ResponseEntity<FrontEndResponse<List<String>>> {
        return listDocuments("")
    }

    @EessiPensjonTilgang
    @Timed("s3.list")
    @GetMapping("/list/{prefix}")
    fun listDocuments(@PathVariable(required = true) prefix: String): ResponseEntity<FrontEndResponse<List<String>>> {
        return listDocuments.measure {
            return@measure try {
                logger.info("Lister S3 dokumenter")
                ResponseEntity.ok(FrontEndResponse(result = storage.list(prefix), status = HttpStatus.OK.name))
            } catch (gcpEx: StorageException) {
                val status = HttpStatus.valueOf(gcpEx.code)
                ResponseEntity.status(status).body(
                    FrontEndResponse(status = status.name, message = gcpEx.message)
                )
            } catch (ex: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontEndResponse(status = HttpStatus.INTERNAL_SERVER_ERROR.name, message = "Klarte ikke å liste s3 dokumenter")
                )
            }
        }
    }

    @EessiPensjonTilgang
    @Timed("s3.delete")
    @DeleteMapping("/{path}")
    fun deleteDocument(@PathVariable(required = true) path: String): ResponseEntity<FrontEndResponse<Boolean>> {
        return deleteDocument.measure {
            return@measure try {
                validerPath(path)
                ResponseEntity.ok(FrontEndResponse(result = storage.slett(path), status = HttpStatus.OK.name))
            } catch (gcpEx: StorageException) {
                val status = HttpStatus.valueOf(gcpEx.code)
                ResponseEntity.status(status).body(
                    FrontEndResponse(status = status.name, message = gcpEx.message)
                )
            } catch (ex: Exception) {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FrontEndResponse(status = HttpStatus.INTERNAL_SERVER_ERROR.name, message = "Klarte ikke å slette s3 dokument")
                )
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
