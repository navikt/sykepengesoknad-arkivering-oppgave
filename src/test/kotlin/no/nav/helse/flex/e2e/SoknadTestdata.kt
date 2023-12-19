import no.nav.helse.flex.sykepengesoknad.kafka.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

fun søknad(
    soknadId: UUID = UUID.randomUUID(),
    sendtNav: LocalDateTime? = LocalDateTime.now(),
    sendtArbeidsgiver: LocalDateTime? = null,
    utenlandskSykmelding: Boolean? = null,
    fnr: String = "fnr",
) = SykepengesoknadDTO(
    fnr = fnr,
    id = soknadId.toString(),
    opprettet = LocalDateTime.now(),
    fom = LocalDate.of(2019, 5, 4),
    tom = LocalDate.of(2019, 5, 8),
    type = SoknadstypeDTO.ARBEIDSTAKERE,
    sporsmal =
        listOf(
            SporsmalDTO(
                id = UUID.randomUUID().toString(),
                tag = "TAGGEN",
                sporsmalstekst = "Har systemet gode integrasjonstester?",
                svartype = SvartypeDTO.JA_NEI,
                svar = listOf(SvarDTO(verdi = "JA")),
            ),
        ),
    status = SoknadsstatusDTO.SENDT,
    sendtNav = sendtNav,
    sendtArbeidsgiver = sendtArbeidsgiver,
    utenlandskSykmelding = utenlandskSykmelding,
)
