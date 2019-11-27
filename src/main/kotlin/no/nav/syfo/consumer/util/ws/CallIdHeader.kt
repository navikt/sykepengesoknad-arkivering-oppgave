package no.nav.syfo.consumer.util.ws

import no.nav.syfo.kafka.NAV_CALLID
import no.nav.syfo.log
import org.apache.cxf.binding.soap.SoapHeader
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.jaxb.JAXBDataBinding
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.slf4j.MDC
import java.util.UUID.randomUUID
import javax.xml.bind.JAXBException
import javax.xml.namespace.QName

class CallIdHeader : AbstractPhaseInterceptor<Message>(Phase.PRE_STREAM) {
    @Throws(Fault::class)
    override fun handleMessage(message: Message) {
        try {
            val qName = QName("uri:no.nav.applikasjonsrammeverk", "callId")
            val header = SoapHeader(qName, callId(), JAXBDataBinding(String::class.java))
            (message as SoapMessage).headers.add(header)
        } catch (ex: JAXBException) {
            log.warn("Error while setting CallId header", ex)
        }

    }

    private fun callId() = MDC.get(NAV_CALLID) ?: randomUUID().toString()

    companion object {
        private val log = CallIdHeader::class.java.log()
    }
}
