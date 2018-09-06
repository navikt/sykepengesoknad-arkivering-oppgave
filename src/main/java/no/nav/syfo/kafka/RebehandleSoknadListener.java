package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.BehandleFeiledeSoknaderService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

@Slf4j
@Component
public class RebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Inject
    public RebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO
    ) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;
    }


    @KafkaListener(topics = "aapen-syfo-soeknadSendt-v1", id = "rebehandleSoknad")
    public void listen(ConsumerRecord<String, String> cr, Acknowledgment acknowledgment) {
        log.info("Melding mottatt på topic: {}, partisjon: {} med offsett: {}", cr.topic(), cr.partition(), cr.offset());

        try {
            Sykepengesoknad deserialisertSoknad = objectMapper.readValue(cr.value(), Sykepengesoknad.class);

            innsendingDAO
                    .hentFeiletInnsendingForSoknad(deserialisertSoknad.getId())
                    .ifPresent(innsending -> {
                        String innsendingId = behandleFeiledeSoknaderService.behandleFeiletSoknad(deserialisertSoknad, innsending);
                        log.info("Søknad med id {} og offset {} er rebehandlet i innsending med id {}",
                                deserialisertSoknad.getId(),
                                cr.offset(),
                                innsendingId
                        );
                    });

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
