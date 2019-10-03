package no.nav.eessi.pensjon.services.auth

class AuthorisationService {

    fun
            harTilgangTilEessiPensjon(gruppemedlemskap: List<AD_Rolle>): Boolean {

        return gruppemedlemskap.containsAll(Tilgang.EESSI_PENSJON.grupper)
    }

    fun harTilgangTilPESYS_Sak(gruppemedlemskap: List<AD_Rolle>, sakType: SakType): Boolean {

        if (sakType == SakType.ALDERSPENSJON) {
            if (gruppemedlemskap.containsAll(listOf(AD_Rolle.PENSJON_UFORE))) {
                return false
            }
            return true
        }
        if (sakType == SakType.UFORETRYGD) {
            if (gruppemedlemskap.containsAll(listOf(AD_Rolle.PENSJON_UFORE))) {
                return true
            }
            return false
        }
/*
        ELLERS HVIS PESYS-saken er Barnepensjon SÅ
HVIS PLB har tilleggsrollen Uføre SÅ
Ikke tilgang - "Saksbehandler med tilleggsrollen 0000-GA-pensjon_ufore har ikke tilgang til behandle en barnepensjon"
ELLERS
Tilgang til saken
SLUTT HVIS
ELLERS HVIS PESYS-saken er Gjenlevendepensjon SÅ
HVIS PLB har tilleggsrollen Uføre SÅ
Ikke tilgang - "Saksbehandler med tilleggsrollen 0000-GA-pensjon_ufore har ikke tilgang til behandle en gjenlevendepensjon"
ELLERS
Tilgang til saken
SLUTT HVIS
SLUTT HVIS
         */

        /* midlertidig return*/
        return true
    }

    fun harTilgangTilBrukere_I_Saken(
        gruppemedlemskap: List<AD_Rolle>
        , ansatt_I_NAV: Boolean
        , fortrolig: Boolean
        , strengtFortrolig: Boolean
    ): Boolean {
/*
HVIS PLB er samme person som bruker SÅ
Ikke tilgang - "Saksbehandler har ikke lov å jobbe på seg selv"
ELLERS HVIS bruker er ansatt i NAV SÅ
HVIS PLB ikke har tilleggsrollen Utvidet SÅ
Ikke tilgang - "Må ha tilleggsrollen 0000-GA-GOSYS_UTVIDET eller 0000-GA-Pensjon_UTVIDET i PESYS for å få tilgang til bruker som er ansatt i NAV"
SLUTT HVIS
ELLERS HVIS bruker er merket med kode 6 SÅ
HVIS PLB ikke har tilleggsrollen Kode 6 SÅ
Ikke tilgang - "Må ha tilleggsrollen 0000-GA-GOSYS_KODE6 eller 0000-GA-Pensjon_KODE6 i PESYS for å få tilgang til bruker merket med kode 6"
SLUTT HVIS
ELLERS HVIS bruker er merket med kode 7 SÅ
HVIS PLB ikke har tilleggsrollen Kode 7 SÅ
Ikke tilgang - "Må ha tilleggsrollen 0000-GA-GOSYS_KODE7 eller 0000-GA-Pensjon_KODE7 i PESYS for å få tilgang til bruker merket med kode 6"
SLUTT HVIS
ELLERS
Tilgang til bruker
SLUTT HVIS
 */
        return false
    }

    fun harTilgangTil_BUC(gruppemedlemskap: List<AD_Rolle>, bucType: String): Boolean{
        /*
        HVIS BUC01 - Krav om alder SÅ
// Ingen begrensning. Det viser seg at uføresaksbehandler jobber med alderspensjon for rene utenlandssaker.


ELLERS HVIS BUC02 - Krav om gjenlevendepensjon/ etterlattepensjon SÅ
Hent ut feltet P2100→3.2.1.pensjonstype
NB! Det var feilt felt over. Vi har ikke funnet det riktige feltet ennå så denne sjekken faller ut hvis vi ikke finner opplysningen i SEDen.
HVIS pensjonstype er barnepensjon SÅ
HVIS PLB har tilleggsrollen Uføre SÅ
Ikke tilgang - "Saksbehandler tilleggsrollen 0000-GA-pensjon_ufore har ikke tilgang til behandle en barnepensjon"
ELLERS
Tilgang til BUC
SLUTT HVIS
ELLERS HVIS BUC03 - Krav uføretrygd SÅ
HVIS PLB har tilleggsrollen Uføre SÅ
Tilgang til BUC
ELLERS
Ikke tilgang - "Saksbehandler uten tilleggsrollen 0000-GA-pensjon_ufore har ikke tilgang til behandle en uføretrygd"
SLUTT HVIS
ELLERS
Tilgang til BUC - "Resten av BUCene kan brukes av alle saksbehandlere"
SLUTT HVIS

         */
        return false
    }
}

enum class AD_Rolle(val gruppeNavn: String) {
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

enum class SakType(sakType: String) {
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    BARNEPENSJON("BARNEPENSJON"),
    GJENLEVENDE("GJENLEVENDE")
}

enum class BUC_Type(bucType: String){
    PBUC01("PBUC01"),
    PBUC02_KRAV_OM_ETTERLATTEPENSJON("PBUC02"),
    PBUC03_KRAV_OM_UFORETRYGD("PBUC03"),
    PBUC04("PBUC04"),
    PBUC05("PBUC05"),
    PBUC06("PBUC06"),
    PBUC07("PBUC07"),
    PBUC08("PBUC08"),
    PBUC09("PBUC09")
    // Mangler mange BUC-er. Skal vi heller bruke String i stedet for ENUM?
    // Kan da lettere legge til nye BUC-er uten å endre på koden
    // NB! Da vil det bli tilgang til alle nye BUCer hvis det ikke legges inn en sperre for den nye BUCen
}

enum class Tilgang(var grupper: List<AD_Rolle>) {
    EESSI_PENSJON(listOf(AD_Rolle.PENSJON_UTLAND, AD_Rolle.PENSJON_SAKSBEHANDLER))
}

