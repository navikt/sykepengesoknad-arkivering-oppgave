package no.nav.syfo.consumer.ws

import no.nav.syfo.controller.PDFRestController
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.PDFTemplate
import no.nav.syfo.domain.dto.PDFTemplate.SELVSTENDIGNAERINGSDRIVENDE
import no.nav.syfo.domain.dto.PDFTemplate.SYKEPENGERUTLAND
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.domain.dto.Soknadstype.*
import no.nav.syfo.log
import no.nav.syfo.util.DatoUtil.norskDato
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.behandlejournal.*
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSDokumentinfoRelasjon
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalfoertDokumentInfo
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.WSJournalpost
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import javax.inject.Inject

@Component
class BehandleJournalConsumer @Inject
constructor(
        private val behandleJournalV2: BehandleJournalV2,
        private val personConsumer: PersonConsumer,
        private val pdfRestController: PDFRestController) {

    fun opprettJournalpost(soknad: Soknad, saksId: String): String {
        val pdf: ByteArray?

        try {
            pdf = pdfRestController.getPDF(soknad, hentPDFTemplateEtterSoknadstype(soknad.soknadstype!!))
        } catch (e: RuntimeException) {
            val feilmelding = "Kunne ikke generere PDF for søknad med id: " + soknad.soknadsId + " og saks id: " + saksId
            log().error(feilmelding, e)
            throw RuntimeException(feilmelding, e)
        }

        return journalforSoknad(soknad, saksId, pdf)
    }

    private fun journalforSoknad(soknad: Soknad, saksId: String, pdf: ByteArray?): String {
        try {
            return behandleJournalV2.journalfoerInngaaendeHenvendelse(
                    WSJournalfoerInngaaendeHenvendelseRequest()
                            .withApplikasjonsID("SYFOGSAK")
                            .withJournalpost(WSJournalpost()
                                    .withDokumentDato(LocalDateTime.now())
                                    .withJournalfoerendeEnhetREF(JOURNALFORENDE_ENHET)
                                    .withKanal(WSKommunikasjonskanaler().withValue("NAV_NO"))
                                    .withSignatur(WSSignatur().withSignert(true))
                                    .withArkivtema(WSArkivtemaer().withValue("SYK"))
                                    .withForBruker(WSPerson().withIdent(WSNorskIdent().withIdent(soknad.fnr)))
                                    .withOpprettetAvNavn("Syfogsak")
                                    .withInnhold(getJournalPostInnholdNavn(soknad.soknadstype!!))
                                    .withEksternPart(WSEksternPart()
                                            .withNavn(personConsumer.finnBrukerPersonnavnByFnr(soknad.fnr!!))
                                            .withEksternAktoer(WSPerson().withIdent(WSNorskIdent().withIdent(soknad.fnr))))
                                    .withGjelderSak(WSSak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                    .withMottattDato(LocalDateTime.now())
                                    .withDokumentinfoRelasjon(
                                            WSDokumentinfoRelasjon()
                                                    .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                    .withJournalfoertDokument(WSJournalfoertDokumentInfo()
                                                            .withBegrensetPartsInnsyn(false)
                                                            .withDokumentType(WSDokumenttyper().withValue(getBrevkode(soknad)))
                                                            .withSensitivitet(true)
                                                            .withTittel(getJornalfoertDokumentTittel(soknad))
                                                            .withKategorikode("ES")
                                                            .withBeskriverInnhold(
                                                                    WSStrukturertInnhold()
                                                                            .withFilnavn(getWSStruktureltInnholdFilnavn(soknad))
                                                                            .withFiltype(WSArkivfiltyper().withValue("PDF"))
                                                                            .withInnhold(pdf)
                                                                            .withVariantformat(WSVariantformater().withValue("ARKIV"))
                                                            ))
                                    ))
            ).journalpostId
        } catch (e: RuntimeException) {
            val feilmelding = "Kunne ikke behandle journalpost for søknad med id " + soknad.soknadsId + " og saks id: " + saksId
            log().error(feilmelding, e)
            throw RuntimeException(feilmelding, e)
        }
    }

    private fun getBrevkode(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "NAV 08-07.09"
            SELVSTENDIGE_OG_FRILANSERE, ARBEIDSTAKERE, ARBEIDSLEDIG -> "NAV 08-07.04 D"
            else -> throw RuntimeException("Har ikke implementert brevkode for søknad av typen: " + soknad.soknadstype)
        }
    }

    private fun getJornalfoertDokumentTittel(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
            SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + soknad.fom!!.format(norskDato) + " til " + soknad.tom!!.format(norskDato)
            ARBEIDSTAKERE -> "Søknad om sykepenger " + soknad.fom!!.format(norskDato) + " - " + soknad.tom!!.format(norskDato)
            ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: " + soknad.fom!!.format(norskDato) + " til " + soknad.tom!!.format(norskDato)
            else -> throw RuntimeException("Har ikke implementert journalført dokumenttittel for søknad av typen: " + soknad.soknadstype!!)
        }
    }

    private fun getWSStruktureltInnholdFilnavn(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "soknad-" + soknad.innsendtDato!!.format(norskDato)
            SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: " + soknad.fom!!.format(norskDato) + " til " + soknad.tom!!.format(norskDato)
            ARBEIDSTAKERE -> "Søknad om sykepenger " + soknad.fom!!.format(norskDato) + " - " + soknad.tom!!.format(norskDato)
            ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: " + soknad.fom!!.format(norskDato) + " til " + soknad.tom!!.format(norskDato)
            else -> throw RuntimeException("Har ikke implementert strukturert innhold-filnavn for søknad av typen: " + soknad.soknadstype!!)
        }
    }

    private fun getJournalPostInnholdNavn(soknadstype: Soknadstype?): String {
        return when (soknadstype) {
            OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
            SELVSTENDIGE_OG_FRILANSERE, ARBEIDSTAKERE, ARBEIDSLEDIG -> "Søknad om sykepenger"
            else -> throw RuntimeException("Har ikke implementert strukturert innhold-filnavn for søknad av typen: $soknadstype")
        }
    }

    private fun hentPDFTemplateEtterSoknadstype(soknadstype: Soknadstype?): PDFTemplate {
        return when (soknadstype) {
            OPPHOLD_UTLAND -> SYKEPENGERUTLAND
            SELVSTENDIGE_OG_FRILANSERE -> SELVSTENDIGNAERINGSDRIVENDE
            ARBEIDSTAKERE -> PDFTemplate.ARBEIDSTAKERE
            ARBEIDSLEDIG -> PDFTemplate.ARBEIDSLEDIG
            else -> throw RuntimeException("Har ikke implementert PDF-template for søknad av typen: $soknadstype")
        }
    }

    companion object {
        private val GOSYS = "FS22"
        private val JOURNALFORENDE_ENHET = "9999"
    }
}
