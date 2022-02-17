package no.nav.helse.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DatoUtil {
    var norskDato: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
}

fun LocalDateTime.tilOsloZone(): OffsetDateTime = this.atZone(ZoneId.of("Europe/Oslo")).toOffsetDateTime()
