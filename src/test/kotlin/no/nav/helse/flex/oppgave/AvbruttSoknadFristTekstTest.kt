package no.nav.helse.flex.oppgave

import meldingDersomAvbruttFristFeil
import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Soknadstype
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDate

class AvbruttSoknadFristTekstTest {
    val forventetBeskrivelse = """Ved en feil har brukeren, for akkurat denne søknadsperioden, fått beskjed om en annen frist enn hva folketrygdloven § 22-13 tredje ledd tilsier. Hvis denne søknaden er sendt inn innen den alminnelige fristen, behandles søknaden som normalt, og du kan se vekk fra teksten under.

Hvis søknaden er sendt inn senere, gjelder følgende:

Pga feilinformasjonen om fristen, er akkurat denne søknaden unntatt hovedregelen om frist og skal realitetsbehandles i henhold til folketrygdloven § 22-13 syvende ledd andre punktum så lenge søknaden er mottatt innen 01.01.1971.

Selv om søknaden innvilges, skal det i dette tilfellet sendes melding om vedtak til bruker, hvor bruker får informasjon om at søknaden er innvilget etter en unntaksregel. Forslag til tekst som kan brukes:

"NAV har innvilget søknaden din om sykepenger for perioden 24.03.2022 - 28.03.2022. Du får utbetalt kr xx. [tilpass teksten hvis arbeidsgiver skal ha refusjon]

Vanligvis gis sykepenger kun for opptil tre måneder før den måneden da kravet ble satt fram, jf. folketrygdloven § 22-13 tredje ledd. Det betyr at du har søkt så seint at NAV vanligvis ville avslått søknaden din. Men i søknaden om sykepenger for akkurat denne sykmeldingsperioden, ga NAV deg beskjed om at fristen for å søke var 01.01.1971. Denne fristen var feil, men fordi NAV ga deg misvisende opplysninger, får du likevel sykepenger for denne perioden. Vi har brukt en unntaksregel i folketrygdloven § 22-13 syvende ledd andre punktum når vi har behandlet søknaden din.

Fordi du fikk feil informasjon knyttet til akkurat denne søknadsperioden, gjelder den forlengede søknadsfristen kun for denne perioden. Det betyr at hvis du søker om sykepenger for andre perioder, vil du ikke få forlenget frist for disse andre periodene. Da gjelder hovedregelen om at sykepenger kun gis for opptil tre måneder før den måneden da kravet ble satt fram, jf. folketrygdloven § 22-13 tredje ledd."

"""

    @Test
    fun testerTekst() {
        val soknad =
            Soknad(
                fom = LocalDate.of(2022, 3, 24),
                tom = LocalDate.of(2022, 3, 28),
                opprettet = LocalDate.EPOCH.atStartOfDay(),
                soknadstype = Soknadstype.ARBEIDSLEDIG,
                sporsmal = emptyList(),
                merknader = listOf("AVBRUTT_FEILINFO"),
                egenmeldingsdagerFraSykmelding = null,
            )

        soknad.meldingDersomAvbruttFristFeil() `should be equal to` forventetBeskrivelse
    }
}
