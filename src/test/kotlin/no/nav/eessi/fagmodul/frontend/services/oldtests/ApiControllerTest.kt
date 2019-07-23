package no.nav.eessi.fagmodul.frontend.services.oldtests

import no.nav.eessi.fagmodul.frontend.services.eux.RinaAksjon

//denne kan vel fjenres snart?
class ApiControllerTest {

    private fun getAksjonlist(): List<RinaAksjon> {
        return listOf(
                RinaAksjon(
                        navn = "Create",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "P6000",
                        dokumentId = "213123123"
                ),
                RinaAksjon(
                        navn = "Create",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "X6000",
                        dokumentId = "213123123"
                ),
                RinaAksjon(
                        navn = "Update",
                        id = "123123343123",
                        kategori = "Documents",
                        dokumentType = "X200",
                        dokumentId = "213123123"
                )
        )
    }
}