package no.nav.syfo.service

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.OppgavestyringLogDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.mapper.toSykepengesoknad
import no.nav.syfo.log
import org.springframework.stereotype.Component
import no.nav.syfo.util.DatoUtil.tidTil
import org.springframework.beans.factory.annotation.Value
import java.time.LocalDateTime

@Component
class SpreOppgaverService(@Value("\${default.timeout.timer}") private val defaultTimeoutTimer: String,
                        private val syfosoknadConsumer: SyfosoknadConsumer,
                        private val toggle: ToggleImpl,
                        private val saksbehandlingsService: SaksbehandlingsService,
                        private val oppgavestyringLogDAO: OppgavestyringLogDAO,
                        private val oppgavestyringDAO: OppgavestyringDAO) {
    private val log = log()
    private val timeout = defaultTimeoutTimer.toLong()

    fun prosesserOppgave(oppgave: OppgaveDTO) {
        if(oppgave.dokumentType == DokumentTypeDTO.Søknad) {
            val id = oppgavestyringLogDAO.loggEvent(oppgave)
            log.info("Gjelder ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId}, databaseindeks $id")
            when(oppgave.oppdateringstype) {
                OppdateringstypeDTO.Utsett -> utsettOppgave(oppgave.dokumentId.toString(), oppgave.timeout!!)
                OppdateringstypeDTO.Opprett -> opprettOppgave(id = oppgave.dokumentId.toString())
                OppdateringstypeDTO.Ferdigbehandlet -> viBehandlerIkkeOppgaven(oppgave.dokumentId.toString())
            }
        }
    }

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if(sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                if(sykepengesoknad.soknadstype == ARBEIDSTAKERE && toggle.isNotProduction()) {
                    utsettOppgave(sykepengesoknad.id, sykepengesoknad.sendtNav?.plusHours(timeout) ?: LocalDateTime.now().plusHours(timeout))
                    saksbehandlingsService.behandleSoknad(sykepengesoknad)
                }
                else  {
                    saksbehandlingsService.behandleSoknad(sykepengesoknad)
                    opprettOppgave(sok = sykepengesoknad)
                }
            }
        } catch (e: Exception) {
            saksbehandlingsService.innsendingFeilet(sykepengesoknad, e)
        }
    }

    fun utsettOppgave(id: String, behandles: LocalDateTime) {
        if(toggle.isNotProduction()) {
            log.info("TEST: oppgave opprettelse utsettes med ${behandles.tidTil()} for søknad $id")
            //TODO: Sjekk om "behandles" er senere enn eksistrende utsettelse, og oppdater denne
        }
    }

    fun opprettOppgave(sok: Sykepengesoknad? = null, id: String? = null) {
        val sykepengesoknad = sok ?: syfosoknadConsumer.hentSoknad(id!!).toSykepengesoknad()
        if(id != null) {
            if (skalBehandlesAvNav(sykepengesoknad) && toggle.isNotProduction()) {
                saksbehandlingsService.opprettOppgave(sykepengesoknad)
                //TODO: Gjør alltid else, da aapen-helse-spre-oppgaver kan bestemme om oppgaver skal opprettes
            }
        }
        else {
            if (skalBehandlesAvNav(sykepengesoknad)) {
                saksbehandlingsService.opprettOppgave(sykepengesoknad)
            }
        }
    }

    fun viBehandlerIkkeOppgaven(id: String) {
        if(toggle.isNotProduction()) {
            log.info("TEST: syfogsak skal ikke opprette oppgaven")
            oppgavestyringDAO.hentSøknad(id)?.let {
                oppgavestyringDAO.fjernSøknad(it.søknadsId)
            }
        }
    }

    private fun skalBehandlesAvNav(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtNav != null

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) = sykepengesoknad.sendtArbeidsgiver != null
        && sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}
