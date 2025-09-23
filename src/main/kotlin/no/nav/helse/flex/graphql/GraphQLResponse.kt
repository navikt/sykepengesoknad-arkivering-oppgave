package no.nav.helse.flex.graphql

data class GraphQLResponse<T>(
    val data: T,
    val errors: List<ResponseError>?,
) {
    fun hentErrors(): String? {
        return errors?.toString()
    }
}

data class ResponseError(
    val message: String?,
    val locations: List<ErrorLocation>?,
    val path: List<String>?,
    val extensions: ErrorExtension?,
)

data class ErrorLocation(
    val line: String?,
    val column: String?,
)

data class ErrorExtension(
    val code: String?,
    val classification: String?,
)
