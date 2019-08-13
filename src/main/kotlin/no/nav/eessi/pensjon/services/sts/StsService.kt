package no.nav.eessi.pensjon.services.sts

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.jodah.expiringmap.ExpiringMap
import no.nav.eessi.pensjon.utils.counter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

private val logger = LoggerFactory.getLogger(SecurityTokenExchangeService::class.java)

data class SecurityTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String?,
        @JsonProperty("token_type")
        val tokenType: String?,
        @JsonProperty("expires_in")
        val expiresIn: Long?
)

@Service
class SecurityTokenExchangeService(@Qualifier("stsRestTemplate") val stsRestTemplate: RestTemplate) {

    private val tokenCache = ExpiringMap.builder().variableExpiration().build<String, String>()

    final val hentOidctokenForSystembrukerTellerNavn = "eessipensjon_mottak.hentoidctokenforsystembruker"
    val hentOidctokenForSystembrukerVellykkede = counter(hentOidctokenForSystembrukerTellerNavn, "vellykkede")
    val hentOidctokenForSystembrukerFeilede = counter(hentOidctokenForSystembrukerTellerNavn, "feilede")

    fun getSystemOidcToken(): String {
        val token = tokenCache["token"]
        if(!token.isNullOrEmpty()) {
            logger.debug("Using cached token: $token")
            return token
        }

        val uri = UriComponentsBuilder.fromPath("/")
                .queryParam("grant_type", "client_credentials")
                .queryParam("scope", "openid")
                .build().toUriString()

        try {
            val responseEntity = stsRestTemplate.exchange(uri, HttpMethod.GET, null, SecurityTokenResponse::class.java)
            validateResponse(responseEntity)
            val accessToken = responseEntity.body!!.accessToken

            val exp = extractExpirationField(accessToken)
            // Make the cache-entry expire 30 seconds before the token is no longer valid, to be sure not to use any invalid tokens
            val expiresInSeconds = Duration.between(LocalDateTime.now(), exp).seconds.minus(30)

            tokenCache.put("token", accessToken, expiresInSeconds, TimeUnit.SECONDS)

            logger.debug("Added token to cache, expires in $expiresInSeconds seconds")
            hentOidctokenForSystembrukerVellykkede.increment()
            return accessToken!!
        } catch (ex: RestClientException) {
            logger.error("Noe gikk galt ved rest kall til STS, ${ex.message}")
            hentOidctokenForSystembrukerFeilede.increment()
            throw ex
        }
    }

    private fun validateResponse(responseEntity: ResponseEntity<SecurityTokenResponse>) {
        if (responseEntity.statusCode.isError)
            throw RuntimeException("SecurityTokenExchange received http-error ${responseEntity.statusCode}:${responseEntity.statusCodeValue}")
        if(responseEntity.body?.accessToken == null)
            throw RuntimeException("Fikk ikke et OIDCtoken fra STS")
    }

    private fun extractExpirationField(jwtString: String?): LocalDateTime {
        val parts = jwtString?.split('.')
        val expirationTimestamp = jacksonObjectMapper().readTree(Base64.getDecoder().decode(parts?.get(1))).at("/exp")
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(expirationTimestamp.asLong()), ZoneId.systemDefault())
    }
}
