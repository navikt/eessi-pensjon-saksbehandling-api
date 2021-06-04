package no.nav.eessi.pensjon.services.pdf

import io.mockk.spyk
import no.nav.eessi.pensjon.api.pdf.PdfController
import no.nav.eessi.pensjon.services.BaseTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

open class PdfBaseTest : BaseTest() {

    lateinit var pdfController: PdfController
    lateinit var pdfService: PdfService
    lateinit var templateService: TemplateService

    @BeforeEach
    fun _init() {
        pdfService = spyk(PdfService())
        templateService = spyk(TemplateService())
        pdfController = spyk(PdfController(pdfService))
    }

    @Test
    fun _dummy() {}
}
