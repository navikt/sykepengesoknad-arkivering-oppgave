package no.nav.helse.flex.client.pdl

const val AKTORID = "AKTORID"
const val FOLKEREGISTERIDENT = "FOLKEREGISTERIDENT"

data class ResponseError(
    val message: String?,
    val locations: List<ErrorLocation>?,
    val path: List<String>?,
    val extensions: ErrorExtension?
)

data class ErrorLocation(
    val line: String?,
    val column: String?
)

data class ErrorExtension(
    val code: String?,
    val classification: String?
)
