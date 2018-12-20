package no.nav.syfo.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.kafka.TestProducer;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;

@RestController
@Slf4j
@RequestMapping(value = "/test")
public class SoknadProducer {

    private TestProducer testProducer;

    public SoknadProducer(TestProducer testProducer) {
        this.testProducer = testProducer;
    }

    @ResponseBody
    @RequestMapping(value = "/produce", produces = MediaType.TEXT_PLAIN_VALUE)
    public String produce() {
        SoknadDTO sykepengesoknad = SoknadDTO.builder()
                .aktorId("aktorId")
                .fom(LocalDate.of(2018, 10, 10))
                .tom(LocalDate.of(2018, 10, 10))
                .id("id")
                .innsendtDato(LocalDate.of(2018, 10, 10))
                .soknadstype(Soknadstype.SELVSTENDIGE_OG_FRILANSERE.name())
                .status("SENDT")
                .sykmeldingId("sykmeldingId")
                .sporsmal(Collections.emptyList())
                .build();
        testProducer.soknadSendt(sykepengesoknad);

        return "Lagt en sendt sykepengesoknad på topic 👌";
    }
}
