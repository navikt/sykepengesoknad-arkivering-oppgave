package no.nav.helse.flex.arkivering

import no.nav.helse.flex.domain.*
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.tittel.skapTittel
import java.time.LocalDate

fun skapJournalpostRequest(
    pdf: ByteArray,
    soknad: Soknad,
): JournalpostRequest {
    return JournalpostRequest(
        bruker =
            Bruker(
                id = soknad.fnr!!,
                idType = "FNR",
            ),
        dokumenter =
            listOf(
                Dokument(
                    dokumentvarianter =
                        listOf(
                            Dokumentvarianter(
                                filnavn = soknad.skapTittel(),
                                filtype = "PDFA",
                                variantformat = "ARKIV",
                                fysiskDokument = pdf,
                            ),
                        ),
                    brevkode = getBrevkode(soknad = soknad),
                    tittel = soknad.skapTittel(),
                ),
            ),
        sak =
            Sak(
                sakstype = "GENERELL_SAK",
            ),
        journalpostType = "INNGAAENDE",
        journalfoerendeEnhet = "9999",
        tema = "SYK",
        eksternReferanseId = soknad.soknadsId,
        datoMottatt = LocalDate.now(),
        kanal = "NAV_NO",
        tittel = soknad.skapTittel(),
        avsenderMottaker =
            AvsenderMottaker(
                id = soknad.fnr,
                idType = "FNR",
            ),
    )
}

private fun getBrevkode(soknad: Soknad): String {
    return when (soknad.soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "NAV 08-07.09"
        Soknadstype.GRADERT_REISETILSKUDD, Soknadstype.REISETILSKUDD -> "NAV 08-14.01"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
        Soknadstype.ARBEIDSTAKERE,
        Soknadstype.ARBEIDSLEDIG,
        Soknadstype.BEHANDLINGSDAGER,
        Soknadstype.ANNET_ARBEIDSFORHOLD,
        -> "NAV 08-07.04 D"
    }
}
