package no.nav.eessi.pensjon.api.userinfo

import com.nhaarman.mockitokotlin2.doReturn
import com.nimbusds.jwt.JWTClaimsSet
import no.nav.eessi.pensjon.config.FeatureToggle
import no.nav.eessi.pensjon.services.storage.S3StorageBaseTest
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.ResponseEntity
import java.util.*

class UserInfoControllerTest : S3StorageBaseTest() {
    private lateinit var toggleMock: FeatureToggle
    private lateinit var userInfoController: UserInfoController

    private val EXPIRATION_TIME = 1300819380L

    @BeforeEach
    fun mockSetup() {
        toggleMock = FeatureToggle()
        toggleMock.setCurrentEnv("q2")
        userInfoController = Mockito.spy(UserInfoController(toggleMock, SpringTokenValidationContextHolder()))
    }

    @Test fun `Calling UserInfoController getUserInfo in Q2 returns OK response`() {
        toggleMock.setCurrentEnv("q2")
        createMockedToken()
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to true
            )
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }


    @Test fun `Calling UserInfoController getUserInfo in P returns OK response`() {
        toggleMock.setCurrentEnv("p")

        createMockedToken()
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to false
            )
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController getUserInfo saksbehandler in Q2 returns OK response`() {
        createMockedToken("A123456")

        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to true
            )
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    @Test fun `Calling UserInfoController  getUserInfo saksbehandler in P returns OK response`() {
        createMockedToken("A123456")
        toggleMock.setCurrentEnv("p")
        val usr =  UserInfoResponse(subject ="A123456",
            role ="SAKSBEHANDLER",
            allowed = true,
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to false
            )
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }


    @Test fun `Calling UserInfoController|getRole`() {
        assertEquals("BRUKER", getRole("12345678910"))
        assertEquals("SAKSBEHANDLER", getRole("Z123456"))
        assertEquals("UNKNOWN", getRole("ZZZ"))
    }

    @Test fun CallingUserInfoController_getUserInfowithEXP() {
        createMockedToken()
        val usr =  UserInfoResponse(subject ="12345678910",
            role ="BRUKER",
            allowed = true,
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to true
            )
        )
        val result = userInfoController.getUserInfo()
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), result)
        val resultUserInfo = mapJsonToAny(result.body!!, typeRefs<UserInfoResponse>())
        assertEquals(EXPIRATION_TIME, resultUserInfo.expirationTime)
    }

    private fun createMockedToken(subject: String = "12345678910") {
        val claimsSet = JWTClaimsSet.Builder()
            .subject(subject)
            .expirationTime(Date(EXPIRATION_TIME))
            .claim("http://example.com/is_root", true)
            .build()
        val twtToken = JwtTokenClaims(claimsSet)
        Mockito.spy(twtToken)

        doReturn(subject).`when`(userInfoController).getSubjectFromToken()
        doReturn(twtToken).`when`(userInfoController).getClaims()
    }

}
