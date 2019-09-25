package no.nav.eessi.pensjon.services.ldap

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ldap.provider")
class LdapProviderProperties {

    // all DNs relative to ldap.basedn
    var userDnPattern: String? = null
    var serviceuserDnPattern: String? = null

    var userSearchBase: String? = null
    var userSearchFilter = "(&(objectClass=user)(sAMAccountName={0}))"

    var groupSearchBase: String? = null
    var groupSearchFilter = "(&(objectClass=group)(member={0}))"
    var rolePrefix = "ROLE_"
    var isConvertRoleToUpperCase = true
}
