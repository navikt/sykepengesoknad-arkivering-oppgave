package no.nav.syfo.config.ws;

import no.nav.syfo.consumer.util.ws.LogErrorHandler;
import no.nav.syfo.consumer.util.ws.WsClient;
import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.OppgavebehandlingV3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@Configuration
public class OppgavebehandlingConfig {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockWS", havingValue = "false", matchIfMissing = true)
    @Primary
    public OppgavebehandlingV3 oppgavebehandlingV3(@Value("${servicegateway.url}") String serviceUrl) {
        return new WsClient<OppgavebehandlingV3>().createPort(serviceUrl, OppgavebehandlingV3.class, Collections.singletonList(new LogErrorHandler()));
    }
}
