package no.nav.helse.flex

import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.mockito.Mockito

/**
 * Returns Mockito.any() as nullable type to avoid java.lang.IllegalStateException when
 * null is returned.
 */
fun <T> any(): T = Mockito.any()

fun <T> skapConsumerRecord(key: String, value: T): ConsumerRecord<String, T> {
    return ConsumerRecord(
        "topic-v1",
        1,
        1L,
        key,
        value
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

val mockBehandlingsdagerdDTO: SykepengesoknadDTO =
    objectMapper.readValue(
        Application::class.java.getResource("/soknadBehandlingsdagerTodagerDTO.json"),
        SykepengesoknadDTO::class.java
    )
