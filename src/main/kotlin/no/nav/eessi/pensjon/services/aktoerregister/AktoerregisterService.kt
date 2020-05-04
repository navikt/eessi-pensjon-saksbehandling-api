package no.nav.eessi.pensjon.services.aktoerregister
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

import com.fasterxml.jackson.module.kotlin.readValue
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.eessi.pensjon.metrics.MetricsHelper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.util.*
import javax.annotation.PostConstruct

data class Identinfo(
        val ident: String,
        val identgruppe: String,
        val gjeldende: Boolean
)

data class IdentinfoForAktoer(
        val identer: List<Identinfo>?,
        val feilmelding: String?
)

@Service
class AktoerregisterService(val aktoerregisterRestTemplate: RestTemplate,
                            @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())) {

    private val logger = LoggerFactory.getLogger(AktoerregisterService::class.java)

    @Value("\${NAIS_APP_NAME}")
    lateinit var appName: String

    private lateinit var aktoerregister: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        aktoerregister = metricsHelper.init("aktoerregister")
    }


    fun hentGjeldendeNorskIdentForAktorId(aktorid: String): String {
        val response = doRequest(aktorid, "NorskIdent")
        validateResponse(aktorid, response)
        return response.getValue(aktorid).identer!![0].ident
    }

    fun hentGjeldendeAktorIdForNorskIdent(norskIdent: String): String {
        val response = doRequest(norskIdent, "AktoerId")
        validateResponse(norskIdent, response)
        return response.getValue(norskIdent).identer!![0].ident
    }

    private fun validateResponse(aktorid: String, response: Map<String, IdentinfoForAktoer>) {
        if (response[aktorid] == null)
            throw AktoerregisterIkkeFunnetException("Ingen identinfo for $aktorid ble funnet")

        val identInfoForAktoer = response[aktorid]!!

        if (identInfoForAktoer.feilmelding != null)
            throw AktoerregisterException(identInfoForAktoer.feilmelding)

        if (identInfoForAktoer.identer == null || identInfoForAktoer.identer.isEmpty())
            throw AktoerregisterIkkeFunnetException("Ingen identer returnert for $aktorid")

        if (identInfoForAktoer.identer.size > 1) {
            logger.info("Identer returnert fra aktoerregisteret:")
            identInfoForAktoer.identer.forEach {
                logger.info("ident: ${it.ident}, gjeldende: ${it.gjeldende}, identgruppe: ${it.identgruppe}")
            }
            throw AktoerregisterException("Forventet 1 ident, fant ${identInfoForAktoer.identer.size}")
        }
    }

    private fun doRequest(ident: String,
                          identGruppe: String,
                          gjeldende: Boolean = true): Map<String, IdentinfoForAktoer> {
        val headers = HttpHeaders()
        headers["Nav-Personidenter"] = ident
        headers["Nav-Consumer-Id"] = appName
        headers["Nav-Call-Id"] = UUID.randomUUID().toString()
        val requestEntity = HttpEntity<String>(headers)

        val uriBuilder = UriComponentsBuilder.fromPath("/identer")
                .queryParam("identgruppe", identGruppe)
                .queryParam("gjeldende", gjeldende)
        logger.info("Kaller aktørregisteret: /identer")
        var resp : ResponseEntity<String>

        return aktoerregister.measure {
            try {
                resp = aktoerregisterRestTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    String::class.java)

            } catch (ex: HttpStatusCodeException) {
                logger.error("En feil oppstod under kall til aktørregisteret ex: $ex body: ${ex.responseBodyAsString}")
                throw ex
            } catch (ex: Exception) {
                logger.error("En feil oppstod under kall til aktørregisteret ex: $ex")
                throw ex
            }
            jacksonObjectMapper().readValue(resp.body!!)
        }
    }
}

class AktoerregisterIkkeFunnetException(message: String?) : Exception(message)

class AktoerregisterException(message: String) : Exception(message)
