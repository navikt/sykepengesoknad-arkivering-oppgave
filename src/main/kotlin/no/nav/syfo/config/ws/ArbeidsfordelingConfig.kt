package no.nav.syfo.config.ws

import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.WsClient
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ArbeidsfordelingConfig {
    @Bean
    @ConditionalOnProperty(value = ["mockWS"], havingValue = "false", matchIfMissing = true)
    @Primary
    fun arbeidsfordelingV1(@Value("\${virksomhet.arbeidsfordeling.v1.endpointurl}") serviceUrl: String): ArbeidsfordelingV1 {
        return WsClient<ArbeidsfordelingV1>().createPort(serviceUrl, ArbeidsfordelingV1::class.java, listOf(LogErrorHandler()))
    }
}
