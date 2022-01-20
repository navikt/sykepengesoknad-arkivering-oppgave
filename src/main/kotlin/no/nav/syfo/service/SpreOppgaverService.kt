package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.domain.DokumentTypeDTO
import no.nav.syfo.domain.OppdateringstypeDTO
import no.nav.syfo.domain.OppgaveDTO
import no.nav.syfo.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.logger
import no.nav.syfo.repository.OppgaveStatus
import no.nav.syfo.repository.OppgavestyringDAO
import no.nav.syfo.repository.SpreOppgave
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SpreOppgaverService(
    @Value("\${default.timeout.timer}") private val defaultTimeoutTimer: String,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val oppgavestyringDAO: OppgavestyringDAO,
    registry: MeterRegistry,
) {
    private val log = logger()
    private val timeout = defaultTimeoutTimer.toLong()
    private val gjenopplivetCounter = registry.counter("syfogsak_gjenopplivet_oppgave")

    // Er Synchronized pga. race condition mellom saksbehandling i vårt system og saksbehandling i Bømlo's system
    @Synchronized
    fun prosesserOppgave(oppgave: OppgaveDTO, kilde: OppgaveKilde) {
        val eksisterendeOppgave = oppgavestyringDAO.hentSpreOppgave(oppgave.dokumentId.toString())
        when (kilde) {
            OppgaveKilde.Søknad -> håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
            OppgaveKilde.Saksbehandling -> håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
        }
    }

    private fun håndterOppgaveFraSøknad(
        eksisterendeOppgave: SpreOppgave?,
        oppgave: OppgaveDTO
    ) {
        if (eksisterendeOppgave != null) {
            oppgavestyringDAO.avstem(eksisterendeOppgave.søknadsId)
        } else {
            oppgavestyringDAO.nySpreOppgave(
                oppgave.dokumentId,
                LocalDateTime.now().plusHours(48),
                OppgaveStatus.Utsett,
                avstemt = true
            )
        }
    }

    // Dersom on-prem og aiven konsumering slåss om kallet
    @Synchronized
    private fun håndterOppgaveFraBømlo(
        eksisterendeOppgave: SpreOppgave?,
        oppgave: OppgaveDTO
    ) {
        when {
            eksisterendeOppgave == null -> {
                oppgavestyringDAO.nySpreOppgave(
                    oppgave.dokumentId,
                    timeout(oppgave),
                    oppgave.oppdateringstype.tilOppgaveStatus()
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.Utsett -> {
                oppgavestyringDAO.oppdaterOppgave(
                    oppgave.dokumentId,
                    timeout(oppgave),
                    oppgave.oppdateringstype.tilOppgaveStatus()
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.IkkeOpprett && oppgave.oppdateringstype == OppdateringstypeDTO.Opprett -> {
                log.info("Vil opprette oppgave for søknad ${oppgave.dokumentId} som vi tidligere ble bedt om å ikke opprette")
                gjenopplivetCounter.increment()
                oppgavestyringDAO.oppdaterOppgave(
                    oppgave.dokumentId,
                    timeout(oppgave),
                    oppgave.oppdateringstype.tilOppgaveStatus()
                )
            }
            else -> {
                log.info("Gjør ikke ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId} fordi status er ${eksisterendeOppgave.status.name}")
            }
        }
    }

    private fun timeout(oppgave: OppgaveDTO) =
        if (oppgave.oppdateringstype == OppdateringstypeDTO.Utsett) oppgave.timeout else null

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                val innsendingsId = saksbehandlingsService.behandleSoknad(sykepengesoknad)
                if (sykepengesoknad.soknadstype == ARBEIDSTAKERE && skalBehandlesAvNav(sykepengesoknad)) {
                    prosesserOppgave(
                        OppgaveDTO(
                            dokumentId = UUID.fromString(sykepengesoknad.id),
                            dokumentType = DokumentTypeDTO.Søknad,
                            oppdateringstype = OppdateringstypeDTO.Utsett,
                            timeout = sykepengesoknad.sendtNav?.plusHours(timeout) ?: LocalDateTime.now()
                                .plusHours(timeout)
                        ),
                        OppgaveKilde.Søknad
                    )
                } else {
                    if (skalBehandlesAvNav(sykepengesoknad)) {
                        val innsending = saksbehandlingsService.finnEksisterendeInnsending(sykepengesoknad.id)
                            ?: throw RuntimeException("Fant ikke eksisterende innsending")
                        saksbehandlingsService.opprettOppgave(sykepengesoknad, innsending)
                    }
                }
                saksbehandlingsService.settFerdigbehandlet(innsendingsId)
            }
        } catch (e: Exception) {
            saksbehandlingsService.innsendingFeilet(sykepengesoknad, e)
        }
    }

    private fun skalBehandlesAvNav(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtNav != null

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtArbeidsgiver != null &&
            sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}

fun OppdateringstypeDTO.tilOppgaveStatus() = when (this) {
    OppdateringstypeDTO.Utsett -> OppgaveStatus.Utsett
    OppdateringstypeDTO.Ferdigbehandlet -> OppgaveStatus.IkkeOpprett
    OppdateringstypeDTO.OpprettSpeilRelatert -> OppgaveStatus.OpprettSpeilRelatert
    OppdateringstypeDTO.Opprett -> OppgaveStatus.Opprett
}

enum class OppgaveKilde {
    Søknad, Saksbehandling
}
