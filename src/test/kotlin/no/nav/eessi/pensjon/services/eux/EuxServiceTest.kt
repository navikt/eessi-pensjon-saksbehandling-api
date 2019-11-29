package no.nav.eessi.pensjon.services.eux

import com.nhaarman.mockitokotlin2.*
import no.nav.eessi.pensjon.utils.mapAnyToJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class EuxServiceTest {

    @Mock
    lateinit var restTemplate: RestTemplate

    lateinit var euxService: EuxService

    @BeforeEach
    fun cleanUpTest() {
        euxService = EuxService(restTemplate)
    }

    @Test
    fun `Calling euxService|getInstitusjoner returns valid list`() {

        val bucType = "P_BUC_01"
        val landKode = "NO"

        val moockList = listOf("INST1")

        val mockResponse = ResponseEntity.ok().body(mapAnyToJson(moockList))

        doReturn(mockResponse).whenever(restTemplate).exchange(
                eq("/institusjoner?BuCType=$bucType&LandKode=$landKode"),
                eq(HttpMethod.GET),
                eq(HttpEntity("")),
                eq(String::class.java))

        val generatedResponse = euxService.getInstitusjoner(bucType, landKode)
        assertEquals(generatedResponse, mockResponse)
    }

    @Test
    fun `Calling euxService|getInstitusjoner returns error`() {

        val bucType = "P_BUC_01"
        val landKode = "NN"

        doThrow(HttpClientErrorException(HttpStatus.BAD_REQUEST, "bad request")).whenever(restTemplate).exchange(
                eq("/institusjoner?BuCType=P_BUC_01&LandKode=NN"),
                eq(HttpMethod.GET),
                eq(HttpEntity("")),
                eq(String::class.java)
        )

        assertThrows<RuntimeException> {
            euxService.getInstitusjoner(bucType, landKode)
        }
    }

    @Test
    fun `Gitt forespørsel om påkoblede land for P_BUC_01 så returner liste av påkoblede landkoder`() {

        val bucType = "P_BUC_01"

        val instResponse = DefaultResourceLoader().getResource("classpath:json/eux/institusjonerResponse.json").file.readText()

        val mockResponse = ResponseEntity.ok().body(instResponse)

        doReturn(mockResponse).whenever(restTemplate).exchange(
            eq("/institusjoner?BuCType=$bucType"),
            eq(HttpMethod.GET),
            eq(HttpEntity("")),
            eq(String::class.java))

        val landkoder = euxService.getPaakobledeLand(bucType)
        assertTrue(landkoder.contains("CH"))
        assertTrue(landkoder.contains("BE"))
        assertTrue(landkoder.contains("BG"))
    }
}