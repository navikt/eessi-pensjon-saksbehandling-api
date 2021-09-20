package no.nav.eessi.pensjon.eux

import no.nav.eessi.pensjon.eux.model.buc.BucType
import no.nav.eessi.pensjon.eux.model.buc.Institusjon
import no.nav.eessi.pensjon.security.sts.typeRef
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate

@Component
class EuxKlient(private val euxOidcRestTemplate: RestTemplate) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(EuxKlient::class.java) }

    internal fun hentInstitusjoner(bucType: BucType, landkode: String = ""): List<Institusjon> {
        logger.info("Henter intstitusjoner (BucType: $bucType, Landkode: $landkode")

        val response = execute {
            euxOidcRestTemplate.exchange(
                "/institusjoner?BuCType=$bucType&LandKode=$landkode",
                HttpMethod.GET,
                null,
                typeRef<List<Institusjon>>()
            )
        }

        return response?.body ?: emptyList()
    }


    private fun <T> execute(block: () -> T): T? {
        try {
            return block.invoke()
        } catch (ex: Exception) {
            if (ex is HttpStatusCodeException && ex.statusCode == HttpStatus.NOT_FOUND)
                return null

            logger.error("Ukjent feil oppsto: ", ex)
            throw ex
        }
    }

}
