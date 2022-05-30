package no.nav.helse.flex

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.threeten.bp.Duration
import java.util.concurrent.TimeUnit

@Component
class OppgaveBucket(
    @Value("\${BUCKET_NAME}") private val bucketName: String,
) {

    private val log = logger()
    private val retrySettings = RetrySettings.newBuilder().setTotalTimeout(Duration.ofMillis(3000)).build()
    private val storage = StorageOptions.newBuilder().setRetrySettings(retrySettings).build().service

    @Scheduled(initialDelay = 120, fixedDelay = 1_000, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val blobId = "test.csv"

        val blob = getBlob(blobId)

        readFile(blob)
    }

    private fun readFile(blob: Blob) {
        val content = blob.getContent().decodeToString()

        log.info(content)

        content.lines().forEachIndexed { lidx, line ->
            log.info("linje $lidx = $line")
            line.split(',').forEachIndexed { ridx, row ->
                log.info("linje $lidx row $ridx = $row")
            }
        }
    }

    private fun getBlob(
        blobId: String
    ): Blob {
        val bucket = storage.get(bucketName) ?: throw RuntimeException("Fant ikke bøtte ved navn $bucketName")
        return bucket.get(blobId)
    }

    private fun createBlob(
        blobId: String,
        file: String,
    ) {
        val bInfo = BlobInfo.newBuilder(bucketName, blobId)
            .setContentType("text/csv")
            .build()

        storage.create(bInfo, file.encodeToByteArray())
    }
}
