package no.nav.helse.flex

import org.amshove.kluent.`should be equal to`
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

fun Instant.trunacteNanoUTC(): Instant = this.truncatedTo(ChronoUnit.MILLIS)

@Suppress("ktlint:standard:function-naming")
infix fun Instant.`should be equal to ignoring nano and zone`(expected: Instant) =
    this.trunacteNanoUTC() `should be equal to` expected.trunacteNanoUTC()

@Suppress("ktlint:standard:function-naming")
infix fun OffsetDateTime.`should be equal to ignoring nano and zone`(expected: Instant?) =
    this.toInstant() `should be equal to ignoring nano and zone` expected!!
