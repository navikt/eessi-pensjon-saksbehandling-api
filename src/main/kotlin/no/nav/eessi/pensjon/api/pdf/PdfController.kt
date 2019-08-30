package no.nav.eessi.pensjon.api.pdf

import no.nav.eessi.pensjon.services.pdf.PDFRequest
import no.nav.eessi.pensjon.services.pdf.PdfService
import no.nav.security.oidc.api.Protected
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val logger = LoggerFactory.getLogger(PdfController::class.java)

@Protected
@RestController
@RequestMapping("/pdf")
class PdfController(private val pdfService: PdfService) {

    @PostMapping("/generate")
    fun generatePDF(@RequestBody request: PDFRequest): ResponseEntity<Map<String, Any>> {
        logger.info("Genererer PDF")
        return try {
            val response = pdfService.generate(request)
            ResponseEntity.ok(response)
        } catch (e : Exception) {
            ResponseEntity.badRequest().body(mapOf("message" to "invalidPDFRecipe"))
        }
    }
}

