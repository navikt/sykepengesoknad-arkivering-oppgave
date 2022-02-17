package no.nav.helse.flex.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import no.nav.helse.flex.domain.Periode
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import java.io.IOException

object PeriodeMapper {
    private val log = logger()

    fun jsonTilPeriode(json: String?): Periode {
        val message = "Mapping av periode-json feiler"
        try {
            val periode = objectMapper.readValue(json, Periode::class.java)
            if (periode.fom.isAfter(periode.tom)) {
                log.error(message)
                throw IllegalArgumentException(message)
            }
            return periode
        } catch (exception: JsonParseException) {
            log.error(message)
            throw IllegalArgumentException(message, exception)
        } catch (exception: JsonMappingException) {
            log.error(message)
            throw IllegalArgumentException(message, exception)
        } catch (iOException: IOException) {
            log.error(message)
            throw RuntimeException(message, iOException)
        }
    }
}
