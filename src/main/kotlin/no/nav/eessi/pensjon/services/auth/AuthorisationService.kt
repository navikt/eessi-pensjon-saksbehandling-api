package no.nav.eessi.pensjon.services.auth

class AuthorisationService {

    fun harTilgangTilEessiPensjon(roller: List<AdRolle>): Boolean {

        return roller.containsAll(Tilgang.EESSI_PENSJON.grupper)
    }

    fun harTilgangTilPesysSak(roller: List<AdRolle>, pesysSakstype: PesysSakstype?): Boolean {

        if (pesysSakstype == null){
            throw AuthorisationUkjentSakstypeException("Ukjent sakstype fra PESYS: $pesysSakstype")
        }

        if (pesysSakstype == PesysSakstype.ALDERSPENSJON) {
            if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))) {
                return false
            }
            return true
        }

        if (pesysSakstype == PesysSakstype.UFORETRYGD) {
            if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))) {
                return true
            }
            return false
        }

        if (pesysSakstype == PesysSakstype.BARNEPENSJON){
            if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))){
                return false
            }
            return true
        }

        if (pesysSakstype == PesysSakstype.GJENLEVENDE){
            if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))){
                return false
            }
            return true
        }
        // Vil aldri havne her
        return false
    }

    fun harTilgangTilBrukerISaken(
        roller: List<AdRolle>,
        saksbehandlerFnr: String,
        brukerFNR: String,
        brukerAnsattINav: Boolean,
        adressesperre: Adressesperre
    ): Boolean {

        if (brukerFNR == saksbehandlerFnr){
            return false
        }
        if (brukerAnsattINav){
            if (roller.containsAll(listOf(AdRolle.PENSJON_NAV_ANSATT, AdRolle.GOSYS_NAV_ANSATT))){
                return true
            }
            return false
        }
        if (adressesperre == Adressesperre.STRENGT_FORTROLIG_ADRESSE){
            if (roller.containsAll(listOf(AdRolle.PENSJON_STRENGT_FORTROLIG, AdRolle.GOSYS_STRENGT_FORTROLIG))){
                return true
            }
            return false
        }
        if (adressesperre == Adressesperre.FORTROLIG_ADRESSE){
            if (roller.containsAll(listOf(AdRolle.PENSJON_FORTROLIG, AdRolle.GOSYS_FORTROLIG))){
                return true
            }
            return false
        }
        return true
    }

    fun harTilgangTilBuc(roller: List<AdRolle>, buctype: Buctype, sedPensjonstype: SedPensjonstype): Boolean{
        if (buctype == Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON){
            if (sedPensjonstype == SedPensjonstype.UKJENT){
                // Når pensjonstypen ikke er kjent må saksbehandler få tilgang
                return true
            }
            if (sedPensjonstype == SedPensjonstype.BARNEPENSJON){
                if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))){
                    return false
                }
                return true
            }
            return true
        }
        if (buctype == Buctype.PBUC03_KRAV_OM_UFORETRYGD){
            if (roller.containsAll(listOf(AdRolle.PENSJON_UFORE))){
                return true
            }
            return false
        }
        return true
    }
}

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

enum class PesysSakstype(val pesysSakstype: String) {
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    BARNEPENSJON("BARNEPENSJON"),
    GJENLEVENDE("GJENLEVENDE")
}

enum class Buctype(val buctype: String){
    PBUC01_KRAV_OM_ALDER("PBUC01"),
    PBUC02_KRAV_OM_ETTERLATTEPENSJON("PBUC02"),
    PBUC03_KRAV_OM_UFORETRYGD("PBUC03")
}

enum class SedPensjonstype(val pensjonstype: String){
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    ETTERLATTEPENSJON("ETTERLATTE"),
    BARNEPENSJON("BARNEPENSJON"),
    UKJENT("UKJENT")
}

enum class Tilgang(var grupper: List<AdRolle>) {
    EESSI_PENSJON(listOf(AdRolle.PENSJON_UTLAND, AdRolle.PENSJON_SAKSBEHANDLER))
}

class AuthorisationUkjentSakstypeException(message: String?) : Exception(message)
