package no.nav.eessi.pensjon.services.whitelist

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import no.nav.eessi.pensjon.services.storage.StorageService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class WhitelistServiceTest {

    @Mock
    lateinit var storageService: StorageService

    lateinit var whitelistService: WhitelistService


    @BeforeEach
    fun setUp() {
        whitelistService = Mockito.spy(WhitelistService(storageService,
                listOf("User1", "User2"),
                "whitelisted",
                "___"))
        whitelistService.initMetrics()
    }

    @Test
    fun `Given a valid whitelist s3 key When checking if key is refering to a whitelist object Then return true`() {
       assertTrue(whitelistService.isKeyWhitelist("12345678910___whitelisted"))
    }

    @Test
    fun `Given an invalid whitelist s3 key When checking if key is refering to a whitelist object Then return true`() {
        assertFalse(whitelistService.isKeyWhitelist("12345678910___somethingElse"))
    }

    @Test
    fun `Given an empty key When checking if key is refering to a whitelist object Then return false`() {
        assertFalse(whitelistService.isKeyWhitelist(""))
    }

    @Test
    fun `Given an key without personIdentifierSeparator When checking if key is refering to a whitelist object Then return false`() {
        assertFalse(whitelistService.isKeyWhitelist("12345678910Whitelisted"))
    }

    @Test
    fun `Given two not whitelisted users When initializing Then add users to whitelist`() {
        whitelistService.startup()
        assertEquals(whitelistService.newUsersToWhitelist.size, 2)
        verify(storageService, times(2)).put(any(), any())
    }
}
