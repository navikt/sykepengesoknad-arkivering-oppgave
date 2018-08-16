package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.SaksbehandlingsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Component
@Slf4j
public class SoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Inject
    public SoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1", id = "soknadSendt", idIsGroup = false)
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) throws Exception {
        try {
            Sykepengesoknad deserialisertSoknad = objectMapper.readValue(cr.value(), Sykepengesoknad.class);
            String soknadId = saksbehandlingsService.behandleSoknad(deserialisertSoknad);

            log.info("Søknad med id {} og offset {} er behandlet", soknadId, cr.offset());
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke deserialisere sykepengesøknad", e);
        }
    }
}
