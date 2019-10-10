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
        private val saksbehandlingsService: SaksbehandlingsService) {

    fun behandleFeiletSoknad(innsending: Innsending, sykepengesoknad: Sykepengesoknad) {
        val innsendingsId = innsending.innsendingsId
        var aktorId = innsending.aktorId
        var saksId = innsending.saksId
        var journalpostId = innsending.journalpostId

        try {
            if (aktorId == null) {
                aktorId = sykepengesoknad.aktorId
                innsendingDAO.oppdaterAktorId(innsendingsId, aktorId)
            }

            if (saksId == null) {
                saksId = behandleFraSaksId(innsendingsId, aktorId, sykepengesoknad.fom)
            }
            if (journalpostId == null) {
                journalpostId = behandleFraJournalpost(innsendingsId, saksId, sykepengesoknad)
            }
            if (innsending.oppgaveId == null) {
                behandleFraOppgave(innsendingsId, saksId, journalpostId, sykepengesoknad)
            }

            if (innsending.behandlet == null) {
                innsendingDAO.settBehandlet(innsendingsId)
                log().info("Fullført rebehandling av innsending med id: {} av soknad med id: {}",
                        innsendingsId, sykepengesoknad.id)
            } else {
                log().warn("Forsøkte å rebehandle ferdigbehandlet søknad med innsendingid: {} og søknadsid: {}",
                        innsendingsId, sykepengesoknad.id)
            }
        } catch (e: RuntimeException) {
            log().error("Feilet ved rebehandling av innsending med ressursid: {}", sykepengesoknad.id, e)
            throw RuntimeException(e)
        }

    }

    private fun behandleFraOppgave(
            innsendingsId: String,
            saksId: String,
            journalpostId: String,
            sykepengesoknad: Sykepengesoknad) {
        val aktorId = sykepengesoknad.aktorId
        val fnr = aktorConsumer.finnFnr(aktorId)

        saksbehandlingsService.opprettOppgave(
                innsendingsId,
                fnr,
                aktorId,
                saksbehandlingsService.opprettSoknad(sykepengesoknad, fnr),
                saksId,
                journalpostId
        )
    }

    private fun behandleFraJournalpost(innsendingsId: String, saksId: String, sykepengesoknad: Sykepengesoknad): String {
        val soknad = saksbehandlingsService
                .opprettSoknad(
                        sykepengesoknad,
                        aktorConsumer.finnFnr(sykepengesoknad.aktorId)
                )

        return saksbehandlingsService
                .opprettJournalpost(
                        innsendingsId,
                        soknad,
                        saksId)
    }

    private fun behandleFraSaksId(innsendingsId: String, aktorId: String, soknadFom: LocalDate?): String {
        return saksbehandlingsService.finnEllerOpprettSak(innsendingsId, aktorId, soknadFom)
    }
}
