package no.nav.eessi.fagmodul.frontend.services.pdf

class PDFRequest(
        val recipe: Map<String, List<RecipeStep>>,
        val files: List<Files>,
        val watermark: PDFWatermark
)

class PDFWatermark(
        val watermarkText: String? = null,
        val watermarkTextColor: Map<String, Any>? = null
)

class RecipeStep(
        val name: String? = null,
        val pageNumber: Int = 0,
        val separatorText : String? = null,
        val separatorTextColor : Map<String, Any>? = null,
        val type: String
)

class FileContent(
        val base64: String
)

class Files(
        val content: FileContent,
        val name: String,
        val mimetype: String,
        val size: Int
)


