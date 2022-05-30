package no.nav.helse.flex

import com.google.cloud.storage.Storage
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.TimeUnit

class OppgaveBucket(
    @Value("\${BUCKET_NAME}") private val bucketName: String,
    private val storage: Storage,
) {

    val log = logger()

    @Scheduled(initialDelay = 120, fixedDelay = 1_000, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val bucket = storage.get(bucketName) ?: throw RuntimeException("Fant ikke bøtte ved navn $bucketName")
        val blob = bucket.get("unik.csv")

        log.info("Hentet blob ${blob.metadata}")
    }
}
