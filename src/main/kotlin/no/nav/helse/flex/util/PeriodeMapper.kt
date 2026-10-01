package no.nav.helse.flex.util

import no.nav.helse.flex.domain.Periode
import no.nav.helse.flex.logger
import no.nav.helse.flex.objectMapper
import tools.jackson.core.JacksonException

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
        } catch (exception: JacksonException) {
            log.error(message)
            throw IllegalArgumentException(message, exception)
        }
    }
}
