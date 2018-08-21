package no.nav.syfo.web.selftest;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/internal")
public class SelftestController {
    private static final String APPLICATION_LIVENESS = "Application is alive!";
    private static final String APPLICATION_READY = "Application is ready!";

    private MeterRegistry registry;

    public SelftestController(MeterRegistry registry) {
        this.registry = registry;
    }

    @ResponseBody
    @RequestMapping(value = "/isAlive", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> isAlive() {

        double antallFataleKafkafeil = registry.counter("syfogsak.kafka.feil", Tags.of("type", "fatale")).count();
        if (antallFataleKafkafeil > 2.0) {
            return new ResponseEntity<>("Feil: for mange fatale kafkafeil", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return ResponseEntity.ok(APPLICATION_LIVENESS);
    }

    @ResponseBody
    @RequestMapping(value = "/isReady", produces = MediaType.TEXT_PLAIN_VALUE)
    public String isReady() {
        return APPLICATION_READY;
    }
}
