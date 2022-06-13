package no.nav.helse.flex

import com.google.api.gax.retrying.RetrySettings
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import no.nav.helse.flex.client.SykepengesoknadBackendClient
import no.nav.helse.flex.sykepengesoknad.kafka.FravarstypeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsperiodeDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SoknadsstatusDTO
import no.nav.helse.flex.sykepengesoknad.kafka.SykepengesoknadDTO
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

    @Scheduled(initialDelay = 20, fixedDelay = 100_000, timeUnit = TimeUnit.SECONDS)
    fun job() {
        val bucket = storage.get(bucketName) ?: throw RuntimeException("Fant ikke bøtte ved navn $bucketName")
        val blob = bucket.get("resultat_filtrert_formattert.csv")
        val content = blob.getContent().decodeToString()

        val arbeidsgiverperiodeOutput = mutableListOf<SoknadData>()
        val delvisUtbetaltOutput = mutableListOf<SoknadData>()
        val ikkeUtbetaltOutput = mutableListOf<SoknadData>()
        val feilStatusOutput = mutableListOf<SoknadData>()

        content.lines().forEach { line ->
            if (line.isBlank()) return@forEach

            val columns = line.split(';').map { it.trim() }

            val status = columns[0]
            val fnr = columns[1].let { if (it.length < 11) "0$it" else it }
            val id = columns[2]
            val fom = LocalDate.parse(columns[3])
            val tom = LocalDate.parse(columns[4])
            val cics = columns[5]

            val soknad = sykepengesoknadBackendClient.hentSoknad(id)

            try {
                require(soknad.fnr == fnr) { "Soknad $id har feil fnr" }
                require(soknad.id == id) { "Soknad $id har feil id: ${soknad.id}" }
                require(soknad.fom == fom) { "Soknad $id har feil fom: $fom og ${soknad.fom}" }
                require(soknad.tom == tom) { "Soknad $id har feil tom: $tom og ${soknad.tom}" }
                require(
                    status in listOf(
                        "ARBEIDSGIVERPERIODE",
                        "DELVIS_UTBETALT",
                        "IKKE_UTBETALT"
                    )
                ) { "Soknad $id har feil status: $status" }
            } catch (e: IllegalArgumentException) {
                log.info(e.message)
                return@forEach
            }

            val soknadData = SoknadData(soknad, cics)

            if (soknad.status != SoknadsstatusDTO.SENDT) {
                log.info("Soknad ${soknad.id} har status ${soknad.status} og legges i egen liste")
                feilStatusOutput.add(soknadData.copy(korrigertAv = soknad.korrigertAv))
                var korrigering = soknad

                do {
                    korrigering = sykepengesoknadBackendClient.hentSoknad(korrigering.korrigertAv!!)
                    log.info("Soknad ${korrigering.id} har status ${korrigering.status} og legges i egen liste")

                    feilStatusOutput.add(
                        SoknadData(korrigering, cics)
                    )
                } while (korrigering.status == SoknadsstatusDTO.KORRIGERT)
            } else {
                when (status) {
                    "ARBEIDSGIVERPERIODE" -> arbeidsgiverperiodeOutput.add(soknadData)
                    "DELVIS_UTBETALT" -> delvisUtbetaltOutput.add(soknadData)
                    "IKKE_UTBETALT" -> ikkeUtbetaltOutput.add(soknadData)
                }
            }
        }

        createBlob(
            blobId = "arbeidsgiverperiodeOutput.csv",
            file = arbeidsgiverperiodeOutput.joinToString("\n")
        )

        createBlob(
            blobId = "delvisUtbetaltOutput.csv",
            file = delvisUtbetaltOutput.joinToString("\n")
        )

        createBlob(
            blobId = "ikkeUtbetaltOutput.csv",
            file = ikkeUtbetaltOutput.joinToString("\n")
        )

        createBlob(
            blobId = "feilStatusOutput.csv",
            file = feilStatusOutput.joinToString("\n") {
                "$it;${it.korrigertAv}"
            }
        )

        log.info("Output er lagt til i bøtta")
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
        val startSyketilfelle: LocalDate,
        val andreInntektskilder: String,
        val permitteringer: String,
        val arbeidGjenopptatt: LocalDate?,
        val soktUtenlandsopphold: Boolean,
        val arbeidUtenforNorge: Boolean,
        val behandlingsdager: String,

        val fravarFerie: String,
        val fravarPermisjon: String,
        val fravarUtlandsopphold: String,
        val fravarUtdanning: String,

        val korrigertAv: String? = null,
    ) {

        constructor(soknad: SykepengesoknadDTO, cics: String) : this(
            fnr = soknad.fnr,
            id = soknad.id,
            fom = soknad.fom!!,
            tom = soknad.tom!!,
            cics = cics,
            soknadsperioder = soknad.soknadsperioder!!.formatter(),
            startSyketilfelle = soknad.startSyketilfelle!!,
            andreInntektskilder = soknad.andreInntektskilder!!.serialisertTilString(),
            permitteringer = soknad.permitteringer!!.serialisertTilString(),
            arbeidGjenopptatt = soknad.arbeidGjenopptatt,
            soktUtenlandsopphold = soknad.soktUtenlandsopphold ?: false,
            arbeidUtenforNorge = soknad.arbeidUtenforNorge ?: false,
            behandlingsdager = soknad.behandlingsdager?.serialisertTilString() ?: "[]",

            fravarFerie = soknad.fravar?.filter { it.type == FravarstypeDTO.FERIE }?.serialisertTilString() ?: "[]",
            fravarPermisjon = soknad.fravar?.filter { it.type == FravarstypeDTO.PERMISJON }?.serialisertTilString()
                ?: "[]",
            fravarUtlandsopphold = soknad.fravar?.filter { it.type == FravarstypeDTO.UTLANDSOPPHOLD }
                ?.serialisertTilString() ?: "[]",
            fravarUtdanning = soknad.fravar?.filter {
                it.type in listOf(
                    FravarstypeDTO.UTDANNING_FULLTID,
                    FravarstypeDTO.UTDANNING_DELTID
                )
            }?.serialisertTilString() ?: "[]",

            korrigertAv = soknad.korrigertAv
        )

        override fun toString(): String {
            return "$fnr;$id;$fom;$tom;$cics;$soknadsperioder;$startSyketilfelle;$andreInntektskilder;$permitteringer;$arbeidGjenopptatt;$soktUtenlandsopphold;$arbeidUtenforNorge;$fravarFerie;$fravarPermisjon;$fravarUtlandsopphold;$fravarUtdanning"
        }
    }
}

fun List<SoknadsperiodeDTO>.formatter(): String {
    data class SoknadPeriodeFormattert(
        val fom: LocalDate,
        val tom: LocalDate,
        val utbetalingsgrad: Int,
    )

    return this.map {
        SoknadPeriodeFormattert(
            fom = it.fom!!,
            tom = it.tom!!,
            utbetalingsgrad = if (it.faktiskGrad != null) {
                100 - it.faktiskGrad!!
            } else it.sykmeldingsgrad!!
        )
    }.serialisertTilString()
}
