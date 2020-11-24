package no.nav.syfo.kafkaincident

import no.nav.syfo.kafka.felles.SykepengesoknadDTO
import no.nav.syfo.log
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
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
class KafkaIncidentListener() : ConsumerSeekAware {
    val log = log()

    var first = true
    var last = true
    val soknadsid = "48b61ab5-c1c3-4faa-aff3-d928e512436e"

    @KafkaListener(
            topics = ["syfo-soknad-v3"],
            id = "soknadKafkaIncidentFix",
            idIsGroup = false,
            groupId = "syfogsak-kafka-incident-v3-engangs",
            containerFactory = "kafkaListenerContainerFactory",
            properties = ["auto.offset.reset = latest"],
            autoStartup = "false"
    )
    fun listen(cr: ConsumerRecord<String, SykepengesoknadDTO>, acknowledgment: Acknowledgment) {
        val melding = cr.value()

        try {
            val timestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(cr.timestamp()), ZoneOffset.UTC)

            if (timestamp.isBefore(LocalDate.of(2020, 10, 23).atStartOfDay(ZoneOffset.UTC))) {
                if (first) {
                    log.info("kafkaIncidentFix: Sjekker første soknad med timestamp $timestamp")
                    first = false
                }
            }

            if(cr.key()==soknadsid){
                log.info("Kom over $soknadsid med status ${melding.status?.name} timestamp $timestamp")
            }

            if (timestamp.isAfter(LocalDate.of(2020, 11, 10).atStartOfDay(ZoneOffset.UTC))) {
                if (last) {
                    log.info("kafkaIncidentFix: Er forbi timestamp $timestamp")
                    last = false
                }
            }
            acknowledgment.acknowledge()
        } catch (e: Exception) {
            val feilmelding = "kafkaIncidentFix: Uventet feil ved behandling av søknad ${melding?.id} fra topic ${cr.topic()}"
            log.error(feilmelding, e)
        }
    }

    override fun registerSeekCallback(callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // register custom callback
        log.info("kafkaIncidentFix: registerSeekCallback ${this.javaClass.simpleName}")
    }

    override fun onPartitionsAssigned(assignments: Map<TopicPartition, Long>, callback: ConsumerSeekAware.ConsumerSeekCallback) {
        // Seek all the assigned partition to a certain offset
        log.info("kafkaIncidentFix: onPartitionsAssigned ${this.javaClass.simpleName}")

        val tjueandreOktober = LocalDate.of(2020, 10, 22).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        callback.seekToTimestamp(assignments.keys, tjueandreOktober)
        log.info("kafkaIncidentFix: ferdig med seek seekToTimestamp ${this.javaClass.simpleName}")

    }

    override fun onIdleContainer(p0: MutableMap<TopicPartition, Long>?, p1: ConsumerSeekAware.ConsumerSeekCallback?) {
        log.info("kafkaIncidentFix: onIdleContainer ${this.javaClass.simpleName}")
    }
}
