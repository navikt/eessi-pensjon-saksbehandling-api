package no.nav.eessi.pensjon.api.userinfo

import com.nimbusds.jwt.JWTClaimsSet
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import no.nav.eessi.pensjon.config.FeatureToggleService
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import java.util.*

class UserInfoControllerToggleTest {
    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var userInfoController: UserInfoController
    private var restTemplate: RestTemplate = mockk()

    private val EXPIRATION_TIME = 1300819380L

    @BeforeEach
    fun mockSetup() {
        featureToggleService = FeatureToggleService("eessi-pensjon", "","", mockk(relaxed = true), mockk(), restTemplate)
        userInfoController = UserInfoController(mockk(), SpringTokenValidationContextHolder(), featureToggleService)
    }

    @Test
    fun `CallingUserInfoController_getTogglesForUser`() {
        val response = responseFraUnleash()
        every {
            restTemplate.exchange(
                "eessi-pensjon/admin/features",
                HttpMethod.GET,
                any(),
                String::class.java

            )
        } returns ResponseEntity.ok().body(response)

        val result = userInfoController.getTogglesForUser()
        assertEquals(listOf("P5000_UPDATES_VISIBLE", "EESSI_ADMIN"), result?.body)
    }

    fun responseFraUnleash() = """
        {
          "features": [
            {
              "name": "P5000_UPDATES_VISIBLE",
              "description": "Gjør P5000 oppdateringer synlig i UI",
              "enabled": true,
              "strategies": [
                {
                  "name": "default",
                  "parameters": {}
                }
              ],
              "variants": [],
              "createdAt": "2023-10-02T12:34:56.789Z",
              "lastSeenAt": "2023-10-10T12:34:56.789Z",
              "impressionData": false,
              "stale": false,
              "type": "release"
            },
            {
              "name": "EESSI_ADMIN",
              "description": "Aktiverer admin funksjonalitet i EESSI",
              "enabled": true,
              "strategies": [
                {
                  "name": "default",
                  "parameters": {}
                }
              ],
              "variants": [],
              "createdAt": "2023-09-15T08:22:33.456Z",
              "lastSeenAt": null,
              "impressionData": false,
              "stale": false,
              "type": "release"
            }
          ],
          "total": 2,
          "count": 2,
          "pageSize": 20,
          "pageNumber": 1
        }
    """.trimIndent()

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
