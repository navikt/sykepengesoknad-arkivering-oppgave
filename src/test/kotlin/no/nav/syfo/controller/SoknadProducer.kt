package no.nav.syfo.controller

import no.nav.syfo.domain.dto.Soknadstype
import no.nav.syfo.kafka.TestProducer
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.soknad.dto.SporsmalDTO
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import java.time.LocalDate

@RestController
@RequestMapping(value = ["/test"])
class SoknadProducer(private val testProducer: TestProducer) {

    @ResponseBody
    @RequestMapping(value = ["/produce"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun produce(): String {
        val sykepengesoknad = SoknadDTO.builder()
                .aktorId("aktorId")
                .fom(LocalDate.of(2018, 10, 10))
                .tom(LocalDate.of(2018, 10, 10))
                .id("id")
                .innsendtDato(LocalDate.of(2018, 10, 10))
                .soknadstype(Soknadstype.SELVSTENDIGE_OG_FRILANSERE.name)
                .status("SENDT")
                .sykmeldingId("sykmeldingId")
                .sporsmal(emptyList<SporsmalDTO>())
                .build()
        testProducer.soknadSendt(sykepengesoknad)

        return "Lagt en sendt sykepengesoknad på topic 👌"
    }
}
