package no.nav.syfo.web.selftest;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.controller.PDFRestController;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sporsmal;
import no.nav.syfo.domain.dto.Svar;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.UUID;

import static no.nav.syfo.domain.dto.Svartype.*;
import static no.nav.syfo.domain.dto.SvarverdiType.FOM;
import static no.nav.syfo.domain.dto.SvarverdiType.TOM;


@Slf4j
@RestController
@RequestMapping(value = "/internal")
public class SelftestController {
    private static final String APPLICATION_LIVENESS = "Application is alive!";
    private static final String APPLICATION_READY = "Application is ready!";

    @ResponseBody
    @RequestMapping(value = "/isAlive", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isAlive() {
        return APPLICATION_LIVENESS;
    }

    @ResponseBody
    @RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isReady() {
        return APPLICATION_READY;
    }

    @ResponseBody
    @RequestMapping(value = "/getPDF", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getGeneratedPDF() {
        Soknad testSoknad = Soknad.builder().aktørId("abcdef")
                .soknadsId("testuuid" + UUID.randomUUID().toString().substring(8))
                .aktørId("abcdefg")
                .fnr("12345612345")
                .navn("Kari Normann Paulsrud Granholt")
                .sendt("2019-01-08T00:00:00.000Z")
                .sporsmal(Arrays.asList(
                        Sporsmal.builder()
                                .id("107")
                                .tag("PERIODEUTLAND")
                                .sporsmalstekst("Når skal du være utenfor Norge?")
                                .svartype(PERIODER)
                                .svar(Arrays.asList(
                                        Svar.builder()
                                        .verdi("2019-01-08T00:00:00.000Z")
                                        .svarverdiType(FOM)
                                        .build(),
                                        Svar.builder()
                                        .verdi("2019-01-11T00:00:00.000Z")
                                        .svarverdiType(TOM)
                                        .build()
                                    )
                                )
                                .build(),
                        Sporsmal.builder()
                                .id("108")
                                .tag("LAND")
                                .sporsmalstekst("Hvilket land skal du reise til?")
                                .svartype(FRITEKST)
                                .svar(Arrays.asList(
                                        Svar.builder()
                                        .verdi("Somalia")
                                        .build()
                                    )
                                )
                                .build(),
                        Sporsmal.builder()
                                .id("109")
                                .tag("ARBEIDSGIVER")
                                .sporsmalstekst("Har du arbeidsgiver?")
                                .svartype(JA_NEI)
                                .svar(Arrays.asList(
                                        Svar.builder()
                                        .verdi("JA").build()
                                ))
                                .undersporsmal(Arrays.asList(
                                        Sporsmal.builder()
                                                .tag("SYKEMELDINGSGRAD")
                                                .sporsmalstekst("Er du 100% sykmeldt?")
                                                .svartype(JA_NEI)
                                                .svar(Arrays.asList(
                                                        Svar.builder()
                                                        .verdi("JA").build()
                                                ))
                                        .build(),
                                        Sporsmal.builder()
                                                .tag("FERIE")
                                                .sporsmalstekst("Skal du ha ferie i hele perioden?")
                                                .svartype(JA_NEI)
                                                .svar(Arrays.asList(
                                                        Svar.builder()
                                                                .verdi("NEI")
                                                                .build()
                                                ))
                                        .build()


                                        )
                                )
                                .build(),
                        Sporsmal.builder()
                        .id("112")
                        .tag("BEKREFT_OPPLYSNINGER_UTLAND_INFO")
                        .svartype(IKKE_RELEVANT)
                        .undertekst("<ul>\\n    <li>Reisen vil ikke gjøre at jeg blir dårligere</li>\n    <li>Reisen vil ikke gjøre at sykefraværet blir lengre</li>\n    <li>Reisen vil ikke hindre planlagt behandling eller oppfølging</li>\n</ul>")
                        .sporsmalstekst("Jeg har lest all informasjonen jeg har fått i søknaden og bekrefter at opplysningene jeg har gitt er korrekte")
                        .build()
                    )
                )
                .build();
        return PDFRestController.getPDF(testSoknad);
    }
}
