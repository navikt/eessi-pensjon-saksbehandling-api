package no.nav.eessi.pensjon.api.storage

import com.amazonaws.AmazonServiceException
import io.micrometer.core.annotation.Timed
import no.nav.eessi.pensjon.logging.AuditLogger
import no.nav.eessi.pensjon.services.ldap.LdapService
import no.nav.eessi.pensjon.services.storage.StorageService
import no.nav.eessi.pensjon.utils.*
import no.nav.security.oidc.api.Protected
import no.nav.security.oidc.context.OIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Protected
@RestController
@RequestMapping("/api/storage")
class StorageController(private val storage: StorageService,
                        private val ldapService: LdapService,
                        private val oidcRequestContextHolder: OIDCRequestContextHolder) {

    private val logger = LoggerFactory.getLogger(StorageController::class.java)
    private val auditLogger = AuditLogger(oidcRequestContextHolder)

    @Timed("s3.put")
    @PostMapping("/{path}")
    fun storeDocument(@PathVariable(required = true) path: String,
                      @RequestBody(required = true) document: String): ResponseEntity<String>{
        return try {
            validerPath(path)
            logger.info("Lagrer S3 dokument")
            auditLogger.log("storeDocument")
            storage.put(path, document)
            ResponseEntity.ok().body(successBody())
        } catch (awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode)).body(errorBody(awsEx.errorMessage, uuid))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Klarte ikke å lagre s3 dokumenter", uuid))
        }
    }

    @Timed("s3.get")
    @GetMapping(value = ["/get/{path}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return try {
            validerPath(path)
            logger.info("Henter S3 dokument")
            auditLogger.log("getDocument")
            ResponseEntity.ok().body(storage.get(path))
        } catch (awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode)).body(errorBody(awsEx.errorMessage, uuid))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Klarte ikke å hente s3 dokument", uuid))
        }
    }

    @Timed("s3.list")
    @GetMapping("/list/{prefix}")
    fun listDocuments(@PathVariable(required = true) prefix: String): ResponseEntity<List<String>> {
        return try {
            validerPath(prefix)
            logger.info("Lister S3 dokumenter")
            auditLogger.log("listDocuments")
            ResponseEntity.ok().body(storage.list(prefix))
        } catch (awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode)).body(listOf(errorBody(awsEx.errorMessage, uuid)))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(listOf(errorBody("Klarte ikke å liste s3 dokumenter", uuid)))
        }
    }

    @Timed("s3.delete")
    @DeleteMapping("/{path}")
    fun deleteDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return try {
            validerPath(path)
            auditLogger.log("deleteDocument")
            storage.delete(path)
            ResponseEntity.ok().body(successBody())
        } catch (awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode)).body(errorBody(awsEx.errorMessage, uuid))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Klarte ikke å slette s3 dokument", uuid))
        }
    }

    @Timed("s3.delete")
    @DeleteMapping("/multiple/{path}")
    fun deleteMultipleDocuments(@PathVariable(required = true) path: String): ResponseEntity<String> {

        return try {
            validerPath(path)
            val paths = storage.list(path)
            if (!paths.isEmpty()) {
                paths.forEach {
                    validerPath(it)
                }
                auditLogger.log("deleteMultipleDocuments")
                storage.multipleDelete(paths)
            }
            ResponseEntity.ok().body(successBody())
        } catch (awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode)).body(errorBody(awsEx.errorMessage, uuid))
        } catch (ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Klarte ikke å slette s3 dokumenter", uuid))
        }
    }

    private fun validerPath(path: String) {
        if (path.isEmpty()) {
            throw IllegalArgumentException("s3 path kan ikke være tom")
        }
        if (!path.matches(Regex("^.+___.+"))) {
            throw IllegalArgumentException("s3 path må følge mønsteret")
        }
        logger.info(maskerPersonIdentifier(path) + " validert")
    }
}
