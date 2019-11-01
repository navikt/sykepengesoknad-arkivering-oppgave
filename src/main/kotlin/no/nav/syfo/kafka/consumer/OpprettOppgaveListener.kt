package no.nav.syfo.kafka.consumer

import no.nav.syfo.domain.dto.OpprettGosysOppgaveDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class OpprettOppgaveListener() {
    val log = log()

    @KafkaListener(
        topics = ["privat-helse-sykepenger-opprettGosysOppgave"],
        id = "syfogsak-opprettOppgave",
        idIsGroup = false,
        containerFactory = "opprettOppgaveContainerFactory"
    )
    fun listen(cr: ConsumerRecord<String, OpprettGosysOppgaveDTO>, acknowledgment: Acknowledgment) {
        log.info("Mottatt forespørsel om å opprette oppgaver, gjør ingenting")
    }
}
