package no.nav.syfo.config.ws;

import no.nav.syfo.consumer.util.ws.LogErrorHandler;
import no.nav.syfo.consumer.util.ws.WsClient;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Collections;

@Configuration
public class ArbeidsfordelingConfig {

    @SuppressWarnings("unchecked")
    @Bean
    @ConditionalOnProperty(value = "mockWS", havingValue = "false", matchIfMissing = true)
    @Primary
    public ArbeidsfordelingV1 arbeidsfordelingV1(@Value("${virksomhet.arbeidsfordeling.v1.endpointurl}") String serviceUrl) {
        return new WsClient<ArbeidsfordelingV1>().createPort(serviceUrl, ArbeidsfordelingV1.class, Collections.singletonList(new LogErrorHandler()));
    }
}
