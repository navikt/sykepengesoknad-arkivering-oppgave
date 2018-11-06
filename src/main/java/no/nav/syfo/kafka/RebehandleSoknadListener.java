package no.nav.syfo.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.kafka.mapper.DtoToSykepengesoknadMapper;
import no.nav.syfo.kafka.sykepengesoknad.dto.SykepengesoknadDTO;
import no.nav.syfo.service.BehandleFeiledeSoknaderService;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

import static java.util.UUID.randomUUID;
import static no.nav.syfo.config.ApplicationConfig.CALL_ID;
import static no.nav.syfo.kafka.KafkaHeaderConstants.getLastHeaderByKeyAsString;
import static no.nav.syfo.kafka.mapper.DtoToSykepengesoknadMapper.konverter;

@Slf4j
@Component
public class RebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final MeterRegistry registry;
    private Consumer<String, SykepengesoknadDTO> consumer;

    @Inject
    public RebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO,
            MeterRegistry registry,
            @Value("${fasit.environment.name}") String miljonavn,
            ConsumerFactory<String, SykepengesoknadDTO> consumerFactory) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;
        this.registry = registry;

        String groupId = "syfogsak-" + miljonavn + "-rebehandleSoknad";
        consumer = consumerFactory.createConsumer(groupId, "", "");
        consumer.subscribe(Collections.singletonList("privat-syfo-soknadSendt-v1"));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void listen() {
        List<Innsending> feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger();

        consumer.poll(100L);
        consumer.seekToBeginning(consumer.assignment());
        ConsumerRecords<String, SykepengesoknadDTO> records;
        try {
            while (!(records = consumer.poll(1000L)).isEmpty()) {
                for (ConsumerRecord<String, SykepengesoknadDTO> record : records) {
                    log.debug("Melding mottatt på topic: {}, partisjon: {} med offsett: {}",
                            record.topic(), record.partition(), record.offset());
                    try {
                        MDC.put(CALL_ID, getLastHeaderByKeyAsString(record.headers(), CALL_ID, randomUUID().toString()));

                        Sykepengesoknad sykepengesoknad = konverter(record.value());

                        feilendeInnsendinger.stream()
                                .filter(innsending -> innsending.getRessursId().equals(sykepengesoknad.getId()))
                                .peek(innsending -> log.info("Rebehandler søknad med id {}", innsending.getRessursId()))
                                .findAny()
                                .ifPresent(innsending -> behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, sykepengesoknad));
                    } catch (Exception e) {
                        log.warn("Uventet feil ved behandling av søknad", e);
                    } finally {
                        MDC.remove(CALL_ID);
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