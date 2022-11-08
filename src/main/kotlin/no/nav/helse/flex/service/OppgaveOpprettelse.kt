package no.nav.helse.flex.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.client.SøknadIkkeFunnetException
import no.nav.helse.flex.config.EnvironmentToggles
import no.nav.helse.flex.kafka.mapper.toSykepengesoknad
import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveRepository
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime

@Component
class OppgaveOpprettelse(
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val sykepengesoknadBackendClient: SykepengesoknadBackendClient,
    private val environmentToggles: EnvironmentToggles,
    private val registry: MeterRegistry,
    private val identService: IdentService
) {
    private val log = logger()

    // @Scheduled(initialDelay = 120, fixedDelay = 60, timeUnit = TimeUnit.SECONDS)
    // Only no-args methods can be Scheduled.
    fun startOppgaveBehandling() {
        behandleOppgaver()
    }

    fun behandleOppgaver(tid: Instant = Instant.now()) {
        val oppgaver = spreOppgaveRepository.findOppgaverTilOpprettelse(tid)

        if (oppgaver.isNotEmpty()) {
            log.info("Behandler ${oppgaver.size} oppgaver som skal opprettes")
        }

        oppgaver.forEach {
            try {
                val innsending = saksbehandlingsService.finnEksisterendeInnsending(it.sykepengesoknadId)
                if (innsending != null) {
                    val soknadDTO = sykepengesoknadBackendClient.hentSoknad(it.sykepengesoknadId)
                    val aktorId = identService.hentAktorIdForFnr(soknadDTO.fnr)
                    val soknad = soknadDTO.toSykepengesoknad(aktorId)
                    saksbehandlingsService.opprettOppgave(
                        sykepengesoknad = soknad,
                        innsending = innsending,
                        speilRelatert = it.status == OppgaveStatus.OpprettSpeilRelatert
                    )
                    spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                        sykepengesoknadId = it.sykepengesoknadId,
                        timeout = null,
                        status = tilOpprettetStatus(it.status),
                        tid
                    )
                } else {
                    log.info("Fant ikke eksisterende innsending, ignorerer søknad med id ${it.sykepengesoknadId}")
                    if (environmentToggles.isQ() && it.opprettet < OffsetDateTime.now().minusDays(1).toInstant()) {
                        // Dette skjer hvis Bømlo selv mocker opp søknader som ikke går gjennom sykepengesoknad-backend
                        log.info("Sletter oppgave fra ${it.opprettet} siden den ikke har en tilhørende søknad")
                        spreOppgaveRepository.deleteOppgaveBySykepengesoknadId(it.sykepengesoknadId)
                    }
                }
                if (it.status == OppgaveStatus.Utsett) {
                    val tidBrukt = Duration.between(it.opprettet, it.timeout ?: LocalDateTime.now())
                    log.info("Soknad ${it.sykepengesoknadId} timet ut. Total ventetid: ${tidBrukt.toHours()} timer")
                    tellTimeout()
                }
            } catch (e: SøknadIkkeFunnetException) {
                if (environmentToggles.isQ()) {
                    log.warn("Søknaden ${it.sykepengesoknadId} finnes ikke i Q, hopper over oppgaveopprettelse og fortsetter")
                    spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                        sykepengesoknadId = it.sykepengesoknadId,
                        timeout = null,
                        status = OppgaveStatus.IkkeOpprett,
                        tid
                    )
                } else {
                    log.error("SøknadIkkeFunnetException ved opprettelse av oppgave ${it.sykepengesoknadId}", e)
                    throw e
                }
            } catch (e: RuntimeException) {
                log.error("Runtime-feil ved opprettelse av oppgave ${it.sykepengesoknadId}", e)
            }
        }
    }

    private fun tilOpprettetStatus(oppgaveStatus: OppgaveStatus): OppgaveStatus {
        return when (oppgaveStatus) {
            OppgaveStatus.Opprett -> OppgaveStatus.Opprettet
            OppgaveStatus.OpprettSpeilRelatert -> OppgaveStatus.OpprettetSpeilRelatert
            // OppgaveStatus.Utsett  + timeout < now()
            else -> OppgaveStatus.OpprettetTimeout
        }
    }

    private fun tellTimeout() {
        registry.counter(
            "bomlo.timeout",
            Tags.of("type", "info")
        ).increment()
    }
}
