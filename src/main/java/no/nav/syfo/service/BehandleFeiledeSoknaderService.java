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
        if (innsendingDAO.hentFeiletInnsendingForSoknad(sykepengesoknad.getId()).isPresent()) {
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
            } catch (RuntimeException e) {
                log.error("Feilet ved rebehandling av innsending med id: {}", innsending.getInnsendingsId());
            }
        }
    }

    private void fortsetterBehandlingFraOppgave(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String fnr = aktorConsumer.finnFnr(innsending.getAktorId());
        String innsendingId = innsending.getInnsendingsId();

        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsendingId)
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .saksId(innsending.getSaksId())
                .journalpostId(innsending.getJournalpostId())
                .build();


        saksbehandlingsService.opprettOppgave(
                innsendingId,
                fnr,
                saksbehandlingsService.opprettSoknad(sykepengesoknad, fnr),
                fullfortInnsending.getSaksId(),
                fullfortInnsending.getJournalpostId()
        );

        innsendingDAO.settBehandlet(fullfortInnsending.getInnsendingsId());
        innsendingDAO.fjernFeiletInnsending(fullfortInnsending.getInnsendingsId());
    }

    private void fortsettBehandlingFraJournalpost(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        String innsendingsId = innsending.getInnsendingsId();
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsendingsId)
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .saksId(innsending.getSaksId())
                .build();

        Soknad soknad = saksbehandlingsService
                .opprettSoknad(
                        sykepengesoknad,
                        aktorConsumer.finnFnr(innsending.getAktorId())
                );

        fullfortInnsending.setJournalpostId(saksbehandlingsService
                .opprettJournalpost(
                        innsendingsId,
                        soknad,
                        innsending.getSaksId()));

        fortsetterBehandlingFraOppgave(fullfortInnsending, sykepengesoknad);
    }

    private void fortsettBehandlingFraSaksId(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .aktorId(innsending.getAktorId())
                .build();

        fullfortInnsending
                .setSaksId(saksbehandlingsService
                        .opprettSak(
                                innsending.getInnsendingsId(),
                                aktorConsumer.finnFnr(innsending.getAktorId())
                        )
                );

        fortsettBehandlingFraJournalpost(fullfortInnsending, sykepengesoknad);
    }

    private void fortsettBehandlingFraAktor(Innsending innsending, Sykepengesoknad sykepengesoknad) {
        Innsending fullfortInnsending = Innsending.builder()
                .innsendingsId(innsending.getInnsendingsId())
                .ressursId(innsending.getRessursId())
                .build();

        fullfortInnsending.setAktorId(aktorConsumer.finnFnr(sykepengesoknad.getAktorId()));
        innsendingDAO.oppdaterAktorId(fullfortInnsending.getInnsendingsId(), sykepengesoknad.getAktorId());
        fortsettBehandlingFraSaksId(fullfortInnsending, sykepengesoknad);
    }

}
