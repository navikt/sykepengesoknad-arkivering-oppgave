package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.consumer.ws.BehandleJournalConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.Innsending
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandlingProducer
import no.nav.syfo.log
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime.now
import java.time.temporal.ChronoUnit
import java.util.stream.Stream

@Component
class SaksbehandlingsService(
    private val sakConsumer: SakConsumer,
    private val oppgaveConsumer: OppgaveConsumer,
    private val behandleJournalConsumer: BehandleJournalConsumer,
    private val behandlendeEnhetService: BehandlendeEnhetService,
    private val aktorConsumer: AktorConsumer,
    private val innsendingDAO: InnsendingDAO,
    private val personConsumer: PersonConsumer,
    private val registry: MeterRegistry,
    private val rebehandlingProducer: RebehandlingProducer
) {

    private val log = log()

    fun behandleSoknad(sykepengesoknad: Sykepengesoknad): String {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        val innsendingId = eksisterendeInnsending?.innsendingsId
            ?: innsendingDAO.opprettInnsending(sykepengesoknad.id, sykepengesoknad.aktorId, sykepengesoknad.fom, sykepengesoknad.tom)
        val fnr = aktorConsumer.finnFnr(sykepengesoknad.aktorId)
        val soknad = opprettSoknad(sykepengesoknad, fnr)
        val saksId = eksisterendeInnsending?.saksId
            ?: finnEllerOpprettSak(innsendingId, sykepengesoknad.aktorId, soknad.fom)
        eksisterendeInnsending?.journalpostId ?: opprettJournalpost(innsendingId, soknad, saksId)
        log.info("Journalført søknad: ${sykepengesoknad.id}")
        return innsendingId
    }

    fun opprettOppgave(sykepengesoknad: Sykepengesoknad, innsending: Innsending) {
        val fnr = aktorConsumer.finnFnr(sykepengesoknad.aktorId)
        val soknad = opprettSoknad(sykepengesoknad, fnr)

        val behandlendeEnhet = behandlendeEnhetService.hentBehandlendeEnhet(fnr, soknad.soknadstype)
        val requestBody = OppgaveConsumer.lagRequestBody(sykepengesoknad.aktorId, behandlendeEnhet, innsending.saksId!!, innsending.journalpostId!!, soknad, sykepengesoknad.harRedusertVenteperiode)
        val oppgaveId = oppgaveConsumer.opprettOppgave(requestBody).id.toString()

        innsendingDAO.oppdaterOppgaveId(uuid = innsending.innsendingsId, oppgaveId = oppgaveId)

        tellInnsendingBehandlet(soknad.soknadstype)
        log.info("Oppretter oppgave ${innsending.innsendingsId} for ${soknad.soknadstype?.name?.toLowerCase()} søknad: ${soknad.soknadsId}")
    }

    fun settFerdigbehandlet(innsendingsId: String) {
        innsendingDAO.settBehandlet(innsendingsId)
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
            .maxBy { it.soknadTom }
            ?.let {
                innsendingDAO.oppdaterSaksId(innsendingId, it.saksId)
                return it.saksId
            }
            ?: opprettSak(aktorId, innsendingId)

    fun finnEksisterendeInnsending(sykepengesoknadId: String) =
        innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId)

    fun innsendingFeilet(sykepengesoknad: Sykepengesoknad, e: Exception) {
        val eksisterendeInnsending = finnEksisterendeInnsending(sykepengesoknad.id)
        tellInnsendingFeilet(sykepengesoknad.soknadstype)
        log.error(
            "Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}, legger på intern rebehandling-topic",
            eksisterendeInnsending?.innsendingsId,
            sykepengesoknad.id,
            e
        )
        rebehandlingProducer.leggPaRebehandlingTopic(sykepengesoknad, now().plusMinutes(10))
    }

    private fun opprettSak(aktorId: String, innsendingId: String): String {
        val saksId = sakConsumer.opprettSak(aktorId)
        innsendingDAO.oppdaterSaksId(innsendingId, saksId)
        return saksId
    }

    fun erPaFolgendeInkludertHelg(one: LocalDate, two: LocalDate): Boolean =
        Stream.iterate(one.plusDays(1)) { it.plusDays(1) }
            .limit(ChronoUnit.DAYS.between(one, two) - 1)
            .map { it.dayOfWeek }
            .allMatch { it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY }

    fun opprettSoknad(sykepengesoknad: Sykepengesoknad, fnr: String): Soknad =
        Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr))

    private fun tellInnsendingBehandlet(soknadstype: Soknadstype?) {
        registry.counter(
            "syfogsak.innsending.behandlet",
            Tags.of(
                "type", "info",
                "soknadstype", soknadstype?.name ?: "UKJENT",
                "help", "Antall ferdigbehandlede innsendinger."
            )
        ).increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry.counter(
            "syfogsak.innsending.feilet",
            Tags.of(
                "type", "info",
                "soknadstype", soknadstype?.name ?: "UKJENT",
                "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
            )
        ).increment()
    }
}
