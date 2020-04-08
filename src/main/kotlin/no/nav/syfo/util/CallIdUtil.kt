package no.nav.syfo.util

import no.nav.syfo.kafka.NAV_CALLID
import org.slf4j.MDC
import java.util.*


fun callId(): String {
    val callId = MDC.get(NAV_CALLID)
    return if (callId.isNullOrEmpty()) {
        UUID.randomUUID().toString()
    } else {
        callId
    }
}
