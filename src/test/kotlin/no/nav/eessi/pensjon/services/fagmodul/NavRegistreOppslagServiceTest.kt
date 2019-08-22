package no.nav.eessi.pensjon.services.fagmodul

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class NavRegistreOppslagServiceTest: FagmodulBaseTest() {

    @AfterEach
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }


    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns OK`() {

        val expectedResponse = Personinformasjon(fulltNavn = "testName")
        val aktoerId = "123"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/personinfo/$aktoerId"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(Personinformasjon::class.java))

        val generatedResponse = navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns error`() {

        val aktoerId = "123"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/personinfo/$aktoerId"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(Personinformasjon::class.java))

        val exception = assertThrows<PersonInformasjonException> {
            navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        }
        assertEquals(exception.message, "Feil ved henting av Personinformasjon")
    }

    @Test
    fun `navRegistreOppslagService|hentPersoninformasjonNavn returns OK`() {

        val expectedResponse = "testName"
        val mockData = Personinformasjon(fulltNavn = expectedResponse)
        val aktoerId = "123"
        val mockResponse = ResponseEntity(mockData, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/personinfo/$aktoerId"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(Personinformasjon::class.java))

        val generatedResponse = navRegistreOppslagService.hentPersoninformasjonNavn(aktoerId)
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|landkoder returns OK`() {

        val expectedResponse = listOf("NO", "FI", "SE", "DK")
        val mockResponse = ResponseEntity( mapAnyToJson(expectedResponse), HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/landkoder/landkoder2"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val generatedResponse: List<String> = navRegistreOppslagService.landkoder()
        assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|landkoder returns error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/landkoder/landkoder2"),
                any<HttpMethod>(),
                any<HttpEntity<*>>(),
                eq(String::class.java))

        val exception = assertThrows<LandkodeException> {
            navRegistreOppslagService.landkoder()
        }
        assertEquals(exception.message, "Feil under listing av landkoder")
    }
}
