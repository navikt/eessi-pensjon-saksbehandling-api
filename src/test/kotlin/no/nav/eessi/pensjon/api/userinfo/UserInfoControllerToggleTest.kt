package no.nav.eessi.pensjon.api.userinfo

import com.nimbusds.jwt.JWTClaimsSet
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.eessi.pensjon.config.FeatureToggleService
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*

//@Disabled
class UserInfoControllerToggleTest {
    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var userInfoController: UserInfoController
    private var restTemplate: RestTemplate = mockk()

    private val EXPIRATION_TIME = 1300819380L

    @BeforeEach
    fun mockSetup() {
        featureToggleService = FeatureToggleService("eessi-pensjon", "",mockk(relaxed = true), mockk(), restTemplate)
        userInfoController = UserInfoController(mockk(), SpringTokenValidationContextHolder(), featureToggleService)
    }

    @Test
    fun `CallingUserInfoController_getTogglesForUser`() {
        val response = """
                {
                    "version": 1,
                    "features": [
                        {
                            "impressionData": false,
                            "enabled": false,
                            "name": "P5000_UPDATES_VISIBLE",
                            "description": null,
                            "project": "default",
                            "stale": false,
                            "type": "release",
                            "lastSeenAt": null,
                            "variants": [],
                            "createdAt": "2025-09-11T07:59:44.442Z",
                            "environments": [
                                {
                                    "name": "production",
                                    "lastSeenAt": "2025-09-29T13:04:21.453Z",
                                    "enabled": false
                                },
                                {
                                    "name": "development",
                                    "lastSeenAt": "2025-09-29T12:59:21.451Z",
                                    "enabled": false
                                }
                            ],
                            "strategies": []
                        },
                        {
                            "impressionData": false,
                            "enabled": false,
                            "name": "EESSI_ADMIN",
                            "description": null,
                            "project": "default",
                            "stale": false,
                            "type": "release",
                            "lastSeenAt": null,
                            "variants": [],
                            "createdAt": "2025-09-18T10:54:25.570Z",
                            "environments": [
                                {
                                    "name": "production",
                                    "lastSeenAt": "2025-09-29T13:04:21.453Z",
                                    "enabled": false
                                },
                                {
                                    "name": "development",
                                    "lastSeenAt": "2025-09-29T12:59:21.451Z",
                                    "enabled": false
                                }
                            ],
                            "strategies": []
                        }
                    ]
                }
            """.trimIndent()

        every {
            restTemplate.exchange(
                "/eessi-pensjon/admin/features",
                HttpMethod.GET,
                any(),
                String::class.java,
                any()

            )
        } returns ResponseEntity.ok().body(response)

        val result = userInfoController.getTogglesForUser()
        assertEquals(ResponseEntity.ok().body(response), result.body)

//        val resultFeatures = mapJsonToAny<Map<String, Boolean>>(result.body!!)
//        assertEquals(true, resultFeatures["P5000_SUMMER_VISIBLE"])


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

    //        val usr =  UserInfoResponse(
//            subject ="12345678910",
//            role ="BRUKER",
//            expirationTime = EXPIRATION_TIME,
//            features = mapOf(
//                    "P5000_SUMMER_VISIBLE" to true,
//                    "P5000_UPDATES_VISIBLE" to true,
//                    "X010_X009_VISIBLE" to true,
//                    "ADMIN_NOTIFICATION_MESSAGE" to true
//            ),
//        )
}
