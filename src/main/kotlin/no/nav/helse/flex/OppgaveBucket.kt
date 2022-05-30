package no.nav.helse.flex

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
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
        val blobId = "newFile.csv"

        createBlob(
            blobId = blobId,
            file = "abc, oiegjr"
        )

        val blob = getBlob(blobId)

        log.info("Hentet blob ${blob.metadata} ${blob.bucket} ${blob.blobId} $blob")
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
        val bId = BlobId.of(bucketName, blobId)

        val bInfo = BlobInfo.newBuilder(bId)
            .setContentType("text/csv")
            .build()

        storage.create(bInfo, file.encodeToByteArray())
    }
}
