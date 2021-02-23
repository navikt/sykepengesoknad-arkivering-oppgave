import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.syfo.AAD
import java.util.*
import kotlin.collections.HashMap

fun MockOAuth2Server.token(
    issuerId: String = AAD,
    clientId: String = UUID.randomUUID().toString(),
    subject: String = UUID.randomUUID().toString(),
    audience: String = "syfogsak-client-id",
    appId: String? = "syfoinntektsmelding_clientid",
): String {
    val claims = HashMap<String, String>()
    if (appId != null) {
        claims["appid"] = appId
    }
    return this.issueToken(
        issuerId,
        clientId,
        DefaultOAuth2TokenCallback(
            issuerId,
            subject,
            listOf(audience),
            claims,
            3600
        )
    ).serialize()
}
