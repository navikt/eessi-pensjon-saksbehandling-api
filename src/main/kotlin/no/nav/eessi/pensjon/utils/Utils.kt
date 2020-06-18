package no.nav.eessi.pensjon.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.TokenContext
import org.springframework.core.ParameterizedTypeReference
import java.util.*

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

fun mapAnyToJson(data: Any): String {
    return jacksonObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data)
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

    val validIssuer = context.issuers.filterNot { issuer ->
        val oidcClaims = context.getClaims(issuer)
        oidcClaims.claimSet.expirationTime.before(Date())
    }.map { it }


    if (validIssuer.isNotEmpty()) {
        val issuer = validIssuer.first()
        return context.getClaims(issuer)
    }
    throw RuntimeException("No valid issuer found in context")

}



fun getToken(oidcRequestContextHolder: OIDCRequestContextHolder): TokenContext {
    val context = oidcRequestContextHolder.oidcValidationContext
    if(context.issuers.isEmpty())
        throw RuntimeException("No issuer found in context")
    val issuer = context.issuers.first()

    return context.getToken(issuer)
}

fun errorBody(error: String?, uuid: String = "no-uuid"): String {
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

