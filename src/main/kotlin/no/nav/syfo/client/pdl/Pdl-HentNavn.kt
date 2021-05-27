package no.nav.syfo.client.pdl

import org.apache.commons.text.WordUtils

val HENT_NAVN_QUERY =
    """
query(${"$"}ident: ID!){
  hentPerson(ident: ${"$"}ident) {
  	navn(historikk: false) {
  	  fornavn
  	  mellomnavn
  	  etternavn
    }
  }
}
"""

data class HentNavnResponse(
    val data: HentNavnResponseData,
    val errors: List<ResponseError>?
)

data class HentNavnResponseData(
    val hentPerson: HentNavn? = null,
)

data class HentNavn(
    val navn: List<Navn>? = null,
)

data class Navn(
    val fornavn: String,
    val mellomnavn: String?,
    val etternavn: String
)

fun Navn.format(): String {
    val navn = if (mellomnavn != null) {
        "$fornavn $mellomnavn $etternavn"
    } else {
        "$fornavn $etternavn"
    }

    return WordUtils.capitalizeFully(navn, ' ', '-')
}
