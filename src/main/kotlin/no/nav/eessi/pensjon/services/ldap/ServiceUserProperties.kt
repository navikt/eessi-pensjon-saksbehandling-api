package no.nav.eessi.pensjon.services.ldap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Serviceuser properties that can be used with ABAC and LDAP.
 */
@Configuration
@EnableConfigurationProperties
class ServiceUserProperties {

    var srvusername: String? = null
    var srvpassword: String? = null
}

