package no.nav.eessi.pensjon.security.sts

import org.apache.cxf.Bus
import org.apache.cxf.binding.soap.Soap12
import org.apache.cxf.binding.soap.SoapMessage
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.ws.policy.PolicyBuilder
import org.apache.cxf.ws.policy.PolicyEngine
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver
import org.apache.cxf.ws.security.SecurityConstants
import org.apache.cxf.ws.security.trust.STSClient
import org.apache.neethi.Policy
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

lateinit var STS_REQUEST_SAML_POLICY: String
const val STS_CLIENT_AUTHENTICATION_POLICY = "classpath:policy/untPolicy.xml"

@Service
class StsOidcToSamlService {

    @Value("\${security-token-service-token.url}")
    lateinit var baseUrl: String

    @Value("\${srveessipensjon.username}")
    lateinit var username: String

    @Value("\${srveessipensjon.password}")
    lateinit var password: String

    // Only requestSamlPolicyNoTransportBinding.xml on localhost, should use the requestSamlPolicy.xml with transport binding https when in production.
    @Value("\${requestsamlpolicy.path:classpath:policy/requestSamlPolicy.xml}")
    fun setRequestSamlPolicy(path: String) {
        STS_REQUEST_SAML_POLICY = path
    }

    fun <T> configureRequestSamlToken(service: T) {
        val client = ClientProxy.getClient(service)
        configureSTSRequestSamlToken(client)
    }

    private fun configureSTSRequestSamlToken(client: Client) {
        val stsClient = createCustomSTSClient(client.bus)
        configureSTSWithPolicyForClient(stsClient, client, STS_REQUEST_SAML_POLICY)
    }

    private fun configureSTSWithPolicyForClient(stsClient: STSClient, client: Client, policyReference: String) {
        configureSTSClient(stsClient)
        client.requestContext[SecurityConstants.STS_CLIENT] = stsClient
        client.requestContext[SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT] = true
        setEndpointPolicyReference(client, policyReference)
    }

    /**
     * Creating custom STS client because the STS on Datapower requires KeyType as a child to RequestSecurityToken and
     * TokenType as a child to SecondaryParameters. Standard CXF client put both elements in SecondaryParameters. By overriding
     * the useSecondaryParameters method you can exactly specify the request in the RequestSecurityTokenTemplate in the policy.
     * @param bus
     * @return custom STSClient
     */
    private fun createCustomSTSClient(bus: Bus): STSClient {
        /**
         * Only here to allow to use elements for both WS-Trust 1.3 and 1.4 in the request, as the STS implemented on Datapower
         * requires the use of KeyType directly as child to RequestSecurityToken even if you use SecondaryParameters.
         *
         * Setting this to false should allow you to specify a RequestSecurityTokenTemplate with SecondaryParameters in
         * policy attachment, at the same time as KeyType is specified as a child to RequestSecurityToken.
         */
        class STSClientWSTrust13And14(b: Bus) : STSClient(b) {
            override fun useSecondaryParameters(): Boolean {
                return false
            }
        }
        return STSClientWSTrust13And14(bus)
    }

    private fun configureSTSClient(stsClient: STSClient) {
        stsClient.apply {
            isEnableAppliesTo = false
            isAllowRenewing = false
            location = baseUrl
            // Debug/logging av meldinger som sendes mellom app og STS
            // features = listOf(LoggingFeature()) // TODO: Add denne featureren bare dersom DEBUG er enabled
            properties = mapOf(
                SecurityConstants.USERNAME to username,
                SecurityConstants.PASSWORD to password
            )
            setPolicy(STS_CLIENT_AUTHENTICATION_POLICY)
        }
    }


    private fun setEndpointPolicyReference(client: Client, uri: String) {
        val policy: Policy = resolvePolicyReference(client, uri)
        setClientEndpointPolicy(client, policy)
    }

    private fun resolvePolicyReference(client: Client, uri: String): Policy {
        val policyBuilder: PolicyBuilder = client.bus.getExtension(PolicyBuilder::class.java)
        val resolver: ReferenceResolver = RemoteReferenceResolver("", policyBuilder)
        return resolver.resolveReference(uri)
    }

    private fun setClientEndpointPolicy(client: Client, policy: Policy) {
        val endpoint = client.endpoint
        val endpointInfo = endpoint.endpointInfo
        val policyEngine = client.bus.getExtension(PolicyEngine::class.java)
        val message = SoapMessage(Soap12.getInstance())
        val endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message)
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message))
    }

}