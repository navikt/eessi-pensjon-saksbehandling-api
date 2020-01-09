package no.nav.eessi.pensjon.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Paths

class SedHendelseModelTest {

    @Test
    fun `Gitt en gyldig SEDSendt json når mapping så skal alle felter mappes`() {
        val sedSendtJson = String(Files.readAllBytes(Paths.get("src/test/resources/sed/P_BUC_01.json")))
        val sedHendelse = SedHendelseModel.fromJson(sedSendtJson)
        assertEquals(sedHendelse.id, 1869L)
        assertEquals(sedHendelse.sedId, "P2000_b12e06dda2c7474b9998c7139c841646_2")
        assertEquals(sedHendelse.sektorKode, "P")
        assertEquals(sedHendelse.bucType, "P_BUC_01")
        assertEquals(sedHendelse.rinaSakId, "147729")
        assertEquals(sedHendelse.avsenderId, "NO:NAVT003")
        assertEquals(sedHendelse.avsenderNavn, "NAVT003")
        assertEquals(sedHendelse.mottakerNavn, "NAV Test 07")
        assertEquals(sedHendelse.rinaDokumentId, "b12e06dda2c7474b9998c7139c841646")
        assertEquals(sedHendelse.rinaDokumentVersjon, "2")
        assertEquals(sedHendelse.sedType, "P2000")
        assertEquals(sedHendelse.navBruker, "12378945601")
    }
}