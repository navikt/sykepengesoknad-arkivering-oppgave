package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.config.Toggle
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.consumer.syfosoknad.SøknadIkkeFunnetException
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

@Component
class BehandleVedTimeoutService(
    private val oppgavestyringDAO: OppgavestyringDAO,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val syfosoknadConsumer: SyfosoknadConsumer,
    private val toggle: Toggle,
    private val registry: MeterRegistry,
    private val identService: IdentService
) {
    private val log = logger()

    @Scheduled(fixedDelayString = "\${behandle.oppgave.intervall}", initialDelayString = "\${behandle.oppgave.deploy}")
    fun behandleTimeout() {
        val oppgaver = oppgavestyringDAO.hentOppgaverTilOpprettelse()
        log.info("Behandler ${oppgaver.size} oppgaver som har passert timeout")
        oppgaver.forEach {
            try {
                val innsending = saksbehandlingsService.finnEksisterendeInnsending(it.søknadsId)
                if (innsending != null) {
                    val soknadDTO = syfosoknadConsumer.hentSoknad(it.søknadsId)
                    val aktorId = identService.hentAktorIdForFnr(soknadDTO.fnr)
                    val soknad = soknadDTO.toSykepengesoknad(aktorId)
                    saksbehandlingsService.opprettOppgave(soknad, innsending)
                    oppgavestyringDAO.oppdaterOppgave(UUID.fromString(it.søknadsId), null, OppgaveStatus.Opprettet)
                } else {
                    log.info("Fant ikke eksisterende innsending, ignorerer søknad med id ${it.søknadsId}")
                    if (toggle.isQ() && it.opprettet < LocalDateTime.now().minusDays(1)) {
                        // Dette skjer hvis bømlo selv mocker opp søknader som ikke går gjennom syfosoknad
                        log.info("Sletter oppgave fra ${it.opprettet} som ikke har en tilhørende søknad")
                        oppgavestyringDAO.slettSpreOppgave(it.søknadsId)
                    }
                }
                if (it.status == OppgaveStatus.Utsett) {
                    val tidBrukt = Duration.between(it.opprettet, it.timeout ?: LocalDateTime.now())
                    log.info("Soknad ${it.søknadsId}  timet ut. Total ventetid: ${tidBrukt.toHours()} timer")
                    tellTimeout()
                }
            } catch (e: SøknadIkkeFunnetException) {
                if (toggle.isQ()) {
                    log.warn("Søknaden ${it.søknadsId} finnes ikke i Q, hopper over oppgaveopprettelse og fortsetter")
                    oppgavestyringDAO.oppdaterOppgave(UUID.fromString(it.søknadsId), null, OppgaveStatus.IkkeOpprett)
                } else {
                    log.error("SøknadIkkeFunnetException ved opprettelse av oppgave ${it.søknadsId}", e)
                    throw e
                }
            } catch (e: RuntimeException) {
                log.error("Runtime-feil ved opprettelse av oppgave ${it.søknadsId}", e)
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
