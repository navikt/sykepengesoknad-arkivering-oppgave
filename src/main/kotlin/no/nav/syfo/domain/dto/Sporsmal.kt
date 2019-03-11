package no.nav.syfo.domain.dto

data class Sporsmal(

    val id: String,
    val tag: String,
    val sporsmalstekst: String? = null,
    val undertekst: String? = null,
    val svartype: Svartype? = null,
    val min: String? = null,
    val max: String? = null,
    val kriterieForVisningAvUndersporsmal: Visningskriterie? = null,
    val svar: List<Svar>? = null,
    val undersporsmal: List<Sporsmal>? = null
)
