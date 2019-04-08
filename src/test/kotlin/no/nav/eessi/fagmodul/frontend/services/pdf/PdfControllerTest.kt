package no.nav.eessi.fagmodul.frontend.services.pdf

import org.junit.Test
import org.junit.Assert.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdfControllerTest : PdfBaseTest() {

    private val logger: Logger by lazy { LoggerFactory.getLogger(PdfControllerTest::class.java) }

    val validPdfPayload = PDFRequest(
        recipe = mapOf("testPDF" to listOf(
            RecipeStep(
                type = "specialPage",
                separatorText = "separatorText123",
                separatorTextColor = mapOf("r" to 0, "g" to 0, "b" to 0)
            )
        )),
        files = ArrayList(),
        watermark = PDFWatermark(
            watermarkText = "watertextText123",
            watermarkTextColor = mapOf("r" to 0, "g" to 0, "b" to 0)
        )
    )

    val invalidPdfPayload = PDFRequest(
        recipe = mapOf("testPDF" to listOf(
            RecipeStep(
                type = "pickPage",
                pageNumber = 1,
                name = "untitled.pdf"
            )
        )),
        files = ArrayList(), watermark = PDFWatermark()
    )

    @Test
    fun `Calling PdfController|generatePDF with invalid PDF payload returns bad request response`() {
        var response = pdfController.generatePDF(invalidPdfPayload)
        assertNotNull(response.body)
        assertTrue(response.statusCode.is4xxClientError)
        assertEquals(response.body?.get("serverMessage") as String, "invalidPDFRecipe")
    }

    @Test
    fun `Calling PDF controller POST |api|generate with valid PDF payload returns OK response`() {
        var response = pdfController.generatePDF(validPdfPayload)
        assertNotNull(response.body)
        assertTrue(response.statusCode.is2xxSuccessful)
        assertTrue(response.body!!.containsKey("testPDF"))
    }
}