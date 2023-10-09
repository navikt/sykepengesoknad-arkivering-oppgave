package no.nav.helse.flex.client.pdl

const val HENT_IDENTER_QUERY =
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

const val AKTORID = "AKTORID"
const val FOLKEREGISTERIDENT = "FOLKEREGISTERIDENT"

data class HentIdenterResponseData(
    val hentIdenter: HentIdenter? = null
)

data class HentIdenter(
    val identer: List<PdlIdent>
)

data class PdlIdent(
    val gruppe: String,
    val ident: String
)

fun List<PdlIdent>.aktorId(): String {
    return find { it.gruppe == AKTORID }?.ident
        ?: throw RuntimeException("Kunne ikke finne aktørid i pdl response")
}

fun List<PdlIdent>.fnr(): String {
    return find { it.gruppe == FOLKEREGISTERIDENT }?.ident
        ?: throw RuntimeException("Kunne ikke finne fnr i pdl response")
}
