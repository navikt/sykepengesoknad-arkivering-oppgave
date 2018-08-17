package no.nav.syfo.controller;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.kafka.TestProducer;
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
        Sykepengesoknad sykepengesoknad = new Sykepengesoknad();
        sykepengesoknad.setAktorId("aktorId");
        sykepengesoknad.setFom(LocalDate.of(2018, 10, 10));
        sykepengesoknad.setTom(LocalDate.of(2018, 10, 10));
        sykepengesoknad.setId("id");
        sykepengesoknad.setInnsendtDato(LocalDate.of(2018, 10, 10));
        sykepengesoknad.setSoknadstype(Soknadstype.SELVSTENDIGE_OG_FRILANSERE);
        sykepengesoknad.setStatus("SENDT");
        sykepengesoknad.setSykmeldingId("sykmeldingId");
        sykepengesoknad.setSporsmal(Collections.emptyList());
        testProducer.soknadSendt(sykepengesoknad);

        return "Lagt en sendt sykepengesoknad på topic 👌";
    }
}
