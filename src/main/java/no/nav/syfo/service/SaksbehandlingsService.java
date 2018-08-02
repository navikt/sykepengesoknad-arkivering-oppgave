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
    private final PersonConsumer personConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final InnsendingDAO innsendingDAO;

    @Inject
    public SaksbehandlingsService(
            BehandleSakConsumer behandleSakConsumer,
            OppgavebehandlingConsumer oppgavebehandlingConsumer,
            BehandleJournalConsumer behandleJournalConsumer,
            AktørConsumer aktørConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            InnsendingDAO innsendingDAO,
            PersonConsumer personConsumer){
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktørConsumer = aktørConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.innsendingDAO = innsendingDAO;
        this.personConsumer = personConsumer;
    }

    public void behandleSoknad(Soknad soknad) {
        log.info("Behandler søknad med id: {}", soknad.soknadsId);

        String fnr = aktørConsumer.finnFnr(soknad.aktørId);
        log.info("fant fnr " + fnr + " for aktorid " + soknad.getAktørId());
        String saksId = behandleSakConsumer.opprettSak(fnr);

        soknad.setFnr(fnr);
        soknad.setNavn(personConsumer.finnBrukerPersonnavnByFnr(fnr));

        String journalPostId = behandleJournalConsumer.opprettJournalpost(soknad, saksId);
        String behandlendeEnhet = behandlendeEnhetConsumer.hentBehandlendeEnhet(fnr, soknad.soknadstype);
        String oppgaveId = oppgavebehandlingConsumer.opprettOppgave(fnr, behandlendeEnhet, saksId, journalPostId, soknad.lagBeskrivelse(), soknad.soknadstype);

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