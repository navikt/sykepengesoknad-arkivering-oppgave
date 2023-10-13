package no.nav.helse.flex.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.DokArkivClient
import no.nav.helse.flex.client.PDFClient
import no.nav.helse.flex.domain.JournalpostRequest
import no.nav.helse.flex.domain.JournalpostResponse
import no.nav.helse.flex.domain.LogiskVedleggRequest
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.PDFTemplate
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svar
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class Arkivaren(
    val pdfClient: PDFClient,
    val dokArkivClient: DokArkivClient,
    val registry: MeterRegistry
) {

    val log = logger()

    fun transformDateFormat(date: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun leggTilLogiskVedleggForBehandlingsDager(soknad: Soknad, journalpostResponse:  MeassureBlock<JournalpostResponse>) {


        val dokumentInfoId: String = journalpostResponse.result.dokumenter[0].dokumentInfoId ?: ""

        var behandlingsdagMessage = ""
        val behandlingsdagerUkerToppnivå = soknad.sporsmal
            .filter { it.tag.startsWith("ENKELTSTAENDE_BEHANDLINGSDAGER_") }

        var behandlingsdagerUker: MutableList<Sporsmal> = mutableListOf()
        for (i in behandlingsdagerUkerToppnivå) {
            if (i.undersporsmal is List<Sporsmal>) {
                behandlingsdagerUker.addAll(i.undersporsmal)
            }
        }

        var svarListe = mutableListOf<Svar>()
        for (i in behandlingsdagerUker) {
            if (i.svar is List<Svar>) {
                svarListe.addAll(i.svar)
            }
        }

        svarListe = svarListe.filter { it.verdi != "Ikke til behandling" }.toMutableList()


        behandlingsdagMessage += " Antall behandlingsdager: ${svarListe?.size} "

        if (svarListe.size > 0) {
            behandlingsdagMessage += " Behandlingsdager: "
        }

        for (item in svarListe.withIndex()) {
            behandlingsdagMessage += " ${item.value.verdi?.let { transformDateFormat(it) }}"
        }

        val request2: LogiskVedleggRequest =
            LogiskVedleggRequest(
                tittel = behandlingsdagMessage
            )

        if (dokumentInfoId != "") {
            dokArkivClient.opprettLogiskVedlegg(
                request2,
                dokumentInfoId
            )


        }
    }

    fun opprettJournalpost(soknad: Soknad): String {
        val pdf = measureTimeMillisWithResult {
            pdfClient.getPDF(soknad = soknad.sorterViktigeSporsmalFørst(), template = hentPDFTemplateEtterSoknadstype(soknad.soknadstype))
        }

        val request: JournalpostRequest =
            skapJournalpostRequest(pdf = pdf.result, soknad = soknad)

        val journalpostResponse = measureTimeMillisWithResult {
            dokArkivClient.opprettJournalpost(request, soknad.soknadsId!!)
        }


        if (!journalpostResponse.result.journalpostferdigstilt) {
            log.warn("Journalpost ${journalpostResponse.result.journalpostId} for søknad ${soknad.soknadsId} ble ikke ferdigstilt")
        }

        log.info("Arkiverte søknad ${soknad.soknadsId}. PDF tid: ${pdf.millis} . Dokarkiv tid: ${journalpostResponse.millis}")
        registry.counter("søknad_arkivert").increment()


        val erBehandlingsDagSoknad = soknad.soknadstype == Soknadstype.BEHANDLINGSDAGER



        if (erBehandlingsDagSoknad) {
            leggTilLogiskVedleggForBehandlingsDager(soknad, journalpostResponse)
        }
        return journalpostResponse.result.journalpostId
    }

    private fun hentPDFTemplateEtterSoknadstype(soknadstype: Soknadstype): PDFTemplate {
        return when (soknadstype) {
            Soknadstype.OPPHOLD_UTLAND -> PDFTemplate.SYKEPENGERUTLAND
            Soknadstype.SELVSTENDIGE_OG_FRILANSERE -> PDFTemplate.SELVSTENDIGNAERINGSDRIVENDE
            Soknadstype.ARBEIDSTAKERE -> PDFTemplate.ARBEIDSTAKERE
            Soknadstype.ARBEIDSLEDIG -> PDFTemplate.ARBEIDSLEDIG
            Soknadstype.BEHANDLINGSDAGER -> PDFTemplate.BEHANDLINGSDAGER
            Soknadstype.ANNET_ARBEIDSFORHOLD -> PDFTemplate.ANNETARBEIDSFORHOLD
            Soknadstype.REISETILSKUDD -> PDFTemplate.REISETILSKUDD
            Soknadstype.GRADERT_REISETILSKUDD -> PDFTemplate.GRADERT_REISETILSKUDD
        }
    }
}

class MeassureBlock<T>(
    val millis: Long,
    val result: T
)

inline fun <T> measureTimeMillisWithResult(block: () -> T): MeassureBlock<T> {
    val start = System.currentTimeMillis()
    val result = block()
    return MeassureBlock(
        millis = System.currentTimeMillis() - start,
        result = result
    )
}
