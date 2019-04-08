package no.nav.eessi.fagmodul.frontend.services.storage

interface StorageService {
    fun list(path: String): List<String>
    fun put(path: String, content: String)
    fun get(path: String): String?
    fun delete(path: String)
    fun multipleDelete (paths: List<String>)
}