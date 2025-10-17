package no.nav.helse.flex.util

import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DatoUtil {
    var norskDato: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    var aarMaaned: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.of("nb", "NO"))
}

fun LocalDateTime.tilOsloZone(): OffsetDateTime = this.atZone(ZoneId.of("Europe/Oslo")).toOffsetDateTime()
