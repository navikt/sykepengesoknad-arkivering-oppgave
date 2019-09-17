package no.nav.syfo.consumer.util.ws

import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.phase.PhaseInterceptor
import org.apache.cxf.ws.addressing.WSAddressingFeature

import javax.xml.ws.BindingProvider
import javax.xml.ws.handler.Handler

class WsClient<T> {

    fun createPort(serviceUrl: String, portType: Class<*>, handlers: List<Handler<*>>, vararg interceptors: PhaseInterceptor<out Message>): T {
        val jaxWsProxyFactoryBean = JaxWsProxyFactoryBean()
        jaxWsProxyFactoryBean.serviceClass = portType
        jaxWsProxyFactoryBean.address = serviceUrl
        jaxWsProxyFactoryBean.features.add(WSAddressingFeature())
        val port: T = jaxWsProxyFactoryBean.create() as T
        (port as BindingProvider).binding.handlerChain = handlers
        val client = ClientProxy.getClient(port)
        interceptors.map { client.outInterceptors.add(it) }
        STSClientConfig.configureRequestSamlToken<T>(port)
        return port
    }

}
