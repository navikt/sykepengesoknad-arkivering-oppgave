package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.Soknad;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.SaksbehandlingsService;
import no.nav.syfo.util.Toggle;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class SoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;

    @Inject
    public SoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1")
    public void listen(ConsumerRecord<String, String> cr) throws Exception {
        log.info("Mottatt melding med timestamp {} partition {}, offset {}, id {} og value {}",
                toLocalDateTime(cr.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME),
                cr.partition(),
                cr.offset(),
                cr.key(),
                cr.value());

        if (Toggle.skipSaksbehandling) {
            log.info("Togglet av: Skipper saksbehandling");
            return;
        }

        try {
            Sykepengesoknad deserialisertSoknad = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(cr.value(), Sykepengesoknad.class);
            log.info("Deserialiserte sykepengesøknad: {}", deserialisertSoknad.toString());

            Soknad soknad = Soknad.lagSoknad(deserialisertSoknad);
            saksbehandlingsService.behandleSoknad(soknad);
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke deserialisere sykepengesøknad", e);
        }
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
