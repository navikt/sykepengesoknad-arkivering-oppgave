package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.*;
import no.nav.syfo.domain.Innsending;
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
    private final AktorConsumer aktorConsumer;
    private final PersonConsumer personConsumer;
    private final BehandlendeEnhetConsumer behandlendeEnhetConsumer;
    private final InnsendingDAO innsendingDAO;

    @Inject
    public SaksbehandlingsService(
            BehandleSakConsumer behandleSakConsumer,
            OppgavebehandlingConsumer oppgavebehandlingConsumer,
            BehandleJournalConsumer behandleJournalConsumer,
            AktorConsumer aktorConsumer,
            BehandlendeEnhetConsumer behandlendeEnhetConsumer,
            InnsendingDAO innsendingDAO,
            PersonConsumer personConsumer) {
        this.behandleSakConsumer = behandleSakConsumer;
        this.oppgavebehandlingConsumer = oppgavebehandlingConsumer;
        this.behandleJournalConsumer = behandleJournalConsumer;
        this.aktorConsumer = aktorConsumer;
        this.behandlendeEnhetConsumer = behandlendeEnhetConsumer;
        this.innsendingDAO = innsendingDAO;
        this.personConsumer = personConsumer;
    }

    public String behandleSoknad(Sykepengesoknad sykepengesoknad) {
        Innsending innsending = innsendingDAO.finnInnsendingForSykepengesøknad(sykepengesoknad.getId());

        if (innsending != null) {
            log.info("Innsending for sykepengesøknad {} alerede opprettet med id {}.",
                    sykepengesoknad.getId(),
                    innsending.getInnsendingsId()
            );
            return innsending.getInnsendingsId();
        }

        String uuid = innsendingDAO.opprettInnsending(sykepengesoknad.getId());

        try {
            String fnr = aktorConsumer.finnFnr(sykepengesoknad.getAktorId());

            Soknad soknad = Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr));
            innsendingDAO.oppdaterAktorId(uuid, soknad.aktorId);

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

        return uuid;
    }
}