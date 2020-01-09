package no.nav.eessi.pensjon.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class SedHendelseModel (
    val id: Long? = 0,
    val sedId: String? = null,
    val sektorKode: String,
    val bucType: BucType? = null,
    val rinaSakId: String,
    val avsenderId: String? = null,
    val avsenderNavn: String? = null,
    val avsenderLand: String? = null,
    val mottakerId: String? = null,
    val mottakerNavn: String? = null,
    val mottakerLand: String? = null,
    val rinaDokumentId: String,
    val rinaDokumentVersjon: String? = null,
    val sedType: SedType? = null,
    val navBruker: String? = null
) {
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): SedHendelseModel = sedMapper.readValue(json, SedHendelseModel::class.java)
    }
}