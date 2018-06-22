package no.nav.syfo.web.selftest;

import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.service.SaksbehandlingsService;
import no.nav.syfo.util.Toggle;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;

@Slf4j
@RestController
@RequestMapping(value = "/internal")
public class SelftestController {
    private static final String APPLICATION_LIVENESS = "Application is alive!";
    private static final String APPLICATION_READY = "Application is ready!";

    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public SelftestController(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

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
    @RequestMapping(value = "behandle-soknad", produces = MediaType.TEXT_PLAIN_VALUE)
    public String test(@RequestParam(name = "aktoer") String aktoerId) {
        if (Toggle.endepunkter) {
            log.info("Fikk aktoer: {}", aktoerId);
            saksbehandlingsService.behandleSoknad(Soknad.builder().aktørId(aktoerId).build());
        }
        return "Ok";
    }
}
