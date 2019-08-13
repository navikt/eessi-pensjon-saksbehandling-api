package no.nav.eessi.pensjon.services.eux

data class RinaAksjon(
        val dokumentType: String? = null,
        val navn: String? = null,
        val dokumentId: String? = null,
        val kategori: String? = null,
        val id: String? = null
)

data class RinaSak(
        val id: String? = null,
        val applicationRoleId: String? = null,
        val status: String? = null,
        val processDefinitionId: String? = null,
        val traits: RinaTraits? = null,
        val properties: RinaProperties? = null
)

data class RinaTraits(
        val birthday: String? =null,
        val localPin: String? = null,
        val surname: String? = null,
        val caseId: String? = null,
        val name: String? = null,
        val flowType: String? = null,
        val status: String? = null
)

data class RinaProperties(
        val importance: String? = null,
        val criticality: String? = null
)
