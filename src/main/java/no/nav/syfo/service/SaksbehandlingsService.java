package no.nav.syfo.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Optional;

@Slf4j
@Component
public class SaksbehandlingsService {

    private final BehandleSakConsumer behandleSakConsumer;
    private final OppgavebehandlingConsumer oppgavebehandlingConsumer;
    private final BehandleJournalConsumer behandleJournalConsumer;
    private final AktorConsumer aktorConsumer;
    private final PersonConsumer personConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final InnsendingDAO innsendingDAO;
    private final MeterRegistry registry;

    @Inject
    public SaksbehandlingsService(
            BehandleSakConsumer behandleSakConsumer,
            OppgavebehandlingConsumer oppgavebehandlingConsumer,
            BehandleJournalConsumer behandleJournalConsumer,
            AktorConsumer aktorConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            InnsendingDAO innsendingDAO,
            PersonConsumer personConsumer,
            MeterRegistry registry) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktorConsumer = aktorConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.innsendingDAO = innsendingDAO;
        this.personConsumer = personConsumer;
        this.registry = registry;
    }

    public String behandleSoknad(Sykepengesoknad sykepengesoknad) {
        String sykepengesoknadId = sykepengesoknad.getId();
        String aktorId = sykepengesoknad.getAktorId();
        Optional<Innsending> innsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId);

        if (innsending.isPresent()) {
            String innsendingId = innsending.get().getInnsendingsId();
            log.warn("Innsending for sykepengesøknad {} allerede opprettet med id {}.",
                    sykepengesoknadId,
                    innsendingId
            );
            return innsendingId;
        }

        String innsendingId = innsendingDAO.opprettInnsending(sykepengesoknadId, aktorId);

        try {
            String fnr = aktorConsumer.finnFnr(aktorId);
            Soknad soknad = opprettSoknad(sykepengesoknad, fnr);
            String saksId = opprettSak(innsendingId, fnr);
            String journalpostId = opprettJournalpost(innsendingId, soknad, saksId);

            opprettOppgave(innsendingId, fnr, soknad, saksId, journalpostId);

            innsendingDAO.settBehandlet(innsendingId);
            tellInnsendingBehandlet();
        } catch (Exception e) {
            log.error("Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}",
                    innsendingId,
                    sykepengesoknadId,
                    e);
            innsendingDAO.leggTilFeiletInnsending(innsendingId);
            tellInnsendingFeilet();
        }

        return innsendingId;
    }

    public void opprettOppgave(String innsendingId, String fnr, Soknad soknad, String saksId, String journalpostId) {
        String behandlendeEnhet = behandlendeEnhetConsumer.hentBehandlendeEnhet(fnr, soknad.getSoknadstype());
        String oppgaveId = oppgavebehandlingConsumer
                .opprettOppgave(fnr, behandlendeEnhet, saksId, journalpostId, soknad.lagBeskrivelse(), soknad.getSoknadstype());
        innsendingDAO.oppdaterOppgaveId(innsendingId, oppgaveId);
    }

    public String opprettJournalpost(String innsendingId, Soknad soknad, String saksId) {
        String journalpostId = behandleJournalConsumer.opprettJournalpost(soknad, saksId);
        innsendingDAO.oppdaterJournalpostId(innsendingId, journalpostId);
        return journalpostId;
    }

    public String opprettSak(String innsendingId, String fnr) {
        String saksId = behandleSakConsumer.opprettSak(fnr);
        innsendingDAO.oppdaterSaksId(innsendingId, saksId);
        return saksId;
    }

    public Soknad opprettSoknad(Sykepengesoknad sykepengesoknad, String fnr) {
        return Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr));
    }

    private void tellInnsendingBehandlet() {
        registry.counter(
                "syfogsak.innsending.behandlet",
                Tags.of("type", "info", "help", "Antall ferdigbehandlede innsendinger."))
                .increment();
    }

    private void tellInnsendingFeilet() {
        registry.counter(
                "syfogsak.innsending.feilet",
                Tags.of(
                        "type", "info",
                        "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
                ))
                .increment();
    }
}