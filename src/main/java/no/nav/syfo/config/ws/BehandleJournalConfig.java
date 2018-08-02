package no.nav.syfo.config.ws;

import no.nav.syfo.consumer.util.ws.LogErrorHandler;
import no.nav.syfo.consumer.util.ws.WsClient;
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@Configuration
public class BehandleJournalConfig {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockWS", havingValue = "false", matchIfMissing = true)
    @Primary
    public BehandleJournalV2 behandleJournalV2(@Value("${behandleJournal.v2.endpointurl}") String serviceUrl) {
        return new WsClient<BehandleJournalV2>().createPort(serviceUrl, BehandleJournalV2.class, Collections.singletonList(new LogErrorHandler()));
    }
}