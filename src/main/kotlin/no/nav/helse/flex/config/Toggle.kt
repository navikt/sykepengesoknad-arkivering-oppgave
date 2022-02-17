package no.nav.helse.flex.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class Toggle(
    @Value("\${fasit.environment.name:p}") val fasitEnvironmentName: String
) {
    fun isQ(): Boolean =
        fasitEnvironmentName == "q1"
}
