package no.nav.syfo.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.syfo.client.DokArkivClient
import no.nav.syfo.client.PDFClient
import no.nav.syfo.client.pdl.PdlClient
import no.nav.syfo.domain.JournalpostRequest
import no.nav.syfo.domain.JournalpostResponse
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.PDFTemplate
import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.logger
import org.springframework.stereotype.Component

@Component
class Arkivaren(
    val pdfClient: PDFClient,
    val dokArkivClient: DokArkivClient,
    val pdlClient: PdlClient,
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