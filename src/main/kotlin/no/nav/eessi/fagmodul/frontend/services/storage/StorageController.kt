package no.nav.eessi.fagmodul.frontend.services.storage

import com.amazonaws.AmazonServiceException
import com.amazonaws.SdkClientException
import io.micrometer.core.annotation.Timed
import no.nav.eessi.fagmodul.frontend.utils.*
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@Protected
@RestController
@RequestMapping("/api/storage")
class StorageController(val storage: StorageService) {

    private val logger = LoggerFactory.getLogger(StorageController::class.java)

    @Timed("s3.put")
    @PostMapping("/{path}")
    fun storeDocument(@PathVariable(required = true) path: String,
                      @RequestBody(required = true) document: String): ResponseEntity<String>{
        return try {
            validerPath(path)
            logger.info("Lagrer S3 dokument")
            storage.put(path, document)
            ResponseEntity.ok().body(successBody())
        } catch(awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å lagre s3 dokument : ${maskerPersonIdentifier(path)}, ${awsEx.errorMessage}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å lagre s3 dokument", uuid))
        } catch(sdkEx: SdkClientException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å lagre s3 dokument: ${maskerPersonIdentifier(path)} , ${sdkEx.message}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å lagre s3 dokument", uuid))
        }
    }

    @Timed("s3.get")
    @GetMapping(value = ["/get/{path}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return try {
            validerPath(path)
            logger.info("Henter S3 dokument")
            ResponseEntity.ok().body(storage.get(path))
        } catch(awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            if(awsEx.statusCode == HttpStatus.NOT_FOUND.value()){
                logger.info("S3 dokumentet eksisterer ikke, $uuid")
                ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode))
                        .body(errorBody("S3 dokumentet eksisterer ikke", uuid))
            } else {
                logger.error("Klarte ikke å hente s3 dokument fra : ${maskerPersonIdentifier(path)} , ${awsEx.errorMessage}, $uuid")
                ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode))
                        .body(errorBody("Klarte ikke å hente s3 dokument", uuid))
            }
        } catch(sdkEx: SdkClientException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å hente s3 dokument, ${sdkEx.message}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å hente s3 dokument", uuid))
        }
    }

    @Timed("s3.list")
    @GetMapping("/list/{prefix}")
    fun listDocuments(@PathVariable(required = true) prefix: String): ResponseEntity<List<String>> {
        return try {
            validerPath(prefix)
            logger.info("Lister S3 dokumenter")
            ResponseEntity.ok().body(storage.list(prefix))
        } catch(awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å liste s3 dokumenter fra : ${maskerPersonIdentifier(prefix)}, ${awsEx.errorMessage}, $uuid")
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode))
                    .body(listOf(errorBody("Klarte ikke å liste s3 dokumenter", uuid)))
        } catch(sdkEx: SdkClientException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å liste s3 dokumenter fra : ${maskerPersonIdentifier(prefix)}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(listOf(errorBody("Klarte ikke å liste s3 dokumenter", uuid)))
        } catch(ex: Exception) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Generell exception list/{prefix}, $ex, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(listOf(errorBody("Klarte ikke å liste s3 dokumenter", uuid)))
        }
    }

    @Timed("s3.delete")
    @DeleteMapping("/{path}")
    fun deleteDocument(@PathVariable(required = true) path: String): ResponseEntity<String> {
        return try {
            validerPath(path)
            storage.delete(path)
            ResponseEntity.ok().body(successBody())
        } catch(awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å slette s3 dokument: ${maskerPersonIdentifier(path)}, ${awsEx.errorMessage}, $uuid")
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode))
                    .body(errorBody("Klarte ikke å slette s3 dokument", uuid))
        } catch(sdkEx: SdkClientException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å slette s3 dokument: ${maskerPersonIdentifier(path)}, ${sdkEx.message}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å slette s3 dokument", uuid))
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
                storage.multipleDelete(paths)
            }
            ResponseEntity.ok().body(successBody())
        } catch(awsEx: AmazonServiceException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å slette s3 dokumenterer med mønsteret: ${maskerPersonIdentifier(path)}, ${awsEx.errorMessage}, $uuid")
            ResponseEntity.status(HttpStatus.valueOf(awsEx.statusCode))
                    .body(errorBody("Klarte ikke å slette s3 dokument", uuid))
        } catch(sdkEx: SdkClientException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å slette s3 dokument: ${maskerPersonIdentifier(path)}, ${sdkEx.message}, $uuid")
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorBody("Klarte ikke å slette s3 dokumenter med mønsteret", uuid))
        } catch(iaEx: IllegalArgumentException) {
            val uuid = UUID.randomUUID().toString()
            logger.error("Klarte ikke å slette s3 dokument: ${maskerPersonIdentifier(path)}, ${iaEx.message}, $uuid")
            ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(errorBody("Klarte ikke å slette s3 dokumenter med mønsteret", uuid))
        }
    }
}
