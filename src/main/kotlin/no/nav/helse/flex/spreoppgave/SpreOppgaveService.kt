package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.SaksbehandlingsService
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.UUID

@Component
class SpreOppgaverService(
    @Value("\${DEFAULT_TIMEOUT_TIMER}")
    private val defaultTimeoutTimer: String,
    private val saksbehandlingsService: SaksbehandlingsService,
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val handterOppave: HandterOppgaveInterface,
) {
    private val timeout = defaultTimeoutTimer.toLong()

    fun prosesserOppgave(oppgave: OppgaveDTO, kilde: OppgaveKilde) {
        val eksisterendeOppgave = spreOppgaveRepository.findBySykepengesoknadId(oppgave.dokumentId.toString())
        when (kilde) {
            OppgaveKilde.Søknad -> handterOppave.håndterOppgaveFraSøknad(eksisterendeOppgave, oppgave)
            OppgaveKilde.Saksbehandling -> handterOppave.håndterOppgaveFraBømlo(eksisterendeOppgave, oppgave)
        }
    }

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
        } catch (e: DbActionExecutionException) {
            // Kastes videre for å gjøre acknowledgment.nack()
            throw e
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

enum class OppgaveKilde {
    Søknad, Saksbehandling
}
