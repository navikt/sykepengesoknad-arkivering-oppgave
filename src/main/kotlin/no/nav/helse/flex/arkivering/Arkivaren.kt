package no.nav.helse.flex.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.DokArkivClient
import no.nav.helse.flex.client.PDFClient
import no.nav.helse.flex.domain.JournalpostRequest
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.PDFTemplate
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class Arkivaren(
    val pdfClient: PDFClient,
    val dokArkivClient: DokArkivClient,
    val registry: MeterRegistry,
) {

    val log = logger()

    fun opprettJournalpost(soknad: Soknad): String {

        val pdf = measureTimeMillisWithResult {
            pdfClient.getPDF(soknad = soknad, template = hentPDFTemplateEtterSoknadstype(soknad.soknadstype))
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
    val result: T,
)

inline fun <T> measureTimeMillisWithResult(block: () -> T): MeassureBlock<T> {
    val start = System.currentTimeMillis()
    val result = block()
    return MeassureBlock(
        millis = System.currentTimeMillis() - start,
        result = result
    )
}
