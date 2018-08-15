package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

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
            PersonConsumer personConsumer) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktørConsumer = aktørConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.innsendingDAO = innsendingDAO;
        this.personConsumer = personConsumer;
    }

    public void behandleSoknad(Sykepengesoknad sykepengesoknad) {
        String uuid = innsendingDAO.opprettInnsending();

        try {
            String fnr = aktørConsumer.finnFnr(sykepengesoknad.getAktorId());

            Soknad soknad = Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr));
            log.info("Behandler søknad med id: {}", soknad.soknadsId);
            innsendingDAO.oppdaterRessursIdOgAktorId(uuid, soknad.soknadsId, soknad.aktørId);

            String saksId = behandleSakConsumer.opprettSak(fnr);
            innsendingDAO.oppdaterSaksId(uuid, saksId);

            String journalpostId = behandleJournalConsumer.opprettJournalpost(soknad, saksId);
            innsendingDAO.oppdaterJournalpostId(uuid, journalpostId);

            String behandlendeEnhet = behandlendeEnhetConsumer.hentBehandlendeEnhet(fnr, soknad.soknadstype);
            String oppgaveId = oppgavebehandlingConsumer
                    .opprettOppgave(fnr, behandlendeEnhet, saksId, journalpostId, soknad.lagBeskrivelse(), soknad.soknadstype);
            innsendingDAO.oppdaterOppgaveId(uuid, oppgaveId);

            innsendingDAO.settBehandlet(uuid);

        } catch (Exception e) {
            log.error("Kunne ikke fullføre innsending av søknad med uuid: {}.", uuid, e);
            innsendingDAO.leggTilFeiletInnsending(uuid);
        }
    }
}