package no.nav.helse.flex.domain.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import java.math.BigInteger

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

data class AarVerdi(
    val aar: String,
    val verdi: BigInteger,
)

data class Beregnet(
    val snitt: BigInteger,
    val p25: BigInteger,
    val m25: BigInteger,
)

data class SigrunInntekt(
    val inntekter: List<AarVerdi>,
    @param:JsonProperty(value = "g-verdier")
    val gVerdier: List<AarVerdi>,
    @param:JsonProperty(value = "g-sykmelding")
    val gSykmelding: BigInteger,
    val beregnet: Beregnet,
)
