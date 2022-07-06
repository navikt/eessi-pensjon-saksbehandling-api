package no.nav.eessi.pensjon.utils

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import java.util.*

fun getClaims(tokenValidationContextHolder: TokenValidationContextHolder): JwtTokenClaims {
    val context = tokenValidationContextHolder.tokenValidationContext
    if(context.issuers.isEmpty())
        throw RuntimeException("No issuer found in context")

    val validIssuer = context.issuers.filterNot { issuer ->
        val oidcClaims = context.getClaims(issuer)
        oidcClaims.expirationTime.before(Date())
    }.map { it }


    if (validIssuer.isNotEmpty()) {
        val issuer = validIssuer.first()
        return context.getClaims(issuer)
    }
    throw RuntimeException("No valid issuer found in context")
}

fun getToken(tokenValidationContextHolder: TokenValidationContextHolder): JwtToken {
    val context = tokenValidationContextHolder.tokenValidationContext
    if(context.issuers.isEmpty())
        throw RuntimeException("No issuer found in context")
    val issuer = context.issuers.first()

    return context.getJwtToken(issuer)
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

