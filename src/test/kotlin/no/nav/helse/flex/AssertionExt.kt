package no.nav.helse.flex

import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

fun Instant.trunacteNanoUTC(): Instant = this.truncatedTo(ChronoUnit.MILLIS)

@Suppress("ktlint:standard:function-naming")
infix fun Instant.`should be equal to ignoring nano and zone`(expected: Instant) =
    this.trunacteNanoUTC() shouldBe expected.trunacteNanoUTC()

@Suppress("ktlint:standard:function-naming")
infix fun OffsetDateTime.`should be equal to ignoring nano and zone`(expected: Instant?) =
    this.toInstant() `should be equal to ignoring nano and zone` expected!!
