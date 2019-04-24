package no.nav.eessi.fagmodul.frontend.services.pdf


import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Test
import org.junit.Assert.*
import org.springframework.core.io.DefaultResourceLoader
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*

class PdfServiceTest : PdfBaseTest() {

    @Test
    fun `generate a PDF from a valid PDFRequest succeeds`() {
        val formTemplate = "src/main/resources/pdf-templates/E_207vev.pdf"
        val baos = ByteArrayOutputStream()
        PDDocument.load(File(formTemplate)).save(baos)

        val payload = PDFRequest(
            files = listOf(
                Files(
                    content = FileContent(
                        base64 = Base64.getEncoder().encodeToString(baos.toByteArray())
                    ),
                    name = "e207.pdf",
                    numPages = 2,
                    mimetype = "application/pdf",
                    size = baos.size()
                )
            ),
            recipe = mapOf("testPDF" to listOf(
                RecipeStep(
                    type = "pickPage",
                    pageNumber = 1,
                    name = "e207.pdf"
                )
            )),
            watermark = PDFWatermark(
                watermarkText = "watertextText123",
                watermarkTextColor = mapOf("r" to 0, "g" to 0, "b" to 0)
            )
        )

        val pdf = pdfService.generate(payload)
        assertNotNull(pdf)

        val testpdf = pdf["testPDF"]
        assertNotNull(testpdf)

        assertEquals(testpdf?.get("name"), "testPDF.pdf")
        assertTrue(testpdf?.get("size") as Int > 0)
        assertEquals(testpdf["numPages"], 1)
        assertEquals(testpdf["mimetype"], "application/pdf")
        assertTrue(testpdf.containsKey("content"))
    }

    @Test
    fun `generateReceipt`() {
        val mockJsonString = DefaultResourceLoader().getResource(
            "classpath:json/submissions.json").file.readText()

        val mockPerson = "testPerson"
        val generateReceipt = pdfService.generateReceipt(mockJsonString, mockPerson)
        assertEquals(generateReceipt?.get("name"), "kvittering.pdf")
    }
}