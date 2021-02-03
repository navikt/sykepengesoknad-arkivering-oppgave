package no.nav.syfo.consumer.util.ws

import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.phase.PhaseInterceptor
import org.apache.cxf.ws.addressing.WSAddressingFeature

import javax.xml.ws.BindingProvider
import javax.xml.ws.handler.Handler

inline fun <reified T> createPort(
        serviceUrl: String,
        handlers: List<Handler<*>>,
        wsStsEnabled: Boolean,
        wsAddressingEnabled: Boolean = true,
        vararg interceptors: PhaseInterceptor<out Message>,

): T {
    val jaxWsProxyFactoryBean = JaxWsProxyFactoryBean()
    jaxWsProxyFactoryBean.serviceClass = T::class.java
    jaxWsProxyFactoryBean.address = serviceUrl
    if(wsAddressingEnabled) {
        jaxWsProxyFactoryBean.features.add(WSAddressingFeature())
    }
    val port: T = jaxWsProxyFactoryBean.create() as T

    (port as BindingProvider).binding.handlerChain = handlers
    if(wsStsEnabled) {
        val client = ClientProxy.getClient(port)
        interceptors.map { client.outInterceptors.add(it) }
        STSClientConfig.configureRequestSamlToken(port)
    }
    return port
}
