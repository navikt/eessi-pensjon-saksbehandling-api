package no.nav.eessi.pensjon.services.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AuthorisationServiceTest {

    // Tilgang til EESSI-Pensjon

     @Test
     fun `Gitt en pensjonsaksbehandler med utenlandstilgang når tilgang blir etterspurt så returner True`() {

         val autorisasjonsservice = AuthorisationService()
         val roller = listOf(AdRolle.PENSJON_UTLAND,
             AdRolle.PENSJON_SAKSBEHANDLER,
             AdRolle.PENSJON_STRENGT_FORTROLIG)

         val tilgangEessipensjon = autorisasjonsservice.harTilgangTilEessiPensjon(roller)

         assertTrue(tilgangEessipensjon)

     }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til utland Når tilgang blir etterspurt så returner FALSE`(){

        val autorisasjonsservice = AuthorisationService()
        val roller = listOf( AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_STRENGT_FORTROLIG)

        val tilgangEessipensjon= autorisasjonsservice.harTilgangTilEessiPensjon(roller)

        assertFalse(tilgangEessipensjon)
    }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til pensjon Når tilgang blir etterspurt så returner FALSE` () {

        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.GOSYS_STRENGT_FORTROLIG)

        val tilgangEessipensjon= autorisasjonsservice.harTilgangTilEessiPensjon(roller)

        assertFalse(tilgangEessipensjon)

    }

    // Tilgang til PESYS-sak

    @Test
    fun `Gitt en saksbehandler ikke har tilleggsrollen Uføre OG sakstypen er alderspensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_STRENGT_FORTROLIG,
            AdRolle.PENSJON_NAV_ANSATT)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller,
            PesysSakstype.ALDERSPENSJON)

        assertTrue(tilgangPesysSak)
    }

    @Test
    fun `Gitt en saksbehandler har tilleggsrollen Uføre OG sakstypen er alderspensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_STRENGT_FORTROLIG,
            AdRolle.PENSJON_UFORE)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller,
            PesysSakstype.ALDERSPENSJON)

        assertFalse(tilgangPesysSak)
    }

    @Test
    fun `Gitt en saksbehandler har tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_STRENGT_FORTROLIG,
            AdRolle.PENSJON_UFORE)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller,
            PesysSakstype.UFORETRYGD)

        assertTrue(tilgangPesysSak)
    }


    @Test
    fun `Gitt en saksbehandler har ikke tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_STRENGT_FORTROLIG,
            AdRolle.GOSYS_NAV_ANSATT)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller, PesysSakstype.UFORETRYGD)

        assertFalse(tilgangPesysSak)
    }


    @Test
    fun `Gitt en saksbehandler har tilleggsrollen Uføre OG sakstypen er barnepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_FORTROLIG,
            AdRolle.PENSJON_UFORE)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller, PesysSakstype.BARNEPENSJON)

        assertFalse(tilgangPesysSak)
    }

    @Test
    fun `Gitt saksbehandler ikke rollen Uføre OG sakstypen er barnepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_FORTROLIG,
            AdRolle.GOSYS_NAV_ANSATT)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller, PesysSakstype.BARNEPENSJON)

        assertTrue(tilgangPesysSak)
    }

    @Test
    fun `Gitt saksbehandler rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.PENSJON_STRENGT_FORTROLIG,
            AdRolle.PENSJON_UFORE)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller, PesysSakstype.GJENLEVENDE)

        assertFalse(tilgangPesysSak)
    }

    @Test
    fun `Gitt saksbehandler rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_NAV_ANSATT)

        val tilgangPesysSak = autorisasjonsservice.harTilgangTilPesysSak(roller, PesysSakstype.GJENLEVENDE)

        assertTrue(tilgangPesysSak)
    }


    @Test
    fun `Gitt sakstypen er ukjent SÅ kast feil`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_NAV_ANSATT)

        assertThrows<AuthorisationUkjentSakstypeException>{
            autorisasjonsservice.harTilgangTilPesysSak(roller, null)
        }
    }

    // Tilgang til bruker

    @Test
    fun `Gitt saksbehandler sitt FNR er det samme som bruker sitt FNR SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "12345678901"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang til NAV-ansatte i PESYS og GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_NAV_ANSATT, AdRolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = true
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler ikke har tilgang til NAV-ansatte i PESYS OG har tilgang i GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = true
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang til NAV-ansatte i PESYS OG ikke tilgang i GOSYS OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = true
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har ikke tilgang til NAV-ansatte OG bruker jobber i NAV SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_SAKSBEHANDLER)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = true
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 og 7 OG bruker er merket fortrolig SÅ returner TRUE`() {
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(
            AdRolle.GOSYS_FORTROLIG,
            AdRolle.GOSYS_STRENGT_FORTROLIG,
            AdRolle.PENSJON_FORTROLIG,
            AdRolle.PENSJON_STRENGT_FORTROLIG
        )

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre
        )

        assertTrue(harTilgangTilBruker)
    }

        @Test
        fun `Gitt saksbehandler har tilgang kode 6 og 7 OG bruker er merket strengt fortrolig SÅ returner TRUE`(){
            val autorisasjonsservice = AuthorisationService()
            val roller = listOf(AdRolle.GOSYS_FORTROLIG,
                AdRolle.GOSYS_STRENGT_FORTROLIG,
                AdRolle.PENSJON_FORTROLIG,
                AdRolle.PENSJON_STRENGT_FORTROLIG)

            val brukerFNR = "12345678901"
            val saksbehandlerFNR = "123456123451"
            val brukerAnsattINav = false
            val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
            val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
                roller,
                brukerFNR,
                saksbehandlerFNR,
                brukerAnsattINav,
                adressesperre)

            assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 og ikke 6 OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_FORTROLIG, AdRolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 og ikke 7 OG bruker er merket strengt fortrolig SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_STRENGT_FORTROLIG, AdRolle.PENSJON_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 PESYS og ikke 6 GOSYS OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 GOSYS og ikke 6 PESYS OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 PESYS og 7 GOSYS OG bruker er merket fortrolig SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_FORTROLIG, AdRolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 GOSYS og ikke 7 PESYS OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 PESYS og ikke 7 GOSYS OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 7 OG bruker er ikke skjermet SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_FORTROLIG, AdRolle.GOSYS_FORTROLIG
        )

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.INGEN_ADRESSESPERRE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertTrue(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har tilgang kode 6 OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_STRENGT_FORTROLIG, AdRolle.GOSYS_STRENGT_FORTROLIG)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har ikke har tilgang kode 6 og 7 OG bruker er merket fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }

    @Test
    fun `Gitt saksbehandler har ikke har tilgang kode 6 og 7 OG bruker er merket strengt fortrolig SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.GOSYS_NAV_ANSATT)

        val brukerFNR = "12345678901"
        val saksbehandlerFNR = "123456123451"
        val brukerAnsattINav = false
        val adressesperre = Adressesperre.STRENGT_FORTROLIG_ADRESSE
        val harTilgangTilBruker = autorisasjonsservice.harTilgangTilBrukerISaken(
            roller,
            brukerFNR,
            saksbehandlerFNR,
            brukerAnsattINav,
            adressesperre)

        assertFalse(harTilgangTilBruker)
    }


    // Tilgang til BUC

    @Test
    fun `Gitt saksbehandler BUC er søknad om alder SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_NAV_ANSATT)
        val sedPensjonstype = SedPensjonstype.ALDERSPENSJON
        val bucType = Buctype.PBUC01_KRAV_OM_ALDER

        val tilgangBUC= autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler har ikke rollen UFØRE OG BUC er søknad om etterlattepensjon OG sed-ytelse er barnepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT)
        val sedPensjonstype = SedPensjonstype.BARNEPENSJON
        val bucType = Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler har rollen UFØRE OG BUC er søknad om etterlattepensjon OG sed-ytelse er barnepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_UFORE)
        val sedPensjonstype = SedPensjonstype.BARNEPENSJON
        val bucType = Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertFalse(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler ikke rollen UFØRE OG BUC er søknad om etterlattepensjon OG sed-ytelse er ikke barnepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_STRENGT_FORTROLIG)
        val sedPensjonstype = SedPensjonstype.ETTERLATTEPENSJON
        val bucType = Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler  BUC er etterlattepensjon OG sed-ytelse er ukjent SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_STRENGT_FORTROLIG)
        val sedPensjonstype = SedPensjonstype.UKJENT
        val bucType = Buctype.PBUC02_KRAV_OM_ETTERLATTEPENSJON

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler rollen uføre OG BUC er søknad om uføre SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.PENSJON_UFORE)
        val sedPensjonstype = SedPensjonstype.UKJENT
        val bucType = Buctype.PBUC03_KRAV_OM_UFORETRYGD

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller, bucType, sedPensjonstype)

        assertTrue(tilgangBUC)
    }

    @Test
    fun `Gitt saksbehandler ikke rollen uføre OG BUC er søknad om uføre SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val roller = listOf(AdRolle.PENSJON_UTLAND,
            AdRolle.PENSJON_SAKSBEHANDLER,
            AdRolle.GOSYS_NAV_ANSATT,
            AdRolle.GOSYS_STRENGT_FORTROLIG)
        val sedPensjonstype = SedPensjonstype.UKJENT
        val bucType = Buctype.PBUC03_KRAV_OM_UFORETRYGD

        val tilgangBUC = autorisasjonsservice.harTilgangTilBuc(roller,bucType, sedPensjonstype)

        assertFalse(tilgangBUC)
    }
}
