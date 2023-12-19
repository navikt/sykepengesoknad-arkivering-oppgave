import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.util.DatoUtil

fun Soknad.meldingDersomAvbruttFristFeil() =
    if (merknader?.contains("AVBRUTT_FEILINFO") == true) {
        val feilinformertFrist = this.opprettet.plusYears(1).toLocalDate().format(DatoUtil.norskDato)

        """Ved en feil har brukeren, for akkurat denne søknadsperioden, fått beskjed om en annen frist enn hva folketrygdloven § 22-13 tredje ledd tilsier. Hvis denne søknaden er sendt inn innen den alminnelige fristen, behandles søknaden som normalt, og du kan se vekk fra teksten under.

Hvis søknaden er sendt inn senere, gjelder følgende:

Pga feilinformasjonen om fristen, er akkurat denne søknaden unntatt hovedregelen om frist og skal realitetsbehandles i henhold til folketrygdloven § 22-13 syvende ledd andre punktum så lenge søknaden er mottatt innen $feilinformertFrist.

Selv om søknaden innvilges, skal det i dette tilfellet sendes melding om vedtak til bruker, hvor bruker får informasjon om at søknaden er innvilget etter en unntaksregel. Forslag til tekst som kan brukes:

"NAV har innvilget søknaden din om sykepenger for perioden ${fom!!.format(
            DatoUtil.norskDato,
        )} - ${tom!!.format(DatoUtil.norskDato)}. Du får utbetalt kr xx. [tilpass teksten hvis arbeidsgiver skal ha refusjon]

Vanligvis gis sykepenger kun for opptil tre måneder før den måneden da kravet ble satt fram, jf. folketrygdloven § 22-13 tredje ledd. Det betyr at du har søkt så seint at NAV vanligvis ville avslått søknaden din. Men i søknaden om sykepenger for akkurat denne sykmeldingsperioden, ga NAV deg beskjed om at fristen for å søke var $feilinformertFrist. Denne fristen var feil, men fordi NAV ga deg misvisende opplysninger, får du likevel sykepenger for denne perioden. Vi har brukt en unntaksregel i folketrygdloven § 22-13 syvende ledd andre punktum når vi har behandlet søknaden din.

Fordi du fikk feil informasjon knyttet til akkurat denne søknadsperioden, gjelder den forlengede søknadsfristen kun for denne perioden. Det betyr at hvis du søker om sykepenger for andre perioder, vil du ikke få forlenget frist for disse andre periodene. Da gjelder hovedregelen om at sykepenger kun gis for opptil tre måneder før den måneden da kravet ble satt fram, jf. folketrygdloven § 22-13 tredje ledd."

"""
    } else {
        ""
    }
