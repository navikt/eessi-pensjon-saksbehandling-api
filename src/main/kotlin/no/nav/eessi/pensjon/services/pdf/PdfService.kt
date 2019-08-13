package no.nav.eessi.pensjon.services.pdf

import com.openhtmltopdf.extend.FSSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState
import org.apache.pdfbox.util.Matrix
import org.codehaus.jackson.JsonNode
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.NullNode
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val logger = LoggerFactory.getLogger(PdfService::class.java)

@Service
@Description("Service class for PDF")
class PdfService {

    private val logger = LoggerFactory.getLogger(PdfService::class.java)

    private final val fontPath = "/fonts/LiberationSans-Regular.ttf"
    val htmlPath = "/html-templates/kvittering.html"

    val url: URL = this.javaClass.getResource(fontPath)
    val fontName = "LiberationSans"

    private fun hentKvitteringHtmlSomStream(): InputStream {
        return this.javaClass.getResource(htmlPath).openStream()
    }

    fun generate(request: PDFRequest) : HashMap<String, Map<String, Any>> {

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
                            addWatermark(outputPdf, page, request.watermark.watermarkText, request.watermark.watermarkTextColor!!)
                        }
                        numPages++
                    }

                    if (step.type == "pickImage") {

                        val sourceImage = workingImages[step.name]
                        val page = PDPage()
                        addImage(outputPdf, page, sourceImage!!)

                        if (request.watermark.watermarkText != null) {
                            addWatermark(outputPdf, page, request.watermark.watermarkText, request.watermark.watermarkTextColor!!)
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

    private fun addWatermark(outputPdf : PDDocument, page : PDPage, watermarkText : String, watermarkTextColor : Map<String, Any>) {
        val font = PDType1Font.HELVETICA_BOLD
        val fontSize = 100f
        val r0 = PDExtendedGraphicsState()
        var alpha = 0.0f
        if (watermarkTextColor["a"] is Int) {
            alpha = (watermarkTextColor["a"] as Int)* 1.0f
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
        val centerY = if (rotate) pageWidth  / 2f else pageHeight / 2f
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
        cs.setTextMatrix(Matrix.getRotateInstance(angle, (centerX - offsetX * scale).toFloat(), (centerY - offsetY * scale).toFloat()))
        cs.showText(watermarkText)
        cs.endText()
        cs.close()
    }

    private fun addSpecialPage(outputPdf : PDDocument, _text : String, separatorTextColor: Map<String, Any>) {
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

        val contentWidth = pageWidth - 2 *margin

        val centerX = if (rotate) pageHeight / 2f else pageWidth / 2f
        val centerY = if (rotate) pageWidth  / 2f else pageHeight / 2f

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
            alpha = (separatorTextColor["a"] as Int)* 1.0f
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

    private fun addImage(outputPdf : PDDocument, page: PDPage, image : PDImageXObject) {

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
        val centerY = if (rotate) pageWidth  / 2f else pageHeight / 2f
        val offsetX = imageWidth / 2
        val offsetY = imageHeight / 2

        if (imageWidth > contentWidth) {
            scale = contentWidth / imageWidth
        }

        val cs = PDPageContentStream(outputPdf, page)
        cs.drawImage(image, (centerX - offsetX * scale),(centerY - offsetY * scale), imageWidth * scale, imageHeight * scale)
        cs.close()
    }

    private fun getFSSsupplierStream() : FSSupplier<InputStream> {
        logger.info("url fontpath : $url")
        return FontSupplierStream(url)
    }

    fun generateReceipt(rawJsonData : String, subject : String) : Map<String, Any> {

        val doc = Jsoup.parse(hentKvitteringHtmlSomStream(), "UTF-8", "")
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)

        val data = ObjectMapper().readTree(rawJsonData)
        fillUpReceiptWithData(data, doc, subject)

        val baos = ByteArrayOutputStream()
        val builder  = PdfRendererBuilder()

        builder.useFont(getFSSsupplierStream(), fontName)
        val buildPdfRenderer = builder.withW3cDocument(W3CDom().fromJsoup(doc), null)
            .toStream(baos)
            .buildPdfRenderer()

        buildPdfRenderer.layout()
        buildPdfRenderer.createPDF()
        val byteArray = baos.toByteArray()
        return mapOf(
            "name" to "kvittering.pdf",
            "size" to byteArray.size,
            "mimetype" to "application/pdf",
            "content" to mapOf("base64" to Base64.getEncoder().encodeToString(byteArray))
        )
    }

    private fun getTextValue( it: JsonNode, key : String) : String {
        if (it.has(key)) {
            val node = it.get(key)
            if (node !is NullNode) {
                return node.textValue
            }
        }
        return "-"
    }

    private fun fillUpPeriod(it : JsonNode, type : String ) : String {

        val div = Element("div")
        div.append(Element("dt").addClass("grey").text("Periode").toString())
        div.append(Element("dd").addClass("grey").text(type + " - " + getTextValue(it.get("periode"), "fom") + " - " +  getTextValue(it.get("periode"), "tom")).toString())

        div.append(Element("dt").addClass("col-4").text("Land").toString())
        div.append(Element("dd").text(getTextValue(it, "land")).toString())

        div.append(Element("dt").addClass("grey").text("Trygdeordning navn").toString())
        div.append(Element("dd").addClass("grey").text(getTextValue(it, "trygdeordningnavn")).toString())

        div.append(Element("dt").text("Trygdenummer/ID-nummer").toString())
        div.append(Element("dd").text(getTextValue(it, "forsikringId")).toString())

        div.append(Element("dt").addClass("grey").text("Medlemskap i trygdeordning").toString())
        div.append(Element("dd").addClass("grey").text(getTextValue(it, "medlemskap")).toString())

        div.append(Element("dt").text("Sted").toString())
        div.append(Element("dd").text(getTextValue(it, "sted")).toString())

        div.append(Element("dt").addClass("grey").text("Land").toString())
        div.append(Element("dd").addClass("grey").text(getTextValue(it, "firmaLand")).toString())

        if (type == "Arbeid") {

            div.append(Element("dt").text("Arbeidsgivers sted").toString())
            div.append(Element("dd").text(getTextValue(it, "firmaSted")).toString())

            div.append(Element("dt").addClass("grey").text("Arbeidsgivers navn").toString())
            div.append(Element("dd").addClass("grey").text(getTextValue(it, "navnFirma")).toString())

            div.append(Element("dt").text("Trkesaktivitet").toString())
            div.append(Element("dd").text(getTextValue(it, "jobbUnderAnsattEllerSelvstendig")).toString())
        }

        if (type == "Utdanning") {
            div.append(Element("dt").text("Navn på utdanningsinstitusjon").toString())
            div.append(Element("dd").text(getTextValue(it, "navnPaaInstitusjon")).toString())
        }

        div.append(Element("p").toString())
        return div.toString()
    }

    private fun fillUpReceiptWithData(data : JsonNode, doc : Document, subject : String) {

        val personInfo = data.get("personInfo")
        doc.select("#person-id").first().text(subject)
        doc.select("#person-nameAtBirth").first().text(getTextValue(personInfo, "etternavnVedFodsel"))
        doc.select("#person-previousName").first().text(getTextValue(personInfo, "tidligereNavn"))
        doc.select("#person-country").first().text(getTextValue(personInfo, "fodestedLand"))
        doc.select("#person-place").first().text(getTextValue(personInfo, "fodestedBy"))
        doc.select("#person-region").first().text(getTextValue(personInfo, "provinsEllerDepartement"))
        doc.select("#person-phone").first().text(getTextValue(personInfo, "telefonnummer"))
        doc.select("#person-email").first().text(getTextValue(personInfo, "epost"))
        doc.select("#person-fatherName").first().text(getTextValue(personInfo, "farsNavn"))
        doc.select("#person-motherName").first().text(getTextValue(personInfo, "morsNavn"))

        val bankInfo = data.get("bankinfo")
        doc.select("#bank-name").first().text(getTextValue(bankInfo, "navn"))
        doc.select("#bank-address").first().text(getTextValue(bankInfo, "adresse"))
        doc.select("#bank-country").first().text(getTextValue(bankInfo, "land"))
        doc.select("#bank-swift").first().text(getTextValue(bankInfo, "bicEllerSwift"))
        doc.select("#bank-iban").first().text(getTextValue(bankInfo, "kontonummerEllerIban"))

        val stayAbroadInfo = data.get("periodeInfo")
        val stayAbroadDiv = doc.select("#stayAbroad")

        stayAbroadInfo.get("ansattSelvstendigPerioder").forEach {
            stayAbroadDiv.append(fillUpPeriod(it, "Arbeid"))
        }

        stayAbroadInfo.get("opplaeringPerioder").forEach {
            stayAbroadDiv.append(fillUpPeriod(it, "Utdanning"))
        }

        stayAbroadInfo.get("forsvartjenestePerioder").forEach {
            stayAbroadDiv.append(fillUpPeriod(it, "Militær eller siviltjeneste"))
        }

        stayAbroadInfo.get("boPerioder").forEach {
            stayAbroadDiv.append(fillUpPeriod(it, "Boperiode"))
        }
        doc.select("#comment").first().text(getTextValue(data, "comment"))
    }

}

class FontSupplierStream(private val ttfurl: URL) : FSSupplier<InputStream> {
    override fun supply(): InputStream {
        logger.info("ttfurl fontpath for stream : $ttfurl")
        return ttfurl.openStream()
    }

}
