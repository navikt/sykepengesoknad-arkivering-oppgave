package no.nav.syfo.kafka

import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.log
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TestProducer(private val kafkaTemplate: KafkaTemplate<String, SoknadDTO>) {

    fun soknadSendt(sykepengesoknad: SoknadDTO) {
        kafkaTemplate.send(
                SyfoProducerRecord("privat-syfo-soknadSendt-v1", sykepengesoknad.id, sykepengesoknad))
        log().info("Soknad med id: {}, er sendt", sykepengesoknad.id)
    }
}
