package no.nav.eessi.pensjon.api.userinfo

import com.nimbusds.jwt.JWTClaimsSet
import io.mockk.every
import io.mockk.spyk
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
        userInfoController = spyk(UserInfoController(toggleMock, SpringTokenValidationContextHolder()))
    }

    @Test fun `Calling UserInfoController getUserInfo in Q2 returns OK response`() {
        toggleMock.setCurrentEnv("q2")
        createMockedToken()
        val usr =  UserInfoResponse(
            subject ="12345678910",
            role ="BRUKER",
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                    "P5000_SUMMER_VISIBLE" to true,
                    "X010_X009_VISIBLE" to true
            )
        )
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), userInfoController.getUserInfo())
    }

    // Denne testen er midlertidig for P5000
    @Test fun `Calling UserInfoController getUserInfo in P reading selected users returns OK response`() {
        toggleMock.setCurrentEnv("P")
        createMockedToken("H145594")
        val usr =  UserInfoResponse(
            subject ="H145594",
            role ="SAKSBEHANDLER",
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                "P5000_SUMMER_VISIBLE" to true,
                "X010_X009_VISIBLE" to true
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
        val usr =  UserInfoResponse(
            subject ="12345678910",
            role ="BRUKER",
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                "P5000_SUMMER_VISIBLE" to true,
                "X010_X009_VISIBLE" to true
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
        spyk(twtToken)

        every { userInfoController.getSubjectFromToken() } returns subject
        every { userInfoController.getClaims() } returns twtToken
    }
}
