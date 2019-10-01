package no.nav.eessi.pensjon.services.ldap

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import javax.naming.directory.*

@ExtendWith(MockitoExtension::class)
class LdapServiceTest {

    @Mock
    lateinit var ldapKlient: LdapKlient

    @InjectMocks
    lateinit var ldapService: LdapService

    @Test
    fun `gitt en bruker med medlemskap i flere grupper når etterspør memberOf så returner en liste av CN feltet for gruppene`() {
        val attribute = BasicAttribute("memberOf")
        attribute.add("CN=0000-GA-EnGruppe,OU=AccountGroups,OU=Groups,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no")
        attribute.add("CN=0000-GA-EnAnnenGruppe,OU=AccountGroups,OU=Groups,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no")
        val attributes = BasicAttributes()
        attributes.put(attribute)

        doReturn(SearchResult("something", null, attributes)).`when`(ldapKlient).ldapSearch(anyString())
        val brukerInfo = ldapService.hentBrukerInformasjon("Z12345")
        assertEquals(2, brukerInfo!!.medlemAv.size)
        assertTrue(brukerInfo.medlemAv.contains("0000-GA-EnGruppe"))
        assertTrue(brukerInfo.medlemAv.contains("0000-GA-EnAnnenGruppe"))
    }
}