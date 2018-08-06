package no.nav.syfo.mock;

import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(value = "mockWS", havingValue = "true")
public class BehandleJournalMock implements BehandleJournalV2 {

    public WSArkiverUstrukturertKravResponse arkiverUstrukturertKrav(WSArkiverUstrukturertKravRequest wsArkiverUstrukturertKravRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSJournalfoerUtgaaendeHenvendelseResponse journalfoerUtgaaendeHenvendelse(WSJournalfoerUtgaaendeHenvendelseRequest wsJournalfoerUtgaaendeHenvendelseRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ferdigstillDokumentopplasting(WSFerdigstillDokumentopplastingRequest wsFerdigstillDokumentopplastingRequest) throws FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSJournalfoerNotatResponse journalfoerNotat(WSJournalfoerNotatRequest wsJournalfoerNotatRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSLagreVedleggPaaJournalpostResponse lagreVedleggPaaJournalpost(WSLagreVedleggPaaJournalpostRequest wsLagreVedleggPaaJournalpostRequest) throws LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public WSJournalfoerInngaaendeHenvendelseResponse journalfoerInngaaendeHenvendelse(WSJournalfoerInngaaendeHenvendelseRequest wsJournalfoerInngaaendeHenvendelseRequest) {
        throw new RuntimeException("Ikke implementert i mock");
    }

    public void ping() { }
}
