package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.consumer.ws.*
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.log
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

@Component
class SaksbehandlingsService(
        private val sakConsumer: SakConsumer,
        private val oppgavebehandlingConsumer: OppgavebehandlingConsumer,
        private val behandleJournalConsumer: BehandleJournalConsumer,
        private val behandlendeEnhetService: BehandlendeEnhetService,
        private val aktorConsumer: AktorConsumer,
        private val innsendingDAO: InnsendingDAO,
        private val personConsumer: PersonConsumer,
        private val registry: MeterRegistry) {

    val log = log()

    private fun ikkeSendtTilNav(sykepengesoknad: Sykepengesoknad): Boolean {
        return !("SENDT" == sykepengesoknad.status && sykepengesoknad.sendtNav != null)
    }

    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad): Boolean {
        return sykepengesoknad.sendtArbeidsgiver != null && sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false
    }

    fun behandleSoknad(sykepengesoknad: Sykepengesoknad) {
        if (ikkeSendtTilNav(sykepengesoknad) || ettersendtTilArbeidsgiver(sykepengesoknad)) {
            return
        }

        val sykepengesoknadId = sykepengesoknad.id
        val aktorId = sykepengesoknad.aktorId
        val innsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId)

        if (innsending != null) {
            val innsendingId = innsending.innsendingsId
            log.warn("Innsending for sykepengesøknad {} allerede opprettet med id {}.",
                    sykepengesoknadId,
                    innsendingId
            )
            return
        }

        val innsendingId = innsendingDAO.opprettInnsending(sykepengesoknadId, aktorId, sykepengesoknad.fom, sykepengesoknad.tom)

        try {
            val fnr = aktorConsumer.finnFnr(aktorId)
            val soknad = opprettSoknad(sykepengesoknad, fnr)

            val saksId = finnEllerOpprettSak(innsendingId, aktorId, soknad.fom)
            val journalpostId = opprettJournalpost(innsendingId, soknad, saksId)

            opprettOppgave(innsendingId, fnr, soknad, saksId, journalpostId)

            innsendingDAO.settBehandlet(innsendingId)

            tellInnsendingBehandlet(sykepengesoknad.soknadstype)
            log.info("Søknad med id {} er behandlet i innsending med id {}",
                    soknad.soknadsId,
                    innsendingId
            )
        } catch (e: Exception) {
            innsendingDAO.leggTilFeiletInnsending(innsendingId)

            tellInnsendingFeilet(sykepengesoknad.soknadstype)
            log.error("Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}",
                    innsendingId,
                    sykepengesoknadId,
                    e)
        }
    }


    fun opprettOppgave(innsendingId: String, fnr: String, soknad: Soknad, saksId: String, journalpostId: String) {
        val behandlendeEnhet = behandlendeEnhetService.hentBehandlendeEnhet(fnr, soknad.soknadstype)
        val oppgaveId = oppgavebehandlingConsumer
                .opprettOppgave(fnr, behandlendeEnhet, saksId, journalpostId, soknad)
        innsendingDAO.oppdaterOppgaveId(innsendingId, oppgaveId)
    }

    fun opprettJournalpost(innsendingId: String, soknad: Soknad, saksId: String): String {
        val journalpostId = behandleJournalConsumer.opprettJournalpost(soknad, saksId)
        innsendingDAO.oppdaterJournalpostId(innsendingId, journalpostId)
        return journalpostId
    }

    fun finnEllerOpprettSak(innsendingId: String, aktorId: String, soknadFom: LocalDate?): String =
            innsendingDAO.finnTidligereInnsendinger(aktorId)
                    .filter { (it.soknadTom).isBefore(soknadFom ?: LocalDate.MIN) }
                    .filter { erPaFolgendeInkludertHelg(it.soknadTom, soknadFom ?: LocalDate.MAX) }
                    .sortedByDescending { it.soknadTom }
                    .firstOrNull()
                    ?.let {
                        innsendingDAO.oppdaterSaksId(innsendingId, it.saksId)
                        return it.saksId
                    }
                    ?: opprettSak(aktorId, innsendingId)

    private fun opprettSak(aktorId: String, innsendingId: String): String {
        val saksId = sakConsumer.opprettSak(aktorId)
        innsendingDAO.oppdaterSaksId(innsendingId, saksId)
        return saksId
    }

    fun erPaFolgendeInkludertHelg(one: LocalDate, two: LocalDate): Boolean {

        return Stream.iterate(one.plusDays(1)) { it.plusDays(1) }
                .limit(ChronoUnit.DAYS.between(one, two) - 1)
                .map { it.dayOfWeek }
                .allMatch{ it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY}
    }

    fun opprettSoknad(sykepengesoknad: Sykepengesoknad, fnr: String): Soknad =
            Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr))

    private fun tellInnsendingBehandlet(soknadstype: Soknadstype?) {
        registry.counter(
                "syfogsak.innsending.behandlet",
                Tags.of(
                        "type", "info",
                        "soknadstype", soknadstype?.name ?: "UKJENT",
                        "help", "Antall ferdigbehandlede innsendinger."
                ))
                .increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry.counter(
                "syfogsak.innsending.feilet",
                Tags.of(
                        "type", "info",
                        "soknadstype", soknadstype?.name ?: "UKJENT",
                        "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
                ))
                .increment()
    }
}
