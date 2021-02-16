package no.nav.syfo.consumer.token

import java.time.Instant

data class Token(
    val access_token: String,
    val token_type: String,
    val expires_in: Int
) {

    val expirationTime: Instant = Instant.now().plusSeconds(expires_in - 10L)

    companion object {
        fun shouldRenewToken(token: Token?): Boolean {
            if (token == null) {
                return true
            }
            return isExpired(token)
        }

        private fun isExpired(token: Token): Boolean {
            return token.expirationTime.isBefore(Instant.now())
        }
    }
}
