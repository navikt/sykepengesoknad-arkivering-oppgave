package no.nav.syfo.service

import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.inject.Inject

@Component
class BehandleFeiledeSoknaderService @Inject
constructor(
    private val innsendingDAO: InnsendingDAO,
    private val aktorConsumer: AktorConsumer,
    private val saksbehandlingsService: SaksbehandlingsService
) {
    fun behandleFeiletSoknad(innsending: Innsending, sykepengesoknad: Sykepengesoknad) {
        try {
            val innsendingId = innsending.innsendingsId
            val aktorId = innsending.aktorId ?: sykepengesoknad.aktorId.also {
                innsendingDAO.oppdaterAktorId(innsendingId, it)
            }
            val saksId = innsending.saksId ?: behandleFraSaksId(innsendingId, aktorId, sykepengesoknad.fom)
            val journalpostId =
                innsending.journalpostId ?: behandleFraJournalpost(innsendingId, saksId, sykepengesoknad)

            if (innsending.oppgaveId == null) {
                behandleFraOppgave(innsendingId, saksId, journalpostId, sykepengesoknad)
            }

            if (innsending.behandlet == null) {
                innsendingDAO.settBehandlet(innsendingId)
                log().info(
                    "Fullført rebehandling av innsending med id: {} av soknad med id: {}",
                    innsendingId, sykepengesoknad.id
                )
            } else {
                log().warn(
                    "Forsøkte å rebehandle ferdigbehandlet søknad med innsendingid: {} og søknadsid: {}",
                    innsendingId, sykepengesoknad.id
                )
            }
        } catch (e: RuntimeException) {
            throw RuntimeException("Feilet ved rebehandling av innsending med ressursid: ${sykepengesoknad.id}", e)
        }
    }

    private fun behandleFraOppgave(
        innsendingsId: String,
        saksId: String,
        journalpostId: String,
        sykepengesoknad: Sykepengesoknad
    ) {
        val fnr = aktorConsumer.finnFnr(sykepengesoknad.aktorId)

        saksbehandlingsService.opprettOppgave(
            innsendingsId,
            fnr,
            sykepengesoknad,
            saksbehandlingsService.opprettSoknad(sykepengesoknad, fnr),
            saksId,
            journalpostId
        )
    }

    private fun behandleFraJournalpost(
        innsendingsId: String,
        saksId: String,
        sykepengesoknad: Sykepengesoknad
    ): String {
        val soknad = saksbehandlingsService
            .opprettSoknad(
                sykepengesoknad,
                aktorConsumer.finnFnr(sykepengesoknad.aktorId)
            )

        return saksbehandlingsService
            .opprettJournalpost(
                innsendingsId,
                soknad,
                saksId
            )
    }

    private fun behandleFraSaksId(innsendingsId: String, aktorId: String, soknadFom: LocalDate?): String {
        return saksbehandlingsService.finnEllerOpprettSak(innsendingsId, aktorId, soknadFom)
    }
}
