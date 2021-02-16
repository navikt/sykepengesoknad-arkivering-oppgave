import com.nimbusds.jwt.JWTClaimsSet
import no.nav.security.oidc.test.support.JwtTokenGenerator
import java.util.*

const val AUD = "aud-localhost"
const val ACR = "Level4"
const val EXPIRY = 12960000L

fun createSignedJWT(subject: String, issuer: String): String {
    val jwtClaimsSet = JwtTokenGenerator.buildClaimSet(subject, issuer, AUD, ACR, EXPIRY)
    return JwtTokenGenerator.createSignedJWT(jwtClaimsSet).serialize()
}

fun buildClaimSet(
    subject: String,
    issuer: String,
    audience: String = AUD,
    authLevel: String = ACR,
    expiry: Long = EXPIRY,
    appId: String? = null
): JWTClaimsSet {
    val now = Date()

    val builder = JWTClaimsSet.Builder()
        .subject(subject)
        .issuer(issuer)
        .audience(audience)
        .jwtID(UUID.randomUUID().toString())
        .claim("acr", authLevel)
        .claim("ver", "1.0")
        .claim("nonce", "myNonce")
        .claim("auth_time", now)
        .notBeforeTime(now).issueTime(now)
        .expirationTime(Date(now.time + expiry))

    appId?.let { builder.claim("appid", it) }

    return builder.build()
}
