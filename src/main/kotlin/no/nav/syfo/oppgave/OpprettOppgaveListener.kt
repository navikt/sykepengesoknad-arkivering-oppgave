package no.nav.syfo.oppgave

import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import javax.inject.Inject

@Component
class OpprettOppgaveListener @Inject constructor() {
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
