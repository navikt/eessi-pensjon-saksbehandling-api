package no.nav.eessi.pensjon.api.userinfo

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.config.FeatureToggle
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.ResponseEntity

class UserInfoControllerTest : S3StorageBaseTest() {
    private lateinit var toggleMock: FeatureToggle
    private lateinit var userInfoController: UserInfoController


    @BeforeEach
    fun mockSetup() {
        toggleMock = FeatureToggle()
        toggleMock.setCurrentEnv("q2")
        userInfoController = Mockito.spy(UserInfoController(toggleMock, generateMockContextHolder(), whitelistService))
    }

    @Test fun `Calling UserInfoController getUserInfo in Q2 returns OK response`() {
        toggleMock.setCurrentEnv("q2")
        doReturn(true).whenever(userInfoController).checkWhitelist()

        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 3531157178000,
            features = mapOf("P5000_VISIBLE" to true, "P_BUC_02_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController getUserInfo in P returns OK response`() {
        toggleMock.setCurrentEnv("p")
        doReturn(true).whenever(userInfoController).checkWhitelist()
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 3531157178000,
            features = mapOf("P5000_VISIBLE" to true, "P_BUC_02_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController getUserInfo saksbehandler in Q2 returns OK response`() {
        val toggleMock2 = FeatureToggle()
        val userInfoController2 = Mockito.spy(UserInfoController(toggleMock2, generateMockSaksbehContextHolder(),whitelistService))

        toggleMock2.setCurrentEnv("q2")
        doReturn(true).whenever(userInfoController2).checkWhitelist()
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = 3531157178000,
            features = mapOf("P5000_VISIBLE" to true, "P_BUC_02_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())
    }

    @Test fun `Calling UserInfoController  getUserInfo saksbehandler in P returns OK response`() {
        val toggleMock2 = FeatureToggle()
        val userInfoController2 = Mockito.spy(UserInfoController(toggleMock2, generateMockSaksbehContextHolder(),whitelistService))

        toggleMock2.setCurrentEnv("p")
        doReturn(true).whenever(userInfoController2).checkWhitelist()
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = 3531157178000,
            features = mapOf("P5000_VISIBLE" to true, "P_BUC_02_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())
    }


    @Test fun `Calling UserInfoController|getRole`() {
        assertEquals("BRUKER", getRole("12345678910"))
        assertEquals("SAKSBEHANDLER", getRole("Z123456"))
        assertEquals("UNKNOWN", getRole("ZZZ"))
    }

    @Test fun CallingUserInfoController_getUserInfowithEXP() {
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 3531157178000,
            features = mapOf("P5000_VISIBLE" to true, "P_BUC_02_VISIBLE" to true)
        )
        val result = userInfoController.getUserInfo()
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), result)
        val resultUserInfo = mapJsonToAny(result.body!!, typeRefs<UserInfoResponse>())
        assertEquals(3531157178000, resultUserInfo.expirationTime)
    }

}
