package no.nav.syfo.client.pdl

val HENT_GEOGRAFISK_TILKNYTNING_QUERY =
    """
query(${"$"}ident: ID!) {
    hentGeografiskTilknytning(ident: ${"$"}ident) {
        gtType
        gtKommune
        gtBydel
        gtLand
    }
    hentPerson(ident: ${"$"}ident) {
        adressebeskyttelse(historikk: false) {
            gradering
        }
    }
}
"""

data class HentGeografiskTilknytningResponse(
    val data: HentGeografiskTilknytningResponseData,
    val errors: List<ResponseError>?
)

data class HentGeografiskTilknytningResponseData(
    val hentGeografiskTilknytning: HentGeografiskTilknytning? = null,
    val hentPerson: HentPerson? = null
)

data class HentGeografiskTilknytning(
    val gtType: String,
    val gtLand: String?,
    val gtKommune: String?,
    val gtBydel: String?
)

data class HentPerson(
    val adressebeskyttelse: List<Adressebeskyttelse>?
)

data class Adressebeskyttelse(
    val gradering: Gradering
)

enum class Gradering {
    UGRADERT,
    FORTROLIG,
    STRENGT_FORTROLIG,
    STRENGT_FORTROLIG_UTLAND
}

fun HentGeografiskTilknytning.finnGT(): String? {
    if (gtType == "BYDEL" && !gtBydel.isNullOrEmpty()) {
        return gtBydel
    } else if (gtType == "KOMMUNE" && !gtKommune.isNullOrEmpty()) {
        return gtKommune
    } else if (gtType == "UTLAND" && !gtLand.isNullOrEmpty()) {
        return gtLand
    } else {
        gtKommune?.let { return it }
        gtBydel?.let { return it }
        gtLand?.let { return it }
        return null
    }
}

fun HentPerson.getDiskresjonskode(): String? {
    return when (adressebeskyttelse?.lastOrNull()?.gradering) {
        Gradering.STRENGT_FORTROLIG, Gradering.STRENGT_FORTROLIG_UTLAND -> "SPSF"
        Gradering.FORTROLIG -> "SPFO"
        else -> null
    }
}
