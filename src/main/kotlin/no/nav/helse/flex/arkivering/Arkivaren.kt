package no.nav.helse.flex.arkivering

import io.micrometer.core.instrument.MeterRegistry
import no.nav.helse.flex.client.DokArkivClient
import no.nav.helse.flex.client.PDFClient
import no.nav.helse.flex.domain.JournalpostRequest
import no.nav.helse.flex.domain.LogiskVedleggRequest
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.PDFTemplate
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sporsmal
import no.nav.helse.flex.domain.dto.Svar
import no.nav.helse.flex.logger
import org.springframework.stereotype.Component

@Component
class Arkivaren(
    val pdfClient: PDFClient,
    val dokArkivClient: DokArkivClient,
    val registry: MeterRegistry
) {

    val log = logger()

    fun opprettJournalpost(soknad: Soknad): String {
        val pdf = measureTimeMillisWithResult {
            pdfClient.getPDF(soknad = soknad.sorterViktigeSporsmalFørst(), template = hentPDFTemplateEtterSoknadstype(soknad.soknadstype))
        }

        val request: JournalpostRequest =
            skapJournalpostRequest(pdf = pdf.result, soknad = soknad)

        val journalpostResponse = measureTimeMillisWithResult {
            dokArkivClient.opprettJournalpost(request, soknad.soknadsId!!)
        }

        //  https://reflectoring.io/spring-webclient/ Dette er det du trenger

        if (!journalpostResponse.result.journalpostferdigstilt) {
            log.warn("Journalpost ${journalpostResponse.result.journalpostId} for søknad ${soknad.soknadsId} ble ikke ferdigstilt")
        }

        log.info("Arkiverte søknad ${soknad.soknadsId}. PDF tid: ${pdf.millis} . Dokarkiv tid: ${journalpostResponse.millis}")
        registry.counter("søknad_arkivert").increment()

        // det er her vi ville hentet ut journalpostid eller noe og lagt inn evt. behandlinsdager

        // val id = journalpostResponse.result.journalpostId

        // her må vi legge inn behandlingsdager om de finnes

        val erBehandlingsDagSoknad = soknad.soknadstype == Soknadstype.BEHANDLINGSDAGER

        // loop trough behandlingsdager
        // for hver behandlingsdag, opprett logisk vedlegg

        var behandlingsdagMessage = "start "

        if (erBehandlingsDagSoknad) {
            behandlingsdagMessage += " behandlingsdag søknad "
        }

        // ikke til behandling eller en tekststreng som er en dato ... vi må utelukke

        val dokumentInfoId: String = journalpostResponse.result.dokumenter[0].dokumentInfoId ?: ""

        // INFO_BEHANDLINGSDAGER

        /*

              "tag": "ENKELTSTAENDE_BEHANDLINGSDAGER_0",
      "sporsmalstekst": "Hvilke dager måtte du være helt borte fra jobben på grunn av behandling mellom 11. - 17. desember 2019?",
      "undertekst": null,
      "svartype": "INFO_BEHANDLINGSDAGER",

         */

        // init behandlingsdager as an empty array of Sporsmal
        // var behandlingsdager: List<Sporsmal> = mutableListOf()
        // behandlingsdager = soknad.sporsmal.filter { it.tag == "ENKELTSTAENDE_BEHANDLINGSDAGER_UKE_0" }.first().undersporsmal

        if (erBehandlingsDagSoknad) {
            val behandlingsdagerUkerToppnivå = soknad.sporsmal
                .filter { it.tag.startsWith("ENKELTSTAENDE_BEHANDLINGSDAGER_") }

            var behandlingsdagerUker: MutableList<Sporsmal> = mutableListOf()
            var behandlingsdagerDatoer = mutableListOf<String>()
            // loop trough behandlingsdagerToppnivå
            for (i in behandlingsdagerUkerToppnivå) {
                // check that of type List<Sporsmal>
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

            behandlingsdagMessage += " dager antall ${svarListe?.size} "
            for (item in svarListe.withIndex()) {
                println(item)
                // opprett logisk vedlegg

                behandlingsdagMessage += " ${item.index} ${item.value.verdi}" // $item
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

                // check that it was a 200 response
            }
        }
        // /Users/kuls/code/sykepengesoknad-arkivering-oppgave/src/test/resources/soknadBehandlingsdagerMedNeisvar.json
//        if (soknad.soknadstype == Soknadstype.BEHANDLINGSDAGER) {
//            val behandlingsdager = soknad.sporsmal
//
//
//        }

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
