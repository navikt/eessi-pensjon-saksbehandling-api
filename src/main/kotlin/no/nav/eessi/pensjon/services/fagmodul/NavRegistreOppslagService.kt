package no.nav.eessi.pensjon.services.fagmodul

import no.nav.eessi.pensjon.utils.counter
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class NavRegistreOppslagService(private val fagmodulUntToRestTemplate: RestTemplate) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(NavRegistreOppslagService::class.java) }

    private final val hentPesoninformasjonTellerNavn = "eessipensjon_frontend-api.hentpesoninformasjon"
    private val hentPesoninformasjonVellykkede = counter(hentPesoninformasjonTellerNavn, "vellykkede")
    private val hentPesoninformasjonFeilede = counter(hentPesoninformasjonTellerNavn, "feilede")
    private final val hentLandkoderTellerNavn = "eessipensjon_frontend-api.hentlandkoder"
    private val hentLandkoderVellykkede = counter(hentLandkoderTellerNavn, "vellykkede")
    private val hentLandkoderFeilede = counter(hentLandkoderTellerNavn, "feilede")

    /**
     * Henter ut personinformasjon fra TPS via PersonV3
     * @param aktoerId
     * @return Personinformasjon
     */
    fun hentPersoninformasjon(aktoerId: String): Personinformasjon? {
        val path = "/personinfo/$aktoerId"
        val uriParams = mapOf("aktoerid" to aktoerId)
        val builder = UriComponentsBuilder.fromUriString(path).buildAndExpand(uriParams)
        val httpEntity = HttpEntity("")

        try {
            logger.info("Kaller fagmodulen for å hente personinformasjon")
            val response = fagmodulUntToRestTemplate.exchange(builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    Personinformasjon::class.java)
            if (!response.statusCode.isError) {
                hentPesoninformasjonVellykkede.increment()
                return response.body
            } else {
                hentPesoninformasjonFeilede.increment()
                throw PersonInformasjonException("Noe gikk galt under henting av persjoninformasjon fra fagmodulen: ${response.statusCode}")
            }
        } catch (ex: Exception) {
            hentPesoninformasjonFeilede.increment()
            logger.error("Noe gikk galt under henting av persjoninformasjon fra fagmodulen: ${ex.message}")
            throw PersonInformasjonException("Feil ved henting av Personinformasjon")
        }
    }


    fun hentPersoninformasjonNavn(aktoerId: String): String {
        return hentPersoninformasjon(aktoerId)?.fulltNavn ?: "N/A"
    }


    fun landkoder(): List<String> {
        val path = "/landkoder/landkoder2"
        logger.info("Kaller fagmodulen: $path")

        val builder = UriComponentsBuilder.fromPath(path)
        val httpEntity = HttpEntity("", HttpHeaders())

        try {
            val response = fagmodulUntToRestTemplate.exchange(
                    builder.toUriString(),
                    HttpMethod.GET,
                    httpEntity,
                    String::class.java)

            if (!(response.statusCode.isError || response.body == null || response.body!!.isEmpty())) {
                hentLandkoderVellykkede.increment()
                return mapJsonToAny(response.body!!, typeRefs())
            }
            hentLandkoderFeilede.increment()
            throw LandkodeException("Feil under listing av landkoder")
        } catch(ex: Exception) {
            hentLandkoderFeilede.increment()
            throw ex
        }
    }


}
