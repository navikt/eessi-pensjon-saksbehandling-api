package no.nav.eessi.pensjon.api.person

import io.mockk.every
import io.mockk.mockk
import no.nav.eessi.pensjon.personoppslag.pdl.PersonService
import no.nav.eessi.pensjon.personoppslag.pdl.model.AktoerId
import no.nav.eessi.pensjon.personoppslag.pdl.model.Ident
import no.nav.eessi.pensjon.personoppslag.pdl.model.Navn
import no.nav.eessi.pensjon.personoppslag.pdl.model.Person
import no.nav.eessi.pensjon.utils.mapJsonToAny
import no.nav.eessi.pensjon.utils.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class NavRegistreOppslagControllerTest {

    private val mockService = mockk<PersonService>()

    private val controller = NavRegistreOppslagController(mockService)

    @Nested
    inner class HentPerson {
        @Test
        fun `Person med fornavn, mellomnavn, og etternavn`() {
            every { mockService.hentPerson(any<Ident<*>>()) } returns personMedNavn("for", "mellom", "etter")

            val result = controller.getDocument("1234")

            val person = mapJsonToAny(result.body!!, typeRefs<Personinformasjon>())

            assertEquals("for", person.fornavn)
            assertEquals("mellom", person.mellomnavn)
            assertEquals("etter", person.etternavn)
            assertEquals("for mellom etter", person.fulltNavn)
        }

        @Test
        fun `Person mangler mellomnavn`() {
            every { mockService.hentPerson(any<Ident<*>>()) } returns personMedNavn("for", null, "etter")

            val result = controller.getDocument("1234")

            val person = mapJsonToAny(result.body!!, typeRefs<Personinformasjon>())

            assertEquals("for", person.fornavn)
            assertNull(person.mellomnavn)
            assertEquals("etter", person.etternavn)
            assertEquals("for etter", person.fulltNavn)
        }

        @Test
        fun `Person ikke funnet`() {
            every { mockService.hentPerson(any<Ident<*>>()) } returns null

            val result = controller.getDocument("1234")

            assertEquals(HttpStatus.NOT_FOUND, result.statusCode)
            assertNotNull(result.body) // Skal inneholde feilmelding til frontend
        }
    }

    @Nested
    inner class HentAktoerId {
        @Test
        fun `Fnr er tomt`() {
            val result = controller.hentGjeldendeAktoerIdForNorskident("")

            assertEquals(HttpStatus.BAD_REQUEST, result.statusCode)
            assertEquals("blankt fnr", result.body)
        }

        @Test
        fun `PDL gir gyldig resultat`() {
            every { mockService.hentAktorId(any()) } returns AktoerId("1")

            val result = controller.hentGjeldendeAktoerIdForNorskident("012345678901")

            assertEquals(HttpStatus.OK, result.statusCode)
            assertEquals("1", result.body)
        }

        @Test
        fun `PDL kaster feil`() {
            every { mockService.hentAktorId(any()) } throws Exception("message")

            val result = controller.hentGjeldendeAktoerIdForNorskident("012345678901")

            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.statusCode)
            assertNotNull(result.body)
        }
    }

    private fun personMedNavn(fornavn: String, mellomnavn: String?, etternavn: String): Person {
        return mockk {
            every { navn } returns Navn(
                fornavn, mellomnavn, etternavn,
                null, null, null, mockk()
            )
        }
    }
}
