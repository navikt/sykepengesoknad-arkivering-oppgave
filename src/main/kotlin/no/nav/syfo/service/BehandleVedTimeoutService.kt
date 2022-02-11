package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.client.SyfosoknadClient
import no.nav.syfo.client.SøknadIkkeFunnetException
import no.nav.syfo.config.Toggle
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.repository.OppgaveRepository
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.OppgavestyringDAO
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime

@Profile("test")
@Component
class BehandleVedTimeoutService(
    private val oppgavestyringDAO: OppgavestyringDAO,
    private val oppgaveRepository: OppgaveRepository,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val syfosoknadClient: SyfosoknadClient,
    private val toggle: Toggle,
    private val registry: MeterRegistry,
    private val identService: IdentService
) {
    private val log = logger()

    @Scheduled(fixedDelay = 1000L * 60 * 1, initialDelay = 1000L * 60 * 10)
    fun behandleTimeout() {
        val oppgaver = oppgaveRepository.findOppgaverTilOpprettelse()

        if (oppgaver.isNotEmpty()) {
            log.info("Behandler ${oppgaver.size} oppgaver som skal opprettes")
        }

        oppgaver.forEach {
            try {
                val innsending = saksbehandlingsService.finnEksisterendeInnsending(it.sykepengesoknadId)
                if (innsending != null) {
                    val soknadDTO = syfosoknadClient.hentSoknad(it.sykepengesoknadId)
                    val aktorId = identService.hentAktorIdForFnr(soknadDTO.fnr)
                    val soknad = soknadDTO.toSykepengesoknad(aktorId)
                    saksbehandlingsService.opprettOppgave(
                        sykepengesoknad = soknad,
                        innsending = innsending,
                        speilRelatert = it.status == OppgaveStatus.OpprettSpeilRelatert
                    )
                    oppgaveRepository.updateOppgaveBySykepengesoknadId(
                        sykepengesoknadId = it.sykepengesoknadId,
                        timeout = null,
                        status = OppgaveStatus.Opprettet
                    )
                } else {
                    log.info("Fant ikke eksisterende innsending, ignorerer søknad med id ${it.sykepengesoknadId}")
                    if (toggle.isQ() && it.opprettet < LocalDateTime.now().minusDays(1)) {
                        // Dette skjer hvis bømlo selv mocker opp søknader som ikke går gjennom syfosoknad
                        log.info("Sletter oppgave fra ${it.opprettet} som ikke har en tilhørende søknad")
                        oppgavestyringDAO.slettSpreOppgave(it.sykepengesoknadId)
                    }
                }
                if (it.status == OppgaveStatus.Utsett) {
                    val tidBrukt = Duration.between(it.opprettet, it.timeout ?: LocalDateTime.now())
                    log.info("Soknad ${it.sykepengesoknadId}  timet ut. Total ventetid: ${tidBrukt.toHours()} timer")
                    tellTimeout()
                }
            } catch (e: SøknadIkkeFunnetException) {
                if (toggle.isQ()) {
                    log.warn("Søknaden ${it.sykepengesoknadId} finnes ikke i Q, hopper over oppgaveopprettelse og fortsetter")
                    oppgaveRepository.updateOppgaveBySykepengesoknadId(
                        sykepengesoknadId = it.sykepengesoknadId,
                        timeout = null,
                        status = OppgaveStatus.IkkeOpprett
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

    private fun tellTimeout() {
        registry.counter(
            "syfogsak.bomlo.timeout",
            Tags.of("type", "info")
        ).increment()
    }

    @Scheduled(cron = "0 6 * * * *")
    fun slettGamleOppgaver() {
        val antall = oppgavestyringDAO.slettGamleSpreOppgaver()
        log.info("Slettet $antall innslag på utgåtte oppgaver")
    }
}
