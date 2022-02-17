package no.nav.helse

import org.amshove.kluent.`should be equal to`
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

fun Instant.trunacteNanoUTC(): Instant =
    this.truncatedTo(ChronoUnit.MILLIS)

infix fun Instant.`should be equal to ignoring nano and zone`(expected: Instant) =
    this.trunacteNanoUTC() `should be equal to` expected.trunacteNanoUTC()

infix fun OffsetDateTime.`should be equal to ignoring nano and zone`(expected: Instant?) =
    this.toInstant() `should be equal to ignoring nano and zone` expected!!
