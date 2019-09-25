package no.nav.eessi.pensjon.services.ldap

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(NavLdapProperties::class)
class NavLdapAutoConfiguration
