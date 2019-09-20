package no.nav.eessi.pensjon.services.pdf

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.util.ResourceUtils
import java.util.*


class TemplateServiceTest : PdfBaseTest() {
    @Suppress("UNCHECKED_CAST")
    @Test
    fun `given valid e207 submission when generating receipt then generate kvittering pdf`() {
        val mockJsonString = DefaultResourceLoader().getResource(
            "classpath:json/submissionE207.json").file.readText()
        val mockPdfReceipt =  ResourceUtils.getFile(
            "classpath:pdf/kvitteringE207.pdf").readBytes()
        val mockBase64: String = Base64.getEncoder().encodeToString(mockPdfReceipt)
        val mockPerson = "testPerson"
        val mockPage = "e207"

        val generateReceipt = templateService.generateReceipt(mockJsonString, mockPerson, mockPage)
        Assertions.assertEquals(generateReceipt["name"], "kvittering_e207.pdf")
        Assertions.assertEquals(
            (generateReceipt["content"] as MutableMap<String, Any?>).get("base64").toString().length,
            mockBase64.length
        )
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `given valid p4000 submission when generating receipt then generate kvittering pdf`() {
        val mockJsonString = DefaultResourceLoader().getResource(
            "classpath:json/submissionP4000.json").file.readText()
        val mockPdfReceipt =  ResourceUtils.getFile(
            "classpath:pdf/kvitteringP4000.pdf").readBytes()
        val mockBase64: String = Base64.getEncoder().encodeToString(mockPdfReceipt)
        val mockPerson = "testPerson"
        val mockPage = "p4000"

        val generateReceipt = templateService.generateReceipt(mockJsonString, mockPerson, mockPage)
        Assertions.assertEquals(generateReceipt["name"], "kvittering_p4000.pdf")
        Assertions.assertEquals(
            (generateReceipt["content"] as MutableMap<String, Any?>).get("base64").toString().length,
            mockBase64.length
        )
    }
}