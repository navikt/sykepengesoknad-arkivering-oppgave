package no.nav.syfo.kafkaincident

import no.nav.syfo.leaderelection.LeaderElection
import no.nav.syfo.log
import org.springframework.context.annotation.Profile
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("remote")
class KafkaListenerStarter(
        val kafkaListenerEndpointRegistry: KafkaListenerEndpointRegistry,
        val leaderElection: LeaderElection
) {
    val log = log()
    var hasStartedListeners = false

    @Scheduled(fixedDelay = 5000, initialDelay = 180000)
    fun run() {
        if (!hasStartedListeners && leaderElection.isLeader()) {

            log.info("kafkaIncidentFix: Starter listeners")

            kafkaListenerEndpointRegistry.getListenerContainer("soknadKafkaIncidentFix").start()

            log.info("kafkaIncidentFix: Ferdig med å starte listener")

            hasStartedListeners = true
        }
    }
}
