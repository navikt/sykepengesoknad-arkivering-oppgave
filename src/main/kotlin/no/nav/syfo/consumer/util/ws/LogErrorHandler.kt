package no.nav.syfo.consumer.util.ws

import no.nav.syfo.log
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.jaxws.handler.soap.SOAPMessageContextImpl
import org.apache.cxf.message.Message
import org.apache.cxf.service.Service
import org.apache.cxf.service.model.OperationInfo
import javax.xml.namespace.QName
import javax.xml.ws.handler.MessageContext
import javax.xml.ws.handler.soap.SOAPHandler
import javax.xml.ws.handler.soap.SOAPMessageContext

class LogErrorHandler : SOAPHandler<SOAPMessageContext> {

    override fun getHeaders(): Set<QName>? = null
    override fun handleMessage(context: SOAPMessageContext) = true
    override fun close(context: MessageContext) {}

    override fun handleFault(context: SOAPMessageContext): Boolean {
        if (context is SOAPMessageContextImpl) {
            val message = context.wrappedMessage
            var exception: Throwable = message.getContent(Exception::class.java)

            if (exception is Fault) exception = exception.cause ?: exception

            log().error(beskrivelse(message).toString(), exception)
        }
        return true
    }

    private fun beskrivelse(message: Message): StringBuilder {
        val beskrivelse = StringBuilder()
        beskrivelse.append("Det oppstod en feil i WS-kallet")

        message.exchange.let { exchange ->
            exchange.get(Service::class.java).let { service ->
                beskrivelse.append(" \'").append(service.name)
                exchange.get(OperationInfo::class.java).let { operationInfo ->
                    beskrivelse.append("#").append(operationInfo.name)
                }
                beskrivelse.append("\'")
            }
        }

        return beskrivelse.append(":")
    }
}
