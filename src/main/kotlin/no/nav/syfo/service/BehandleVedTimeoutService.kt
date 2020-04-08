package no.nav.syfo.service

import no.nav.syfo.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BehandleVedTimeoutService {
    private val log = log()

    @Scheduled(fixedDelayString = "\${behandle.oppgave.intervall}", initialDelayString = "\${behandle.oppgave.deploy}")
    fun behandleTimeout() {
        log.info("Behandler oppgaver som har passert timeout")

    }
}
