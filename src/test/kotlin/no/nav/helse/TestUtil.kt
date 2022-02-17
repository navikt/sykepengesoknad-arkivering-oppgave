package no.nav.helse

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.mockito.Mockito

/**
 * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 */
fun <T> any(): T = Mockito.any()

fun <T> skapConsumerRecord(key: String, value: T, headers: Headers = RecordHeaders()): ConsumerRecord<String, T> {
    @Suppress("DEPRECATION")
    return ConsumerRecord(
        "topic-v1",
        0,
        0,
        0,
        TimestampType.CREATE_TIME,
        0,
        0,
        0,
        key,
        value,
        headers
    )
}

val mockSykepengesoknadDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/arbeidstakersoknad.json"),
        SykepengesoknadDTO::class.java
    )

val mockReisetilskuddDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/reisetilskuddAlleSvar.json"),
        SykepengesoknadDTO::class.java
    )
