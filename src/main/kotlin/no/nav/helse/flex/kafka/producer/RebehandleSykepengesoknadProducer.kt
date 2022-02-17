package no.nav.helse.flex.kafka.producer

import no.nav.helse.flex.BEHANDLINGSTIDSPUNKT
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.kafka.AivenKafkaConfig
import no.nav.helse.flex.kafka.consumer.RETRY_TOPIC
import no.nav.helse.flex.logger
import no.nav.helse.flex.serialisertTilString
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.TopicAuthorizationException
import org.apache.kafka.common.header.Header
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.OffsetDateTime

@Component
class RebehandleSykepengesoknadProducer(
    @Value("\${rebehandling.delay.sekunder}")
    private val delaySekunder: Long,
    private val aivenKafkaConfig: AivenKafkaConfig
) {
    val log = logger()
    var producer = aivenKafkaConfig.skapProducer()

    fun send(
        sykepengesoknad: Sykepengesoknad,
        retries: Int = 20,
        sleepMillis: Long = 500
    ) {
        try {
            producer.send(
                ProducerRecord(
                    RETRY_TOPIC,
                    null,
                    sykepengesoknad.id,
                    sykepengesoknad.serialisertTilString(),
                    ArrayList<Header>().also {
                        it.add(
                            RecordHeader(
                                BEHANDLINGSTIDSPUNKT,
                                OffsetDateTime.now().plusSeconds(delaySekunder).toInstant().toEpochMilli().toString().toByteArray()
                            )
                        )
                    }
                )
            ).get()
        } catch (e: Exception) {
            if (e.isTopicAuthException()) {
                if (retries >= 0) {
                    log.warn("Topic auth exception, $retries retries igjen, sover i $sleepMillis ms: ", e)
                    Thread.sleep(sleepMillis)
                    producer = aivenKafkaConfig.skapProducer()
                    return send(sykepengesoknad, retries - 1, sleepMillis * 2)
                } else {
                    log.error("Topic auth exception, ingen retries igjen :(", e)
                    throw e
                }
            } else {
                log.error("Det feiler når søknad ${sykepengesoknad.id} skal legges på rebehandle topic", e)
                throw e
            }
        }
    }
}

fun Throwable.isTopicAuthException(): Boolean {
    return this.getRootCause() is TopicAuthorizationException
}

fun Throwable.getRootCause(): Throwable {
    this.cause?.let {
        return it.getRootCause()
    }
    return this
}
