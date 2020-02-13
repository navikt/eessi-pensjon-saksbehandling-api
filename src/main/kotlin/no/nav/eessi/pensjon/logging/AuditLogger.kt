package no.nav.eessi.pensjon.logging

import no.nav.security.oidc.context.OIDCClaims
import no.nav.security.oidc.context.OIDCRequestContextHolder
import no.nav.security.spring.oidc.SpringOIDCRequestContextHolder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AuditLogger(private val oidcRequestContextHolder: OIDCRequestContextHolder) {
    private val logger = LoggerFactory.getLogger("auditLogger")

    // Vi trenger denne no arg konstruktøren for å kunne bruke @Spy med mockito
    constructor() : this( SpringOIDCRequestContextHolder() )

    //brukerident: Z990638 tjenesten: getBucogSedView
    fun log(tjenesteFunctionName: String) {
        logger.info("brukerident: ${getSubject()} tjenesten: $tjenesteFunctionName")
    }

    private fun getSubject(): String {
        return try {
            getClaims(oidcRequestContextHolder).subject
        } catch (ex: Exception) {
            logger.error("Brukerident ikke funnet")
            "n/a"
        }
    }

    private fun getClaims(oidcRequestContextHolder: OIDCRequestContextHolder): OIDCClaims {
        val context = oidcRequestContextHolder.oidcValidationContext
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")
        val issuer = context.issuers.first()
        return context.getClaims(issuer)
    }

}