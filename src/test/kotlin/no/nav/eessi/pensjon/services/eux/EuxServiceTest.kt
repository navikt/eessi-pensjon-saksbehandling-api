package no.nav.eessi.pensjon.services.eux

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException

class EuxServiceTest : EuxBaseTest() {

    @AfterEach
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
        assertEquals(generatedResponse, mockResponse)
    }

    @Test
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

        assertThrows<RestClientException> {
            euxService.getInstitusjoner(bucType, landKode)
        }
    }
}
