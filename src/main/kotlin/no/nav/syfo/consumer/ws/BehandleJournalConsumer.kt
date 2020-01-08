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
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.DokumentinfoRelasjon
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.JournalfoertDokumentInfo
import no.nav.tjeneste.virksomhet.behandlejournal.v2.informasjon.journalfoerinngaaendehenvendelse.Journalpost
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.JournalfoerInngaaendeHenvendelseRequest
import org.joda.time.DateTime
import org.springframework.stereotype.Component
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
            if (soknad.soknadstype == BEHANDLINGSDAGER) {    // TODO: Fjern denne, når pdfgen er satt opp
                log().warn("syfopdfgen har ikke implementert behandlingsdager")
                return journalforSoknad(soknad, saksId, ByteArray(1))
            }

            val feilmelding = "Kunne ikke generere PDF for søknad med id: ${soknad.soknadsId} og saks id: $saksId"
            log().error(feilmelding, e)
            throw RuntimeException(feilmelding, e)
        }

        return journalforSoknad(soknad, saksId, pdf)
    }

    private fun journalforSoknad(soknad: Soknad, saksId: String, pdf: ByteArray?): String {
        try {
            return behandleJournalV2.journalfoerInngaaendeHenvendelse(
                    JournalfoerInngaaendeHenvendelseRequest()
                            .withApplikasjonsID("SYFOGSAK")
                            .withJournalpost(Journalpost()
                                    .withDokumentDato(DateTime.now())
                                    .withJournalfoerendeEnhetREF(JOURNALFORENDE_ENHET)
                                    .withKanal(Kommunikasjonskanaler().withValue("NAV_NO"))
                                    .withSignatur(Signatur().withSignert(true))
                                    .withArkivtema(Arkivtemaer().withValue("SYK"))
                                    .withForBruker(Person().withIdent(NorskIdent().withIdent(soknad.fnr)))
                                    .withOpprettetAvNavn("Syfogsak")
                                    .withInnhold(getJournalPostInnholdNavn(soknad.soknadstype!!))
                                    .withEksternPart(EksternPart()
                                            .withNavn(personConsumer.finnBrukerPersonnavnByFnr(soknad.fnr!!))
                                            .withEksternAktoer(Person().withIdent(NorskIdent().withIdent(soknad.fnr))))
                                    .withGjelderSak(Sak().withSaksId(saksId).withFagsystemkode(GOSYS))
                                    .withMottattDato(DateTime.now())
                                    .withDokumentinfoRelasjon(
                                            DokumentinfoRelasjon()
                                                    .withTillknyttetJournalpostSomKode("HOVEDDOKUMENT")
                                                    .withJournalfoertDokument(JournalfoertDokumentInfo()
                                                            .withBegrensetPartsInnsyn(false)
                                                            .withDokumentType(Dokumenttyper().withValue(getBrevkode(soknad)))
                                                            .withSensitivitet(true)
                                                            .withTittel(getJornalfoertDokumentTittel(soknad))
                                                            .withKategorikode("ES")
                                                            .withBeskriverInnhold(
                                                                    StrukturertInnhold()
                                                                            .withFilnavn(getStruktureltInnholdFilnavn(soknad))
                                                                            .withFiltype(Arkivfiltyper().withValue("PDF"))
                                                                            .withInnhold(pdf)
                                                                            .withVariantformat(Variantformater().withValue("ARKIV"))
                                                            ))
                                    ))
            ).journalpostId
        } catch (e: RuntimeException) {
            val feilmelding = "Kunne ikke behandle journalpost for søknad med id ${soknad.soknadsId} og saks id: $saksId"
            log().error(feilmelding, e)
            throw RuntimeException(feilmelding, e)
        }
    }

    private fun getBrevkode(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "NAV 08-07.09"
            SELVSTENDIGE_OG_FRILANSERE, ARBEIDSTAKERE, ARBEIDSLEDIG, BEHANDLINGSDAGER -> "NAV 08-07.04 D"
            else -> throw RuntimeException("Har ikke implementert brevkode for søknad av typen: ${soknad.soknadstype}")
        }
    }

    private fun getJornalfoertDokumentTittel(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
            SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            ARBEIDSTAKERE -> "Søknad om sykepenger ${soknad.fom!!.format(norskDato)} - ${soknad.tom!!.format(norskDato)}"
            ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager fra ${soknad.arbeidssituasjon} for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            else -> throw RuntimeException("Har ikke implementert journalført dokumenttittel for søknad av typen: ${soknad.soknadstype!!}")
        }
    }

    private fun getStruktureltInnholdFilnavn(soknad: Soknad): String {
        return when (soknad.soknadstype) {
            OPPHOLD_UTLAND -> "soknad-${soknad.innsendtDato!!.format(norskDato)}"
            SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            ARBEIDSTAKERE -> "Søknad om sykepenger ${soknad.fom!!.format(norskDato)} - ${soknad.tom!!.format(norskDato)}"
            ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager fra ${soknad.arbeidssituasjon} for periode: ${soknad.fom!!.format(norskDato)} til ${soknad.tom!!.format(norskDato)}"
            else -> throw RuntimeException("Har ikke implementert strukturert innhold-filnavn for søknad av typen: ${soknad.soknadstype!!}")
        }
    }

    private fun getJournalPostInnholdNavn(soknadstype: Soknadstype?): String {
        return when (soknadstype) {
            OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
            SELVSTENDIGE_OG_FRILANSERE, ARBEIDSTAKERE, ARBEIDSLEDIG -> "Søknad om sykepenger"
            BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager"
            else -> throw RuntimeException("Har ikke implementert strukturert innhold-filnavn for søknad av typen: $soknadstype")
        }
    }

    private fun hentPDFTemplateEtterSoknadstype(soknadstype: Soknadstype?): PDFTemplate {
        return when (soknadstype) {
            OPPHOLD_UTLAND -> SYKEPENGERUTLAND
            SELVSTENDIGE_OG_FRILANSERE -> SELVSTENDIGNAERINGSDRIVENDE
            ARBEIDSTAKERE -> PDFTemplate.ARBEIDSTAKERE
            ARBEIDSLEDIG -> PDFTemplate.ARBEIDSLEDIG
            BEHANDLINGSDAGER -> PDFTemplate.BEHANDLINGSDAGER
            else -> throw RuntimeException("Har ikke implementert PDF-template for søknad av typen: $soknadstype")
        }
    }

    companion object {
        private val GOSYS = "FS22"
        private val JOURNALFORENDE_ENHET = "9999"
    }
}
