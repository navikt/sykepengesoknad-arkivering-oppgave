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

    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1", id = "soknadSendt", idIsGroup = true)
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) throws Exception {
        log.info("Melding mottatt på topic: {} med offsett: {}", cr.topic(), cr.offset());
        try {
            Sykepengesoknad deserialisertSoknad = objectMapper.readValue(cr.value(), Sykepengesoknad.class);
            String innsendingId = saksbehandlingsService.behandleSoknad(deserialisertSoknad);

            log.info("Søknad med id {} og offset {} er behandlet i innsending med id {}",
                    deserialisertSoknad.getId(),
                    cr.offset(),
                    innsendingId
            );
            acknowledgment.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Kunne ikke deserialisere sykepengesøknad", e);
            throw new RuntimeException("Kunne ikke deserialisere sykepengesøknad");
        } catch (Exception e) {
            log.error("Uventet feil ved behandling av søknad", e);
            throw new RuntimeException("Uventet feil ved behandling av søknad");
        }
    }
}
