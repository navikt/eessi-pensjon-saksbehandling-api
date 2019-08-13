package no.nav.eessi.pensjon.services.submit

data class SubmissionRequest(
        val periodeInfo: PeriodeInfo,
        val personInfo: Personinfo,
        val bankinfo: Bankinfo,
        val comment: String? = null
)

data class Personinfo(
        val etternavnVedFodsel: String? = null,
        val tidligereNavn: String? = null,
        val fodestedBy: String? = null,
        val fodestedLand: String? = null,
        val provinsEllerDepartement: String? = null,
        val telefonnummer: String? = null,
        val morsNavn: String? = null,
        val farsNavn: String? = null,
        val epost: String? = null
)

data class Bankinfo(
        val navn: String? = null,
        val land: String? = null,
        val adresse: String? = null,
        val bicEllerSwift: String? = null,
        val kontonummerEllerIban: String? = null
)

data class PeriodeInfo(
        val ansattSelvstendigPerioder: List<StandardItem>? = null,
        val boPerioder: List<StandardItem>? = null,
        val opplaeringPerioder: List<StandardItem>? = null,
        val forsvartjenestePerioder: List<StandardItem>? = null
)

data class StandardItem(
        val land: String? = null,
        val periode: Periode? = null,
        val sted: String? = null,
        val trygdeordningnavn: String? = null,
        val medlemskap: String? = null,
        val firmaSted: String? = null,
        val forsikringId: String? = null,
        val firmaLand: String? = null,
        val navnPaaInstitusjon: String? = null,
        val navnFirma: String? = null,
        val jobbUnderAnsattEllerSelvstendig: String? = null,
        val forsikringEllerRegistreringNr: String? = null,
        val vedlegg: List<Vedlegg>? = null
)

data class Vedlegg(
        val size: Int,
        val name: String,
        val mimetype: String,
        val content: EncodedContent,
        val numPages: Int? = null
)

data class EncodedContent(
        val base64: String
)

data class Periode(
        var fom: String? = null,
        var tom: String? = null
)
