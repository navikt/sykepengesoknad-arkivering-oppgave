package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.config.unleash.Toggle;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.SaksbehandlingsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static no.nav.syfo.config.unleash.FeatureToggle.SKAL_LESE_SOKNADER_FRA_KOE;

@Component
@Slf4j
public class SoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;
    private final Toggle toggle;

    @Inject
    public SoknadSendtListener(SaksbehandlingsService saksbehandlingsService, Toggle toggle) {
        this.saksbehandlingsService = saksbehandlingsService;
        this.toggle = toggle;
    }

    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1")
    public void listen(ConsumerRecord<String, String> cr) throws Exception {
        log.info("Mottatt melding med timestamp {} partition {}, offset {}, id {} og value {}",
                toLocalDateTime(cr.timestamp()).format(DateTimeFormatter.ISO_DATE_TIME),
                cr.partition(),
                cr.offset(),
                cr.key(),
                cr.value());

        if (toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)) {
            try {
                Sykepengesoknad deserialisertSoknad = new ObjectMapper().registerModule(new JavaTimeModule()).readValue(cr.value(), Sykepengesoknad.class);
                log.info("Deserialiserte sykepengesøknad: {}", deserialisertSoknad.toString());

                saksbehandlingsService.behandleSoknad(deserialisertSoknad);
            } catch (JsonProcessingException e) {
                log.error("Kunne ikke deserialisere sykepengesøknad", e);
            }
        } else {
            log.info("Togglet av: Skipper saksbehandling");
        }
    }

    private LocalDateTime toLocalDateTime(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
