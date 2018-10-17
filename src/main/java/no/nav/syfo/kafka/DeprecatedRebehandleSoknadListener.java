package no.nav.syfo.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
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

@Deprecated
@Slf4j
@Component
public class DeprecatedRebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final MeterRegistry registry;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Consumer<String, String> consumer;

    @Inject
    public DeprecatedRebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO,
            MeterRegistry registry,
            @Value("${fasit.environment.name}") String miljonavn,
            ConsumerFactory<String, String> deprecatedConsumerFactory) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;
        this.registry = registry;

        String groupId = "syfogsak-" + miljonavn + "-rebehandleSoknad";
        consumer = deprecatedConsumerFactory.createConsumer(groupId, "", "");
        consumer.subscribe(Collections.singletonList("aapen-syfo-soeknadSendt-v1"));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void listen() {
        List<Innsending> feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger();

        consumer.poll(100L);
        consumer.seekToBeginning(consumer.assignment());
        ConsumerRecords<String, String> records;
        try {
            while (!(records = consumer.poll(1000L)).isEmpty()) {
                for (ConsumerRecord<String, String> record : records) {
                    log.debug("Melding mottatt på topic: {}, partisjon: {} med offsett: {}",
                            record.topic(), record.partition(), record.offset());
                    try {
                        Sykepengesoknad deserialisertSoknad = objectMapper.readValue(record.value(), Sykepengesoknad.class);

                        feilendeInnsendinger.stream()
                                .filter(innsending -> innsending.getRessursId().equals(deserialisertSoknad.getId()))
                                .findAny()
                                .ifPresent(innsending -> behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, deserialisertSoknad));
                    } catch (JsonProcessingException e) {
                        log.warn("Kunne ikke deserialisere sykepengesøknad", e);
                    } catch (Exception e) {
                        log.warn("Uventet feil ved behandling av søknad", e);
                    }
                }
            }
        } catch (WakeupException e) {
            // ignore for shutdown
        }
        final int antallFeilende = innsendingDAO.hentFeilendeInnsendinger().size();
        log.info("registrerer metrikker, {} rebehandlinger feiler.", antallFeilende);
        registry.gauge("syfogsak.rebehandling.feilet",
                Tags.of(
                        "type", "info",
                        "help", "Antall innsendinger som fortsatt feiler etter rebehandling."
                ),
                antallFeilende);
    }
}
