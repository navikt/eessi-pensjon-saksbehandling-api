package no.nav.eessi.pensjon.services.storage

interface StorageService {
    fun list(path: String): List<String>
    fun put(path: String, content: String)
    fun get(path: String): String?
    fun delete(path: String)
    fun multipleDelete (paths: List<String>)
}
