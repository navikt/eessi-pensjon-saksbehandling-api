package no.nav.eessi.pensjon.services.storage

import java.util.*
import javax.crypto.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter.printHexBinary

private const val ALGORITHM = "AES/GCM/NoPadding"

class Crypto(passphrase: String, val salt: String) {

    private val key = createKey(passphrase, salt)

    fun encrypt(plaintext: String, asHexBinary: Boolean = false): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, salt.toByteArray()))
        val encrypted = Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.toByteArray()))
        return if (asHexBinary)
            printHexBinary(encrypted.toByteArray())
        else
            encrypted
    }

    fun decrypt(ciphertext: String): String {
        try {
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, salt.toByteArray()))
            return String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)))
        } catch (ex: Exception) {
            when (ex) {
                is AEADBadTagException, is IllegalStateException, is IllegalBlockSizeException, is BadPaddingException -> {
                    throw RuntimeException("Error while decrypting text", ex)
                }
                else -> throw ex
            }
        }
    }

    private fun createKey(passphrase: String, salt: String): SecretKey {
        if (passphrase.isEmpty() || salt.isEmpty())
            throw IllegalArgumentException("Both passphrase and salt must be provided")

        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val passwordChars = passphrase.toCharArray()
        val spec = PBEKeySpec(passwordChars, salt.toByteArray(), 10000, 256)
        val key = keyFactory.generateSecret(spec)
        return SecretKeySpec(key.encoded, "AES")
    }
}
