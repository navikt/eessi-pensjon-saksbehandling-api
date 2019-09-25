package no.nav.eessi.pensjon.services.person.tps

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ResponseStatus

@Component
class PersonV3Service(private val service: PersonV3) {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PersonV3Service::class.java) }
    private val hentperson_teller_navn = "eessipensjon_fagmodul.hentperson"
    private val hentperson_teller_type_vellykkede = counter(hentperson_teller_navn, "vellykkede")
    private val hentperson_teller_type_feilede = counter(hentperson_teller_navn, "feilede")

    final fun counter(name: String, type: String): Counter {
        return Metrics.counter(name, "type", type)
    }

    @Throws(PersonV3IkkeFunnetException::class, PersonV3SikkerhetsbegrensningException::class)
    fun hentPerson(fnr: String): HentPersonResponse {
        logger.info("Henter person fra PersonV3Service")

        val request = HentPersonRequest().apply {
            withAktoer(PersonIdent().withIdent(
                    NorskIdent().withIdent(fnr)))

            withInformasjonsbehov(listOf(
                    Informasjonsbehov.ADRESSE,
                    Informasjonsbehov.FAMILIERELASJONER
            ))
        }
        try {
            logger.info("Kaller PersonV3.hentPerson service")
            val resp = service.hentPerson(request)
            hentperson_teller_type_vellykkede.increment()
            return resp
        } catch (personIkkefunnet: HentPersonPersonIkkeFunnet) {
            logger.error("Kaller PersonV3.hentPerson service Feilet: $personIkkefunnet")
            hentperson_teller_type_feilede.increment()
            throw PersonV3IkkeFunnetException(personIkkefunnet.message)
        } catch (personSikkerhetsbegrensning: HentPersonSikkerhetsbegrensning) {
            logger.error("Kaller PersonV3.hentPerson service Feilet $personSikkerhetsbegrensning")
            hentperson_teller_type_feilede.increment()
            throw PersonV3SikkerhetsbegrensningException(personSikkerhetsbegrensning.message)
        }
    }
}

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    class PersonV3IkkeFunnetException(message: String?) : Exception(message)

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    class PersonV3SikkerhetsbegrensningException(message: String?) : Exception(message)