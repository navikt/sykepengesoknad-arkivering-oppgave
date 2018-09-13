package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.domain.Innsending;
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
import java.util.List;

@Slf4j
@Component
public class RebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Consumer<String, String> consumer;

    @Inject
    public RebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO,
            @Value("${fasit.environment.name}") String miljonavn,
            ConsumerFactory<String, String> consumerFactory) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;

        String groupId = "syfogsak-" + miljonavn + "-rebehandleSoknads";
        consumer = consumerFactory.createConsumer(groupId, "pre", "post");
        consumer.subscribe(Collections.singletonList("aapen-syfo-soeknadSendt-v1"));
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void listen() {
        List<Innsending> feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger();

        consumer.poll(100L);
        consumer.seekToBeginning(consumer.assignment());
        ConsumerRecords<String, String> records;
        try {
            while (!(records=consumer.poll(1000L)).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    try {
                        Sykepengesoknad deserialisertSoknad = objectMapper.readValue(record.value(), Sykepengesoknad.class);

                        feilendeInnsendinger.stream()
                                .filter(innsending -> innsending.getRessursId().equals(deserialisertSoknad.getId()))
                                .findAny()
                                .ifPresent(innsending -> {
                                    behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, deserialisertSoknad);
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
