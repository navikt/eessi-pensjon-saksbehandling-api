package no.nav.eessi.pensjon.services.fagmodul

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.hamcrest.core.IsInstanceOf
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

class NavRegistreOppslagServiceTest: FagmodulBaseTest() {

    @After
    fun cleanUpTest() {
        Mockito.reset(mockFagmodulRestTemplate)
    }


    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns OK`() {

        val expectedResponse = Personinformasjon(fulltNavn = "testName")
        val aktoerId = "123"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/personinfo/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Personinformasjon::class.java))

        val generatedResponse = navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        Assert.assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|hentPersoninformasjon returns error`() {

        val aktoerId = "123"
        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/personinfo/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Personinformasjon::class.java))

        try {
            navRegistreOppslagService.hentPersoninformasjon(aktoerId)
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(PersonInformasjonException::class.java))
            Assert.assertEquals(e.message, "Feil ved henting av Personinformasjon")
        }
    }

    @Test
    fun `navRegistreOppslagService|hentPersoninformasjonNavn returns OK`() {

        val expectedResponse = "testName"
        val mockData = Personinformasjon(fulltNavn = expectedResponse)
        val aktoerId = "123"
        val mockResponse = ResponseEntity(mockData, HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/personinfo/$aktoerId"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(Personinformasjon::class.java))

        val generatedResponse = navRegistreOppslagService.hentPersoninformasjonNavn(aktoerId)
        Assert.assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|landkoder returns OK`() {

        val expectedResponse = listOf("NO", "FI", "SE", "DK")
        val mockResponse = ResponseEntity( mapAnyToJson(expectedResponse), HttpStatus.OK)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/landkoder/landkoder2"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse: List<String> = navRegistreOppslagService.landkoder()
        Assert.assertEquals(generatedResponse, expectedResponse)
    }

    @Test
    fun `Calling navRegistreOppslagService|landkoder returns error`() {

        val mockResponse = ResponseEntity("", HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockFagmodulRestTemplate).exchange(
                ArgumentMatchers.eq("/landkoder/landkoder2"),
                ArgumentMatchers.any(HttpMethod::class.java),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        try {
            navRegistreOppslagService.landkoder()
        } catch (e : Exception) {
            Assert.assertThat(e, IsInstanceOf.instanceOf(LandkodeException::class.java))
            Assert.assertEquals(e.message, "Feil under listing av landkoder")
        }
    }


}
