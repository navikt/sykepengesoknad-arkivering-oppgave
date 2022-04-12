package no.nav.helse.flex.util

import java.util.UUID

object TimedCache {
    private const val cacheTimeValidityInMillis: Long = 1000L * 60 * 10
    private val hashMap = HashMap<UUID, TimedEntry>()

    fun put(key: UUID, value: Int): Int? {
        val previous = hashMap.put(key, TimedEntry(value))
        clearOld()
        return previous?.value
    }

    private fun clearOld() {
        hashMap
            .filter { it.value.isExpired() }
            .forEach { hashMap.remove(it.key) }
    }

    data class TimedEntry(val value: Int) {
        private val creationTime: Long = now()

        fun isExpired() = (now() - creationTime) > cacheTimeValidityInMillis

        private fun now() = System.currentTimeMillis()
    }
}
