package no.nav.helse.flex.egenemeldinggate

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import java.util.concurrent.TimeUnit
import kotlin.time.measureTime

@Component
class FinnBerorteOppgaver(
    private val spreOppgaveRepository: SpreOppgaveRepository,
) {
    private val log = logger()

    // Only no-args methods can be Scheduled.
    @Scheduled(initialDelay = 30, fixedDelay = 500, timeUnit = TimeUnit.SECONDS)
    fun startOppgaveBehandling() {
        behandleOppgaver()
    }

    fun behandleOppgaver() {
        val measureTime =
            measureTime {
                val oppgaver =
                    spreOppgaveRepository.finnBerorteOppgaver(
                        modifisertEtter = OffsetDateTime.of(2024, 6, 2, 7, 55, 0, 0, ZoneOffset.UTC).toInstant(),
                        opprettetEtter = OffsetDateTime.of(2024, 5, 31, 7, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                    )
                log.info("Fant ${oppgaver.size} berørte oppgave av egenmeldinggate")
            }
        log.info("Hentet oppgaver tok $measureTime")
    }
}
