package no.nav.eessi.pensjon.logging

import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.oidc.context.TokenContext
import no.nav.security.spring.oidc.SpringOIDCRequestContextHolder
import org.slf4j.LoggerFactory

class AuditLogger(private val oidcRequestContextHolder: OIDCRequestContextHolder) {
    private val logger = LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this(SpringOIDCRequestContextHolder())

    //brukerident: Z990638 tjenesten: getBucogSedView
    fun log(tjenesteFunctionName: String) {
        logger.info("brukerident: ${getSubjectfromToken()} tjenesten: $tjenesteFunctionName")
    }

    private fun getSubjectfromToken() : String {

        return try {
            val context = oidcRequestContextHolder.oidcValidationContext
            val tokenContext = getTokenContext("isso")
            val issuer = tokenContext.issuer
            context.getClaims(issuer).subject
        } catch (ex: Exception) {
            logger.error("Brukerident ikke funnet")
            "n/a"
        }
    }

    private fun getTokenContext(tokenKey: String): TokenContext {
        val context = oidcRequestContextHolder.oidcValidationContext
        if (context.issuers.isEmpty()) throw RuntimeException("No issuer found in context")
        val tokenkeys = context.issuers
        if (tokenkeys.contains(tokenKey)) {
            return context.getToken(tokenKey)
        }
        throw RuntimeException("No issuer found in context")
    }



}