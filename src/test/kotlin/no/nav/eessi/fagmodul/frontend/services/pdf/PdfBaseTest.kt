package no.nav.eessi.fagmodul.frontend.services.pdf

import no.nav.eessi.fagmodul.frontend.services.BaseTest

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

open class PdfBaseTest : BaseTest() {

    lateinit var pdfController: PdfController
    lateinit var pdfService: PdfService

    @Before
    fun _init() {
        pdfService = Mockito.spy(PdfService())
        pdfController = Mockito.spy(PdfController(pdfService))
    }

    @Test
    fun _dummy() {}
}