package no.nav.eessi.fagmodul.frontend.services.eux

import com.fasterxml.jackson.core.type.TypeReference
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.whenever
import no.nav.eessi.fagmodul.frontend.utils.errorBody
import no.nav.eessi.fagmodul.frontend.utils.mapAnyToJson
import org.junit.After
import org.junit.Assert
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException

class EuxServiceTest : EuxBaseTest() {

    val rinaAksjonTypeRef = object : TypeReference<List<RinaAksjon>>() {}

    @After
    fun cleanUpTest() {
        Mockito.reset(mockEuxRestTemplate);
    }

    @Test
    fun `Calling euxService|getInstitusjoner returns valid list`() {

        val bucType = "P_BUC_01"
        val landKode = "NO"

        val moockList = listOf("INST1")

        val mockResponse = ResponseEntity.ok().body(mapAnyToJson(moockList))

        doReturn(mockResponse).whenever(mockEuxRestTemplate).exchange(
                ArgumentMatchers.eq("/institusjoner?BuCType=$bucType&LandKode=$landKode"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        val generatedResponse = euxService.getInstitusjoner(bucType, landKode)
        Assert.assertEquals(generatedResponse, mockResponse)
    }

    @Test(expected = RestClientException::class)
    fun `Calling euxService|getInstitusjoner returns error`() {

        val bucType = "P_BUC_01"
        val landKode = "NN"

        val expectedResponse = "{\"msg\":\"error\"}"
        val mockResponse = ResponseEntity(expectedResponse, HttpStatus.BAD_REQUEST)

        doReturn(mockResponse).whenever(mockEuxRestTemplate).exchange(
                ArgumentMatchers.eq("/Institusjoner?BuCType=P_BUC_01&LandKode=NN"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.any(ParameterizedTypeReference::class.java))

        euxService.getInstitusjoner(bucType, landKode)
    }

    @Test
    fun `Calling euxService|getRinaSaker returns OK RinaSak json`() {

        val rinaSakNr = "123"
        val fnr = "123456"

        val list = listOf(RinaSak(id = "123", traits = RinaTraits(flowType = "testBuC")))
        val mockResponse = ResponseEntity.ok().body(mapAnyToJson(list))

//        /rinasaker?BuCType=&F%C3%B8dselsnummer=123456&RINASaksnummer=123&Status=open
        doReturn(mockResponse).whenever(mockEuxRestTemplate).exchange(
                ArgumentMatchers.eq("/rinasaker?BuCType=&F%C3%B8dselsnummer=$fnr&RINASaksnummer=$rinaSakNr&Status=open"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = euxService.getRinaSaker(rinaSakNr, fnr)
        Assert.assertEquals(generatedResponse, mockResponse)
    }


    @Test
    fun `Calling euxService|getRinaSaker returns error`() {
        val rinaSakNr = "123"
        val fnr = "123456"

        val expectedResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody("Feiler ved henting av rinasaker mot EUX"))
        val exception = RuntimeException("error eux")

        doThrow(exception).whenever(mockEuxRestTemplate).exchange(
                ArgumentMatchers.eq("/rinasaker?BuCType=&F%C3%B8dselsnummer=$fnr&RINASaksnummer=$rinaSakNr"),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity::class.java),
                ArgumentMatchers.eq(String::class.java))

        val generatedResponse = euxService.getRinaSaker(rinaSakNr, fnr)
        Assert.assertEquals(generatedResponse, expectedResponse)

    }


    @Test
    fun `Calling euxService|getAvailableSEDonBuc returns BuC lists`() {

        var buc = "P_BUC_01"
        var expectedResponse = listOf("P2000")
        var generatedResponse = euxService.getAvailableSEDonBuc(buc)
        Assert.assertEquals(generatedResponse, expectedResponse)

        buc = "P_BUC_06"
        expectedResponse = listOf("P5000", "P6000", "P7000", "P10000")
        generatedResponse = euxService.getAvailableSEDonBuc(buc)
        Assert.assertEquals(generatedResponse, expectedResponse)
    }
}