package no.nav.syfo.provider

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.time.LocalDateTime

@ControllerAdvice
class RestExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<Any> {
        val apiError = ApiError(
            status = HttpStatus.BAD_REQUEST, message = ex.message
        )
        return buildResponseEntity(apiError)
    }

    @ExceptionHandler(HttpClientErrorException::class)
    fun handleHttpClientErrorException(ex: HttpClientErrorException): ResponseEntity<Any> {
        val apiError = ApiError(
            status = ex.statusCode, message = ex.message
        )
        return buildResponseEntity(apiError)
    }

    private fun buildResponseEntity(apiError: ApiError): ResponseEntity<Any> {
        return ResponseEntity(apiError, apiError.status)
    }
}

data class ApiError(
    var status: HttpStatus,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    var message: String? = null
)
