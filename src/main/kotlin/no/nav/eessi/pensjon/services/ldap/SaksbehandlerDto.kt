package no.nav.eessi.pensjon.services.ldap

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(value = "Metadata for en saksbehandler")
data class SaksbehandlerDto(
        @ApiModelProperty(value = "Saksbehandlers ident") var ident: String = "",
        @ApiModelProperty(value = "Saksbehandlers navn (med eventuelt fornavn bak komma)") var navn: String? = null
)