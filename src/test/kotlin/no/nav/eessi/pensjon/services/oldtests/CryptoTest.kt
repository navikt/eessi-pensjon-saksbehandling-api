package no.nav.eessi.pensjon.services.oldtests

import no.nav.eessi.pensjon.services.storage.Crypto
import org.junit.Test
import java.nio.charset.Charset
import javax.xml.bind.DatatypeConverter.parseHexBinary
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CryptoTest {

    @Test
    fun `encrypt filename and content`() {
        val passphrase = "en kjempelang og vond passphrase som ingen kan cracke"
        val filepath = "12345612345/p4000/2018-01-01/periodeinfo.json"
        val plaintext = "Hemmelig innhold \n" +
                "ny linje"
        val salt = "12345678910"
        val crypto = Crypto(passphrase, salt)

        val fnr: String
        val endpath: String
        fnr = filepath.substring(0,11)
        endpath = filepath.substring(11)

        val encryptedFilepath = crypto.encrypt(fnr, asHexBinary = true) + endpath
        val encryptedContent = crypto.encrypt(plaintext)

        val separator = encryptedFilepath.indexOf("/")
        val stringCiphertext = String(parseHexBinary(encryptedFilepath.substring(0, separator)), Charset.forName("UTF8"))

        assertEquals(filepath, crypto.decrypt(stringCiphertext) + endpath)
        assertEquals(plaintext, crypto.decrypt(encryptedContent))
    }

    @Test
    fun `encryption and decryption should work`() {

        val passphrase = "A secret passphrase"
        val plaintext = "A secret message that should be encrypted"
        val salt = "12345612345"

        val crypto = Crypto(passphrase, salt)
        val encrypted = crypto.encrypt(plaintext)
        assertNotEquals(plaintext, encrypted)

        val decrypted = crypto.decrypt(encrypted)
        assertEquals(plaintext, decrypted)
    }

    @Test(expected = RuntimeException::class)
    fun `encryption and decryption with different salts should fail`() {

        val passphrase = "A secret passphrase"
        val plaintext = "A secret message that should be encrypted"
        val salt = "12345612345"

        val crypto = Crypto(passphrase, salt)
        val encrypted = crypto.encrypt(plaintext)
        assertNotEquals(plaintext, encrypted)

        val decrypto = Crypto(passphrase, salt.reversed())
        decrypto.decrypt(encrypted)
    }

    @Test(expected = RuntimeException::class)
    fun `encryption and decryption with different passwords should fail`() {

        val passphrase = "A secret passphrase"
        val plaintext = "A secret message that should be encrypted"
        val salt = "12345612345"

        val crypto = Crypto(passphrase, salt)
        val encrypted = crypto.encrypt(plaintext)
        assertNotEquals(plaintext, encrypted)

        val decrypto = Crypto(passphrase.reversed(), salt)
        decrypto.decrypt(encrypted)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `passphrase must be proveded`() {
        Crypto("", "1234")
    }

    @Test(expected = IllegalArgumentException::class)
    fun `salt must be proveded`() {
        Crypto("1234", "")
    }
}
