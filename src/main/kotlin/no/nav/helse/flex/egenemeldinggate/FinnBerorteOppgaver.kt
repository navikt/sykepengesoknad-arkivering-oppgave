package no.nav.helse.flex.egenemeldinggate

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.InnsendingRepository
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.OppdaterOppgaveReqeust
import no.nav.helse.flex.service.OppgaveClient
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.*
import java.util.concurrent.TimeUnit

@Component
class FinnBerorteOppgaver(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val innsendingRepository: InnsendingRepository,
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
        var antallFeilregisrert = 0
        var antallAndreStatuser = 0
        var antallOpprettetOgPatchet = 0
        var antallFerdigstilt = 0
        val statuser = mutableMapOf<String, Int>()
        if (oppgaver.size == 6639) {
            oppgaver.forEachIndexed { index, spreOppgaveDbRecord ->
                val innsending =
                    innsendingRepository.findBySykepengesoknadId(spreOppgaveDbRecord.sykepengesoknadId)
                        ?: throw RuntimeException("Fant ikke innsending for sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}")

                if (innsending.oppgaveId != null) {
                    val hentetOppgave = oppgaveClient.hentOppgave(innsending.oppgaveId)
                    if (hentetOppgave.status == "FEILREGISTRERT") {
                        antallFeilregisrert++
                        log.info("Oppgave ${innsending.oppgaveId} er FEILREGISTRERT")
                    } else if (hentetOppgave.status == "FERDIGSTILT") {
                        statuser[hentetOppgave.status] = statuser.getOrDefault(hentetOppgave.status, 0) + 1
                        antallFerdigstilt++
                        log.info(
                            "Oppgave ${innsending.oppgaveId} med søknadid ${innsending.sykepengesoknadId} er " +
                                "ferdigstilt allerede. Sjekk denne manuelt senere",
                        )
                    } else if (hentetOppgave.status == "OPPRETTET") {
                        statuser[hentetOppgave.status] = statuser.getOrDefault(hentetOppgave.status, 0) + 1
                        antallOpprettetOgPatchet++

                        oppgaveClient.oppdaterOppgave(
                            innsending.oppgaveId,
                            OppdaterOppgaveReqeust(
                                status = "FEILREGISTRERT",
                            ),
                        )

                        log.info("Oppgave ${innsending.oppgaveId} var opprettet, vi endret til FEILREGISTRERT")
                    } else {
                        antallAndreStatuser++
                        statuser[hentetOppgave.status] = statuser.getOrDefault(hentetOppgave.status, 0) + 1
                        log.info("Oppgave ${innsending.oppgaveId} har en helt annen status ${hentetOppgave.status}")
                    }
                    /*
                    log.info(
                        "Fjerner oppgave id ${innsending.oppgaveId} fra innsending ${innsending.id} " +
                            "med sykepengesoknadId ${spreOppgaveDbRecord.sykepengesoknadId}",
                    )
                    innsendingRepository.save(innsending.copy(oppgaveId = null))
                     */
                }

                /*
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

                 */
                log.info("Ferdig med nr $index av ${oppgaver.size} oppgaver. ID: ${spreOppgaveDbRecord.id}")
            }
        } else {
            log.error("Fant feil antall oppgaver, forventet 6639 men fant ${oppgaver.size}")
        }
        log.info("Antall feilregistrerte: $antallFeilregisrert")
        log.info("Antall andre statuser: $antallAndreStatuser")
        log.info("Statuser: $statuser")
    }
}
