package no.nav.helse.flex

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class OppgaveBucket(
    @Value("\${BUCKET_NAME}") private val bucketName: String,
) {

    private val log = logger()
    private val storage = StorageOptions.newBuilder().build().service

    @Scheduled(initialDelay = 120, fixedDelay = 1_000, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val blob = getBlob("unik.csv")

        log.info("Hentet blob ${blob.metadata}")

        createBlob(
            blobId = "newFile.csv",
            file = "abc, oiegjr"
        )
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
            .setMetadata(mapOf("content-type" to "text/csv"))
            .build()

        storage.create(bInfo, file.encodeToByteArray())
    }
}
