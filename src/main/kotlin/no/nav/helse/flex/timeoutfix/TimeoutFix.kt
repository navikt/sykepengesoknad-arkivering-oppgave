package no.nav.helse.flex.timeoutfix

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

@Component
class TimeoutFix(
    private val spreOppgaveRepository: SpreOppgaveRepository,
) {
    private val log = logger()

    @Scheduled(initialDelay = 30, fixedDelay = 500000, timeUnit = TimeUnit.SECONDS)
    fun startOppgaveBehandling() {
        behandleOppgaver()
    }

    fun behandleOppgaver(tid: Instant = Instant.now()) {
        log.info("Finner oppgaver som skal få ny timeout")
        val oppgaver = spreOppgaveRepository.finnOppgaverMedTimeout(OffsetDateTime.of(2024, 6, 6, 3, 0, 0, 0, ZoneOffset.UTC).toInstant())

        if (oppgaver.size == 571) {
            log.info("Fant 571 oppgaver som skal få ny timeout")
        } else {
            log.warn("Fant ${oppgaver.size} oppgaver. Forventet 571")
        }
    }
}
