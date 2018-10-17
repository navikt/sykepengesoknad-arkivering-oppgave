package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.SaksbehandlingsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.config.ApplicationConfig.CALL_ID;

@Deprecated
@Component
@Slf4j
public class DeprecatedSoknadSendtListener {

    private final SaksbehandlingsService saksbehandlingsService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Inject
    public DeprecatedSoknadSendtListener(SaksbehandlingsService saksbehandlingsService) {
        this.saksbehandlingsService = saksbehandlingsService;
    }

    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1", id = "deprecatedSoknadSendt", idIsGroup = false, containerFactory = "deprecatedKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) {
        try {
            MDC.put(CALL_ID, randomUUID().toString());

            log.debug("Melding mottatt på topic: {}, partisjon: {} med offsett: {}", cr.topic(), cr.partition(), cr.offset());
            Sykepengesoknad deserialisertSoknad = objectMapper.readValue(cr.value(), Sykepengesoknad.class);
            saksbehandlingsService.behandleSoknad(deserialisertSoknad);

            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke deserialisere sykepengesøknad", e);
            throw new RuntimeException("Kunne ikke deserialisere sykepengesøknad");
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        } finally {
            MDC.remove(CALL_ID);
        }
    }
}
