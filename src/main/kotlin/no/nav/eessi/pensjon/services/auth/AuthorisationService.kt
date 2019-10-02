package no.nav.eessi.pensjon.services.auth

class AuthorisationService {

    fun harTilgangTilEessiPensjon(gruppemedlemskap: List<Gruppe>): Boolean {

        return gruppemedlemskap.containsAll(Tilgang.EESSI_PENSJON.grupper)
    }

    fun harTilgangTilPESYS_Sak(gruppemedlemskap: List<Gruppe>, sakType: SakType): Boolean {

        /* if (sakType == SakType.ALDERSPENSJON){
            if(gruppemedlemskap.containsAll(Gruppe.PENSJON_UFORE)){
                return false
            }
            return true
        }
        if(sakType == SakType.UFORETRYGD){
            if(gruppemedlemskap.containsAll(Gruppe.PENSJON_UFORE)){
                return true
            }
            return false
        }
             */
        return true
    }
}

enum class Gruppe (val gruppeNavn: String) {
    PENSJON_UTLAND("0000-GA-Pensjon_Utland"),
    PENSJON_SAKSBEHANDLER("0000-GA-PENSJON_SAKSBEHANDLER"),
    PENSJON_UFORE("0000-GA-pensjon_ufore"),
    GOSYS_UTVIDET("0000-GA-GOSYS_UTVIDET"),
    PENSJON_UTVIDET("0000-GA-Pensjon_UTVIDET"),
    GOSYS_KODE6("0000-GA-GOSYS_KODE6"),
    PENSJON_KODE6("0000-GA-Pensjon_KODE6"),
    GOSYS_KODE7("0000-GA-GOSYS_KODE7"),
    PENSJON_KODE7("0000-GA-PENSJON_KODE7")
}

enum class SakType (sakType: String){
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    BARNEPENSJON("BARNEPENSJON"),
    GJENLEVENDE("GJENLEVENDE")
}

enum class Tilgang(var grupper : List<Gruppe>) {
    EESSI_PENSJON(listOf(Gruppe.PENSJON_UTLAND, Gruppe.PENSJON_SAKSBEHANDLER))
}

