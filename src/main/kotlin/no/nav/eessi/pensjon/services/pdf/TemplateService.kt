package no.nav.eessi.pensjon.services.pdf

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.NullNode
import com.openhtmltopdf.extend.FSSupplier
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import org.jsoup.Jsoup
import org.jsoup.helper.W3CDom
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Description
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.util.*

private val logger = LoggerFactory.getLogger(TemplateService::class.java)

@Service
@Description("Service class for HTML template generation")

class TemplateService {

    private final val fontPath = "/fonts/LiberationSans-Regular.ttf"
    val htmlPath = "/html-templates"
    val url: URL = this.javaClass.getResource(fontPath)
    val fontName = "LiberationSans"
    val countryData = CountryData()

    private fun hentKvitteringHtmlSomStream(page: String): InputStream {
        return this.javaClass.getResource("$htmlPath/kvittering_$page.html").openStream()
    }

    fun generateReceipt(rawJsonData : String, subject : String, page: String) : Map<String, Any> {

        val doc = Jsoup.parse(hentKvitteringHtmlSomStream(page), "UTF-8", "")
        doc.outputSettings().syntax(Document.OutputSettings.Syntax.xml)

        val data = ObjectMapper().readTree(rawJsonData)
        fillUpReceiptWithData(data, doc, subject, page)

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
            "name" to "kvittering_$page.pdf",
            "size" to byteArray.size,
            "mimetype" to "application/pdf",
            "content" to mapOf("base64" to Base64.getEncoder().encodeToString(byteArray))
        )
    }

    private fun getFSSsupplierStream(): FSSupplier<InputStream> {
        logger.info("url fontpath : $url")
        return FontSupplierStream(url)
    }

    private fun hasValue(it: JsonNode, key : String) : Boolean {
        if (it.has(key)) {
            return it.get(key) !is NullNode
        }
        return false
    }

    private fun getTextValue(it: JsonNode, key : String) : String {
        if (it.has(key)) {
            val node = it.get(key)
            if (node !is NullNode) {
                return node.textValue()
            }
        }
        return "-"
    }

    private fun getAttachments(attachmentsArray: JsonNode) : String {
        var map = listOf<String>()
        attachmentsArray.forEach {
            map += it.get("name").toString()
        }
        return map.joinToString(", ")
    }

    private fun fillUpPeriod(parentNode: Element, it : JsonNode, type : String ) {
        var grey = true
        val div = Element("div")
        var period = it.get("periode")
        var periodValue = ""
        if (hasValue(period, "lukketPeriode")) {
            periodValue = getTextValue(period.get("lukketPeriode"), "fom") + " - " +
                    getTextValue(period.get("lukketPeriode"), "tom")
        }
        if (hasValue(period, "openPeriode")) {
            periodValue +=  getTextValue(period.get("openPeriode"), "fom") + " - "
            if (getTextValue(period.get("openPeriode"), "extra").toString() == "01") {
                periodValue += "Pågående"
            } else {
                periodValue += "Ukjent"
            }
        }
        if (hasValue(it, "usikkerDatoIndikator") && it.get("usikkerDatoIndikator").toString() == "1") {
           periodValue += " (?)"
        }
        appendParagraph(parentNode, type)
        grey = appendEntry(parentNode, grey, "Periode", periodValue)
        if (hasValue(it, "sted")) {
            grey = appendEntry(parentNode, grey, "Sted", getTextValue(it,"sted"))
        }
        if (hasValue(it, "land")) {
            grey = appendEntry(parentNode, grey, "Land", countryData.getLabel(getTextValue(it, "land")))
        }
        if (hasValue(it, "jobbUnderAnsattEllerSelvstendig")) {
            grey = appendEntry(parentNode, grey, "Yrkesaktivitet", getTextValue(it,"jobbUnderAnsattEllerSelvstendig"))
            grey = appendEntry(parentNode, grey, "Arbeidsgivers navn", getTextValue(it,"navnFirma"))
            if (hasValue(it, "typePeriode")) {
                var worktype = "Ansettelsesforhold"
                if (getTextValue(it,"typePeriode") == "02") {
                    worktype = "Selvstendig næringsvirksomhet"
                }
                grey = appendEntry(parentNode, grey, "Type", worktype)
            }
            if (hasValue(it, "firmaSted")) {
                grey = appendEntry(parentNode, grey, "Arbeidsgivers sted", getTextValue(it, "firmaSted"))
            }
            if (hasValue(it, "adresseFirma")) {
                var address = it.get("adresseFirma")
                if (hasValue(address, "gate")) {
                    grey = appendEntry(parentNode, grey, "Gate", getTextValue(address, "gate"))
                }
                if (hasValue(address, "postnummer")) {
                    grey = appendEntry(parentNode, grey, "Postnummer", getTextValue(address, "postnummer"))
                }
                if (hasValue(address, "by")) {
                    grey = appendEntry(parentNode, grey, "By", getTextValue(address, "by"))
                }
                if (hasValue(address, "region")) {
                    grey = appendEntry(parentNode, grey, "Region", getTextValue(address, "region"))
                }
                if (hasValue(address, "land")) {
                    grey = appendEntry(parentNode, grey, "Land", countryData.getLabel(getTextValue(address, "land")))
                }
            }
        }
        if (hasValue(it, "firmaLand")) {
            grey = appendEntry(parentNode, grey, "Arbeidsgivers land",  countryData.getLabel(getTextValue(it, "firmaLand")))
        }

        if (hasValue(it, "trygdeordningnavn")) {
            grey = appendEntry(parentNode, grey, "Trygdeordning navn", getTextValue(it,"trygdeordningnavn"))
        }
        if (hasValue(it, "medlemskap")) {
            grey = appendEntry(parentNode, grey, "Medlemskap i trygdeordning", getTextValue(it,"medlemskap"))
        }
        if (hasValue(it, "forsikringId")) {
            grey = appendEntry(parentNode, grey, "Trygdenummer/ID-nummer", getTextValue(it,"forsikringId"))
        }

        if (hasValue(it, "navnPaaInstitusjon")) {
            var label = "Navn på betalende institusjon"
            if (type == "Utdanning") {
                label = "Navn på opplæringsinstitusjon"
            }
            grey = appendEntry(parentNode, grey, label, getTextValue(it,"navnPaaInstitusjon"))
        }
        if (hasValue(it, "typePeriode")) {
            grey = appendEntry(parentNode, grey, "Type periode", getTextValue(it,"typePeriode"))
        }
        if (hasValue(it, "informasjonBarn")) {
            var barn = it.get("informasjonBarn")
            grey = appendEntry(parentNode, grey, "Barn-s",
                getTextValue(barn,"etternavn") + ", " + getTextValue(barn,"fornavn") + " - født " +
                        getTextValue(barn,"foedseldato") + " - " + countryData.getLabel(getTextValue(barn,"land")))
        }

        if (hasValue(it, "annenInformasjon")) {
            grey = appendEntry(parentNode, grey, "Annen informasjon", getTextValue(it,"annenInformasjon"))
        }
        if (hasValue(it, "vedlegg")) {
            appendEntry(parentNode, grey, "Annen informasjon", getAttachments(it.get("vedlegg")))
        }

        div.append(Element("p").toString())
        parentNode.append(div.toString())
    }

    private fun appendParagraph(parentNode: Element, value: String) {
        parentNode.append(Element("p").text(value).toString())
    }

    private fun appendEntry(parentNode: Element, grey: Boolean, label: String, value: String): Boolean {
        var dt = Element("dt")
        if (grey) { dt.addClass("grey") }
        var dd = Element("dd")
        if (grey) { dd.addClass("grey") }
        parentNode.append(dt.text(label).toString())
        parentNode.append(dd.text(value).toString())
        return !grey
    }

    private fun fillUpPeriods (parentNode: Element, key: String, label: String, stayAbroadInfo: JsonNode) {
        if (stayAbroadInfo.has(key)) {
            stayAbroadInfo.get(key).forEach {
                fillUpPeriod(parentNode, it, label)
            }
        }
    }

    private fun fillUpReceiptWithData(data : JsonNode, doc : Document, subject : String, page: String) {
        var grey = true
        val personInfo = data.get("personInfo")
        var parentNode = doc.select("#personInfo").first()
        grey = appendEntry(parentNode, grey, "Fødselsnummer", subject)
        grey = appendEntry(parentNode, grey, "Etternavn ved fødsel", getTextValue(personInfo, "etternavnVedFodsel"))
        grey = appendEntry(parentNode, grey, "Tidligere navn", getTextValue(personInfo, "tidligereNavn"))
        grey = appendEntry(parentNode, grey, "Land", countryData.getLabel(getTextValue(personInfo, "fodestedLand")))
        grey = appendEntry(parentNode, grey, "Sted", getTextValue(personInfo, "fodestedBy"))
        grey = appendEntry(parentNode, grey, "Region", getTextValue(personInfo, "provinsEllerDepartement"))
        grey = appendEntry(parentNode, grey, "Telefonnummer", getTextValue(personInfo, "telefonnummer"))
        grey = appendEntry(parentNode, grey, "E-postadresse", getTextValue(personInfo, "epost"))
        if (hasValue(personInfo, "farsNavn")) {
            grey = appendEntry(parentNode, grey, "Farsnavn", getTextValue(personInfo, "farsNavn"))
        }
        if (hasValue(personInfo, "morsNavn")) {
            grey = appendEntry(parentNode, grey, "Morsnavn", getTextValue(personInfo, "morsNavn"))
        }

        val bankInfo = data.get("bankInfo")
        parentNode = doc.select("#bankInfo").first()
        grey = appendEntry(parentNode, grey, "Bankens navn",  getTextValue(bankInfo, "navn"))
        grey = appendEntry(parentNode, grey, "Bankens adresse",  getTextValue(bankInfo, "adresse"))
        grey = appendEntry(parentNode, grey, "Land",  countryData.getLabel(getTextValue(bankInfo, "land")))
        grey = appendEntry(parentNode, grey, "BIC / SWIFT",  getTextValue(bankInfo, "bicEllerSwift"))
        appendEntry(parentNode, grey, "Kontonummer/IBAN",  getTextValue(bankInfo, "kontonummerEllerIban"))

        val stayAbroadInfo = data.get("periodeInfo")
        parentNode = doc.select("#stayAbroad").first()
        fillUpPeriods(parentNode,"ansattSelvstendigPerioder", "Arbeid", stayAbroadInfo)
        fillUpPeriods(parentNode,"opplaeringPerioder", "Utdanning", stayAbroadInfo)
        fillUpPeriods(parentNode,"forsvartjenestePerioder", "Militær eller siviltjeneste", stayAbroadInfo)
        fillUpPeriods(parentNode,"boPerioder", "Boperiode", stayAbroadInfo)
        fillUpPeriods(parentNode,"barnepassPerioder", "Omsorg for barn", stayAbroadInfo)
        fillUpPeriods(parentNode,"frivilligPerioder", "Frivillig forsikrings", stayAbroadInfo)
        fillUpPeriods(parentNode,"foedselspermisjonPerioder", "Foreldrepenger til mor", stayAbroadInfo)
        fillUpPeriods(parentNode,"arbeidsledigPerioder", "Dagpenger", stayAbroadInfo)
        fillUpPeriods(parentNode,"sykePerioder", "Sykepenger", stayAbroadInfo)
        fillUpPeriods(parentNode,"andrePerioder", "Annen type forsikringsperioder", stayAbroadInfo)

        if (page == "e207") {
            doc.select("#comment").first().text(getTextValue(data, "comment"))
        }
    }
}

class FontSupplierStream(private val ttfurl: URL) : FSSupplier<InputStream> {
    override fun supply(): InputStream {
        logger.info("ttfurl fontpath for stream : $ttfurl")
        return ttfurl.openStream()
    }
}
