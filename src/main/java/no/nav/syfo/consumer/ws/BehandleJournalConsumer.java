package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Soknad;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSDokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static java.lang.String.format;

@Component
@Slf4j
public class BehandleJournalConsumer {

    private BehandleJournalV2 behandleJournalV2;
    private PersonConsumer personConsumer;

    private static final String GOSYS = "FS22";

    @Inject
    public BehandleJournalConsumer(BehandleJournalV2 behandleJournalV2, PersonConsumer personConsumer) {
        this.behandleJournalV2 = behandleJournalV2;
        this.personConsumer = personConsumer;
    }

    public String opprettOppgave(String fnr, String saksId, Soknad soknad) {

        try {
            byte[] pdf = Files.readAllBytes(Paths.get(ClassLoader.getSystemResource("eksempelsoknad.pdf").toURI()));
            String journalpostId = behandleJournalV2.journalfoerInngaaendeHenvendelse(
                    new WSJournalfoerInngaaendeHenvendelseRequest()
                            .withApplikasjonsID("SYFOSOKNAD")
                            .withJournalpost(new WSJournalpost()
                                    .withDokumentDato(LocalDateTime.now())
                                    .withJournalfoerendeEnhetREF(GOSYS)
                                    .withKanal(new WSKommunikasjonskanaler().withValue("NAV_NO"))
                                    .withSignatur(new WSSignatur().withSignert(true))
                                    .withArkivtema(new WSArkivtemaer().withValue("SYK"))
                                    .withForBruker(new no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.WSPerson().withIdent(new WSNorskIdent().withIdent(fnr)))
                                    .withOpprettetAvNavn("Syfosoknad")
                                    .withInnhold("Søknad om sykepenger")
                                    .withEksternPart(new WSEksternPart()
                                            .withNavn(personConsumer.finnBrukerPersonnavnByFnr(fnr))
                                            .withEksternAktoer(new WSPerson().withIdent(new WSNorskIdent().withIdent(fnr))))
                                    .withGjelderSak(new WSSak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                    .withMottattDato(LocalDateTime.now())
                                    .withDokumentinfoRelasjon(
                                            new WSDokumentinfoRelasjon()
                                                    .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                    .withJournalfoertDokument(new WSJournalfoertDokumentInfo()
                                                            .withBegrensetPartsInnsyn(false)
                                                            .withDokumentType(new WSDokumenttyper().withValue("ES"))
                                                            .withSensitivitet(true)
                                                            .withTittel(format("Søknad om sykepenger fra Selvstendig / Frilanser")) //TODO: Utled perioder i tittel
                                                            .withKategorikode("ES")
                                                            .withBeskriverInnhold(
                                                                    new WSStrukturertInnhold()
                                                                            .withFilnavn(format("filnavn")) //TODO: Utled perioder i tittel
                                                                            .withFiltype(new WSArkivfiltyper().withValue("PDF"))
                                                                            .withInnhold(pdf) //TODO: Generer PDF
                                                                            .withVariantformat(new WSVariantformater().withValue("ARKIV"))
                                                            ))

                                    ))
            ).getJournalpostId();
            log.info("Opprettet journalpost: {} på sak: {}", journalpostId, saksId);
            return journalpostId;
        } catch (IOException | URISyntaxException e) {
            log.error("Klarte ikke lese pdf fra disk");
            throw new RuntimeException("Feil ved oppretting av journalpost", e);
        }
    }
}
