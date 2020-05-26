package no.nav.syfo.service

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.consumer.syfosoknad.SøknadIkkeFunnetException
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class BehandleVedTimeoutService(
    private val oppgavestyringDAO: OppgavestyringDAO,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val syfosoknadConsumer: SyfosoknadConsumer,
    private val toggle: ToggleImpl
) {
    private val log = log()

    @Scheduled(fixedDelayString = "\${behandle.oppgave.intervall}", initialDelayString = "\${behandle.oppgave.deploy}")
    fun behandleTimeout() {
        val oppgaver = oppgavestyringDAO.hentOppgaverTilOpprettelse()
        log.info("Behandler ${oppgaver.size} oppgaver som har passert timeout")
        oppgaver.forEach {
            try {
                val innsending = saksbehandlingsService.finnEksisterendeInnsending(it.søknadsId)
                if (innsending != null) {
                    val søknad = syfosoknadConsumer.hentSoknad(it.søknadsId).toSykepengesoknad()
                    saksbehandlingsService.opprettOppgave(søknad, innsending)
                    oppgavestyringDAO.oppdaterOppgave(UUID.fromString(it.søknadsId), null, OppgaveStatus.Opprettet)
                } else {
                    log.info("Fant ikke eksisterende innsending, ignorerer søknad med id ${it.søknadsId}")
                    if (toggle.isQ() && it.opprettet < LocalDateTime.now().minusDays(1)) {
                        // Dette skjer hvis bømlo selv mocker opp søknader som ikke går gjennom syfosoknad
                        log.info("Sletter oppgave fra ${it.opprettet} som ikke har en tilhørende søknad")
                        oppgavestyringDAO.slettSpreOppgave(it.søknadsId)
                    }
                }
            } catch(e: SøknadIkkeFunnetException) {
                if (toggle.isQ()) {
                    log.warn("Søknaden ${it.søknadsId} finnes ikke i Q, hopper over oppgaveopprettelse og fortsetter")
                    oppgavestyringDAO.oppdaterOppgave(UUID.fromString(it.søknadsId), null, OppgaveStatus.IkkeOpprett)
                }
            } catch (error: RuntimeException) {
                log.error("Runtime-feil ved opprettelse av oppgave ${it.søknadsId}", error)
            }
        }
    }

    @Scheduled(cron = "0 6 * * * *")
    fun slettGamleOppgaver() {
        val antall = oppgavestyringDAO.slettGamleSpreOppgaver()
        log.info("Slettet $antall innslag på utgåtte oppgaver")
    }
}