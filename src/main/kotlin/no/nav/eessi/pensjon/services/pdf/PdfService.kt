package no.nav.eessi.pensjon.services.pdf

import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.util.Matrix
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import java.io.ByteArrayOutputStream
import java.net.URL
import java.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Service
@Description("Service class for PDF")
class PdfService {
    private final val fontPath = "/fonts/LiberationSans-Regular.ttf"
    val url: URL = this.javaClass.getResource(fontPath)

    fun generate(request: PDFRequest): HashMap<String, Map<String, Any>> {

        val workingPdfs = HashMap<String, PDDocument>()
        val workingImages = HashMap<String, PDImageXObject>()
        val response = HashMap<String, Map<String, Any>>()
        val doc = PDDocument()

        request.files.forEach { file ->
            val byteArray = Base64Utils.decodeFromString(file.content.base64)
            if (file.mimetype == "application/pdf") {
                workingPdfs[file.name] = PDDocument.load(byteArray)
            } else {
                workingImages[file.name] = PDImageXObject.createFromByteArray(doc, byteArray, file.name)
            }
        }

        request.recipe.forEach { (targetPdf, recipe) ->

            if (recipe.isNotEmpty()) {

                val outputPdf = PDDocument()
                val baos = ByteArrayOutputStream()
                var numPages = 0

                recipe.forEach { step ->

                    if (step.type == "pickPage") {

                        val sourcePdf = workingPdfs[step.name]

                        val page = sourcePdf?.getPage(step.pageNumber - 1) // page #1 is accessed as index 0
                        outputPdf.addPage(page)

                        if (request.watermark.watermarkText != null && page != null) {
                            addWatermark(
                                outputPdf,
                                page,
                                request.watermark.watermarkText,
                                request.watermark.watermarkTextColor!!
                            )
                        }
                        numPages++
                    }

                    if (step.type == "pickImage") {

                        val sourceImage = workingImages[step.name]
                        val page = PDPage()
                        addImage(outputPdf, page, sourceImage!!)

                        if (request.watermark.watermarkText != null) {
                            addWatermark(
                                outputPdf,
                                page,
                                request.watermark.watermarkText,
                                request.watermark.watermarkTextColor!!
                            )
                        }
                        numPages++
                    }

                    if (step.type == "specialPage" && step.separatorText != null) {
                        addSpecialPage(outputPdf, step.separatorText, step.separatorTextColor!!)
                        numPages++
                    }

                    if (step.type == "pickDocument") {

                        val sourcePdf = workingPdfs[step.name]
                        PDFMergerUtility().appendDocument(outputPdf, sourcePdf)
                        numPages += sourcePdf!!.numberOfPages
                    }
                }

                outputPdf.save(baos)
                outputPdf.close()
                baos.flush()
                baos.close()

                response[targetPdf] = mapOf(
                    "name" to "$targetPdf.pdf",
                    "size" to baos.toByteArray().size,
                    "numPages" to numPages,
                    "mimetype" to "application/pdf",
                    "content" to mapOf("base64" to Base64.getEncoder().encodeToString(baos.toByteArray()))
                )
            }
        }

        return response
    }

    private fun addWatermark(
        outputPdf: PDDocument,
        page: PDPage,
        watermarkText: String,
        watermarkTextColor: Map<String, Any>
    ) {
        val font = PDType1Font.HELVETICA_BOLD
        val fontSize = 100f
        val r0 = PDExtendedGraphicsState()
        var alpha = 0.0f
        if (watermarkTextColor["a"] is Int) {
            alpha = (watermarkTextColor["a"] as Int) * 1.0f
        }
        if (watermarkTextColor["a"] is Double) {
            alpha = (watermarkTextColor["a"] as Double).toFloat()
        }
        r0.nonStrokingAlphaConstant = alpha

        // calculate to center of the page
        val pageSize = page.mediaBox
        val rotation = page.rotation
        val stringWidth = font.getStringWidth(watermarkText) * fontSize / 1000f
        val rotate = rotation == 90 || rotation == 270
        val pageWidth = if (rotate) pageSize.height else pageSize.width
        val pageHeight = if (rotate) pageSize.width else pageSize.height
        val centerX = if (rotate) pageHeight / 2f else pageWidth / 2f
        val centerY = if (rotate) pageWidth / 2f else pageHeight / 2f
        var scale = 1.0f
        val pageDiagonal = sqrt((pageHeight * pageHeight + pageWidth * pageWidth).toDouble())
        val angle = atan((pageHeight / pageWidth).toDouble())
        val offsetX = stringWidth / 2 * cos(angle)
        val offsetY = stringWidth / 2 * sin(angle)

        val cs = PDPageContentStream(outputPdf, page, PDPageContentStream.AppendMode.APPEND, false)
        cs.setGraphicsStateParameters(r0)
        cs.setNonStrokingColor(
            (watermarkTextColor["r"] as Int),
            (watermarkTextColor["g"] as Int),
            (watermarkTextColor["b"] as Int)
        )

        cs.beginText()

        if (rotate) {
            // rotate the text according to the page rotation
            cs.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, centerX, centerY))
        } else {
            cs.setTextMatrix(Matrix.getTranslateInstance(centerX, centerY))
        }

        if (stringWidth > pageDiagonal) {
            scale = (pageDiagonal / stringWidth).toFloat()
            cs.setTextMatrix(Matrix.getScaleInstance(scale, scale))
        }

        cs.setFont(font, fontSize * scale * 0.95f) // 0,95 for edges
        cs.setTextMatrix(
            Matrix.getRotateInstance(
                angle,
                (centerX - offsetX * scale).toFloat(),
                (centerY - offsetY * scale).toFloat()
            )
        )
        cs.showText(watermarkText)
        cs.endText()
        cs.close()
    }

    private fun addSpecialPage(outputPdf: PDDocument, _text: String, separatorTextColor: Map<String, Any>) {
        val page = PDPage()
        outputPdf.addPage(page)

        val font = PDType1Font.HELVETICA_BOLD
        val fontSize = 40f
        val leading = 1.5f * fontSize
        var text = _text
        val margin = 40f

        // calculate to center of the page
        val pageSize = page.mediaBox
        val rotation = page.rotation
        val rotate = rotation == 90 || rotation == 270
        val pageWidth = if (rotate) pageSize.height else pageSize.width
        val pageHeight = if (rotate) pageSize.width else pageSize.height

        val contentWidth = pageWidth - 2 * margin

        val centerX = if (rotate) pageHeight / 2f else pageWidth / 2f
        val centerY = if (rotate) pageWidth / 2f else pageHeight / 2f

        val lines = ArrayList<String>()
        var biggestLineWidth = 0f

        var lastSpace = -1
        while (text.isNotEmpty()) {
            var spaceIndex = text.indexOf(' ', lastSpace + 1)
            if (spaceIndex < 0) {
                spaceIndex = text.length
            }
            var subString = text.substring(0, spaceIndex)
            val stringWidth = fontSize * font.getStringWidth(subString) / 1000f

            when {
                stringWidth > contentWidth -> {
                    if (lastSpace < 0) {
                        lastSpace = spaceIndex
                    }
                    subString = text.substring(0, lastSpace)
                    lines.add(subString)
                    val lineWidth = fontSize * font.getStringWidth(subString) / 1000f
                    if (lineWidth > biggestLineWidth) {
                        biggestLineWidth = lineWidth
                    }
                    text = text.substring(lastSpace).trim()
                    lastSpace = -1
                }
                spaceIndex == text.length -> {
                    lines.add(text)

                    val lineWidth = fontSize * font.getStringWidth(text) / 1000f
                    if (lineWidth > biggestLineWidth) {
                        biggestLineWidth = lineWidth
                    }
                    text = ""
                }
                else -> lastSpace = spaceIndex
            }
        }

        val textX = centerX - biggestLineWidth / 2
        val textY = centerY + lines.size * leading / 2

        val r0 = PDExtendedGraphicsState()
        var alpha = 0.0f
        if (separatorTextColor["a"] is Int) {
            alpha = (separatorTextColor["a"] as Int) * 1.0f
        }
        if (separatorTextColor["a"] is Double) {
            alpha = (separatorTextColor["a"] as Double).toFloat()
        }
        r0.nonStrokingAlphaConstant = alpha

        val cs = PDPageContentStream(outputPdf, page)
        cs.setGraphicsStateParameters(r0)
        cs.beginText()

        if (rotate) {
            // rotate the text according to the page rotation
            cs.setTextMatrix(Matrix.getRotateInstance(Math.PI / 2, textX, textY))
        } else {
            cs.setTextMatrix(Matrix.getTranslateInstance(textX, textY))
        }

        cs.setFont(font, fontSize) // 0,95 for edges

        cs.setNonStrokingColor(
            (separatorTextColor["r"] as Int),
            (separatorTextColor["g"] as Int),
            (separatorTextColor["b"] as Int)
        )

        for (line in lines) {
            cs.newLineAtOffset(0f, -leading)
            cs.showText(line)
        }
        cs.endText()
        cs.close()
    }

    private fun addImage(outputPdf: PDDocument, page: PDPage, image: PDImageXObject) {

        outputPdf.addPage(page)

        val margin = 30f

        // calculate to center of the page
        val pageSize = page.mediaBox
        val rotation = page.rotation
        val rotate = rotation == 90 || rotation == 270
        val pageWidth = if (rotate) pageSize.height else pageSize.width
        val pageHeight = if (rotate) pageSize.width else pageSize.height
        val contentWidth = pageWidth - 2 * margin
        //val contentHeight = pageHeight - 2 * margin
        val imageWidth = image.width
        val imageHeight = image.height
        var scale = 1.0f
        val centerX = if (rotate) pageHeight / 2f else pageWidth / 2f
        val centerY = if (rotate) pageWidth / 2f else pageHeight / 2f
        val offsetX = imageWidth / 2
        val offsetY = imageHeight / 2

        if (imageWidth > contentWidth) {
            scale = contentWidth / imageWidth
        }

        val cs = PDPageContentStream(outputPdf, page)
        cs.drawImage(
            image,
            (centerX - offsetX * scale),
            (centerY - offsetY * scale),
            imageWidth * scale,
            imageHeight * scale
        )
        cs.close()
    }

}