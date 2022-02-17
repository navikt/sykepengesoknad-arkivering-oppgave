package no.nav.helse.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.client.DokArkivClient
import no.nav.helse.client.PDFClient
import no.nav.helse.domain.JournalpostRequest
import no.nav.helse.domain.JournalpostResponse
import no.nav.helse.domain.Soknad
import no.nav.helse.domain.dto.PDFTemplate
import no.nav.helse.domain.dto.Soknadstype
import no.nav.helse.logger
import org.springframework.stereotype.Component

@Component
class Arkivaren(
    val pdfClient: PDFClient,
    val dokArkivClient: DokArkivClient,
    val registry: MeterRegistry,
) {

    val log = logger()

    fun opprettJournalpost(soknad: Soknad): String {

        val soknadsPDF: ByteArray = pdfClient.getPDF(soknad = soknad, template = hentPDFTemplateEtterSoknadstype(soknad.soknadstype))

        val request: JournalpostRequest = skapJournalpostRequest(pdf = soknadsPDF, soknad = soknad)
        val journalpostResponse: JournalpostResponse = dokArkivClient.opprettJournalpost(request, soknad.soknadsId!!)

        if (!journalpostResponse.journalpostferdigstilt) {
            log.warn("Journalpost ${journalpostResponse.journalpostId} for søknad ${soknad.soknadsId} ble ikke ferdigstilt")
        }

        log.info("Arkiverte søknad ${soknad.soknadsId}")
        registry.counter("søknad_arkivert").increment()
        return journalpostResponse.journalpostId
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
