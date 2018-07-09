package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSDokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    public String opprettJournalpost(String fnr, String saksId, LocalDate fom, LocalDate tom) {
        String norskFom = fom.format(DateTimeFormatter.ofPattern("DD.MM.yyyy"));
        String norskTom = tom.format(DateTimeFormatter.ofPattern("DD.MM.yyyy"));

        String journalpostId = behandleJournalV2.journalfoerInngaaendeHenvendelse(
                new WSJournalfoerInngaaendeHenvendelseRequest()
                        .withApplikasjonsID("SYFOGSAK")
                        .withJournalpost(new WSJournalpost()
                                .withDokumentDato(LocalDateTime.now())
                                .withJournalfoerendeEnhetREF(GOSYS)
                                .withKanal(new WSKommunikasjonskanaler().withValue("NAV_NO"))
                                .withSignatur(new WSSignatur().withSignert(true))
                                .withArkivtema(new WSArkivtemaer().withValue("SYK"))
                                .withForBruker(new WSPerson().withIdent(new WSNorskIdent().withIdent(fnr)))
                                .withOpprettetAvNavn("Syfogsak")
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
                                                        .withTittel(format("Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + norskFom + " til " + norskTom))
                                                        .withKategorikode("ES")
                                                        .withBeskriverInnhold(
                                                                new WSStrukturertInnhold()
                                                                        .withFilnavn(format("soknad-" + norskFom + "-" + norskTom))
                                                                        .withFiltype(new WSArkivfiltyper().withValue("PDF"))
                                                                        .withInnhold(new byte[]{12, 46}) //TODO: Generer PDF
                                                                        .withVariantformat(new WSVariantformater().withValue("ARKIV"))
                                                        ))

                                ))
        ).getJournalpostId();
        log.info("Opprettet journalpost: {} på sak: {}", journalpostId, saksId);
        return journalpostId;
    }
}
