package no.nav.eessi.pensjon.services.auth

class AuthorisationService {

    fun
            harTilgangTilEessiPensjon(roller: List<AD_Rolle>): Boolean {

        return roller.containsAll(Tilgang.EESSI_PENSJON.grupper)
    }

    fun harTilgangTilPESYS_Sak(roller: List<AD_Rolle>, sakType: SakType): Boolean {

        if (sakType == SakType.ALDERSPENSJON) {
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))) {
                return false
            }
            return true
        }
        if (sakType == SakType.UFORETRYGD) {
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))) {
                return true
            }
            return false
        }

        if (sakType == SakType.BARNEPENSJON){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))){
                return false
            }
            return true
        }

        if (sakType == SakType.GJENLEVENDE){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))){
                return false
            }
            return true
        }
        throw AuthorisationUkjentSakstypeException("Ukjent sakstype fra PESYS: ${sakType}")
    }

    fun harTilgangTilBrukere_I_Saken(
        roller: List<AD_Rolle>,
        brukerFNR: String,
        saksbehandlerFNR: String,
        brukerAnsattI_NAV: Boolean,
        skjerming: Skjerming): Boolean {

        if (brukerFNR.equals(saksbehandlerFNR)){
            return false
        }
        if (brukerAnsattI_NAV){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_NAV_ANSATT, AD_Rolle.GOSYS_NAV_ANSATT))){
                return true
            }
            return false
        }
        if (skjerming == Skjerming.STRENGT_FORTROLIG){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_STRENGT_FORTROLIG, AD_Rolle.GOSYS_STRENGT_FORTROLIG))){
                return true
            }
            return false
        }
        if (skjerming == Skjerming.FORTROLIG){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_FORTROLIG, AD_Rolle.GOSYS_FORTROLIG))){
                return true
            }
            return false
        }
        return true
    }

    fun harTilgangTil_BUC(roller: List<AD_Rolle>, bucType: BUC_Type, sedPensjonstype: SED_Pensjonstype): Boolean{
        if (bucType.equals(BUC_Type.PBUC02_KRAV_OM_ETTERLATTEPENSJON)){
            if (sedPensjonstype.equals(SED_Pensjonstype.UKJENT)){
                // Når pensjonstypen ikke er kjent må saksbehandler få tilgang
                return true
            }
            if (sedPensjonstype.equals(SED_Pensjonstype.BARNEPENSJON)){
                if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))){
                    return false
                }
                return true
            }
            return true
        }
        if (bucType.equals(BUC_Type.PBUC03_KRAV_OM_UFORETRYGD)){
            if (roller.containsAll(listOf(AD_Rolle.PENSJON_UFORE))){
                return true
            }
            return false
        }
        return true
    }
}

enum class AD_Rolle(val rollenavn: String) {
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

enum class Skjerming(val skjerming: String){
    INGEN_SKJERMING(""),
    FORTROLIG("KODE7"),
    STRENGT_FORTROLIG("KODE6")
}

enum class SakType(val sakType: String) {
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    BARNEPENSJON("BARNEPENSJON"),
    GJENLEVENDE("GJENLEVENDE")
}

enum class BUC_Type(val bucType: String){
    PBUC01_KRAV_OM_ALDER("PBUC01"),
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

enum class SED_Pensjonstype(val pensjonstype: String){
    ALDERSPENSJON("ALDER"),
    UFORETRYGD("UFØRE"),
    ETTERLATTEPENSJON("ETTERLATTE"),
    BARNEPENSJON("BARNEPENSJON"),
    UKJENT("UKJENT")
}

enum class Tilgang(var grupper: List<AD_Rolle>) {
    EESSI_PENSJON(listOf(AD_Rolle.PENSJON_UTLAND, AD_Rolle.PENSJON_SAKSBEHANDLER))
}

class AuthorisationUkjentSakstypeException(message: String?) : Exception(message)
