package no.nav.eessi.fagmodul.frontend.services.pdf

data class PDFRequest(
        val recipe: Map<String, List<RecipeStep>>,
        val files: List<Files>,
        val watermark: PDFWatermark
)

data class PDFWatermark(
        val watermarkText: String? = null,
        val watermarkTextColor: Map<String, Any>? = null
)

data class RecipeStep(
        val name: String? = null,
        val pageNumber: Int = 0,
        val separatorText : String? = null,
        val separatorTextColor : Map<String, Any>? = null,
        val type: String
)

data class FileContent(
        val base64: String
)

data class Files(
        val content: FileContent,
        val name: String,
        val numPages: Int? = null,
        val mimetype: String,
        val size: Int
)

data class E207block1(
        val etternavn: String = "",
        val etternavnVedFodsel: String = "",
        val fornavn: String = "",
        val tidligereNavn: String = "",
        val kjonn: String = "",
        val farsEtternavnOgFornavn: String = "",
        val morsEtternavnOgFornavn: String = "",
        val trygdenummer: String = ""
)

data class E207block2(
        val statsborgerskap: String = "",
        val dni: String = ""
)

data class E207block3(
        val dato: String = "",
        val fodested: String = "",
        val provinsEllerDepartement: String = "",
        val land: String = ""
)

data class E207Block4(
        val adresse: String = ""
)

data class E207Block5(
        val trygdenummerBehandlendeInstitusjon: String = ""
)

data class E207Block6(
        val navn: String = "",
        val adresse: String = "",
        val stempel: String = "",
        val dato: String = "",
        val underskrift: String = ""
)

data class E207Block7(
        val fra: String = "",
        val til: String = "",
        val art: String = "",
        val navn: String = "",
        val sted: String = "",
        val institution: String = "",
        val trygdenummer: String = "",
        val medlemskap: String = "",
        val bosted: String = ""
)

