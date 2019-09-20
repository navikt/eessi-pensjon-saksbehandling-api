package no.nav.eessi.pensjon.services.pdf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CountryDataTest {

    @Test
    fun `Check label`() {
        val countryData = CountryData()
        assertEquals("XX", countryData.getLabel("XX"))
        assertEquals("Norge", countryData.getLabel("NO"))
    }
}