package no.nav.helse.flex.domain.dto

import com.fasterxml.jackson.databind.JsonNode

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
    val undersporsmal: List<Sporsmal>? = null,
    var metadata: JsonNode? = null,
)
