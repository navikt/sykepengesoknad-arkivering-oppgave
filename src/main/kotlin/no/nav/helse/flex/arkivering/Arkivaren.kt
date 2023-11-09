package no.nav.helse.flex.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.DokArkivClient
import no.nav.helse.flex.client.PDFClient
import no.nav.helse.flex.domain.*
import no.nav.helse.flex.domain.dto.PDFTemplate
import no.nav.helse.flex.domain.dto.Soknadstype
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
        val outputFormatter = DateTimeFormatter.ofPattern("ddMMyy")

        val parsedDate = LocalDate.parse(date, inputFormatter)
        return parsedDate.format(outputFormatter)
    }

    fun leggTilLogiskVedleggForBehandlingsDager(soknad: Soknad, journalpostResponse: MeassureBlock<JournalpostResponse>): String {
        val dokumentInfoId = journalpostResponse.result.dokumenter.firstOrNull()?.dokumentInfoId ?: throw RuntimeException("Request til dokarkiv failer")

        val behandlingsdagerUker = soknad.sporsmal
            .filter { it.tag.startsWith("ENKELTSTAENDE_BEHANDLINGSDAGER_") }
            .flatMap { it.undersporsmal ?: emptyList() }

        val svarListe = behandlingsdagerUker
            .flatMap { it.svar ?: emptyList() }
            .filter { it.verdi != "Ikke til behandling" }

        var behandlingsdagMessage = "${svarListe.size} behandlingsdager, "

        if (svarListe.isNotEmpty()) {
            behandlingsdagMessage += "${svarListe.joinToString(" ") { it.verdi?.let { v -> transformDateFormat(v) } ?: "" }}"
        }

        if (soknad.egenmeldingsdagerFraSykmelding?.isNotEmpty() == true) {
            behandlingsdagMessage += "\\n ${soknad.egenmeldingsdagerFraSykmelding.size} egenmeldingsdager, "
            behandlingsdagMessage += soknad.egenmeldingsdagerFraSykmelding.joinToString(" ") {
                "${it.dayOfMonth.toString().padStart(2, '0')}${it.monthValue.toString().padStart(2, '0')}${it.year.toString().substring(2)}"
            }
        }

        val request = LogiskVedleggRequest(tittel = behandlingsdagMessage)

        val response = dokArkivClient.opprettLogiskVedlegg(request, dokumentInfoId)
        return response.logiskVedleggId
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
