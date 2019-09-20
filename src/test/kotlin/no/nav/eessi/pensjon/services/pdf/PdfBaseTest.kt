package no.nav.eessi.pensjon.services.pdf

import no.nav.eessi.pensjon.api.pdf.PdfController
import no.nav.eessi.pensjon.services.BaseTest

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito

open class PdfBaseTest : BaseTest() {

    lateinit var pdfController: PdfController
    lateinit var pdfService: PdfService
    lateinit var templateService: TemplateService

    @BeforeEach
    fun _init() {
        pdfService = Mockito.spy(PdfService())
        templateService = Mockito.spy(TemplateService())
        pdfController = Mockito.spy(PdfController(pdfService))
    }

    @Test
    fun _dummy() {}
}
