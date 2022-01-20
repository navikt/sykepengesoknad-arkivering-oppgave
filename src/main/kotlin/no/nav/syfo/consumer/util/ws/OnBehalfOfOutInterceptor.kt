package no.nav.syfo.consumer.util.ws

import no.nav.syfo.logger
import org.apache.cxf.interceptor.Fault
import org.apache.cxf.message.Message
import org.apache.cxf.phase.AbstractPhaseInterceptor
import org.apache.cxf.phase.Phase
import org.apache.cxf.rt.security.SecurityConstants
import org.w3c.dom.Element
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.IOException
import java.io.StringReader
import java.util.*
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException

class OnBehalfOfOutInterceptor : AbstractPhaseInterceptor<Message>(Phase.SETUP) {

    enum class TokenType constructor(var valueType: String) {
        OIDC("urn:ietf:params:oauth:token-type:jwt")
    }

    @Throws(Fault::class)
    override fun handleMessage(message: Message) {
        log.debug("looking up OnBehalfOfToken from requestcontext with key:$REQUEST_CONTEXT_ONBEHALFOF_TOKEN")
        val token = message[REQUEST_CONTEXT_ONBEHALFOF_TOKEN] as String
        val tokenType = message[REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE] as TokenType

        val tokenBytes = token.toByteArray()
        val wrappedToken = wrapTokenForTransport(tokenBytes, tokenType)

        // This will make sure that the STS client puts the OnBehalfOf Element in the token issue request
        message[SecurityConstants.STS_TOKEN_ON_BEHALF_OF] = createOnBehalfOfElement(wrappedToken)
    }

    private fun wrapTokenForTransport(token: ByteArray, tokenType: TokenType): String {
        when (tokenType) {
            TokenType.OIDC -> return wrapWithBinarySecurityToken(token, tokenType.valueType)
            else -> throw RuntimeException("unsupported token type:$tokenType")
        }
    }

    companion object {
        private val log = OnBehalfOfOutInterceptor::class.java.logger()

        const val REQUEST_CONTEXT_ONBEHALFOF_TOKEN_TYPE = "request.onbehalfof.tokentype"
        const val REQUEST_CONTEXT_ONBEHALFOF_TOKEN = "request.onbehalfof.token"

        private fun createOnBehalfOfElement(content: String): Element? {
            try {
                val factory = DocumentBuilderFactory.newInstance()
                factory.isNamespaceAware = true
                factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
                val builder = factory.newDocumentBuilder()
                val document = builder.parse(InputSource(StringReader(content)))
                return document.documentElement
            } catch (e: ParserConfigurationException) {
                throw RuntimeException(e)
            } catch (e: SAXException) {
                throw RuntimeException(e)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        private fun wrapWithBinarySecurityToken(token: ByteArray, valueType: String): String {
            val base64encodedToken = Base64.getEncoder().encodeToString(token)
            return (
                "<wsse:BinarySecurityToken xmlns:wsse=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd\"" +
                    " EncodingType=\"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary\"" +
                    " ValueType=\"" + valueType + "\" >" + base64encodedToken + "</wsse:BinarySecurityToken>"
                )
        }
    }
} // This can be in any stage before the WS-SP interceptors
// setup the STS client and issued token interceptor.
