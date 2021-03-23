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

    var sisteJournalfoerInngaaendeHenvendelseRequest: JournalfoerInngaaendeHenvendelseRequest? = null

    override fun arkiverUstrukturertKrav(
        wsArkiverUstrukturertKravRequest: ArkiverUstrukturertKravRequest
    ): ArkiverUstrukturertKravResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerUtgaaendeHenvendelse(
        wsJournalfoerUtgaaendeHenvendelseRequest: JournalfoerUtgaaendeHenvendelseRequest
    ): JournalfoerUtgaaendeHenvendelseResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(FerdigstillDokumentopplastingFerdigstillDokumentopplastingjournalpostIkkeFunnet::class)
    override fun ferdigstillDokumentopplasting(
        wsFerdigstillDokumentopplastingRequest: FerdigstillDokumentopplastingRequest
    ) {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerNotat(
        wsJournalfoerNotatRequest: JournalfoerNotatRequest
    ): JournalfoerNotatResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    @Throws(LagreVedleggPaaJournalpostLagreVedleggPaaJournalpostjournalpostIkkeFunnet::class)
    override fun lagreVedleggPaaJournalpost(
        wsLagreVedleggPaaJournalpostRequest: LagreVedleggPaaJournalpostRequest
    ): LagreVedleggPaaJournalpostResponse {
        throw RuntimeException("Ikke implementert i mock")
    }

    override fun journalfoerInngaaendeHenvendelse(
        wsJournalfoerInngaaendeHenvendelseRequest: JournalfoerInngaaendeHenvendelseRequest
    ): JournalfoerInngaaendeHenvendelseResponse {
        sisteJournalfoerInngaaendeHenvendelseRequest = wsJournalfoerInngaaendeHenvendelseRequest
        return JournalfoerInngaaendeHenvendelseResponse().withJournalpostId("journalpostId")
    }

    override fun ping() {}
}
