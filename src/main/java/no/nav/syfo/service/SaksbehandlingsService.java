package no.nav.syfo.service;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class SaksbehandlingsService {

    private final BehandleSakConsumer behandleSakConsumer;
    private final OppgavebehandlingConsumer oppgavebehandlingConsumer;
    private final BehandleJournalConsumer behandleJournalConsumer;
    private final BehandlendeEnhetService behandlendeEnhetService;
    private final AktorConsumer aktorConsumer;
    private final PersonConsumer personConsumer;
    private final InnsendingDAO innsendingDAO;
    private final MeterRegistry registry;

    @Inject
    public SaksbehandlingsService(
            BehandleSakConsumer behandleSakConsumer,
            OppgavebehandlingConsumer oppgavebehandlingConsumer,
            BehandleJournalConsumer behandleJournalConsumer,
            BehandlendeEnhetService behandlendeEnhetService, AktorConsumer aktorConsumer,
            InnsendingDAO innsendingDAO,
            PersonConsumer personConsumer,
            MeterRegistry registry) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.behandlendeEnhetService = behandlendeEnhetService;
        this.aktorConsumer = aktorConsumer;
        this.innsendingDAO = innsendingDAO;
        this.personConsumer = personConsumer;
        this.registry = registry;
    }

    private boolean ikkeSendtTilNav(Sykepengesoknad sykepengesoknad) {
        return !("SENDT".equals(sykepengesoknad.getStatus()) && sykepengesoknad.getSendtNav() != null);
    }

    private boolean ettersendtTilArbeidsgiver(Sykepengesoknad sykepengesoknad) {
        return sykepengesoknad.getSendtArbeidsgiver() != null
                && sykepengesoknad.getSendtNav().isBefore(sykepengesoknad.getSendtArbeidsgiver());
    }

    public void behandleSoknad(Sykepengesoknad sykepengesoknad) {
        if (ikkeSendtTilNav(sykepengesoknad) || ettersendtTilArbeidsgiver(sykepengesoknad)) {
            return;
        }

        String sykepengesoknadId = sykepengesoknad.getId();
        String aktorId = sykepengesoknad.getAktorId();
        Innsending innsending = innsendingDAO.finnInnsendingForSykepengesoknad(sykepengesoknadId);

        if (innsending != null) {
            String innsendingId = innsending.getInnsendingsId();
            log.warn("Innsending for sykepengesøknad {} allerede opprettet med id {}.",
                    sykepengesoknadId,
                    innsendingId
            );
            return;
        }

        String innsendingId = innsendingDAO.opprettInnsending(sykepengesoknadId, aktorId);

        try {
            String fnr = aktorConsumer.finnFnr(aktorId);
            Soknad soknad = opprettSoknad(sykepengesoknad, fnr);
            String saksId = opprettSak(innsendingId, fnr);
            String journalpostId = opprettJournalpost(innsendingId, soknad, saksId);

            opprettOppgave(innsendingId, fnr, soknad, saksId, journalpostId);

            innsendingDAO.settBehandlet(innsendingId);

            tellInnsendingBehandlet(sykepengesoknad.getSoknadstype());
            log.info("Søknad med id {} er behandlet i innsending med id {}",
                    soknad.getSoknadsId(),
                    innsendingId
            );
        } catch (Exception e) {
            innsendingDAO.leggTilFeiletInnsending(innsendingId);

            tellInnsendingFeilet(sykepengesoknad.getSoknadstype());
            log.error("Kunne ikke fullføre innsending av søknad med innsending id: {} og sykepengesøknad id: {}",
                    innsendingId,
                    sykepengesoknadId,
                    e);
        }
    }

    void opprettOppgave(String innsendingId, String fnr, Soknad soknad, String saksId, String journalpostId) {
        String behandlendeEnhet = behandlendeEnhetService.hentBehandlendeEnhet(fnr, soknad.getSoknadstype());
        String oppgaveId = oppgavebehandlingConsumer
                .opprettOppgave(fnr, behandlendeEnhet, saksId, journalpostId, soknad);
        innsendingDAO.oppdaterOppgaveId(innsendingId, oppgaveId);
    }

    String opprettJournalpost(String innsendingId, Soknad soknad, String saksId) {
        String journalpostId = behandleJournalConsumer.opprettJournalpost(soknad, saksId);
        innsendingDAO.oppdaterJournalpostId(innsendingId, journalpostId);
        return journalpostId;
    }

    String opprettSak(String innsendingId, String fnr) {
        String saksId = behandleSakConsumer.opprettSak(fnr);
        innsendingDAO.oppdaterSaksId(innsendingId, saksId);
        return saksId;
    }

    Soknad opprettSoknad(Sykepengesoknad sykepengesoknad, String fnr) {
        return Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr));
    }

    private void tellInnsendingBehandlet(Soknadstype soknadstype) {
        registry.counter(
                "syfogsak.innsending.behandlet",
                Tags.of(
                        "type", "info",
                        "soknadstype", soknadstype.name(),
                        "help", "Antall ferdigbehandlede innsendinger."
                ))
                .increment();
    }

    private void tellInnsendingFeilet(Soknadstype soknadstype) {
        registry.counter(
                "syfogsak.innsending.feilet",
                Tags.of(
                        "type", "info",
                        "soknadstype", soknadstype.name(),
                        "help", "Antall innsendinger hvor feil mot baksystemer gjorde at behandling ikke kunne fullføres."
                ))
                .increment();
    }
}
