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
    private static final String AUTOMATISK_JOBB = "9999";

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
        byte[] pdf;

        try {
            pdf = pdfRestController.getPDF(soknad, hentPDFTemplateEtterSoknadstype(soknad.getSoknadstype()));
        } catch (RuntimeException e) {
            String feilmelding = "Kunne ikke generere PDF for søknad med id: " + soknad.getSoknadsId() + " og saks id: " + saksId;
            log.error(feilmelding, e);
            throw new RuntimeException(feilmelding, e);
        }

        return journalforSoknad(soknad, saksId, pdf);
    }

    private String journalforSoknad(Soknad soknad, String saksId, byte[] pdf) {
        try {
            return behandleJournalV2.journalfoerInngaaendeHenvendelse(
                    new WSJournalfoerInngaaendeHenvendelseRequest()
                            .withApplikasjonsID("SYFOGSAK")
                            .withJournalpost(new WSJournalpost()
                                    .withDokumentDato(LocalDateTime.now())
                                    .withJournalfoerendeEnhetREF(AUTOMATISK_JOBB)
                                    .withKanal(new WSKommunikasjonskanaler().withValue("NAV_NO"))
                                    .withSignatur(new WSSignatur().withSignert(true))
                                    .withArkivtema(new WSArkivtemaer().withValue("SYK"))
                                    .withForBruker(new WSPerson().withIdent(new WSNorskIdent().withIdent(soknad.getFnr())))
                                    .withOpprettetAvNavn("Automatisk jobb")
                                    .withInnhold(getJournalPostInnholdNavn(soknad.getSoknadstype()))
                                    .withEksternPart(new WSEksternPart()
                                            .withNavn(personConsumer.finnBrukerPersonnavnByFnr(soknad.getFnr()))
                                            .withEksternAktoer(new WSPerson().withIdent(new WSNorskIdent().withIdent(soknad.getFnr()))))
                                    .withGjelderSak(new WSSak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                    .withMottattDato(LocalDateTime.now())
                                    .withDokumentinfoRelasjon(
                                            new WSDokumentinfoRelasjon()
                                                    .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                    .withJournalfoertDokument(new WSJournalfoertDokumentInfo()
                                                            .withBegrensetPartsInnsyn(false)
                                                            .withDokumentType(new WSDokumenttyper().withValue(utledDokumenttype(soknad)))
                                                            .withSensitivitet(true)
                                                            .withTittel(getJornalfoertDokumentTittel(soknad))
                                                            .withKategorikode(utledDokumenttype(soknad))
                                                            .withBeskriverInnhold(
                                                                    new WSStrukturertInnhold()
                                                                            .withFilnavn(getWSStruktureltInnholdFilnavn(soknad))
                                                                            .withFiltype(new WSArkivfiltyper().withValue("PDFA"))
                                                                            .withInnhold(pdf)
                                                                            .withVariantformat(new WSVariantformater().withValue("ARKIV"))
                                                            ))
                                    ))
            ).getJournalpostId();
        } catch (RuntimeException e) {
            String feilmelding = "Kunne ikke behandle journalpost for søknad med id " + soknad.getSoknadsId() + " og saks id: " + saksId;
            log.error(feilmelding, e);
            throw new RuntimeException(feilmelding, e);
        }
    }

    private String utledDokumenttype(Soknad soknad) {
        switch (soknad.getSoknadstype()) {
            case SELVSTENDIGE_OG_FRILANSERE: return "søknadsyk";
            case OPPHOLD_UTLAND: return "søknadsykutland";
            default: {
                log.error("Soknadstypen {} er ikke støttet ved journalføring", soknad.getSoknadstype().name());
                throw new RuntimeException("Soknadstypen " + soknad.getSoknadstype().name() + " er ikke støttet ved journalføring");
            }
        }
    }

    private String getJornalfoertDokumentTittel(Soknad soknad) {
        if (soknad.getSoknadstype() == OPPHOLD_UTLAND) {
            return "Søknad om å beholde sykepenger utenfor Norge";
        }
        return "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + soknad.getFom().format(norskDato) + " til " + soknad.getTom().format(norskDato);
    }

    private String getWSStruktureltInnholdFilnavn(Soknad soknad) {
        if (soknad.getSoknadstype() == OPPHOLD_UTLAND) {
            return "soknad-" + soknad.getInnsendtDato().format(norskDato);
        }
        return "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + soknad.getFom().format(norskDato) + " til " + soknad.getTom().format(norskDato);
    }

    private String getJournalPostInnholdNavn(Soknadstype soknadstype) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return "Søknad om å beholde sykepenger utenfor Norge";
        }
        return "Søknad om sykepenger";
    }

    private PDFTemplate hentPDFTemplateEtterSoknadstype(Soknadstype soknadstype) {
        if (soknadstype == OPPHOLD_UTLAND) {
            return SYKEPENGERUTLAND;
        }
        return SELVSTENDIGNAERINGSDRIVENDE;
    }
}
