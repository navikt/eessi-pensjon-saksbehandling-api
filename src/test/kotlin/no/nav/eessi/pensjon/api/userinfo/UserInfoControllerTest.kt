package no.nav.eessi.pensjon.api.userinfo

import com.nimbusds.jwt.JWTClaimsSet
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkBeans
import com.ninjasquad.springmockk.SpykBean
import com.ninjasquad.springmockk.SpykBeans
import io.micrometer.core.instrument.MeterRegistry
import io.mockk.every
import io.mockk.spyk
import no.nav.eessi.pensjon.api.storage.StorageController
import no.nav.eessi.pensjon.gcp.GcpStorageService
import no.nav.eessi.pensjon.models.BrukerInformasjon
import no.nav.eessi.pensjon.services.auth.AuthorisationService
import no.nav.eessi.pensjon.unleash.FeatureToggleService
import no.nav.eessi.pensjon.unleash.FeatureToggleStatus
import no.nav.eessi.pensjon.utils.mapAnyToJson
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.toJson
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.RestTemplate
import java.util.*

@ActiveProfiles(profiles = ["unsecured-webmvctest"])
@EnableMockOAuth2Server
@WebMvcTest(UserInfoController::class)
@MockkBeans(
    value = [
        MockkBean(name = "featureToggleService", classes = [FeatureToggleService::class]),
        MockkBean(name = "meterRegistry", classes = [MeterRegistry::class], relaxed = true),
        MockkBean(name = "gcpStorageService", classes = [GcpStorageService::class], relaxed = true),
        MockkBean(name = "storageController", classes = [StorageController::class], relaxed = true),
        MockkBean(name = "restTemplate", classes = [RestTemplate::class], relaxed = true),
    ]
)
@SpykBeans(
    value = [
        SpykBean(name = "authorisationService", classes = [AuthorisationService::class])
    ]
)
class UserInfoControllerTest {

    @Value("\${no.nav.security.jwt.issuer.aad.accepted_audience}")
    lateinit var audience: String

    private val EXPIRATION_TIME = 1300819380L

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var featureToggleService: FeatureToggleService

    @Autowired
    lateinit var mockOAuth2Server: MockOAuth2Server

    @Autowired
    lateinit var restTemplate: RestTemplate

    @BeforeEach
    fun mockSetup() {
        every { featureToggleService.getAllFeaturesForProject() } returns listOf(
            FeatureToggleStatus("P5000_SUMMER_VISIBLE", true),
            FeatureToggleStatus("P5000_UPDATES_VISIBLE", true)
        )
        every { featureToggleService.isFeatureEnabled("P5000_SUMMER_VISIBLE") } returns true
        every { featureToggleService.isFeatureEnabled("P5000_UPDATES_VISIBLE") } returns true
    }

    @Test
    fun CallingUserInfoController_getAvailableToggles() {
        val token = mockOAuth2Server.issueToken("aad", "12345678910", audience).serialize()

        val response = mockMvc.perform(
            get("/api/availableToggles")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        ).andExpect(status().isOk()).andReturn().response

        val isFeatureEnabled = mapJsonToAny<List<FeatureToggleStatus>>(response.contentAsString)
            .any { it.name == "P5000_SUMMER_VISIBLE" && it.enabled }
        assertEquals(true, isFeatureEnabled)
    }

    @Test fun `Calling UserInfoController getUserInfo returns OK response`() {
        val brukerInfo = BrukerInformasjon(ident = "12345678910", medlemAv = listOf("0000-GA-Pensjon_Utland", "0000-ga-eessi-basis"))

        every { restTemplate.exchange(any<String>(), any(), any(), String::class.java) }returns ResponseEntity(brukerInfo.toJson(), HttpStatus.OK)
            val token = mockOAuth2Server.issueToken("aad", "12345678910", audience).serialize()

        val response = mockMvc.perform(
            get("/api/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andReturn().response

        val usr =  UserInfoResponse(
            subject ="Unknown",
            role ="UNKNOWN",
            expirationTime = EXPIRATION_TIME,
            features = mapOf(
                "P5000_SUMMER_VISIBLE" to true,
                "P5000_UPDATES_VISIBLE" to true,
            ),
        )
        val expected = mapAnyToJson(
            usr.copy(expirationTime = mapJsonToAny<UserInfoResponse>(response.contentAsString).expirationTime)
        )
        assertEquals(expected, response.contentAsString)
    }

    @Test fun `Calling UserInfoController getRole`() {
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
                "P5000_UPDATES_VISIBLE" to true,
                "X010_X009_VISIBLE" to true,
                "ADMIN_NOTIFICATION_MESSAGE" to true
            ),
        )
        val response = mockMvc.perform(
            get("/api/userinfo")
                .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
        )
            .andExpect(status().isOk)
            .andReturn().response

        val result = response.contentAsString
        assertEquals(ResponseEntity.ok().body(mapAnyToJson(usr)), result)
        val resultUserInfo = mapJsonToAny<UserInfoResponse>(result.body!!)
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

//        every { userInfoController.getSubjectFromToken() } returns subject
//        every { userInfoController.getClaims() } returns twtToken
    }
}
