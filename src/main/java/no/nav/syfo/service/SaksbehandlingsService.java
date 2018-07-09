package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
public class SaksbehandlingsService {

    private final BehandleSakConsumer behandleSakConsumer;
    private final OppgavebehandlingConsumer oppgavebehandlingConsumer;
    private final BehandleJournalConsumer behandleJournalConsumer;
    private final AktørConsumer aktørConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final InnsendingDAO innsendingDAO;

    @Inject
    public SaksbehandlingsService(BehandleSakConsumer behandleSakConsumer, OppgavebehandlingConsumer oppgavebehandlingConsumer, BehandleJournalConsumer behandleJournalConsumer, AktørConsumer aktørConsumer, BehandlendeEnhetConsumer behandlendeEnhetConsumer, InnsendingDAO innsendingDAO) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktørConsumer = aktørConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.innsendingDAO = innsendingDAO;
    }

    public void behandleSoknad(Soknad soknad) {
        log.info("Behandler søknad med id: {}", soknad.soknadsId);
        String fnr = aktørConsumer.finnFnr(soknad.aktørId);
        String saksId = behandleSakConsumer.opprettSak(fnr);
        String journalPostId = behandleJournalConsumer.opprettJournalpost(fnr, saksId, soknad.fom, soknad.tom);
        String behandlendeEnhet = behandlendeEnhetConsumer.hentBehandlendeEnhet(fnr);
        String oppgaveId = oppgavebehandlingConsumer.opprettOppgave(fnr, behandlendeEnhet, saksId, journalPostId, soknad.lagBeskrivelse());

        innsendingDAO.lagreInnsending(Innsending.builder()
                .innsendingsId(UUID.randomUUID().toString())
                .aktørId(soknad.aktørId)
                .ressursId(soknad.soknadsId)
                .saksId(saksId)
                .journalpostId(journalPostId)
                .oppgaveId(oppgaveId)
                .behandlet(LocalDate.now())
                .build());
    }
}