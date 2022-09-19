package no.nav.helse.flex.arkivering

import no.nav.helse.flex.domain.*
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.util.DatoUtil
import java.time.LocalDate

fun skapJournalpostRequest(
    pdf: ByteArray,
    soknad: Soknad
): JournalpostRequest {
    return JournalpostRequest(
        bruker = Bruker(
            id = soknad.fnr!!,
            idType = "FNR"
        ),
        dokumenter = listOf(
            Dokument(
                dokumentvarianter = listOf(
                    Dokumentvarianter(
                        filnavn = getStruktureltInnholdFilnavn(soknad = soknad),
                        filtype = "PDFA",
                        variantformat = "ARKIV",
                        fysiskDokument = pdf
                    )
                ),
                brevkode = getBrevkode(soknad = soknad),
                tittel = getJornalfoertDokumentTittel(soknad = soknad),
            )
        ),
        sak = Sak(
            sakstype = "GENERELL_SAK"
        ),
        journalpostType = "INNGAAENDE",
        journalfoerendeEnhet = "9999",
        tema = "SYK",
        eksternReferanseId = soknad.soknadsId,
        datoMottatt = LocalDate.now(),
        kanal = "NAV_NO",
        tittel = getJornalfoertDokumentTittel(soknad = soknad),
        avsenderMottaker = AvsenderMottaker(
            id = soknad.fnr,
            idType = "FNR",
        )
    )
}

private fun getBrevkode(soknad: Soknad): String {
    return when (soknad.soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "NAV 08-07.09"
        Soknadstype.GRADERT_REISETILSKUDD, Soknadstype.REISETILSKUDD -> "NAV 08-14.01"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE, Soknadstype.ARBEIDSTAKERE, Soknadstype.ARBEIDSLEDIG, Soknadstype.BEHANDLINGSDAGER, Soknadstype.ANNET_ARBEIDSFORHOLD -> "NAV 08-07.04 D"
    }
}

private fun getJornalfoertDokumentTittel(soknad: Soknad): String {
    return when (soknad.soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "Søknad om å beholde sykepenger utenfor EØS"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.ARBEIDSTAKERE -> "Søknad om sykepenger ${soknad.fom!!.format(DatoUtil.norskDato)} - ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: ${soknad.fom!!.format(DatoUtil.norskDato)} til ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager fra ${soknad.arbeidssituasjon.toString().lowercase()} for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.ANNET_ARBEIDSFORHOLD -> "Søknad om sykepenger med uavklart arbeidssituasjon fra ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.REISETILSKUDD -> "Søknad om reisetilskudd for periode: ${soknad.fom!!.format(DatoUtil.norskDato)} til ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.GRADERT_REISETILSKUDD -> "Søknad om sykepenger med reisetilskudd for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
    }
}

private fun getStruktureltInnholdFilnavn(soknad: Soknad): String {
    return when (soknad.soknadstype) {
        Soknadstype.OPPHOLD_UTLAND -> "soknad-${soknad.innsendtTid!!.format(DatoUtil.norskDato)}"
        Soknadstype.SELVSTENDIGE_OG_FRILANSERE -> "Søknad om sykepenger fra Selvstendig/Frilanser for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.ARBEIDSTAKERE -> "Søknad om sykepenger ${soknad.fom!!.format(DatoUtil.norskDato)} - ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.ARBEIDSLEDIG -> "Søknad om sykepenger fra arbeidsledig for periode: ${soknad.fom!!.format(DatoUtil.norskDato)} til ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.BEHANDLINGSDAGER -> "Søknad om enkeltstående behandlingsdager fra ${soknad.arbeidssituasjon.toString().lowercase()} for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.ANNET_ARBEIDSFORHOLD -> "Søknad om sykepenger med uavklart arbeidssituasjon fra ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
        Soknadstype.REISETILSKUDD -> "Søknad om reisetilskudd for periode: ${soknad.fom!!.format(DatoUtil.norskDato)} til ${soknad.tom!!.format(
            DatoUtil.norskDato
        )}"
        Soknadstype.GRADERT_REISETILSKUDD -> "Søknad om sykepenger med reisetilskudd for periode: ${soknad.fom!!.format(
            DatoUtil.norskDato
        )} til ${soknad.tom!!.format(DatoUtil.norskDato)}"
    }
}
