package no.nav.syfo.service

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException

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
                    oppgavestyringDAO.settTimeout(it.søknadsId, null)
                    oppgavestyringDAO.settStatus(it.søknadsId, OppgaveStatus.Opprettet)
                } else {
                    log.info("Fant ikke eksisterende innsending, ignorerer søknad med id ${it.søknadsId}")
                }
            } catch(e: HttpClientErrorException) {
                if (toggle.isQ() && e.rawStatusCode == 404) {
                    log.warn("Søknaden ${it.søknadsId} er slettet fra Q, hopper over oppgaveopprettelse og fortsetter")
                }
                else {
                    log.error("Rest kall feiler", e)
                }
            } catch (error: RuntimeException) {
                log.error("Runtime-feil ved opprettelse av oppgave ${it.søknadsId}", error)
            }
        }

    }
}
