package no.nav.eessi.fagmodul.frontend.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.TokenContext
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestClientException

class Utils

inline fun <reified T : Any> typeRef(): ParameterizedTypeReference<T> = object : ParameterizedTypeReference<T>() {}
inline fun <reified T : Any> typeRefs(): TypeReference<T> = object : TypeReference<T>() {}
inline fun <reified T : Any> mapJsonToAny(json: String, objec: TypeReference<T>, failonunknown: Boolean = false): T {
    if (validateJson(json)) {
        return jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, failonunknown)
                .readValue<T>(json, objec)
    } else {
        throw IllegalArgumentException("Not valid json format")
    }
}

fun createErrorMessage(responseBody: String): RestClientException {
    return mapJsonToAny(responseBody, typeRefs())
}

fun mapAnyToJson(data: Any): String {
    return jacksonObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data)
}

fun mapAnyToJson(data: Any, nonempty: Boolean = false): String {
    return if (nonempty) {
        val json = jacksonObjectMapper()
                .setDefaultPropertyInclusion(JsonInclude.Include.NON_EMPTY)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(data)
        json
    } else {
        mapAnyToJson(data)
    }
}

fun validateJson(json: String): Boolean {
    return try {
        jacksonObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                .readTree(json)
        true
    } catch (ex: Exception) {
        false
    }
}

fun getClaims(oidcRequestContextHolder: OIDCRequestContextHolder): OIDCClaims {
    val context = oidcRequestContextHolder.oidcValidationContext
    if(context.issuers.isEmpty())
        throw RuntimeException("No issuer found in context")
    val issuer = context.issuers.first()

    return context.getClaims(issuer)
}



fun getToken(oidcRequestContextHolder: OIDCRequestContextHolder): TokenContext {
    val context = oidcRequestContextHolder.oidcValidationContext
    if(context.getIssuers().isEmpty())
        throw RuntimeException("No issuer found in context")
    val issuer = context.getIssuers().first()

    return context.getToken(issuer)
}

fun counter(name: String, type: String): Counter {
    return Metrics.counter(name, "type", type)
}

fun errorBody(error: String, uuid: String = "no-uuid"): String {
    return "{\"success\": false, \n \"error\": \"$error\", \"uuid\": \"$uuid\"}"
}

fun successBody(): String {
    return "{\"success\": true}"
}

/**
 * Maskerer fnr/dnr i en S3 key
 *
 * S3 key format: <fnr/dnr>___<valgfri filending>
 *
 * @param path
 */
fun maskerPersonIdentifier(path: String): String {
    val personIdentifier = path.split("___")[0]
    val mask = personIdentifier.replace(Regex("."), "*")
    return path.replace(Regex("^.*___"), "${mask}___")
}

fun maskerPersonIdentifier(paths: List<String>): String {
    return paths.map { path ->
        maskerPersonIdentifier(path)
    }.joinToString ( separator= "," )
}


fun filterPensionSedAndSort(sedList: List<String>): List<String> {
    if (sedList.isNotEmpty()) {
        return  sedList.asSequence().filter { it.startsWith("P") }.sortedBy { it }.toList()
    }
    return sedList
}

fun <E> List<E>.toJson(): String {
    return mapAnyToJson(this)
}

fun <E> List<E>.toResponse(): ResponseEntity<String?> {
    return ResponseEntity.ok().body(this.toJson())
}

