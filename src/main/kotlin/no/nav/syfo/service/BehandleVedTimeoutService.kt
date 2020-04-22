package no.nav.syfo.service

import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.log
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class BehandleVedTimeoutService(
    private val oppgavestyringDAO: OppgavestyringDAO,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val syfosoknadConsumer: SyfosoknadConsumer
) {
    private val log = log()

    @Scheduled(fixedDelayString = "\${behandle.oppgave.intervall}", initialDelayString = "\${behandle.oppgave.deploy}")
    fun behandleTimeout() {
        log.info("Behandler oppgaver som har passert timeout")
        val oppgaver = oppgavestyringDAO.hentOppgaverTilOpprettelse()
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
            } catch (error: RuntimeException) {
                log.error("Runtime-feil ved opprettelse av oppgave ${it.søknadsId}", error)
            }
        }

    }
}
