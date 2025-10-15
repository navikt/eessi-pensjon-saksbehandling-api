package no.nav.eessi.pensjon.api.userinfo

import io.getunleash.Unleash
import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.metrics.MetricsHelper
import no.nav.eessi.pensjon.unleash.FeatureToggleService
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

class UserInfoControllerToggleTest {
    private lateinit var featureToggleService: FeatureToggleService
    private lateinit var userInfoController: UserInfoController
    private var tokenValidationContextHolder = mockk<SpringTokenValidationContextHolder>()
    private var unleash: Unleash = mockk(relaxed = true)

    @BeforeEach
    fun mockSetup() {
        featureToggleService = FeatureToggleService(unleash,  tokenValidationContextHolder)
        userInfoController = UserInfoController(tokenValidationContextHolder, featureToggleService, MetricsHelper.ForTest())

        every { unleash.more().featureToggleNames } returns listOf("P5000_UPDATES_VISIBLE", "EESSI_ADMIN")
        every { unleash.isEnabled("P5000_UPDATES_VISIBLE") } returns true
        every { unleash.isEnabled("EESSI_ADMIN") } returns false

    }

    @Test
    fun `getAvailableToggles skal vises en liste med toggles og om de er enabled`() {
        createMockedToken()

        val result = userInfoController.getAvailableToggles()
        assertEquals(
            "[FeatureToggleStatus(name=P5000_UPDATES_VISIBLE, enabled=false), FeatureToggleStatus(name=EESSI_ADMIN, enabled=false)]",
            result?.body.toString()
        )
    }

    private fun createMockedToken() {
        val payload = """{"sub":"12345678910","exp":${System.currentTimeMillis() / 1000 + 3600}}"""
        val dummyJwt = buildString {
            append(Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"HS256"}""".toByteArray()))
            append('.')
            append(Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray()))
            append(".signature")
        }
        val context = TokenValidationContext(mapOf("issuer" to JwtToken(dummyJwt)))
        every { tokenValidationContextHolder.getTokenValidationContext() } returns context
    }

}
