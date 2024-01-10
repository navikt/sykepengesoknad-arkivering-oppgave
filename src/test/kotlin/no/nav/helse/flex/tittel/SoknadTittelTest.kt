package no.nav.helse.flex.tittel

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Arbeidssituasjon
import no.nav.helse.flex.domain.dto.Soknadstype
import no.nav.helse.flex.domain.dto.Sporsmal
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SoknadTittelTest {
    val grunnsoknad =
        Soknad(
            opprettet = LocalDateTime.now(),
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
            fom = LocalDate.of(2021, 3, 4),
            tom = LocalDate.of(2021, 3, 5),
            sporsmal = emptyList(),
        )

    @Test
    fun `viser vedlegg info ved inntektsoppysningsspørsmål for næringsdrivende`() {
        val soknad =
            grunnsoknad.copy(
                arbeidssituasjon = Arbeidssituasjon.NAERINGSDRIVENDE,
                soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
                sporsmal =
                    listOf(
                        Sporsmal(
                            id = UUID.randomUUID().toString(),
                            tag = "INNTEKTSOPPLYSNINGER_DRIFT_VIRKSOMHETEN",
                        ),
                    ),
            )
        soknad.skapTittel() shouldBeEqualTo "Søknad om sykepenger for næringsdrivende for perioden " +
            "04.03.2021 til 05.03.2021 - med vedlegg inntektsopplysninger"
    }

    @Test
    fun `viser ikke vedlegg info uten inntektsoppysningsspørsmål for næringsdrivende`() {
        val soknad =
            grunnsoknad.copy(
                arbeidssituasjon = Arbeidssituasjon.NAERINGSDRIVENDE,
                soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
                sporsmal =
                    listOf(
                        Sporsmal(
                            id = UUID.randomUUID().toString(),
                            tag = "ARBEIDUNDERVEIS",
                        ),
                    ),
            )
        soknad.skapTittel() shouldBeEqualTo "Søknad om sykepenger for næringsdrivende for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for andre søknadstyper`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.FRILANSER,
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
        ).skapTittel() shouldBeEqualTo "Søknad om sykepenger for frilanser for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for frilanser`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.FRILANSER,
            soknadstype = Soknadstype.SELVSTENDIGE_OG_FRILANSERE,
        )
            .skapTittel() shouldBeEqualTo "Søknad om sykepenger for frilanser for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for arbeidsledig`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG,
            soknadstype = Soknadstype.ARBEIDSLEDIG,
        ).skapTittel() shouldBeEqualTo "Søknad om sykepenger for arbeidsledig for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for reisetilskudd`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG,
            soknadstype = Soknadstype.REISETILSKUDD,
        ).skapTittel() shouldBeEqualTo "Søknad om reisetilskudd for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for gradert reisetilskudd`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSLEDIG,
            soknadstype = Soknadstype.GRADERT_REISETILSKUDD,
        ).skapTittel() shouldBeEqualTo "Søknad om sykepenger med reisetilskudd for perioden 04.03.2021 til 05.03.2021"
    }

    @Test
    fun `fornuftig tittel for opphold utland`() {
        grunnsoknad.copy(
            soknadstype = Soknadstype.OPPHOLD_UTLAND,
        ).skapTittel() shouldBeEqualTo "Søknad om å beholde sykepenger utenfor EØS"
    }

    @Test
    fun `fornuftig tittel for arbeidstaker`() {
        grunnsoknad.copy(
            arbeidssituasjon = Arbeidssituasjon.ARBEIDSTAKER,
            soknadstype = Soknadstype.ARBEIDSTAKERE,
        ).skapTittel() shouldBeEqualTo "Søknad om sykepenger for perioden 04.03.2021 til 05.03.2021"
    }
}
