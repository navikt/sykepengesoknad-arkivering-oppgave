package no.nav.helse.flex.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class EnvironmentToggles(
    @param:Value("\${NAIS_CLUSTER_NAME}") private val naisCluster: String,
) {
    fun isDevGcp() = "dev-gcp" == naisCluster
}
