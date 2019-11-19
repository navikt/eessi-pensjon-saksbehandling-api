package no.nav.eessi.pensjon.services.fagmodul

import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException

class NavRegistreOppslagServiceTest: FagmodulBaseTest() {

    @AfterEach
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }

    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns OK`() {


        val aktoerId = "123"
        val mockResponse = ResponseEntity(
            DefaultResourceLoader().getResource("classpath:json/personinformasjon/personinformasjon.json").file.readText(),
            HttpStatus.OK
        )

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                eq("/personinfo/$aktoerId"),
                any(),
                any(),
                eq(String::class.java))

        val actualResponse = navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        assertEquals(actualResponse!!.fornavn, "RASK")
        assertEquals(actualResponse.etternavn, "MASKIN")
        assertNull(actualResponse.mellomnavn)
        assertEquals(actualResponse.fulltNavn, "MASKIN RASK")
    }

    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns error`() {

        val aktoerId = "123"

        doThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST, "bad request")).whenever(mockFagmodulRestTemplate).exchange(
                eq("/personinfo/$aktoerId"),
                any(),
                any(),
                eq(String::class.java))

        val exception = assertThrows<RuntimeException> {
            navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        }
        assertEquals(exception.message, "En feil oppstod under henting av personinformasjon ex: 400 bad request body: ")
    }
}
