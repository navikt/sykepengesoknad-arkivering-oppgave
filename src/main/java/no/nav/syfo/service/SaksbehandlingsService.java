package no.nav.syfo.service;

import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Soknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
public class SaksbehandlingsService {

    private BehandleSakConsumer behandleSakConsumer;
    private OppgavebehandlingConsumer oppgavebehandlingConsumer;
    private BehandleJournalConsumer behandleJournalConsumer;
    private AktørConsumer aktørConsumer;
    private BehandlendeEnhetConsumer behandlendeEnhetConsumer;

    @Inject
    public SaksbehandlingsService(BehandleSakConsumer behandleSakConsumer, OppgavebehandlingConsumer oppgavebehandlingConsumer, BehandleJournalConsumer behandleJournalConsumer, AktørConsumer aktørConsumer, BehandlendeEnhetConsumer behandlendeEnhetConsumer) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktørConsumer = aktørConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
    }

    public void behandleSoknad(Soknad soknad) {
        String fnr = aktørConsumer.finnFnr(soknad.aktørId);

        String saksId = behandleSakConsumer.opprettSak(fnr);
        String journalPostId = behandleJournalConsumer.opprettOppgave(fnr, saksId, soknad);
        String behandlendeEnhet = behandlendeEnhetConsumer.hentBehandlendeEnhet(fnr);
        String oppgaveId = oppgavebehandlingConsumer.opprettOppgave(fnr, behandlendeEnhet, saksId, journalPostId);
    }
}