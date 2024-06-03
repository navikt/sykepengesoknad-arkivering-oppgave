package no.nav.helse.flex.egenemeldinggate

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import java.util.concurrent.TimeUnit

@Component
class FinnBerorteOppgaver(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val innsendingRepository: InnsendingRepository,
) {
    private val log = logger()

    // Only no-args methods can be Scheduled.
    @Scheduled(initialDelay = 30, fixedDelay = 500, timeUnit = TimeUnit.SECONDS)
    fun startOppgaveBehandling() {
        behandleOppgaver()
    }

    fun behandleOppgaver() {
        val oppgaver =
            spreOppgaveRepository.finnBerorteOppgaver(
                modifisertEtter = OffsetDateTime.of(2024, 6, 2, 7, 55, 0, 0, ZoneOffset.UTC).toInstant(),
                opprettetEtter = OffsetDateTime.of(2024, 5, 31, 7, 0, 0, 0, ZoneOffset.UTC).toInstant(),
            )
        log.info("Fant ${oppgaver.size} berørte oppgave av egenmeldinggate")

        if (oppgaver.size == 6639) {
            oppgaver.forEachIndexed { index, spreOppgaveDbRecord ->
                val innsending =
                    innsendingRepository.findBySykepengesoknadId(spreOppgaveDbRecord.sykepengesoknadId)
                        ?: throw RuntimeException("Fant ikke innsending for sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}")

                log.info(
                    "Fjerner oppgave id ${innsending.oppgaveId} fra innsending ${innsending.id} " +
                        "med sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}",
                )
                innsendingRepository.save(innsending.copy(oppgaveId = null))

                log.info(
                    "Oppdaterer status til UTSETT for oppgavestyring ${spreOppgaveDbRecord.id} med " +
                        "sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}",
                )
                spreOppgaveRepository.save(
                    spreOppgaveDbRecord.copy(
                        status = OppgaveStatus.Utsett,
                        timeout =
                            OffsetDateTime.of(2024, 6, 6, 3, 0, 0, 0, ZoneOffset.UTC)
                                .toInstant(),
                        modifisert = Instant.now(),
                    ),
                )
                log.info("Ferdig med nr $index av ${oppgaver.size} oppgaver. ID: ${spreOppgaveDbRecord.id}")
            }
        } else {
            log.error("Fant feil antall oppgaver, forventet 6639 men fant ${oppgaver.size}")
        }
    }
}
