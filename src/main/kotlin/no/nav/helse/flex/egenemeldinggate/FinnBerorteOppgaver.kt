package no.nav.helse.flex.egenemeldinggate

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.OppgaveClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import java.util.concurrent.TimeUnit

@Component
class FinnBerorteOppgaver(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val innsendingRepository: InnsendingRepository,
    private val finnBerorteOppgaverTx: FinnBerorteOppgaverTx,
    private val oppgaveClient: OppgaveClient,
) {
    private val log = logger()

    // Only no-args methods can be Scheduled.
    @Scheduled(initialDelay = 30, fixedDelay = 50000, timeUnit = TimeUnit.SECONDS)
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

                if (innsending.oppgaveId != null) {
                    val hentetOppgave = oppgaveClient.hentOppgave(innsending.oppgaveId)
                    if (hentetOppgave.status == "FEILREGISTRERT") {
                        finnBerorteOppgaverTx.behandleOppgaverTransactional(spreOppgaveDbRecord, innsending)
                    } else {
                        log.info(
                            "Oppgave ${innsending.oppgaveId} med soknad ${innsending.sykepengesoknadId} " +
                                "har en helt annen status ${hentetOppgave.status}. Vi oppdaterer derfor ikke lokale data." +
                                " Sjekk med SB senere",
                        )
                    }
                }

                log.info("Ferdig med nr $index av ${oppgaver.size} oppgaver. ID: ${spreOppgaveDbRecord.id}")
            }
        } else {
            log.error("Fant feil antall oppgaver, forventet 6639 men fant ${oppgaver.size}")
        }
    }
}
