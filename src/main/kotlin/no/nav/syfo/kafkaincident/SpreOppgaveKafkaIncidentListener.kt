package no.nav.syfo.kafkaincident

import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.kafka.getSafeNavCallIdHeaderAsString
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.MDC
import org.springframework.context.annotation.Profile
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.listener.ConsumerSeekAware
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime

@Component
@Profile("remote")
class SpreOppgaveKafkaIncidentListener(
        private val spreOppgaveSjekkService: SpreOppgaveSjekkService
) : ConsumerSeekAware {
    val log = log()

    var first = true

    @KafkaListener(
            topics = ["aapen-helse-spre-oppgaver"],
            id = "spreOppgaveKafkaIncidentFix",
            idIsGroup = false,
            groupId = "syfogsak-kafka-incident-fix",
            containerFactory = "spreOppgaverContainerFactory",
            properties = ["auto.offset.reset = latest"],
            autoStartup = "false"
    )
    fun listen(cr: ConsumerRecord<String, OppgaveDTO>, acknowledgment: Acknowledgment) {
        val melding = cr.value()

        try {
            MDC.put(NAV_CALLID, getSafeNavCallIdHeaderAsString(cr.headers()))
            val zoned = ZonedDateTime.ofInstant(Instant.ofEpochMilli(cr.timestamp()), ZoneOffset.UTC)

            if (zoned.isBefore(LocalDate.of(2020, 11, 7).atStartOfDay(ZoneOffset.UTC))) {
                if (first) {
                    log.info("kafkaIncidentFix: Sjekker første syfosoknad melding med timestamp $zoned")
                    first = false
                }
                spreOppgaveSjekkService.sjekk(melding)
            }

            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val feilmelding = "kafkaIncidentFix: Uventet feil ved behandling av spre oppgave ${melding?.dokumentId} fra topic ${cr.topic()}"
            log.error(feilmelding, e)
        } finally {
            MDC.remove(NAV_CALLID)
        }
    }

    override fun registerSeekCallback(callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // register custom callback
        log.info("kafkaIncidentFix: registerSeekCallback ${this.javaClass.simpleName}")
    }

    override fun onPartitionsAssigned(assignments: Map<TopicPartition, Long>, callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // Seek all the assigned partition to a certain offset
        log.info("kafkaIncidentFix: onPartitionsAssigned ${this.javaClass.simpleName}")

        val mandagAndreNovember = LocalDate.of(2020, 11, 2).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        callback.seekToTimestamp(assignments.keys, mandagAndreNovember)
        log.info("kafkaIncidentFix: ferdig med seek seekToTimestamp ${this.javaClass.simpleName}")

    }

    override fun onIdleContainer(p0: MutableMap<TopicPartition, Long>?, p1: ConsumerSeekAware.ConsumerSeekCallback?) {
        log.info("kafkaIncidentFix: onIdleContainer ${this.javaClass.simpleName}")
    }
}
