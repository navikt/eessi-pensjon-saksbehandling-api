package no.nav.eessi.fagmodul.frontend.services.whitelist

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import junit.framework.TestCase.*
import no.nav.eessi.fagmodul.frontend.services.storage.StorageService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class WhitelistServiceTest {

    @Mock
    lateinit var storageService: StorageService

    lateinit var whitelistService: WhitelistService


    @Before
    fun setUp() {
        whitelistService = Mockito.spy(WhitelistService(storageService,
                listOf("User1", "User2"),
                "whitelisted",
                "___"))
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
