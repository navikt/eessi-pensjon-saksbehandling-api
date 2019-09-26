package no.nav.eessi.pensjon.services.ldap

import org.springframework.beans.factory.InitializingBean
import org.springframework.boot.autoconfigure.ldap.LdapProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.ldap.support.LdapEncoder

import java.text.MessageFormat

import org.springframework.ldap.support.LdapUtils.newLdapName
import org.springframework.ldap.support.LdapUtils.prepend
import org.springframework.util.StringUtils.hasText
import org.springframework.util.StringUtils.isEmpty

/**
 * Configures LDAP properties.
 * URL, base DN, username and password are optional as they can be set via
 * Spring's LdapProperties (spring.ldap) instead. However, we will update LdapProperties
 * if the properties are provided in NavLdapProperties so that the ContextSource bean will still work nicely.
 *
 *
 * Username and password can also be set via a service user:
 * serviceuser.username = foo
 * serviceuser.password = bar
 *
 *
 * Remember to set the correct DN for the service user ({0} is replaced with serviceuser.username):
 * ldap.serviceuser-dn-pattern=CN={0},OU=ServiceAccounts
 */
@ConfigurationProperties(prefix = "ldap")
class NavLdapProperties(
    private val properties: LdapProperties,
    private val environment: Environment,
    private val providerProperties: LdapProviderProperties,
    private val serviceUserProperties: ServiceUserProperties
) : InitializingBean {

    var url: String
        get() = properties.determineUrls(environment)[0]
        set(url) {
            properties.urls = arrayOf(url)
        }

    var username: String
        get() = properties.username
        set(username) {
            properties.username = username
        }

    var password: String
        get() = properties.password
        set(password) {
            properties.password = password
        }

    var basedn: String
        get() = properties.base
        set(basedn) {
            properties.base = basedn
        }

    override fun afterPropertiesSet() {
        if (hasText(username) && hasText(password)) {
            return
        }

        val dnPattern = providerProperties.serviceuserDnPattern

        if (hasText(dnPattern)) {
            serviceUserProperties.srvusername = getDistinguishedName(dnPattern!!, basedn, LdapEncoder.nameEncode(username))
        } else {
            serviceUserProperties.srvusername = username
        }

        password = serviceUserProperties.srvpassword!!
    }

    private fun getDistinguishedName(pattern: String, base: String, vararg args: String): String {
        val dnPattern = MessageFormat(pattern)
        val dn = dnPattern.format(args)
        return if (isEmpty(base)) dn else prependName(base, dn)
    }

    private fun prependName(base: String, dn: String): String {
        return prepend(newLdapName(dn), newLdapName(base)).toString()
    }
}
