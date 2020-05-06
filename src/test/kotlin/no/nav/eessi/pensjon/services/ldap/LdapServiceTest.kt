package no.nav.eessi.pensjon.services.ldap

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.doReturn
import org.mockito.junit.jupiter.MockitoExtension
import javax.naming.directory.BasicAttribute
import javax.naming.directory.BasicAttributes
import javax.naming.directory.SearchResult

@ExtendWith(MockitoExtension::class)
class LdapServiceTest {

    @Mock
    lateinit var ldapKlient: LdapKlient

    lateinit var ldapService: LdapService

    @BeforeEach
    fun setUp(){
       ldapService = LdapService(ldapKlient)
        ldapService.initMetrics()
    }

    @Test
    fun `gitt en bruker med medlemskap i flere grupper når etterspør memberOf så returner en liste av CN feltet for gruppene`() {
        val attribute = BasicAttribute("memberOf")
        attribute.add("CN=0000-GA-EnGruppe,OU=AccountGroups,OU=Groups,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no")
        attribute.add("CN=0000-GA-EnAnnenGruppe,OU=AccountGroups,OU=Groups,OU=NAV,OU=BusinessUnits,DC=adeo,DC=no")
        val attributes = BasicAttributes()
        attributes.put(attribute)

        doReturn(SearchResult("something", null, attributes)).`when`(ldapKlient).ldapSearch(anyString())
        val brukerInfo = ldapService.hentBrukerInformasjon("Z12345")
        assertEquals(2, brukerInfo.medlemAv.size)
        assertTrue(brukerInfo.medlemAv.contains("0000-GA-EnGruppe"))
        assertTrue(brukerInfo.medlemAv.contains("0000-GA-EnAnnenGruppe"))

        assertEquals("0000-GA-EnAnnenGruppe", brukerInfo.medlemAv[1])
    }

    @Test
    fun `gitt en tom ident når etterspør memberOf så kast illegalArgumentException`() {
        assertThrows<IllegalArgumentException> {
            ldapService.hentBrukerInformasjon("")
        }
    }

    @Test
    fun `gitt et fødselsnummer som ident når etterspør memberOf så returner tomt svar`() {
        val brukerInfo = ldapService.hentBrukerInformasjon("12345678912")
        assertEquals(0, brukerInfo.medlemAv.size)
        assertEquals("12345678912", brukerInfo.ident)
    }

    @Test
    fun `gitt en bruker som ikke finnes i AD når etterspør memberOf så returner tomt svar`() {
        doReturn(null).`when`(ldapKlient).ldapSearch(anyString())
        val brukerInfo = ldapService.hentBrukerInformasjon("Z999999")
        assertEquals(0, brukerInfo.medlemAv.size)
        assertEquals("Z999999", brukerInfo.ident)
    }

    @Test
    fun `gitt en bruker uten memberOf attributtet når etterspør memberOf så returner tomt svar`() {
        val attributes = BasicAttributes()

        doReturn(SearchResult("something", null, attributes)).`when`(ldapKlient).ldapSearch(anyString())
        val brukerInfo = ldapService.hentBrukerInformasjon("Z12345")
        assertEquals(0, brukerInfo.medlemAv.size)
        assertEquals("Z12345", brukerInfo.ident)
    }
}