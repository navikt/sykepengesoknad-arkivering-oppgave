package no.nav.syfo.consumer.ws;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.controller.PDFRestController;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.PDFTemplate;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.*;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSDokumentinfoRelasjon;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalfoertDokumentInfo;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalpost;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.LocalDateTime;

import static no.nav.syfo.domain.dto.PDFTemplate.SELVSTENDIGNAERINGSDRIVENDE;
import static no.nav.syfo.domain.dto.PDFTemplate.SYKEPENGERUTLAND;
import static no.nav.syfo.domain.dto.Soknadstype.OPPHOLD_UTLAND;
import static no.nav.syfo.util.DatoUtil.norskDato;

@Component
@Slf4j
public class BehandleJournalConsumer {

    private BehandleJournalV2 behandleJournalV2;
    private PersonConsumer personConsumer;
    private PDFRestController pdfRestController;

    private static final String GOSYS = "FS22";

    @Inject
    public BehandleJournalConsumer(
            BehandleJournalV2 behandleJournalV2,
            PersonConsumer personConsumer,
            PDFRestController pdfRestController) {
        this.behandleJournalV2 = behandleJournalV2;
        this.personConsumer = personConsumer;
        this.pdfRestController = pdfRestController;
    }

    public String opprettJournalpost(Soknad soknad, String saksId) {
        try {
            byte[] pdf = pdfRestController.getPDF(soknad, hentPDFTemplateEtterSoknadstype(soknad.soknadstype));
            return journalforSoknad(soknad, saksId, pdf);
        } catch (RuntimeException e) {
            String feilmelding = "Kunne ikke generere PDF for søknad med id: " + soknad.getSoknadsId() + " og saks id: " + saksId;
            log.error(feilmelding, e);
            throw new RuntimeException(feilmelding, e);
        }
    }

    private String journalforSoknad(Soknad soknad, String saksId, byte[] pdf) {
        String norskFom = soknad.fom.format(norskDato);
        String norskTom = soknad.tom.format(norskDato);
        String norskInnsendtDato = soknad.innsendtDato.format(norskDato);
        try {
            String journalpostId = behandleJournalV2.journalfoerInngaaendeHenvendelse(
                    new WSJournalfoerInngaaendeHenvendelseRequest()
                            .withApplikasjonsID("SYFOGSAK")
                            .withJournalpost(new WSJournalpost()
                                    .withDokumentDato(LocalDateTime.now())
                                    .withJournalfoerendeEnhetREF(GOSYS)
                                    .withKanal(new WSKommunikasjonskanaler().withValue("NAV_NO"))
                                    .withSignatur(new WSSignatur().withSignert(true))
                                    .withArkivtema(new WSArkivtemaer().withValue("SYK"))
                                    .withForBruker(new WSPerson().withIdent(new WSNorskIdent().withIdent(soknad.fnr)))
                                    .withOpprettetAvNavn("Syfogsak")
                                    .withInnhold(getJournalPostInnholdNavn(soknad.soknadstype))
                                    .withEksternPart(new WSEksternPart()
                                            .withNavn(personConsumer.finnBrukerPersonnavnByFnr(soknad.fnr))
                                            .withEksternAktoer(new WSPerson().withIdent(new WSNorskIdent().withIdent(soknad.fnr))))
                                    .withGjelderSak(new WSSak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                    .withMottattDato(LocalDateTime.now())
                                    .withDokumentinfoRelasjon(
                                            new WSDokumentinfoRelasjon()
                                                    .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                    .withJournalfoertDokument(new WSJournalfoertDokumentInfo()
                                                            .withBegrensetPartsInnsyn(false)
                                                            .withDokumentType(new WSDokumenttyper().withValue("ES"))
                                                            .withSensitivitet(true)
                                                            .withTittel(getJornalfoertDokumentTittel(soknad.soknadstype, norskFom, norskTom))
                                                            .withKategorikode("ES")
                                                            .withBeskriverInnhold(
                                                                    new WSStrukturertInnhold()
                                                                            .withFilnavn(getWSStruktureltInnholdFilnavn(soknad.soknadstype, norskFom, norskTom, norskInnsendtDato))
                                                                            .withFiltype(new WSArkivfiltyper().withValue("PDF"))
                                                                            .withInnhold(pdf)
                                                                            .withVariantformat(new WSVariantformater().withValue("ARKIV"))
                                                            ))
                                    ))
            ).getJournalpostId();
            return journalpostId;
        } catch (RuntimeException e) {
            String feilmelding = "Kunne ikke behandle journalpost for søknad med id " + soknad.getSoknadsId() + " og saks id: " + saksId;
            log.error(feilmelding, e);
            throw new RuntimeException(feilmelding, e);
        }
    }

    String getJornalfoertDokumentTittel(Soknadstype soknadstype, String norskFom, String norskTom) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return "Søknad om å beholde sykepenger utenfor Norge";
        }
        return "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + norskFom + " til " + norskTom;
    }

    String getWSStruktureltInnholdFilnavn(Soknadstype soknadstype, String norskFom, String norskTom, String norskInnsendtDato) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return "soknad-" + norskInnsendtDato;
        }
        return "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + norskFom + " til " + norskTom;
    }

    String getJournalPostInnholdNavn(Soknadstype soknadstype) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return "Søknad om å beholde sykepenger utenfor Norge";
        }
        return "Søknad om sykepenger";
    }

    PDFTemplate hentPDFTemplateEtterSoknadstype(Soknadstype soknadstype) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return SYKEPENGERUTLAND;
        }
        return SELVSTENDIGNAERINGSDRIVENDE;
    }
}
