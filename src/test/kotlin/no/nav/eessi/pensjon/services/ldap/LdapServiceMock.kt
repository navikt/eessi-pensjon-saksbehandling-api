package no.nav.eessi.pensjon.services.ldap

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.lang.IllegalArgumentException
import java.util.regex.Pattern
import javax.naming.NamingException

@Profile("integrationtest", "test")
@Service
class LdapServiceMock(): BrukerInformasjonService {

    // Pattern for NAV brukerident, f.eks Z123456
    private val IDENT_PATTERN = Pattern.compile("^[a-zA-Z][0-9]{6}") // sjekke om det kan væe 6 sifre her i stedet
    private val logger = LoggerFactory.getLogger(LdapServiceMock::class.java)

    /**
     *
     * Saksbehandler X000000 angir
     *               |||||||
     *               |||||| -------- 7 gir tilgang til kode 7 brukere
     *               ||||||
     *               ||||| --------- 6 gir tilgang til kode 6 brukere
     *               |||||
     *               |||| ---------- Ubrukt
     *               ||||
     *               ||| ----------- Ubrukt
     *               |||
     *               || ------------ 3 gir tilgang til NAV-ansatte
     *               ||
     *               | ------------- 2 gir tilgang til utland
     *               |
     *                -------------- Saksbehandler på: A Alderspensjon, U Uføre
     */

    override fun hentBrukerInformasjon(ident: String): BrukerInformasjon {
        logger.info("Henter bruker-informasjon fra LDAP")
        if (ident.isEmpty()) {
            logger.warn("Brukerident mangler")
            throw IllegalArgumentException("Brukerident mangler")
        }

        // Unngår å søke etter fødselsnummer i AD
        val matcher = IDENT_PATTERN.matcher(ident)
        if (!matcher.matches()) {
            logger.error("Identen: $ident er ikke i et format vi kan søke etter")
            return BrukerInformasjon(ident, emptyList())
        }

        val rolleListe = mutableListOf<String>()

        if (ident == "X000000") {
            throw NamingException("Mockfeil ved kall til tjeneste")
        }


        // Alderspensjon - Saksbehandler
        if(ident.substring(0,1) == "A") {
            rolleListe.add("0000-GA-PENSJON_SAKSBEHANDLER")
            rolleListe.add("0000-ga-eessi-basis")
            rolleListe.add("0000-ga-eessi-clerk-pensjon")
        }

        // Uføre - Saksbehandler
        if(ident.substring(0,1) == "U") {
            rolleListe.add("0000-GA-PENSJON_SAKSBEHANDLER")
            rolleListe.add("0000-GA-pensjon_ufore")
            rolleListe.add("0000-ga-eessi-basis")
            rolleListe.add("0000-GA-EESSI-CLERK-UFORE")
        }

        // Utland
        if(ident.substring(1,2) == "2") {
            rolleListe.add("0000-GA-Pensjon_Utland")
        }

        // Nav-ansatte
        if(ident.substring(2,3) == "3") {
            rolleListe.add("0000-GA-GOSYS_UTVIDET")
            rolleListe.add("0000-GA-Pensjon_UTVIDET")
        }

        // Strengt fortrolig adresse
        if(ident.substring(5,6) == "6") {
            rolleListe.add("0000-GA-GOSYS_KODE6")
            rolleListe.add("0000-GA-Pensjon_KODE6")
        }

        // Fortrolig adresse
        if(ident.substring(6,7) == "7") {
            rolleListe.add("0000-GA-GOSYS_KODE7")
            rolleListe.add("0000-GA-PENSJON_KODE7")
        }

        listOf(rolleListe)
        val brukerInformasjon =
            BrukerInformasjon(ident, rolleListe)

        return brukerInformasjon
    }
}