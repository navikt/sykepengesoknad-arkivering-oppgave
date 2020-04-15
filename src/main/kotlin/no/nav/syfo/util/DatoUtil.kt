package no.nav.syfo.util

import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object DatoUtil {
    var norskDato: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    fun LocalDateTime.dagerTil() = Duration.between(LocalDateTime.now(), this).toDays()
    fun LocalDateTime.tidTil(): String {
        val dager = Duration.between(LocalDateTime.now(), this).toDays()
        val timer = Duration.between(LocalDateTime.now(), this).toHours() % 24
        return "$dager dager og $timer timer"
    }
}
