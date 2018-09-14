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
        String aktorId = innsending.getAktorId();
        String saksId = innsending.getSaksId();
        String journalpostId = innsending.getJournalpostId();

        try {
            if (aktorId == null) {
                aktorId = sykepengesoknad.getAktorId();
                innsendingDAO.oppdaterAktorId(innsendingsId, aktorId);
            }

            if (saksId == null) {
                saksId = behandleFraSaksId(innsendingsId, aktorId);
            }
            if (journalpostId == null) {
                journalpostId = behandleFraJournalpost(innsendingsId, saksId, sykepengesoknad);
            }
            if (innsending.getOppgaveId() == null) {
                behandleFraOppgave(innsendingsId, saksId, journalpostId, sykepengesoknad);
            }

            innsendingDAO.settBehandlet(innsendingsId);
            innsendingDAO.fjernFeiletInnsending(innsendingsId);

            log.info("Fullført rebehandling av innsending med id: {} av soknad med id: {}",
                    innsendingsId, sykepengesoknad.getId());
        } catch (RuntimeException e) {
            log.error("Feilet ved rebehandling av innsending med id: {}", innsendingsId);
        }
    }

    private void behandleFraOppgave(
            String innsendingsId,
            String saksId,
            String journalpostId,
            Sykepengesoknad sykepengesoknad) {
        String fnr = aktorConsumer.finnFnr(sykepengesoknad.getAktorId());

        saksbehandlingsService.opprettOppgave(
                innsendingsId,
                fnr,
                saksbehandlingsService.opprettSoknad(sykepengesoknad, fnr),
                saksId,
                journalpostId
        );
    }

    private String behandleFraJournalpost(String innsendingsId, String saksId, Sykepengesoknad sykepengesoknad) {
        Soknad soknad = saksbehandlingsService
                .opprettSoknad(
                        sykepengesoknad,
                        aktorConsumer.finnFnr(sykepengesoknad.getAktorId())
                );

        return saksbehandlingsService
                .opprettJournalpost(
                        innsendingsId,
                        soknad,
                        saksId);
    }

    private String behandleFraSaksId(String innsendingsId, String aktorId) {
        return saksbehandlingsService
                .opprettSak(
                        innsendingsId,
                        aktorConsumer.finnFnr(aktorId)
                );
    }
}
