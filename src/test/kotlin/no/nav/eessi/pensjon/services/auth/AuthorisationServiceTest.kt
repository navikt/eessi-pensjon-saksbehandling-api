package no.nav.eessi.pensjon.services.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthorisationServiceTest {

    // Tilgang til EESSI-Pensjon

     @Test
     fun `Gitt en pensjonsaksbehandler med utenlandstilgang når tilgang blir etterspurt så returner True`() {

         val autorisasjonsservice = AuthorisationService()
         val roller = listOf(AD_Rolle.PENSJON_UTLAND,
             AD_Rolle.PENSJON_SAKSBEHANDLER,
             AD_Rolle.PENSJON_STRENGT_FORTROLIG)

         val tilgangEessipensjon = autorisasjonsservice.harTilgangTilEessiPensjon(roller)

         assertTrue(tilgangEessipensjon)

     }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til utland Når tilgang blir etterspurt så returner FALSE`(){

        val autorisasjonsservice = AuthorisationService()
        val roller = listOf( AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG)

        val tilgangEessipensjon= autorisasjonsservice.harTilgangTilEessiPensjon(roller)

        assertFalse(tilgangEessipensjon)
    }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til pensjon Når tilgang blir etterspurt så returner FALSE` () {

        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.GOSYS_STRENGT_FORTROLIG)

        val tilgangEessipensjon= autorisasjonsservice.harTilgangTilEessiPensjon(roller)

        assertFalse(tilgangEessipensjon)

    }

    // Tilgang til PESYS-sak

    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er alderspensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG,
            AD_Rolle.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller,
            SakType.ALDERSPENSJON)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG,
            AD_Rolle.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller,
            SakType.UFORETRYGD)

        assertTrue(tilgangPESYS_Sak)
    }


    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har ikke tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_STRENGT_FORTROLIG,
            AD_Rolle.GOSYS_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller, SakType.UFORETRYGD)

        assertFalse(tilgangPESYS_Sak)
    }


    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er barnepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.PENSJON_FORTROLIG,
            AD_Rolle.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller, SakType.BARNEPENSJON)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG ikke rollen Uføre OG sakstypen er barnepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_FORTROLIG,
            AD_Rolle.GOSYS_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller, SakType.BARNEPENSJON)

        assertTrue(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG,
            AD_Rolle.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller, SakType.GJENLEVENDE)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(roller, SakType.GJENLEVENDE)

        assertTrue(tilgangPESYS_Sak)
    }

    // Tilgang til bruker

    @Test
    fun `Gitt saksbehandler sitt FNR er det samme som bruker sitt FNR SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "12345678901"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang til NAV-ansatte i PESYS og GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_NAV_ANSATT, AD_Rolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = true
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler ikke har tilgang til NAV-ansatte i PESYS OG har tilgang i GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = true
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang til NAV-ansatte i PESYS OG ikke tilgang i GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = true
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har ikke tilgang til NAV-ansatte OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_SAKSBEHANDLER)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = true
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 og 7 OG bruker er merket fortrolig SÅ returner TRUE`() {
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(
            AD_Rolle.GOSYS_FORTROLIG,
            AD_Rolle.GOSYS_STRENGT_FORTROLIG,
            AD_Rolle.PENSJON_FORTROLIG,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG
        )

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming
        )

        assertTrue(harTilgangTilBruker)
    }

        @Test
        fun `Gitt saksbehandler har tilgang kode 6 og 7 OG bruker er merket strengt fortrolig SÅ returner TRUE`(){
            val autorisasjonsservice = AuthorisationService()
            val roller = listOf(AD_Rolle.GOSYS_FORTROLIG,
                AD_Rolle.GOSYS_STRENGT_FORTROLIG,
                AD_Rolle.PENSJON_FORTROLIG,
                AD_Rolle.PENSJON_STRENGT_FORTROLIG)

            val brukerFNR = "12345678901"
            val saksbehandlerFNR = "123456123451"
            val brukerAnsattI_NAV = false
            val skjerming = Skjerming.STRENGT_FORTROLIG
            val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
                roller,
                brukerFNR,
                saksbehandlerFNR,
                brukerAnsattI_NAV,
                skjerming)

            assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 og ikke 6 OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_FORTROLIG, AD_Rolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.STRENGT_FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 og ikke 7 OG bruker er merket strengt fortrolig SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_STRENGT_FORTROLIG, AD_Rolle.PENSJON_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.STRENGT_FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 PESYS og ikke 6 GOSYS OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.STRENGT_FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 GOSYS og ikke 6 PESYS OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.STRENGT_FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 PESYS og 7 GOSYS OG bruker er merket fortrolig SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_FORTROLIG, AD_Rolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 GOSYS og ikke 7 PESYS OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.GOSYS_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 PESYS og ikke 7 GOSYS OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 OG bruker er ikke skjermet SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_FORTROLIG, AD_Rolle.GOSYS_FORTROLIG
        )

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.INGEN_SKJERMING
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_STRENGT_FORTROLIG, AD_Rolle.GOSYS_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattI_NAV = false
        val skjerming = Skjerming.FORTROLIG
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukere_I_Saken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattI_NAV,
            skjerming)

        assertFalse(harTilgangTilBruker)
    }
/*
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


    // Tilgang til BUC

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG BUC er søknad om alder SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_NAV_ANSATT)

        val tilgangBUC= autorisasjonsservice.harTilgangTil_BUC(roller, "PBUC01")

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen UFØRE OG BUC er søknad om etterlattepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_UFORE)

        val tilgangBUC = autorisasjonsservice.harTilgangTil_BUC(roller, "PBUC02")

        assertFalse(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG ikke rollen UFØRE OG BUC er søknad om etterlattepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_STRENGT_FORTROLIG)

        val tilgangBUC = autorisasjonsservice.harTilgangTil_BUC(roller, "PBUC02")

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen uføre OG BUC er søknad om uføre SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.PENSJON_UFORE)

        val tilgangBUC = autorisasjonsservice.harTilgangTil_BUC(roller, "PBUC03")

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG ikke rollen uføre OG BUC er søknad om uføre SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AD_Rolle.PENSJON_UTLAND,
            AD_Rolle.PENSJON_SAKSBEHANDLER,
            AD_Rolle.GOSYS_NAV_ANSATT,
            AD_Rolle.GOSYS_STRENGT_FORTROLIG)

        val tilgangBUC = autorisasjonsservice.harTilgangTil_BUC(roller,"PBUC03")

        assertTrue(tilgangBUC)
    }
}
