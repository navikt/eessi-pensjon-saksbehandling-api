package no.nav.eessi.pensjon.services.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthorisationServiceTest {

     @Test
     fun `Gitt en pensjonsaksbehandler med utenlandstilgang når tilgang blir etterspurt så returner True`() {

         val autorisasjonsservice = AuthorisationService()
         val gruppemedlemskap =
             listOf(Gruppe.PENSJON_UTLAND,
                 Gruppe.PENSJON_SAKSBEHANDLER,
                 Gruppe.PENSJON_STRENGT_FORTROLIG)

         val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

         assertTrue(tilgangEessipensjon)

     }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til utland Når tilgang blir etterspurt så returner FALSE`(){

        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf( Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.PENSJON_STRENGT_FORTROLIG)

        val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

        assertFalse(tilgangEessipensjon)
    }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til pensjon Når tilgang blir etterspurt så returner FALSE` () {

        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.GOSYS_NAV_ANSATT,
                Gruppe.GOSYS_STRENGT_FORTROLIG)

        val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

        assertFalse(tilgangEessipensjon)

    }

    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er alderspensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.PENSJON_STRENGT_FORTROLIG,
                Gruppe.PENSJON_UFORE)

        val tilgangPESYS_Sak =
            autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.ALDERSPENSJON)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.PENSJON_STRENGT_FORTROLIG,
                Gruppe.PENSJON_UFORE)

        val tilgangPESYS_Sak =
            autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap,
                SakType.UFORETRYGD)

        assertTrue(tilgangPESYS_Sak)
    }


    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har ikke tilleggsrollen Uføre OG sakstypen er uføretrygd SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.GOSYS_STRENGT_FORTROLIG,
                Gruppe.GOSYS_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.UFORETRYGD)

        assertFalse(tilgangPESYS_Sak)
    }


    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er barnepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.PENSJON_FORTROLIG,
                Gruppe.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.BARNEPENSJON)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG ikke rollen Uføre OG sakstypen er barnepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.GOSYS_FORTROLIG,
                Gruppe.GOSYS_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.BARNEPENSJON)

        assertTrue(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.PENSJON_STRENGT_FORTROLIG,
                Gruppe.PENSJON_UFORE)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.GJENLEVENDE)

        assertFalse(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG rollen Uføre OG sakstypen er gjenlevendepensjon SÅ returner TRUE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.GOSYS_NAV_ANSATT,
                Gruppe.PENSJON_NAV_ANSATT)

        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, SakType.GJENLEVENDE)

        assertTrue(tilgangPESYS_Sak)
    }

    @Test
    fun `Gitt saksbehandler med tilgang til EP OG BUC er søknad om alder`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =
            listOf(Gruppe.PENSJON_UTLAND,
                Gruppe.PENSJON_SAKSBEHANDLER,
                Gruppe.GOSYS_NAV_ANSATT,
                Gruppe.PENSJON_NAV_ANSATT)

        val tilgangBUC = autorisasjonsservice.harTilgangTil_BUC(gruppemedlemskap, "PBUC01")

        assertTrue(tilgangBUC)

    }
}
