package no.nav.syfo.service

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SpreOppgaverService(@Value("\${default.timeout.timer}") private val defaultTimeoutTimer: String,
                          private val syfosoknadConsumer: SyfosoknadConsumer,
                          private val toggle: ToggleImpl,
                          private val saksbehandlingsService: SaksbehandlingsService,
                          private val oppgavestyringDAO: OppgavestyringDAO) {
    private val log = log()
    private val timeout = defaultTimeoutTimer.toLong()

    fun prosesserOppgave(oppgave: OppgaveDTO) {
        if (oppgave.dokumentType == DokumentTypeDTO.Søknad) {
            log.info("Gjelder ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId}")
            when (oppgave.oppdateringstype) {
                OppdateringstypeDTO.Utsett -> utsettOppgave(oppgave.dokumentId.toString(), oppgave.timeout!!)
                OppdateringstypeDTO.Opprett -> opprettOppgave(søknadsId = oppgave.dokumentId.toString())
                OppdateringstypeDTO.Ferdigbehandlet -> viBehandlerIkkeOppgaven(oppgave.dokumentId.toString())
            }
        }
    }

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                saksbehandlingsService.behandleSoknad(sykepengesoknad)
                if (sykepengesoknad.soknadstype == ARBEIDSTAKERE && toggle.isNotProduction()) {
                    utsettOppgave(sykepengesoknad.id, sykepengesoknad.sendtNav?.plusHours(timeout) ?: LocalDateTime.now().plusHours(timeout))
                } else {
                    if (skalBehandlesAvNav(sykepengesoknad)) {
                        saksbehandlingsService.opprettOppgave(sykepengesoknad)
                    }
                }
            }
        } catch (e: Exception) {
            saksbehandlingsService.innsendingFeilet(sykepengesoknad, e)
        }
    }

    fun utsettOppgave(id: String, nyTimeout: LocalDateTime) {
        if (toggle.isNotProduction()) {
            val oppgavestyring = oppgavestyringDAO.hentSpreOppgave(id)

            if (oppgavestyring != null) {
                if (nyTimeout.isAfter(oppgavestyring.timeout)) {
                    oppgavestyringDAO.settTimeout(id, nyTimeout)
                }
            } else {
                oppgavestyringDAO.nySpreOppgave(id, nyTimeout, OppgaveStatus.Utsett)
            }
        }
    }

    fun opprettOppgave(søknadsId: String) {
        if (toggle.isNotProduction()) {
            if (oppgavestyringDAO.hentSpreOppgave(søknadsId) != null) {
                oppgavestyringDAO.settTimeout(søknadsId, null)
                oppgavestyringDAO.settStatus(søknadsId, OppgaveStatus.Opprett)
            } else {
                oppgavestyringDAO.nySpreOppgave(søknadsId, null, OppgaveStatus.Opprett)
            }
        }
    }

    fun viBehandlerIkkeOppgaven(id: String) {
        if (toggle.isNotProduction()) {
            val oppgavestyring = oppgavestyringDAO.hentSpreOppgave(id)
            if (oppgavestyring != null) {
                oppgavestyringDAO.settTimeout(id, null)
                oppgavestyringDAO.settStatus(id, OppgaveStatus.IkkeOpprett)
            } else {
                oppgavestyringDAO.nySpreOppgave(id, null, OppgaveStatus.IkkeOpprett)
            }
        }
    }

    private fun skalBehandlesAvNav(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtNav != null

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) = sykepengesoknad.sendtArbeidsgiver != null
        && sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}
