package no.nav.eessi.pensjon.services.auth

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class AuthorisationServiceTest {

     @Test
     fun `Gitt en pensjonsaksbehandler med utenlandstilgang når tilgang blir etterspurt så returner True`() {

         val autorisasjonsservice = AuthorisationService()
         val gruppemedlemskap =  listOf(Gruppe.PENSJON_UTLAND, Gruppe.PENSJON_SAKSBEHANDLER, Gruppe.GOSYS_KODE6)

         val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

         assertTrue(tilgangEessipensjon)

     }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til utland Når tilgang blir etterspurt så returner FALSE`(){

        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =  listOf( Gruppe.PENSJON_SAKSBEHANDLER, Gruppe.GOSYS_KODE6)

        val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

        assertFalse(tilgangEessipensjon)
    }

    @Test
    fun `Gitt en saksbehandler som ikke har tilgang til pensjon Når tilgang blir etterspurt så returner FALSE` () {

        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =  listOf(Gruppe.PENSJON_UTLAND, Gruppe.GOSYS_UTVIDET, Gruppe.GOSYS_KODE6)

        val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)

        assertFalse(tilgangEessipensjon)

    }

    @Test
    fun `Gitt en saksbehandler med tilgang til EP OG har tilleggsrollen Uføre OG sakstypen er alderspensjon SÅ returner FALSE`(){
        val autorisasjonsservice = AuthorisationService()
        val gruppemedlemskap =  listOf(Gruppe.PENSJON_UTLAND, Gruppe.PENSJON_SAKSBEHANDLER, Gruppe.GOSYS_KODE6, Gruppe.PENSJON_UFORE)

        val tilgangEessipensjon =  autorisasjonsservice.harTilgangTilEessiPensjon(gruppemedlemskap)
        val tilgangPESYS_Sak = autorisasjonsservice.harTilgangTilPESYS_Sak(gruppemedlemskap, "ALDER")

        assertFalse(tilgangPESYS_Sak)
    }
 }