package no.nav.eessi.pensjon.services.ldap

import org.apache.commons.logging.LogFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.ldap.core.ContextSource
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.LdapOperations
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.ldap.support.LdapUtils
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider
import org.springframework.security.ldap.authentication.LdapAuthenticator
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

import java.util.ArrayList
import java.util.Arrays

import org.springframework.util.StringUtils.hasText

/**
 * Creates a LdapAuthenticationProvider bean compatible with Active Directory
 * and other LDAP compliant directory servers.
 *
 *
 * Filter-based searches are depending on a service username and password, and
 * will only be enabled if they are provided.
 */
@Configuration
@EnableConfigurationProperties(LdapProviderProperties::class)
class LdapProviderAutoConfiguration(
    private val providerProperties: LdapProviderProperties,
    private val ldapProperties: NavLdapProperties
) {

    @Bean
    @ConditionalOnMissingBean
    fun userDetailsContextMapper(): UserDetailsContextMapper {
        return LdapUserDetailsMapper()
    }

    /**
     * Exposes the LdapTemplate so that the LdapHealthIndicator (Spring Actuator) is auto-configured
     */
    @Bean
    fun ldapTemplate(contextSource: ContextSource): LdapOperations {
        return LdapTemplate(contextSource)
    }

    @Bean
    @ConditionalOnMissingBean
    fun ldapAuthenticationProvider(
        userDetailsContextMapper: UserDetailsContextMapper,
        authenticator: LdapAuthenticator,
        populator: LdapAuthoritiesPopulator
    ): LdapAuthenticationProvider {
        val provider = LdapAuthenticationProvider(authenticator, populator)
        provider.setHideUserNotFoundExceptions(false)
        provider.setUserDetailsContextMapper(userDetailsContextMapper)
        return provider
    }

    @Bean
    fun ldapAuthenticator(contextSource: ContextSource): LdapAuthenticator {
        val ldapContextSource = contextSource as LdapContextSource
        val authenticator = BindAuthenticator(ldapContextSource)
        val userDnPattern = providerProperties.userDnPattern

        if (hasText(userDnPattern)) {
            authenticator.setUserDnPatterns(arrayOf(userDnPattern))
        }

        if (credentialsSpecified()) {
            authenticator.setUserSearch(newFilterBasedLdapUserSearch(ldapContextSource))
        }

        return authenticator
    }

    @Bean
    fun ldapAuthoritiesPopulator(contextSource: ContextSource): LdapAuthoritiesPopulator {
        return if (credentialsSpecified())
            newLdapAuthoritiesPopulator(contextSource)
        else
            newMemberOfAttributeAuthoritiesPopulator()
    }

    private fun credentialsSpecified(): Boolean {
        return hasText(ldapProperties.username) && hasText(ldapProperties.password)
    }

    private fun newFilterBasedLdapUserSearch(contextSource: LdapContextSource): FilterBasedLdapUserSearch {
        return FilterBasedLdapUserSearch(
            providerProperties.userSearchBase,
            providerProperties.userSearchFilter,
            contextSource
        )
    }

    private fun newLdapAuthoritiesPopulator(contextSource: ContextSource): LdapAuthoritiesPopulator {
        val populator = DefaultLdapAuthoritiesPopulator(
            contextSource, providerProperties.groupSearchBase
        )

        populator.setGroupSearchFilter(providerProperties.groupSearchFilter)
        populator.setSearchSubtree(true)
        populator.setRolePrefix(providerProperties.rolePrefix)
        populator.setConvertToUpperCase(providerProperties.isConvertRoleToUpperCase)
        return populator
    }

    private fun newMemberOfAttributeAuthoritiesPopulator(): MemberOfAttributeAuthoritiesPopulator {
        return MemberOfAttributeAuthoritiesPopulator(
            providerProperties.rolePrefix,
            providerProperties.isConvertRoleToUpperCase
        )
    }

    private class MemberOfAttributeAuthoritiesPopulator internal constructor(
        private val rolePrefix: String,
        private val convertToUpperCase: Boolean
    ) : LdapAuthoritiesPopulator {

        override fun getGrantedAuthorities(
            userData: DirContextOperations,
            username: String
        ): Collection<GrantedAuthority> {
            val groups = userData.getStringAttributes("memberOf")

            if (groups == null) {
                LOG.debug("No values for 'memberOf' attribute.")
                return AuthorityUtils.NO_AUTHORITIES
            }

            LOG.debug("'memberOf' attribute values: " + Arrays.asList(*groups))
            val authorities = ArrayList<GrantedAuthority>(groups.size)

            for (group in groups) {
                authorities.add(newSimpleGrantedAuthority(group))
            }

            return authorities
        }

        private fun newSimpleGrantedAuthority(group: String): SimpleGrantedAuthority {
            val name = LdapUtils.newLdapName(group)
            var role = name.getRdn(name.size() - 1).value.toString()

            if (convertToUpperCase) {
                role = role.toUpperCase()
            }

            return SimpleGrantedAuthority(rolePrefix + role)
        }

        companion object {

            private val LOG = LogFactory.getLog(MemberOfAttributeAuthoritiesPopulator::class.java)
        }
    }
}
