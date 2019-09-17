package no.nav.syfo.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.domain.Periode
import no.nav.syfo.log
import java.io.IOException

object PeriodeMapper {

    private val objectMapper = ObjectMapper().registerModules(JavaTimeModule(), KotlinModule())

    fun jsonTilPeriode(json: String?): Periode {
        val message = "Mapping av periode-json feiler"
        try {
            val periode = objectMapper.readValue(json, Periode::class.java)
            if (periode.fom.isAfter(periode.tom)) {
                log().error(message)
                throw IllegalArgumentException(message)
            }
            return periode
        } catch (exception: JsonParseException) {
            log().error(message)
            throw IllegalArgumentException(message, exception)
        } catch (exception: JsonMappingException) {
            log().error(message)
            throw IllegalArgumentException(message, exception)
        } catch (iOException: IOException) {
            log().error(message)
            throw RuntimeException(message, iOException)
        }
    }
}