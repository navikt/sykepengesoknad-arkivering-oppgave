package no.nav.syfo.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.syfo.consumer.aktor.AktorConsumer
import no.nav.syfo.consumer.oppgave.OppgaveConsumer
import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.consumer.sak.SakConsumer
import no.nav.syfo.consumer.ws.BehandleJournalConsumer
import no.nav.syfo.consumer.ws.PersonConsumer
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.syfo.kafka.producer.RebehandlingProducer
import no.nav.syfo.log
import no.nav.syfo.oppgave.UtsattOppgaveDAO
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
    private val utsattOppgaveDAO: UtsattOppgaveDAO,
    private val personConsumer: PersonConsumer,
    private val registry: MeterRegistry,
    private val rebehandlingProducer: RebehandlingProducer
) {

    private val log = log()

    fun behandleSoknad(sykepengesoknad: Sykepengesoknad) {
        val sykepengesoknadId = sykepengesoknad.id
        val aktorId = sykepengesoknad.aktorId
        if (ikkeSendtTilNav(sykepengesoknad) || ettersendtTilArbeidsgiver(sykepengesoknad)) return

        val eksisterendeInnsendingId = finnEksisterendeInnsendingId(sykepengesoknadId)
        if (eksisterendeInnsendingId != null) {
            log.warn(
                "Innsending for sykepengesøknad {} allerede opprettet med id {}.",
                sykepengesoknadId,
                eksisterendeInnsendingId
            )
            return
        }

        lagOppgaver(sykepengesoknadId, aktorId, sykepengesoknad)
    }

    private fun ikkeSendtTilNav(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.status != "SENDT" || sykepengesoknad.sendtNav == null


    private fun ettersendtTilArbeidsgiver(sykepengesoknad: Sykepengesoknad) =
        sykepengesoknad.sendtArbeidsgiver != null && sykepengesoknad.sendtNav?.isBefore(sykepengesoknad.sendtArbeidsgiver) ?: false

    private fun finnEksisterendeInnsendingId(sykepengesoknadId: String) =
        innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId)?.innsendingsId

    private fun lagOppgaver(sykepengesoknadId: String, aktorId: String, sykepengesoknad: Sykepengesoknad) {
        val innsendingId =
            innsendingDAO.opprettInnsending(sykepengesoknadId, aktorId, sykepengesoknad.fom, sykepengesoknad.tom)

        try {
            val fnr = aktorConsumer.finnFnr(aktorId)
            val soknad = opprettSoknad(sykepengesoknad, fnr)

            val saksId = finnEllerOpprettSak(innsendingId, aktorId, soknad.fom)
            val journalpostId = opprettJournalpost(innsendingId, soknad, saksId)

            opprettUtsattOppgave(innsendingId, fnr, aktorId, soknad, saksId, journalpostId)

            when (soknad.soknadstype) {
                Soknadstype.SELVSTENDIGE_OG_FRILANSERE, Soknadstype.OPPHOLD_UTLAND, Soknadstype.ARBEIDSLEDIG -> {
                    aktiverUtsatteOppgaver(aktorId, soknad.soknadstype)
                }
                Soknadstype.ARBEIDSTAKERE -> {
                    aktiverUtsatteOppgaver(aktorId, Soknadstype.ARBEIDSTAKERE)
                }
                null -> error("Søknadstype er null for ${soknad.soknadsId}")
            }

        } catch (e: Exception) {
            innsendingFeilet(sykepengesoknad, innsendingId, e)
        }
    }

    private fun innsendingFeilet(
        sykepengesoknad: Sykepengesoknad,
        innsendingId: String,
        e: Exception
    ) {
        tellInnsendingFeilet(sykepengesoknad.soknadstype)
        log.error(
            "Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}, legger på intern rebehandling-topic",
            innsendingId,
            sykepengesoknad.id,
            e
        )
        rebehandlingProducer.leggPaRebehandlingTopic(sykepengesoknad, now().plusMinutes(10))
    }

    fun opprettUtsattOppgave(
        innsendingId: String,
        fnr: String,
        aktorId: String,
        soknad: Soknad,
        saksId: String,
        journalpostId: String
    ) {
        val behandlendeEnhet = behandlendeEnhetService.hentBehandlendeEnhet(fnr, soknad.soknadstype)
        val requestBody = OppgaveConsumer.lagRequestBody(aktorId, behandlendeEnhet, saksId, journalpostId, soknad)
        utsattOppgaveDAO.lagreUtsattOppgave(soknad.soknadsId!!, requestBody)
    }

    fun aktiverUtsatteOppgaver(aktorId: String, soknadstype: Soknadstype) {
        for (utsattOppgave in utsattOppgaveDAO.hentUtsattOppgaverForAktorId(aktorId)) {
            val oppgaveId = oppgaveConsumer.opprettOppgave(utsattOppgave.oppgaveRequest).id.toString()
            val innsendingId = utsattOppgave.innsendingId

            innsendingDAO.oppdaterOppgaveId(oppgaveId, innsendingId)
            utsattOppgaveDAO.fjernUtsattOppgave(innsendingId)
            innsendingDAO.settBehandlet(innsendingId)

            tellInnsendingBehandlet(soknadstype)
            log.info("Søknad er behandlet i innsending med id {}", innsendingId)
        }
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

    private fun opprettSak(aktorId: String, innsendingId: String): String {
        val saksId = sakConsumer.opprettSak(aktorId)
        innsendingDAO.oppdaterSaksId(innsendingId, saksId)
        return saksId
    }

    fun erPaFolgendeInkludertHelg(one: LocalDate, two: LocalDate): Boolean {

        return Stream.iterate(one.plusDays(1)) { it.plusDays(1) }
            .limit(ChronoUnit.DAYS.between(one, two) - 1)
            .map { it.dayOfWeek }
            .allMatch { it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY }
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
            )
        )
            .increment()
    }

    private fun tellInnsendingFeilet(soknadstype: Soknadstype?) {
        registry.counter(
            "syfogsak.innsending.feilet",
            Tags.of(
                "type", "info",
                "soknadstype", soknadstype?.name ?: "UKJENT",
                "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
            )
        )
            .increment()
    }
}
