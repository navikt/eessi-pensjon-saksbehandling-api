package no.nav.eessi.fagmodul.frontend.services.userinfo

import no.nav.eessi.fagmodul.frontend.services.storage.S3StorageBaseTest
import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import no.nav.eessi.fagmodul.frontend.utils.mapJsonToAny
import no.nav.eessi.fagmodul.frontend.utils.typeRefs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.springframework.http.ResponseEntity

class UserInfoControllerTest : S3StorageBaseTest() {
    lateinit var userInfoController: UserInfoController

    @Before
    fun mockSetup() {
        val mockOidcContextolder = generateMockContextHolder()
        userInfoController = Mockito.spy(UserInfoController(mockOidcContextolder,whitelistService)
        )
    }

    @Test fun `Calling UserInfoController|getUserInfo returns OK response`() {
        val usr =  UserInfoResponse(subject ="12345678910",
                role ="BRUKER",
                allowed = false,
            expirationTime = 1531157178000
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController|checkWhitelist with no whitelist person returns false`() {
        val generatedResponse = userInfoController.checkWhitelist()
        assertFalse(generatedResponse)
    }

    @Test fun `Calling UserInfoController|getRole`() {
        assertEquals("BRUKER", getRole("12345678910"))
        assertEquals("SAKSBEHANDLER", getRole("Z123456"))
        assertEquals("UNKNOWN", getRole("ZZZ"))
    }

    @Test fun CallingUserInfoController_getUserInfowithEXP() {
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = false,
            expirationTime = 1531157178000
        )
        val result = userInfoController.getUserInfo()
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), result)

        val resultUserInfo = mapJsonToAny(result.body!!, typeRefs<UserInfoResponse>())
        assertEquals(1531157178000, resultUserInfo.expirationTime)

    }

}