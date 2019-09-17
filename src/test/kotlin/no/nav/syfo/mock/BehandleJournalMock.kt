package no.nav.syfo.mock

import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2
import no.nav.tjeneste.virksomhet.behandlejournal.v2.FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet
import no.nav.tjeneste.virksomhet.behandlejournal.v2.LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(value = ["mockWS"], havingValue = "true")
class BehandleJournalMock : BehandleJournalV2 {

    override fun arkiverUstrukturertKrav(wsArkiverUstrukturertKravRequest: WSArkiverUstrukturertKravRequest): WSArkiverUstrukturertKravResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerUtgaaendeHenvendelse(wsJournalfoerUtgaaendeHenvendelseRequest: WSJournalfoerUtgaaendeHenvendelseRequest): WSJournalfoerUtgaaendeHenvendelseResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet::class)
    override fun ferdigstillDokumentopplasting(wsFerdigstillDokumentopplastingRequest: WSFerdigstillDokumentopplastingRequest) {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerNotat(wsJournalfoerNotatRequest: WSJournalfoerNotatRequest): WSJournalfoerNotatResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet::class)
    override fun lagreVedleggPaaJournalpost(wsLagreVedleggPaaJournalpostRequest: WSLagreVedleggPaaJournalpostRequest): WSLagreVedleggPaaJournalpostResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerInngaaendeHenvendelse(wsJournalfoerInngaaendeHenvendelseRequest: WSJournalfoerInngaaendeHenvendelseRequest): WSJournalfoerInngaaendeHenvendelseResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun ping() {}
}
