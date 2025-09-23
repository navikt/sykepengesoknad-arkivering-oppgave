package no.nav.helse.flex.spreoppgave

import no.nav.helse.flex.domain.DokumentTypeDTO
import no.nav.helse.flex.domain.OppdateringstypeDTO
import no.nav.helse.flex.domain.OppgaveDTO
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSLEDIG
import no.nav.helse.flex.domain.dto.Soknadstype.ARBEIDSTAKERE
import no.nav.helse.flex.domain.dto.Soknadstype.SELVSTENDIGE_OG_FRILANSERE
import no.nav.helse.flex.domain.dto.Sykepengesoknad
import no.nav.helse.flex.repository.SpreOppgaveRepository
import no.nav.helse.flex.service.SaksbehandlingsService
import org.springframework.data.relational.core.conversion.DbActionExecutionException
import org.springframework.stereotype.Component
import java.util.*

@Component
class SpreOppgaverService(
    private val saksbehandlingsService: SaksbehandlingsService,
    private val spreOppgaveRepository: SpreOppgaveRepository,
    private val handterOppave: HandterOppgaveInterface,
) {
    private val enUke = (7 * 24).toLong()
    private val ettDogn = (1 * 24).toLong()
    private val soknaderSpeilKjennerTil = setOf(ARBEIDSTAKERE, ARBEIDSLEDIG, SELVSTENDIGE_OG_FRILANSERE)

    fun prosesserOppgave(
        oppgave: OppgaveDTO,
        kilde: OppgaveKilde,
    ) {
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
                val timeoutMinutter = beregnTimeoutMinutter(sykepengesoknad)

                if (sykepengesoknad.sendtNav != null) {
                    prosesserOppgave(
                        OppgaveDTO(
                            dokumentId = UUID.fromString(sykepengesoknad.id),
                            dokumentType = DokumentTypeDTO.Søknad,
                            oppdateringstype = OppdateringstypeDTO.VenterPaBomlo,
                            timeout = sykepengesoknad.sendtNav.plusMinutes(timeoutMinutter),
                        ),
                        OppgaveKilde.Søknad,
                    )
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

    private fun beregnTimeoutMinutter(soknad: Sykepengesoknad): Long =
        when {
            !soknad.erSoknadSpeilKjennerTil() ->
                // Bruker timeout på 1 minutt på søknader Bømlo kjenner sånn at det opprettes oppgave så fort som mulig.
                1

            soknad.soknadstype == SELVSTENDIGE_OG_FRILANSERE ->
                // 24 timer for selvstendig næringsdrivende inntil Bømlo gjør vanlig vurdering av alle søknader.
                ettDogn * 60

            else ->
                // 7 dager timeout på søknader Bømlo kjenner til da vi skal få Opprett, Utsett eller IkkeOpprett fra de.
                enUke * 60
        }

    private fun Sykepengesoknad.erSoknadSpeilKjennerTil(): Boolean =
        soknaderSpeilKjennerTil.contains(soknadstype) && this.sendTilGosys != true

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtArbeidsgiver != null &&
            sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
}

enum class OppgaveKilde {
    Søknad,
    Saksbehandling,
}
