package no.nav.helse.flex

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.threeten.bp.Duration
import java.time.LocalDate
import java.util.concurrent.TimeUnit

@Component
class OppgaveBucket(
    @Value("\${BUCKET_NAME}") private val bucketName: String,
    private val sykepengesoknadBackendClient: SykepengesoknadBackendClient,
) {

    private val log = logger()
    private val retrySettings = RetrySettings.newBuilder().setTotalTimeout(Duration.ofMillis(3000)).build()
    private val storage = StorageOptions.newBuilder().setRetrySettings(retrySettings).build().service

    @Scheduled(initialDelay = 10, fixedDelay = 1_000, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val inputBlob = "test.csv"

        val blob = getBlob(inputBlob)

        readFile(blob)
    }

    private fun readFile(blob: Blob) {
        val content = blob.getContent().decodeToString()
        val output = mutableListOf<SoknadData>()

        log.info(content)

        content.lines().forEach { line ->
            if (line.isBlank()) return@forEach

            val columns = line.split(',').map { it.trim() }

            val fnr = columns[0]
            val id = columns[1]
            val fom = LocalDate.parse(columns[2])
            val tom = LocalDate.parse(columns[3])
            val cics = columns[4]

            val soknad = sykepengesoknadBackendClient.hentSoknad(id)

            log.info("Hentet soknad $soknad med cics $cics")

            assert(soknad.fnr == fnr)
            assert(soknad.id == id)
            assert(soknad.fom == fom)
            assert(soknad.tom == tom)
            assert(soknad.status == SoknadsstatusDTO.SENDT)

            output.add(
                SoknadData(
                    fnr = soknad.fnr,
                    id = soknad.id,
                    fom = soknad.fom!!,
                    tom = soknad.tom!!,
                    cics = cics,
                    soknadsperioder = soknad.soknadsperioder!!.serialisertTilString(),
                    fravarForSykmeldingen = soknad.fravarForSykmeldingen!!.serialisertTilString(),
                    fravar = soknad.fravar!!.serialisertTilString(),
                    andreInntektskilder = soknad.andreInntektskilder!!.serialisertTilString(),
                    permitteringer = soknad.permitteringer!!.serialisertTilString()
                )
            )
        }

        createBlob(
            blobId = "output.csv",
            file = output.joinToString("\n")
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
        val bInfo = BlobInfo.newBuilder(bucketName, blobId)
            .setContentType("text/csv")
            .build()

        storage.create(bInfo, file.encodeToByteArray())
    }

    private data class SoknadData(
        val fnr: String,
        val id: String,
        val fom: LocalDate,
        val tom: LocalDate,
        val cics: String,
        val soknadsperioder: String,
        val fravarForSykmeldingen: String,
        val fravar: String,
        val andreInntektskilder: String,
        val permitteringer: String,
    ) {
        override fun toString(): String {
            return "$fnr,$id,$fom,$tom,$cics,$soknadsperioder,$fravarForSykmeldingen,$fravar,$andreInntektskilder,$permitteringer"
        }
    }
}
