package no.nav.syfo

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.apache.kafka.common.record.TimestampType
import org.mockito.Mockito

/**
 * Returns Mockito.eq() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 *
 * Generic T is nullable because implicitly bounded by Any?.
 */
fun <T> eq(obj: T): T = Mockito.eq<T>(obj)

/**
 * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 */
fun <T> any(): T = Mockito.any<T>()

fun <T> skapConsumerRecord(key: String, value: T, headers: Headers = RecordHeaders()): ConsumerRecord<String, T> {
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
