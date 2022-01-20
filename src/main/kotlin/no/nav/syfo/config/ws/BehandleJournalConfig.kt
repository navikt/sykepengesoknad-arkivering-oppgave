package no.nav.syfo.config.ws

import no.nav.syfo.consumer.util.ws.LogErrorHandler
import no.nav.syfo.consumer.util.ws.createPort
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class BehandleJournalConfig {
    @Bean
    @ConditionalOnProperty(value = ["mockWS"], havingValue = "false", matchIfMissing = true)
    @Primary
    fun behandleJournalV2(
        @Value("\${behandleJournal.v2.endpointurl}") serviceUrl: String,
        @Value("\${ws.sts.enabled:true}") wsStsEnabled: Boolean
    ): BehandleJournalV2 {
        return createPort(serviceUrl = serviceUrl, handlers = listOf(LogErrorHandler()), wsStsEnabled = wsStsEnabled)
    }
}
