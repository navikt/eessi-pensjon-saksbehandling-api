package no.nav.eessi.pensjon.interceptor

import no.nav.eessi.pensjon.services.ldap.BrukerInformasjonService
import no.nav.eessi.pensjon.services.ldap.LdapServiceMock
import no.nav.eessi.pensjon.services.storage.StorageService
import no.nav.security.oidc.test.support.spring.TokenGeneratorConfiguration
import org.apache.http.HttpStatus
import org.apache.http.client.ResponseHandler
import org.apache.http.client.methods.HttpDelete
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles

private const val SED_SENDT_TOPIC = "eessi-basis-sedSendt-v1"
private const val SED_MOTTATT_TOPIC = "eessi-basis-sedMottatt-v1"
private const val MOTTAK_TOPIC = "privat-eessipensjon-selvbetjeningsinfoMottatt-test"

@Suppress("NonAsciiCharacters")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [ AuthInterceptorIntegrationTest.TestConfig::class])
@ActiveProfiles("integrationtest")
@Import(TokenGeneratorConfiguration::class)
@EmbeddedKafka(controlledShutdown = true, topics = [SED_SENDT_TOPIC, SED_MOTTATT_TOPIC, MOTTAK_TOPIC])
class AuthInterceptorIntegrationTest() {

    @LocalServerPort
    private lateinit var port: String

   /**
    * For test kan man lage brukere som gir tilganger etter følgende regler
    * Saksbehandler X000000 angir
    *               |||||||
    *               |||||| -------- 7 gir tilgang til kode 7 brukere
    *               ||||||
    *               ||||| --------- 6 gir tilgang til kode 6 brukere
    *               |||||
    *               |||| ---------- Ubrukt
    *
    *               ||||
    *               ||| ----------- Ubrukt
    *               |||
    *               || ------------ 3 gir tilgang til NAV-ansatte
    *               ||
    *               | ------------- 2 gir tilgang til utland
    *               |
    *                -------------- Saksbehandler på: A Alderspensjon, U Uføre
    */

    /**
     * Klargjør token for kall av EESSI-Pensjon tjeneste med
     * tilgangskontroll på seg.
     */

    fun setKallTilGetDocument(brukerId: String, testfil: String): HttpGet{
        // Given
        val request = HttpGet("http://localhost:$port/local/jwt?subject=$brukerId")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/storage/get/$testfil")
        getDocument.setHeader("Authorization", "Bearer $token")

        return getDocument
    }

    /**
     * Saksbehandler med
     *      o ikke tilgang til EESSI-Pensjon
     *
     *
     * lag kopi av denne og bruk den for put, samt legg til tester for ok og ikkeok
     */
    @Test
    fun `Gitt at saksbehandler har ikke roller til å få tilgang til utland SÅ skal det kastes 403` (){
        val brukerId = "Z000000"
        val getDocument = setKallTilGetDocument(brukerId, "testFil.json")

        assertThrows<AuthInterceptor.AuthorisationIkkeTilgangTilEeessiPensjonException> {
            val doc = HttpClientBuilder.create().build().execute(getDocument)
            // Mocket statuskode for å fremheve en exception
            if (doc.statusLine.statusCode == HttpStatus.SC_FORBIDDEN ) { throw AuthInterceptor.AuthorisationIkkeTilgangTilEeessiPensjonException("Ingen tilgang mate!! ....") }
        }
    }

    /**
     * Saksbehandler med
     *      o Tilgang til EESSI-Pensjon
     *      o Ikke behandle uføresaker
     *      o Ikke behandle NAV-ansatte
     *      o Ikke behandle brukere med strengt fortrolig adresse
     *      o Ikke behandle brukere med fortrolig adresse
     *
     * Kaller tjeneste som skal behandle
     *      o PBUC01
     *      o Alderspensjon
     *      o med en bruker som
     *          o ikke er NAV-ansatt
     *          o ingen adressesperre
     */
    @Test
    fun `Gitt at saksbehandler har har rollene Saksbehandler og Utland SÅ skal det gis tilgang til å hente fil fra s3` (){

        // Given
        val request = HttpGet("http://localhost:$port/local/jwt?subject=A234567")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/storage/get/s3TestFil")
        getDocument.setHeader("Authorization", "Bearer $token")

        // Tester tilgang til en EP-tjeneste
        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        assert(responseGetDocument != null)
    }

    @Test
    fun `Gitt at saksbehandler kun har har rollene 6 eller 7 så skal det kastes en 403` (){

        // Given
        val request = HttpGet("http://localhost:$port/local/jwt?subject=Z000567")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/storage/get/s3TestFil")
        getDocument.setHeader("Authorization", "Bearer $token")

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseGetDocument.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
        Assertions.assertTrue(responseGetDocument != null)

    }

    @Test
    fun `Gitt at saksbehandler har har rollene alderpensjon så skal det gis tilgang til å hente fil fra s3` (){

        // Given
        val request = HttpGet("http://localhost:$port/local/jwt?subject=A230067")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/storage/get/12345678910___path___123.json")
        getDocument.setHeader("Authorization", "Bearer $token")

        // Tester tilgang til en EP-tjeneste
        val responseHentDokumenter = HttpClientBuilder.create().build().execute(getDocument)

        val handler: ResponseHandler<String> = BasicResponseHandler()

        // henter ut body
        val body = handler.handleResponse(responseHentDokumenter)
        // henter ut statuskode
        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)
        Assertions.assertEquals("", body)

    }

    @Test
    fun `Gitt at bruker har tilgang til EP så skal det kunne hentes ut en liste fra s3`() {

        val request = HttpGet("http://localhost:$port/local/jwt?subject=A230067")

        val responseTilgangerForBruker = HttpClientBuilder.create().build().execute(request)
        val token = String(responseTilgangerForBruker.entity.content.readBytes())

        val hentListeAvDokumenter = HttpGet("http://localhost:$port/api/storage/list/123454444___path___123.json")
        hentListeAvDokumenter.setHeader("Authorization", "Bearer $token")

        val handler: ResponseHandler<String> = BasicResponseHandler()
        val responseHentDokumenter = HttpClientBuilder.create().build().execute(hentListeAvDokumenter)

        val body = handler.handleResponse(responseHentDokumenter)
        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)
        Assertions.assertEquals("[\"\"]", body)

    }

    @Test
    fun `Gitt at bruker ikke har tilgang til EP så skal det ikke være mulig å hente ut en liste fra s3`() {

        val request = HttpGet("http://localhost:$port/local/jwt?subject=Z030067")

        val responseTilgangerForBruker = HttpClientBuilder.create().build().execute(request)
        val token = String(responseTilgangerForBruker.entity.content.readBytes())

        val hentListeAvDokumenter = HttpGet("http://localhost:$port/api/storage/list/123454444___path___123.json")
        hentListeAvDokumenter.setHeader("Authorization", "Bearer $token")

        val responseHentDokumenter = HttpClientBuilder.create().build().execute(hentListeAvDokumenter)
        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)

    }

    @Test
    fun `Gitt at bruker ikke har tilgang til EP så skal det ikke være mulig å slette fra s3`() {

        val request = HttpGet("http://localhost:$port/local/jwt?subject=Z030067")

        val responseTilgangerForBruker = HttpClientBuilder.create().build().execute(request)
        val token = String(responseTilgangerForBruker.entity.content.readBytes())

        val deleteDok = HttpDelete("http://localhost:$port/api/storage/123454444___path___123.json")
        deleteDok.setHeader("Authorization", "Bearer $token")

        val responseHentDokumenter = HttpClientBuilder.create().build().execute(deleteDok)
        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)

    }

    @Test
    fun `Gitt at bruker har tilgang til EP så skal det være mulig å slette fra s3`() {

        val request = HttpGet("http://localhost:$port/local/jwt?subject=A230067")

        val responseTilgangerForBruker = HttpClientBuilder.create().build().execute(request)
        val token = String(responseTilgangerForBruker.entity.content.readBytes())

        val deleteDok = HttpDelete("http://localhost:$port/api/storage/123454444___path___123.json")
        deleteDok.setHeader("Authorization", "Bearer $token")

        val responseHentDokumenter = HttpClientBuilder.create().build().execute(deleteDok)

        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)
    }


    @Test
    fun `Gitt at saksbehandler har rollen Alderspensjon, så skal det gis tilgang til EP (whitelisted)`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=A230067")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/whitelisted")
        getDocument.setHeader("Authorization", "Bearer $token")

        val handler: ResponseHandler<String> = BasicResponseHandler()

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val body = handler.handleResponse(responseGetDocument)

        Assertions.assertTrue(responseGetDocument != null)
        Assertions.assertEquals("true", body)

    }

    @Test
    fun `Gitt at uføre saksbehandler har rollen Uføre, så skal det gis tilgang til EP (whitelisted)`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=U230067")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/whitelisted")
        getDocument.setHeader("Authorization", "Bearer $token")

        val handler: ResponseHandler<String> = BasicResponseHandler()

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val body = handler.handleResponse(responseGetDocument)

        Assertions.assertTrue(responseGetDocument != null)
        Assertions.assertEquals("true", body)

    }

    @Test
    fun `Gitt at uføre saksbehandler har rollen Uføre men mangler pensjon-utland, så skal det ikke gis tilgang til EP`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=U930067")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/userinfo")
        getDocument.setHeader("Authorization", "Bearer $token")

        val responseHentDokumenter = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseHentDokumenter.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
        Assertions.assertTrue(responseHentDokumenter != null)

    }


    @Test
    fun `Gitt at saksbehandler ikke har rollen Alderspensjon, så skal det ikke gis tilgang til EP`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=Z000000")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/whitelisted")
        getDocument.setHeader("Authorization", "Bearer $token")

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseGetDocument.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
    }

    @Test
    fun `Gitt at saksbehandler har rollen Alderspensjon, ved henting av userinfo så skal det gis tilgang til EP`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=A230067")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/userinfo")
        getDocument.setHeader("Authorization", "Bearer $token")

        //ResponseHandler<String> handler
        val handler: ResponseHandler<String> = BasicResponseHandler()

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val body = handler.handleResponse(responseGetDocument)

        Assertions.assertTrue(responseGetDocument != null)
        Assertions.assertTrue(body.contains("A230067"))

    }

    @Test
    fun `Gitt at ingen token finnes ved henting av userinfo så skal det kastes en 401`() {
        // Then
        val getDocument = HttpGet("http://localhost:$port/api/userinfo")

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseGetDocument.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_UNAUTHORIZED, statusCode)
    }

    @Test
    fun `Gitt at saksbehandler ikke har rollen Alderspensjon, ved henting av userinfo så skal det ikke gis tilgang til EP`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=Z000000")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/userinfo")
        getDocument.setHeader("Authorization", "Bearer $token")

        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseGetDocument.statusLine.statusCode

        Assertions.assertEquals(HttpStatus.SC_FORBIDDEN, statusCode)
    }

    @Test
    fun `Gitt at det feiler ved oppslag av saksbehandler mot LDAP gis det tilgang til EP`() {
        val request = HttpGet("http://localhost:$port/local/jwt?subject=X000000")

        // When
        val response = HttpClientBuilder.create().build().execute(request)
        val token = String(response.entity.content.readBytes())

        // Then
        val getDocument = HttpGet("http://localhost:$port/api/userinfo")
        getDocument.setHeader("Authorization", "Bearer $token")

        val handler: ResponseHandler<String> = BasicResponseHandler()
        val responseGetDocument = HttpClientBuilder.create().build().execute(getDocument)
        val statusCode: Int = responseGetDocument.statusLine.statusCode

        val body = handler.handleResponse(responseGetDocument)

        Assertions.assertEquals(HttpStatus.SC_OK, statusCode)
        Assertions.assertTrue(responseGetDocument != null)
        Assertions.assertTrue(body.contains("X000000"))
    }

    @TestConfiguration
    class TestConfig{

        @Bean
        fun ldapService(): BrukerInformasjonService {
            return LdapServiceMock()
        }

        @Bean
        fun storage(): StorageService {
            return StorageServiceMock()
        }

    }

    //Mock StorageService da vi ikke skal teste selve s3 men kun tilgang eller ikke
    private class StorageServiceMock: StorageService {
        override fun list(path: String): List<String> {
            return listOf("")
        }

        override fun put(path: String, content: String) {
            //nothing
        }

        override fun get(path: String): String? {
            if (path == "X000000___whitelisted") {
                return "$path.json"
            }
            return ""
        }

        override fun delete(path: String) {
            //nothing
        }

        override fun multipleDelete(paths: List<String>) {
            //nothing
        }

    }

}

