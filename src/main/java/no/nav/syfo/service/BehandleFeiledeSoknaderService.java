package no.nav.syfo.service;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.consumer.ws.AktorConsumer;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class BehandleFeiledeSoknaderService {

    private final InnsendingDAO innsendingDAO;
    private final AktorConsumer aktorConsumer;
    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public BehandleFeiledeSoknaderService(
            InnsendingDAO innsendingDAO,
            AktorConsumer aktorConsumer,
            SaksbehandlingsService saksbehandlingsService) {
        this.innsendingDAO = innsendingDAO;
        this.aktorConsumer = aktorConsumer;
        this.saksbehandlingsService = saksbehandlingsService;
    }

    public void behandleFeiletSoknad(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String innsendingsId = innsending.getInnsendingsId();

        try {
            if (innsending.getAktorId() == null) {
                fortsettBehandlingFraAktor(innsending, sykepengesoknad);
            } else if (innsending.getSaksId() == null) {
                fortsettBehandlingFraSaksId(innsending, sykepengesoknad);
            } else if (innsending.getJournalpostId() == null) {
                fortsettBehandlingFraJournalpost(innsending, sykepengesoknad);
            } else if (innsending.getOppgaveId() == null) {
                fortsetterBehandlingFraOppgave(innsending, sykepengesoknad);
            }
            innsendingDAO.settBehandlet(innsendingsId);
            innsendingDAO.fjernFeiletInnsending(innsendingsId);
            log.info("Fullført rebehandling av innsending med id: {} av soknad med id: {}",
                    innsendingsId, sykepengesoknad.getId());
        } catch (RuntimeException e) {
            log.error("Feilet ved rebehandling av innsending med id: {}", innsendingsId);
        }
    }

    private void fortsetterBehandlingFraOppgave(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String fnr = aktorConsumer.finnFnr(innsending.getAktorId());

        saksbehandlingsService.opprettOppgave(
                innsending.getInnsendingsId(),
                fnr,
                saksbehandlingsService.opprettSoknad(sykepengesoknad, fnr),
                innsending.getSaksId(),
                innsending.getJournalpostId()
        );
    }

    private void fortsettBehandlingFraJournalpost(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        Soknad soknad = saksbehandlingsService
                .opprettSoknad(
                        sykepengesoknad,
                        aktorConsumer.finnFnr(innsending.getAktorId())
                );

        String journalpostId = saksbehandlingsService
                .opprettJournalpost(
                        innsending.getInnsendingsId(),
                        soknad,
                        innsending.getSaksId());

        fortsetterBehandlingFraOppgave(innsending.toBuilder().journalpostId(journalpostId).build(), sykepengesoknad);
    }

    private void fortsettBehandlingFraSaksId(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String saksId = saksbehandlingsService
                .opprettSak(
                        innsending.getInnsendingsId(),
                        aktorConsumer.finnFnr(innsending.getAktorId())
                );

        fortsettBehandlingFraJournalpost(innsending.toBuilder().saksId(saksId).build(), sykepengesoknad);
    }

    private void fortsettBehandlingFraAktor(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String aktorId = sykepengesoknad.getAktorId();
        innsendingDAO.oppdaterAktorId(innsending.getInnsendingsId(), sykepengesoknad.getAktorId());

        fortsettBehandlingFraSaksId(innsending.toBuilder().aktorId(aktorId).build(), sykepengesoknad);
    }

}
