package no.nav.helse.flex.arkivering

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.domain.dto.Sporsmal

fun Soknad.sorterViktigeSporsmalFørst(): Soknad {
    val allespm = this.sporsmal
    val viktigeSporsmal = allespm.filter { it.erViktig() }
    val andreSporsmal = allespm.filter { !it.erViktig() }
    val sorterteSporsmal = viktigeSporsmal + andreSporsmal
    return this.copy(sporsmal = sorterteSporsmal)
}

private fun Sporsmal.erViktig(): Boolean {
    return this.svar?.any { it.verdi == viktigSvarverdi() } == true
}

private fun Sporsmal.viktigSvarverdi(): String {
    if (this.tag == "FRISKMELDT") return "NEI"
    return "JA"
}
