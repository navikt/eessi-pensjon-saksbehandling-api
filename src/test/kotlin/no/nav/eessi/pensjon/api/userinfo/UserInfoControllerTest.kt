package no.nav.eessi.pensjon.api.userinfo

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
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

    @Test fun `Calling UserInfoController|getUserInfo in Q2 returns OK response`() {
        userInfoController.fasitEnvironmentName = "q2"
        doReturn(true).whenever(userInfoController).checkWhitelist()

        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 1531157178000,
            features = mapOf("P5000_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController|getUserInfo in P returns OK response`() {
        userInfoController.fasitEnvironmentName = "p"
        doReturn(true).whenever(userInfoController).checkWhitelist()
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 1531157178000,
            features = mapOf("P5000_VISIBLE" to false)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController|getUserInfo saksbehandler in Q2 returns OK response`() {
        val userInfoController2 = Mockito.spy(UserInfoController(generateMockSaksbehContextHolder(),whitelistService))
        userInfoController2.fasitEnvironmentName = "q2"
        doReturn(true).whenever(userInfoController2).checkWhitelist()
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = 1531157178000,
            features = mapOf("P5000_VISIBLE" to true)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())
    }

    @Test fun `Calling UserInfoController|getUserInfo saksbehandler in P returns OK response`() {
        val userInfoController2 = Mockito.spy(UserInfoController(generateMockSaksbehContextHolder(),whitelistService))
        userInfoController2.fasitEnvironmentName = "p"
        doReturn(true).whenever(userInfoController2).checkWhitelist()
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = 1531157178000,
            features = mapOf("P5000_VISIBLE" to false)
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())
    }

    @Test fun `Calling UserInfoController|getUserInfo saksbehandler in P with invalid person does not return features`() {
        val userInfoController2 = Mockito.spy(UserInfoController(generateMockSaksbehContextHolder(),whitelistService))
        userInfoController2.fasitEnvironmentName = "p"
        doReturn(false).whenever(userInfoController2).checkWhitelist()
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = false,
            expirationTime = 1531157178000,
            features = mapOf()
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController2.getUserInfo())
    }
    @Disabled
    @Test fun `Calling UserInfoController|getRole`() {
        assertEquals("BRUKER", getRole("12345678910"))
        assertEquals("SAKSBEHANDLER", getRole("Z123456"))
        assertEquals("UNKNOWN", getRole("ZZZ"))
    }

    @Disabled
    @Test fun CallingUserInfoController_getUserInfowithEXP() {
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = 1531157178000,
            features = mapOf()
        )
        val result = userInfoController.getUserInfo()
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), result)

        val resultUserInfo = mapJsonToAny(result.body!!, typeRefs<UserInfoResponse>())
        assertEquals(1531157178000, resultUserInfo.expirationTime)

    }

}
