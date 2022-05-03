package no.nav.helse.flex.client.pdl

const val HENT_GEOGRAFISK_TILKNYTNING_QUERY =
    """
query(${"$"}ident: ID!) {
    hentGeografiskTilknytning(ident: ${"$"}ident) {
        gtType
        gtKommune
        gtBydel
        gtLand
    }
}
"""

data class HentGeografiskTilknytningResponse(
    val data: HentGeografiskTilknytningResponseData,
    val errors: List<ResponseError>?
)

data class HentGeografiskTilknytningResponseData(
    val hentGeografiskTilknytning: HentGeografiskTilknytning
)

data class HentGeografiskTilknytning(
    val gtType: String,
    val gtLand: String?,
    val gtKommune: String?,
    val gtBydel: String?
)
