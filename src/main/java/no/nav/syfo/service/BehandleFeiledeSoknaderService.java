package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.AktorConsumer;
import no.nav.syfo.consumer.ws.PersonConsumer;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;

@Slf4j
@Component
public class BehandleFeiledeSoknaderService {

    private final InnsendingDAO innsendingDAO;
    private final AktorConsumer aktorConsumer;
    private final PersonConsumer personConsumer;

    @Inject
    public BehandleFeiledeSoknaderService(InnsendingDAO innsendingDAO, AktorConsumer aktorConsumer, PersonConsumer personConsumer) {
        this.innsendingDAO = innsendingDAO;
        this.aktorConsumer = aktorConsumer;
        this.personConsumer = personConsumer;
    }

    public String behandleFeiletSoknad(Sykepengesoknad sykepengesoknad, Innsending innsending) {
        Innsending innsending1 = fullforBehandling(innsending, sykepengesoknad);
        return null;
    }

    private Innsending fullforBehandling(Innsending innsending, Sykepengesoknad soknad) {
        try {
            if (innsending.getAktorId() == null) {
                return fortsettBehandlingFraAktor(innsending, soknad);
            } else if (innsending.getSaksId() == null) {
                return fortsettBehandlingFraSaks(innsending, soknad);
            } else if (innsending.getJournalpostId() == null) {
                return fortsettBehandlingFraJournalpost(innsending, soknad);
            } else if (innsending.getOppgaveId() == null) {
                return fortsetterBehandlingFraOppgave(innsending, soknad);
            }
        } catch (RuntimeException e) {
            log.error("Feilet ved rebehandling av innsending med id: {}", innsending.getInnsendingsId());
        }

        return innsending;
    }

    private Innsending fortsetterBehandlingFraOppgave(Innsending innsending, Sykepengesoknad soknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .saksId(innsending.getSaksId())
                .journalpostId(innsending.getJournalpostId())
                .build();

        fullfortInnsending.setOppgaveId(opprettOppgave(fullfortInnsending));

        return fullfortInnsending;
    }

    private Innsending fortsettBehandlingFraJournalpost(Innsending innsending, Sykepengesoknad soknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .saksId(innsending.getSaksId())
                .build();

        fullfortInnsending.setJournalpostId(opprettJournalpost(fullfortInnsending));
        fullfortInnsending.setOppgaveId(opprettOppgave(fullfortInnsending));

        return fullfortInnsending;
    }

    private Innsending fortsettBehandlingFraSaks(Innsending innsending, Sykepengesoknad soknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .build();

        fullfortInnsending.setSaksId(opprettSak(fullfortInnsending));
        fullfortInnsending.setJournalpostId(opprettJournalpost(fullfortInnsending));
        fullfortInnsending.setOppgaveId(opprettOppgave(fullfortInnsending));

        return fullfortInnsending;
    }

    private Innsending fortsettBehandlingFraAktor(Innsending innsending, Sykepengesoknad soknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .build();

        fullfortInnsending.setAktorId(finnAktorId(soknad.getAktorId()));
        fullfortInnsending.setSaksId(opprettSak(fullfortInnsending));
        fullfortInnsending.setJournalpostId(opprettJournalpost(fullfortInnsending));
        fullfortInnsending.setOppgaveId(opprettOppgave(fullfortInnsending));

        return fullfortInnsending;
    }

    private String finnAktorId(String aktorId) {
        return aktorConsumer.finnFnr(aktorId);
    }

    private Soknad opprettSoknad(Sykepengesoknad sykepengesoknad, String innsendingId, String fnr) {
        Soknad soknad = Soknad.lagSoknad(sykepengesoknad, fnr, personConsumer.finnBrukerPersonnavnByFnr(fnr));
        innsendingDAO.oppdaterAktorId(innsendingId, soknad.getAktorId());
        return soknad;
    }

    private String opprettSak(Innsending innsending) {
        throw new NotImplementedException();
    }

    private String opprettJournalpost(Innsending innsending) {
        throw new NotImplementedException();
    }

    private String opprettOppgave(Innsending innsending) {
        throw new NotImplementedException();
    }
}
