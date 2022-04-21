package no.nav.helse.flex.service

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.OppgaveStatus
import no.nav.helse.flex.repository.SpreOppgaveDbRecord
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.util.tilOsloZone
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

@Component
class SpreOppgaverService(
    @Value("\${DEFAULT_TIMEOUT_TIMER}")
    private val defaultTimeoutTimer: String,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val spreOppgaveRepository: SpreOppgaveRepository,
    registry: MeterRegistry,
) {
    private val log = logger()
    private val timeout = defaultTimeoutTimer.toLong()
    private val gjenopplivetCounter = registry.counter("gjenopplivet_oppgave")

    fun prosesserOppgave(oppgave: OppgaveDTO, kilde: OppgaveKilde) {
        val eksisterendeOppgave = spreOppgaveRepository.findBySykepengesoknadId(oppgave.dokumentId.toString())
        when (kilde) {
            OppgaveKilde.Søknad -> håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
            OppgaveKilde.Saksbehandling -> håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
        }
    }

    private fun håndterOppgaveFraSøknad(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    ) {
        if (eksisterendeOppgave != null) {
            spreOppgaveRepository.updateAvstemtBySykepengesoknadId(eksisterendeOppgave.sykepengesoknadId)
        } else {
            val now = Instant.now()
            spreOppgaveRepository.save(
                SpreOppgaveDbRecord(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    avstemt = true,
                    opprettet = now,
                    modifisert = now
                )
            )
        }
    }

    private fun håndterOppgaveFraBømlo(
        eksisterendeOppgave: SpreOppgaveDbRecord?,
        oppgave: OppgaveDTO
    ) {
        val tidspunkt = Instant.now()
        when {
            eksisterendeOppgave == null -> {
                spreOppgaveRepository.save(
                    SpreOppgaveDbRecord(
                        sykepengesoknadId = oppgave.dokumentId.toString(),
                        timeout = timeout(oppgave),
                        status = oppgave.oppdateringstype.tilOppgaveStatus(),
                        opprettet = tidspunkt,
                        modifisert = tidspunkt
                    )
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.Utsett -> {
                spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    tidspunkt
                )
            }
            eksisterendeOppgave.status == OppgaveStatus.IkkeOpprett && oppgave.oppdateringstype == OppdateringstypeDTO.Opprett -> {
                log.info("Vil opprette oppgave for søknad ${oppgave.dokumentId} som vi tidligere ble bedt om å ikke opprette")
                gjenopplivetCounter.increment()
                spreOppgaveRepository.updateOppgaveBySykepengesoknadId(
                    sykepengesoknadId = oppgave.dokumentId.toString(),
                    timeout = timeout(oppgave),
                    status = oppgave.oppdateringstype.tilOppgaveStatus(),
                    tidspunkt
                )
            }
            else -> {
                log.info("Gjør ikke ${oppgave.oppdateringstype.name} for søknad ${oppgave.dokumentId} fordi status er ${eksisterendeOppgave.status.name}")
            }
        }
    }

    private fun timeout(oppgave: OppgaveDTO) =
        if (oppgave.oppdateringstype == OppdateringstypeDTO.Utsett) oppgave.timeout?.tilOsloZone()?.toInstant() else null

    fun soknadSendt(sykepengesoknad: Sykepengesoknad) {
        try {
            if (sykepengesoknad.status == "SENDT" && !ettersendtTilArbeidsgiver(sykepengesoknad)) {
                val innsendingsId = saksbehandlingsService.behandleSoknad(sykepengesoknad)
                if (sykepengesoknad.skalSynkeOppgaveOpprettelseMedBomlo()) {
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
                    if (sykepengesoknad.skalBehandlesAvNav()) {
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

    private fun Sykepengesoknad.skalSynkeOppgaveOpprettelseMedBomlo(): Boolean {
        return soknadstype == ARBEIDSTAKERE && skalBehandlesAvNav() && this.sendTilGosys != true
    }

    private fun Sykepengesoknad.skalBehandlesAvNav() =
        this.sendtNav != null

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
