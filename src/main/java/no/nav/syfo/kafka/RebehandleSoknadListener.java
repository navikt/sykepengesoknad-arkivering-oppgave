package no.nav.syfo.kafka;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import no.nav.syfo.consumer.repository.InnsendingDAO;
import no.nav.syfo.domain.Innsending;
import no.nav.syfo.domain.dto.Sykepengesoknad;
import no.nav.syfo.kafka.interfaces.Soknad;
import no.nav.syfo.kafka.mapper.SoknadDtoToSykepengesoknadMapper;
import no.nav.syfo.kafka.mapper.SykepengesoknadDtoToSykepengesoknadMapper;
import no.nav.syfo.kafka.soknad.dto.SoknadDTO;
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


@Slf4j
@Component
public class RebehandleSoknadListener {
    private final BehandleFeiledeSoknaderService behandleFeiledeSoknaderService;
    private final InnsendingDAO innsendingDAO;
    private final MeterRegistry registry;
    private Consumer<String, Soknad> consumer;

    @Inject
    public RebehandleSoknadListener(
            BehandleFeiledeSoknaderService behandleFeiledeSoknaderService,
            InnsendingDAO innsendingDAO,
            MeterRegistry registry,
            @Value("${fasit.environment.name}") String miljonavn,
            ConsumerFactory<String, Soknad> consumerFactory) {
        this.behandleFeiledeSoknaderService = behandleFeiledeSoknaderService;
        this.innsendingDAO = innsendingDAO;
        this.registry = registry;

        String groupId = "syfogsak-" + miljonavn + "-rebehandleSoknad";
        consumer = consumerFactory.createConsumer(groupId, "rebehandleSoknad");
        consumer.subscribe(Collections.singletonList("syfo-soknad-v2"));
    }

    @Scheduled(cron = "0 0 * * * *")
    public void listen() {
        List<Innsending> feilendeInnsendinger = innsendingDAO.hentFeilendeInnsendinger();

        consumer.poll(100L);
        consumer.seekToBeginning(consumer.assignment());
        ConsumerRecords<String, Soknad> records;
        try {
            while (!(records = consumer.poll(1000L)).isEmpty()) {
                for (ConsumerRecord<String, Soknad> record : records) {
                    log.debug("Melding mottatt på topic: {}, partisjon: {} med offset: {}",
                            record.topic(), record.partition(), record.offset());
                    try {
                        MDC.put(CALL_ID, getLastHeaderByKeyAsString(record.headers(), CALL_ID).orElseGet(randomUUID()::toString));

                        Sykepengesoknad sykepengesoknad = null;
                        if (record.value() instanceof SykepengesoknadDTO) {
                            sykepengesoknad = SykepengesoknadDtoToSykepengesoknadMapper.INSTANCE.konverter((SykepengesoknadDTO) record.value());
                        } else if (record.value() instanceof SoknadDTO) {
                            sykepengesoknad = SoknadDtoToSykepengesoknadMapper.INSTANCE.konverter((SoknadDTO) record.value());
                        }
                        if (sykepengesoknad != null) {
                            final Sykepengesoknad finalsoknad = sykepengesoknad;
                            feilendeInnsendinger.stream()
                                    .filter(innsending -> innsending.getRessursId().equals(finalsoknad.getId()))
                                    .peek(innsending -> log.info("Rebehandler søknad med id {}", innsending.getRessursId()))
                                    .findAny()
                                    .ifPresent(innsending -> behandleFeiledeSoknaderService.behandleFeiletSoknad(innsending, finalsoknad));
                        }
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
