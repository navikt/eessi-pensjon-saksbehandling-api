package no.nav.eessi.pensjon.api.userinfo

import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.ResponseEntity

class UserInfoControllerTest : S3StorageBaseTest() {
    lateinit var userInfoController: UserInfoController

    @BeforeEach
    fun mockSetup() {
        val mockOidcContextolder = generateMockContextHolder()
        userInfoController = Mockito.spy(UserInfoController(mockOidcContextolder,whitelistService))
    }

    @Test fun `Calling UserInfoController|getUserInfo returns OK response`() {
        val usr =  UserInfoResponse(subject ="12345678910",
                role ="BRUKER",
                allowed = false,
            expirationTime = 1531157178000
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController|getUserInfo saksbehandler returns OK response`() {
        val userInfoController2 = Mockito.spy(UserInfoController(generateMockSaksbehContextHolder(),whitelistService))

        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = false,
            expirationTime = 1531157178000
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())

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
