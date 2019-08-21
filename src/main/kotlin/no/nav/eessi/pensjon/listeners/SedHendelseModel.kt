package no.nav.eessi.pensjon.listeners

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class SedHendelseModel (
    val id: Long? = 0,
    val sedId: String? = null,
    val sektorKode: String,
    val bucType: String?,
    val rinaSakId: String,
    val avsenderId: String,
    val avsenderNavn: String,
    val avsenderLand: String? = null,
    val mottakerId: String,
    val mottakerNavn: String,
    val mottakerLand: String? = null,
    val rinaDokumentId: String,
    val rinaDokumentVersjon: String? = null,
    val sedType: String?,
    val navBruker: String? = null
){
    companion object {
        private val sedMapper: ObjectMapper = jacksonObjectMapper().configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)

        fun fromJson(json: String): SedHendelseModel = sedMapper.readValue(json, SedHendelseModel::class.java)
    }
}