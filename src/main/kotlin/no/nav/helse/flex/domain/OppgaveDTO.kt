package no.nav.helse.flex.domain

import java.time.LocalDateTime
import java.util.UUID

data class OppgaveDTO(
    val dokumentType: DokumentTypeDTO,
    val oppdateringstype: OppdateringstypeDTO,
    val dokumentId: UUID,
    val timeout: LocalDateTime? = null
)

enum class OppdateringstypeDTO {
    Utsett, Opprett, Ferdigbehandlet, OpprettSpeilRelatert
}

enum class DokumentTypeDTO {
    Inntektsmelding, Søknad
}
