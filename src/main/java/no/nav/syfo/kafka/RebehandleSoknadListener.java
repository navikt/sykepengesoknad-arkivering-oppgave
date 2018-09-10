package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.service.BehandleFeiledeSoknaderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;

@Slf4j
@Component
public class RebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private String groupId;
    private Consumer<String, String> consumer;

    @Inject
    public RebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO,
            @Value("${fasit.environment.name}") String miljonavn,
            ConsumerFactory<String, String> consumerFactory) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;
        groupId = "syfogsak-" + miljonavn + "-rebehandleSoknadsss";
        consumer = consumerFactory.createConsumer(groupId, "pre", "post");
        consumer.subscribe(Collections.singletonList("aapen-syfo-soeknadSendt-v1"));
    }

    @Scheduled(cron = "* */30 * * * *")
    public void listen() {
        log.info("Leter etter feilede søknader");
        consumer.poll(100L);
        consumer.seekToBeginning(consumer.assignment());
        ConsumerRecords<String, String> records;
        try {
            while (!(records=consumer.poll(1000L)).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    log.info("Melding mottatt på groupid: {}, topic: {}, partisjon: {} med offsett: {}",
                            groupId, record.topic(), record.partition(), record.offset());
                    try {
                        Sykepengesoknad deserialisertSoknad = objectMapper.readValue(record.value(), Sykepengesoknad.class);

                        innsendingDAO
                                .hentFeiletInnsendingForSoknad(deserialisertSoknad.getId())
                                .ifPresent(innsending -> {
                                    behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, deserialisertSoknad);
                                    log.info("Søknad med id {} og offset {} er rebehandlet i innsending med id {}",
                                            deserialisertSoknad.getId(),
                                            record.offset(),
                                            innsending.getInnsendingsId()
                                    );
                                });
                    } catch (JsonProcessingException e) {
                        log.error("Kunne ikke deserialisere sykepengesøknad", e);
                        throw new RuntimeException("Kunne ikke deserialisere sykepengesøknad");
                    } catch (Exception e) {
                        log.error("Uventet feil ved behandling av søknad", e);
                        throw new RuntimeException("Uventet feil ved behandling av søknad");
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        }
    }
}
