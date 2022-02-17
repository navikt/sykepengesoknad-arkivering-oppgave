package no.nav.helse.client.pdl

val HENT_IDENTER_QUERY =
    """
query(${"$"}ident: ID!){
  hentIdenter(ident: ${"$"}ident, historikk: false) {
    identer {
      ident,
      gruppe
    }
  }
}
"""

data class HentIdenterResponse(
    val data: HentIdenterResponseData,
    val errors: List<ResponseError>?
)

data class HentIdenterResponseData(
    val hentIdenter: HentIdenter? = null,
)

data class HentIdenter(
    val identer: List<PdlIdent>
)

data class PdlIdent(
    val gruppe: String,
    val ident: String
)
