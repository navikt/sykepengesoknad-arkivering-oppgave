package no.nav.syfo.service

import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgaveStatus
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.repository.SpreOppgave
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class SpreOppgaverService(@Value("\${default.timeout.timer}") private val defaultTimeoutTimer: String,
                          private val toggle: ToggleImpl,
                          private val saksbehandlingsService: SaksbehandlingsService,
                          private val oppgavestyringDAO: OppgavestyringDAO) {
    private val log = log()
    private val timeout = defaultTimeoutTimer.toLong()

    // Er Synchronized pga. race condition mellom saksbehandling i vårt system og saksbehandling i Bømlo's system
    @Synchronized
    fun prosesserOppgave(oppgave: OppgaveDTO) {
        if (oppgave.dokumentType == DokumentTypeDTO.Søknad) {
            val oppgavestyring = oppgavestyringDAO.hentSpreOppgave(oppgave.dokumentId.toString())

            when (oppgavestyring?.status to oppgave.oppdateringstype) {
                null to OppdateringstypeDTO.Utsett,
                OppgaveStatus.Utsett to OppdateringstypeDTO.Utsett -> utsettOppgave(oppgave.dokumentId.toString(), oppgave.timeout!!, oppgavestyring)
                null to OppdateringstypeDTO.Opprett,
                OppgaveStatus.Utsett to OppdateringstypeDTO.Opprett -> opprettOppgave(oppgave.dokumentId.toString(), oppgavestyring)
                null to OppdateringstypeDTO.Ferdigbehandlet,
                OppgaveStatus.Utsett to OppdateringstypeDTO.Ferdigbehandlet -> viBehandlerIkkeOppgaven(oppgave.dokumentId.toString(), oppgavestyring)
                else -> log.info("Gjør ikke ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId} fordi status er ${oppgavestyring!!.status.name}")
            }
        }
    }

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                saksbehandlingsService.behandleSoknad(sykepengesoknad)
                if (sykepengesoknad.soknadstype == ARBEIDSTAKERE && skalBehandlesAvNav(sykepengesoknad) && toggle.isNotProduction()) {
                    prosesserOppgave(OppgaveDTO(
                        dokumentId = UUID.fromString(sykepengesoknad.id),
                        dokumentType = DokumentTypeDTO.Søknad,
                        oppdateringstype = OppdateringstypeDTO.Utsett,
                        timeout = sykepengesoknad.sendtNav?.plusHours(timeout) ?: LocalDateTime.now().plusHours(timeout)
                    ))
                } else {
                    if (skalBehandlesAvNav(sykepengesoknad)) {
                        val innsending = saksbehandlingsService.finnEksisterendeInnsending(sykepengesoknad.id)
                            ?: throw RuntimeException("Fant ikke eksisterende innsending")
                        saksbehandlingsService.opprettOppgave(sykepengesoknad, innsending)
                    }
                }
            }
        } catch (e: Exception) {
            saksbehandlingsService.innsendingFeilet(sykepengesoknad, e)
        }
    }

    fun utsettOppgave(søknadsId: String, nyTimeout: LocalDateTime, oppgavestyring: SpreOppgave?) {
        if (toggle.isNotProduction()) {
            if (oppgavestyring != null) {
                if (nyTimeout.isAfter(oppgavestyring.timeout)) {
                    oppgavestyringDAO.settTimeout(søknadsId, nyTimeout)
                }
            } else {
                oppgavestyringDAO.nySpreOppgave(søknadsId, nyTimeout, OppgaveStatus.Utsett)
            }
        }
    }

    fun opprettOppgave(søknadsId: String, oppgavestyring: SpreOppgave?) {
        if (toggle.isNotProduction()) {
            if (oppgavestyring != null) {
                oppgavestyringDAO.settTimeout(søknadsId, null)
                oppgavestyringDAO.settStatus(søknadsId, OppgaveStatus.Opprett)
            } else {
                oppgavestyringDAO.nySpreOppgave(søknadsId, null, OppgaveStatus.Opprett)
            }
        }
    }

    fun viBehandlerIkkeOppgaven(søknadsId: String, oppgavestyring: SpreOppgave?) {
        if (toggle.isNotProduction()) {
            if (oppgavestyring != null) {
                oppgavestyringDAO.settTimeout(søknadsId, null)
                oppgavestyringDAO.settStatus(søknadsId, OppgaveStatus.IkkeOpprett)
            } else {
                oppgavestyringDAO.nySpreOppgave(søknadsId, null, OppgaveStatus.IkkeOpprett)
            }
        }
    }

    private fun skalBehandlesAvNav(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtNav != null

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) = sykepengesoknad.sendtArbeidsgiver != null
        && sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}
