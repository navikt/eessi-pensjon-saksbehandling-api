package no.nav.eessi.pensjon.services.auth

class AuthorisationService {

    /**
     * Det er kun saksbehandlere i PESYS-PSAK som jobber på utenlandsområdet som skal ha tilgang til EESSI-Pensjon.
     * Det er ingen som bare har behov for les-tilgang. All informasjon i EESSI-Pensjon kan leses som SED-dokumenter
     * i dokumentoversikten hentet fra JOARK.
     *
     * Bruker rollene "Saksbehandler" og "Utland" for å sjekke tilgang.
     *
     * @param roller er hentet fra AD. Summen av roller beskriver hva en saksbehandler har tilgang til i PESYS.
     *               EESSI-Pensjon bruker de samme rollene som PESYS-PSAK gjør for å sjekke tilgang.
     */
    fun harTilgangTilEessiPensjon(roller: List<AdRolle>): Boolean {
        return roller.containsAll(Tilgang.EESSI_PENSJON.grupper)
    }

    /**
     * Roller i AD for PESYS-saksbehandlere er lagt opp litt rart. Alle får rollen "saksbehandler". indirekte betyr
     * at saksbehandler har tilgang til alderspensjon, barnepensjon og etterlattepensjon (gjenlevende).
     *
     * Når så rollen "Uføretrygd/pensjon" blir gitt til en saksbehandler så får saksbehandler ikke lengre lov å
     * jobbe på alderspensjon, barnepensjon og etterlattepensjon. Saksbehandler får derimot lov til å jobbe med
     * uføretrygd.
     *
     * NB! Unntak for reglen over:
     *     Uføre saksbehandler har tilgang til etterlattepensjon (gjenlevende) når det er en ren
     *     utenlandssak. Uføretrygd saksbehandler må derfor få tilgang til etterlattepensjon selv om de fleste sakene
     *     skal håndteres av en saksbehandler som ikke jobber med uføretrygd.
     *
     * Geografisk kontroll:
     * I PESYS så sjekkes det på om saksbehandler jobber på samme enhet som saken er fordelt til. Hvis det er tilfelle
     * så har saksbehandler tilgang til saken. Dette har vi valgt å ikke implementere i EESSI-Pensjon siden det gir
     * minimalt med ekstra sikkerhet.
     *
     * @param roller er hentet fra AD. Summen av roller beskriver hva en saksbehandler har tilgang til i PESYS.
     *               EESSI-Pensjon bruker de samme rollene som PESYS-PSAK gjør for å sjekke tilgang.
     * @param pesysSakstype er hvilken type sak som er opprettet i PSAK og som BUCen skal knyttes til.
     */
    fun harTilgangTilPesysSak(roller: List<AdRolle>, pesysSakstype: PesysSakstype?): Boolean {

        if (pesysSakstype == null){
            throw AuthorisationUkjentSakstypeException("Ukjent sakstype fra PESYS: $pesysSakstype")
        }

        return when (pesysSakstype) {
            PesysSakstype.ALDERSPENSJON, PesysSakstype.BARNEPENSJON -> !erMedlemAvUfore(roller)
            PesysSakstype.UFORETRYGD -> erMedlemAvUfore(roller)
            PesysSakstype.GJENLEVENDE -> return true
        }
    }

    /**
     * Saksbehandler har i utgangspunktet lov til å jobbe på alle brukere (borgere) i Norge men med noen unntak
     *      1. Får bare jobbe på brukere ansatt i NAV hvis de har en AD-rolle "Utvidet" (Ansatt i NAV)
     *      2. Får bare jobbe på brukere med strengt fortrolig adresse hvis de har tilsvarende AD-roller
     *         fra PESYS og GOSYS
     *      3. Får bare jobbe på brukere med fortrolig adresse hvis de har tilsvarende AD-roller
     *         fra PESYS og GOSYS
     *
     * @param roller er hentet fra AD. Summen av roller beskriver hva en saksbehandler har tilgang til i PESYS.
     *               EESSI-Pensjon bruker de samme rollene som PESYS-PSAK gjør for å sjekke tilgang.
     * @param saksbehandlerFnr er fnr til saksbehandler som er pålogget.
     * @param brukerFNR er FNR til bruker i saken. Hvis det er flere brukere i saken må tjenesten kalles en gang
     *                  pr. bruker.
     * @param brukerAnsattINav er et flagg som fortelle om bruker er ansatt i NAV. Ikke alle saksbehandlere har
     *                         tilgang til å behandle en sak på en bruker som er ansatt i NAV.
     * @param adressesperre angir om bruker har kode 6, kode 7 eller normal behandling av adresseinformasjon.
     *                      NB! "Adresse" er alt som kan brukes til identifisere en bruker og brukes til
     *                      å finne ut hvor vedkommende bor.
     */
    fun harTilgangTilBrukerISaken(
        roller: List<AdRolle>,
        brukerAnsattINav: Boolean,
        adressesperre: Adressesperre): Boolean {
        if (brukerAnsattINav){
            return erMedlemAvNavAnsatt(roller)
        }
        if (adressesperre == Adressesperre.STRENGT_FORTROLIG_ADRESSE){
            return erMedlemAvStrengtFortrolig(roller)
        }
        if (adressesperre == Adressesperre.FORTROLIG_ADRESSE){
            return erMedlemAvFortrolig(roller)
        }
        return true
    }

    /**
     * Saksbehandlere som jobber med uføretrygd kan håndtere etterlattepensjon for gjenlevende ektefelle.
     * De kan ikke håndtere barnepensjon. Nå viser det seg at det kan være vanskelig å finne i SEDen
     * et felt som sier om det gjelder barnepensjon eller etterlattepensjon for ektefelle. Koden under tar
     * derfor høyde for at denne informasjonen ikke er kjent. Da skal saksbehandler få tilgang.
     *
     * Bruker rollene "Saksbehandler" og "Utland" for å sjekke tilgang.
     *
     * @param roller er hentet fra AD. Summen av roller beskriver hva en saksbehandler har tilgang til i PESYS.
     *               EESSI-Pensjon bruker de samme rollene som PESYS-PSAK gjør for å sjekke tilgang.
     * @param buctype er typen som EU har satt på BUCene. De forskjellige BUCene har forskjellig formål.
     *                Formålet tilsvarer noen ganger sakstypen fra PESYS, men mange BUCer kan brukes på tvers
     *                av sakstype (og ytelsestypene). Det kan derfor ikke begrenses så mye på hvem som skal
     *                ha tilgang til en bucType.
     */
    fun harTilgangTilBuc(roller: List<AdRolle>, buctype: Buctype, sedPensjonstype: SedPensjonstype): Boolean{
        if (buctype == Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON){
            if (sedPensjonstype == SedPensjonstype.UKJENT){
                // Når pensjonstypen ikke er kjent må saksbehandler få tilgang
                return true
            }
            if (sedPensjonstype == SedPensjonstype.BARNEPENSJON){
                return !erMedlemAvUfore(roller)
            }
            return true
        }
        if (buctype == Buctype.PBUC03_KRAV_OM_UFORETRYGD){
            return erMedlemAvUfore(roller)
        }
        return true
    }

    private fun erMedlemAvUfore(roller: List<AdRolle>): Boolean {
        return roller.containsAll(listOf(AdRolle.PENSJON_UFORE))
    }

    private fun erMedlemAvNavAnsatt(roller: List<AdRolle>): Boolean {
        return roller.containsAll(listOf(AdRolle.PENSJON_NAV_ANSATT, AdRolle.GOSYS_NAV_ANSATT))
    }

    private fun erMedlemAvFortrolig(roller: List<AdRolle>): Boolean {
        return roller.containsAll(listOf(AdRolle.PENSJON_FORTROLIG, AdRolle.GOSYS_FORTROLIG))
    }

    private fun erMedlemAvStrengtFortrolig(roller: List<AdRolle>): Boolean {
        return roller.containsAll(listOf(AdRolle.PENSJON_STRENGT_FORTROLIG, AdRolle.GOSYS_STRENGT_FORTROLIG))
    }
}

/**
 * Type roller som som PESYS saksbehandlere kan ha i AD og som EESSI-Pensjon anvender for å sjekke tilgang.
 */

enum class AdRolle(val rollenavn: String) {
    PENSJON_UTLAND("0000-GA-Pensjon_Utland"),
    PENSJON_SAKSBEHANDLER("0000-GA-PENSJON_SAKSBEHANDLER"),
    PENSJON_UFORE("0000-GA-pensjon_ufore"),
    GOSYS_NAV_ANSATT("0000-GA-GOSYS_UTVIDET"),
    PENSJON_NAV_ANSATT("0000-GA-Pensjon_UTVIDET"),
    GOSYS_STRENGT_FORTROLIG("0000-GA-GOSYS_KODE6"),
    PENSJON_STRENGT_FORTROLIG("0000-GA-Pensjon_KODE6"),
    GOSYS_FORTROLIG("0000-GA-GOSYS_KODE7"),
    PENSJON_FORTROLIG("0000-GA-PENSJON_KODE7")
}

enum class Adressesperre(val adressesperre: String){
    INGEN_ADRESSESPERRE(""),
    FORTROLIG_ADRESSE("KODE7"),
    STRENGT_FORTROLIG_ADRESSE("KODE6")
}

/**
 * Sakstyper som hentes fra saken i PESYS.
 */
enum class PesysSakstype(val pesysSakstype: String) {
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    BARNEPENSJON("BARNEPENSJON"),
    GJENLEVENDE("GJENLEVENDE")
}

/**
 * BUC-er som har spesielle tilgangsregler.
 */
enum class Buctype(val buctype: String){
    PBUC01_KRAV_OM_ALDER("PBUC01"),
    PBUC02_KRAV_OM_ETTERLATTEPENSJON("PBUC02"),
    PBUC03_KRAV_OM_UFORETRYGD("PBUC03")
}

/**
 * Pensjonstyper som er hentet fra SED. Skal brukes til å sjekke tilgang.
 * Usikkert om pensjonstypen lar seg hente ut i alle tilfeller.
 */
enum class SedPensjonstype(val pensjonstype: String){
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    ETTERLATTEPENSJON("ETTERLATTE"),
    BARNEPENSJON("BARNEPENSJON"),
    UKJENT("UKJENT")
}

/**
 * Liste av roller som saksbehandler må ha i AD for å få tilgang til EESSI-Pensjon
 */
enum class Tilgang(var grupper: List<AdRolle>) {
    EESSI_PENSJON(listOf(AdRolle.PENSJON_UTLAND, AdRolle.PENSJON_SAKSBEHANDLER))
}

/**
 * Feil som kan kastes: Ukjent sakstype fra PESYS. EESSI-Pensjon håndterer ikke den sakstypen.
 */
class AuthorisationUkjentSakstypeException(message: String?) : Exception(message)
