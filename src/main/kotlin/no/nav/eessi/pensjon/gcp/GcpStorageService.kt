package no.nav.eessi.pensjon.gcp

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.ByteBuffer

@Component
class GcpStorageService( @param:Value("\${GCP_BUCKET_NAME}") var bucketname: String,  private val gcpStorage: Storage) {
    private val logger = LoggerFactory.getLogger(GcpStorageService::class.java)

    init {
        ensureBucketExists()
    }

    private fun ensureBucketExists() {
        when (gcpStorage.get(bucketname) != null) {
            false -> throw IllegalStateException("Fant ikke bucket med navn $bucketname. Må provisjoneres")
            true -> logger.info("Bucket $bucketname funnet.")
        }
    }

    fun lagre(storageKey: String, storageValue: String) {
        val blobInfo =  BlobInfo.newBuilder(BlobId.of(bucketname, storageKey)).setContentType("application/json").build()
        kotlin.runCatching {
            gcpStorage.writer(blobInfo).use {
                it.write(ByteBuffer.wrap(storageValue.toByteArray()))
            }
        }.onFailure { e ->
            logger.warn("Feilet med å lagre dokument med id: ${blobInfo.blobId.name}", e)
        }.onSuccess {
            logger.info("Lagret fil med blobid:  ${blobInfo.blobId.name} og bytes: $it")
        }
    }

    fun hent(storageKey: String): String? {
        val jsonHendelse: Blob
        try {
            jsonHendelse =  gcpStorage.get(BlobId.of(bucketname, storageKey))
            if(jsonHendelse.exists()){
                logger.info("Blob med key:$storageKey funnet")
                return jsonHendelse.getContent().decodeToString()
            }
        } catch ( ex: Exception) {
            logger.info("Objekt $storageKey finnes ikke i bucket")
        }
        return null
    }

    fun slett(storageKey: String) : Boolean {
        val value = hent(storageKey)
        return if (value == null) false else {
            return try {
                gcpStorage.delete(bucketname, storageKey)
                true
            } catch (cause: StorageException) {
                logger.warn("Sletting av dokument med id ${storageKey} feilet.", cause)
                false
            }
        }
    }

    fun list(keyPrefix: String) : List<String> {
        return gcpStorage.list(bucketname , Storage.BlobListOption.prefix(keyPrefix))?.values?.map { v -> v.name}  ?:  emptyList()
    }
}