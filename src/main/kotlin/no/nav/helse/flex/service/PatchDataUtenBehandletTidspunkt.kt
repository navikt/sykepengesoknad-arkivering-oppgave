package no.nav.helse.flex.service

import no.nav.helse.flex.logger
import no.nav.helse.flex.repository.InnsendingRepository
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

@Component
class PatchDataUtenBehandletTidspunkt(
    private val innsendingRepository: InnsendingRepository
) {
    private val log = logger()

    @Scheduled(fixedDelay = 1000L * 60 * 60, initialDelay = 1000L * 60 * 4)
    fun startJob() {
        log.info("Starer patching av innsendinger uten behandlet")

        patchData("049c350f-9117-3217-b13c-e7c25fcffeee", "2022-02-09T14:23:27")
        patchData("0ad4902a-3d13-3cf0-866a-ffd7c53c705a", "2022-02-10T13:20:27")
        patchData("2347f6e1-6188-3bf9-bc6a-0f659e4f9d5f", "2022-02-11T11:34:47")
        patchData("24db2ff0-9599-48c5-9b90-03e3dff97a8e", "2022-02-14T09:28:27")
        patchData("33e95714-6196-32ef-851e-735e79b790d6", "2022-02-21T13:08:22")
        patchData("3c85b8e3-97cf-4d6e-813c-568d71cdac5d", "2022-02-09T22:09:14")
        patchData("4f7d3b17-fd02-373b-9a14-5ea4f258dff8", "2022-02-11T15:54:56")
        patchData("5031e72d-1b87-356a-afab-f5e7e0c5e77b", "2022-02-21T12:46:58")
        patchData("6589668a-7cd9-40dc-9bf6-3bfe7856d998", "2022-02-16T08:09:51")
        patchData("65d21912-6512-4710-8e3e-b68cb3377b47", "2022-02-09T14:29:38")
        patchData("6a394b74-ba3d-3682-aa09-a859a6f8be02", "2022-02-14T13:28:15")
        patchData("7557850b-6961-3a08-8484-933687dc19a4", "2022-02-21T15:42:02")
        patchData("7d9eac44-87d1-369e-9fec-b6c84e6263a7", "2022-02-15T15:40:31")
        patchData("81c1b790-5ae7-3a5d-a183-4cb4619edf8b", "2022-02-08T16:52:31")
        patchData("b1242a36-ee23-30e0-8a25-9df33d26181d", "2022-02-21T15:33:43")
        patchData("c13caec1-dc44-3d13-909c-6570ea06020f", "2022-02-19T13:04:17")
        patchData("c54b9868-014e-4756-a0c9-da54565a153b", "2022-02-10T14:23:08")
        patchData("cee5d35b-d14d-3ea4-b660-4b8fb20d6f18", "2022-02-13T15:33:10")
        patchData("d795f3c6-2dc3-3abf-968c-4d2321eddcab", "2022-02-08T13:45:00")
        patchData("de7e660f-01bb-3584-83af-36b729bfc887", "2022-02-16T14:30:54")
        patchData("dfeaa449-5ea6-30e9-9bb9-39c1e524d6b5", "2022-02-18T13:07:11")
        patchData("feb9b02c-789b-323f-a5f5-55ec1169ed27", "2022-02-13T16:32:10")
        patchData("ffb75bd0-6b08-32f4-b29a-b6241893ce54", "2022-02-08T14:15:25")

        log.info("Ferdig med patching")
    }

    private fun patchData(sykepengesoknadId: String, tidspunkt: String) {
        val offsetDateTime = LocalDateTime.parse(tidspunkt).tilOsloZone()
        val resultat = innsendingRepository.updateBehandletBySykepengesoknadid(
            sykepengesoknadId,
            offsetDateTime.toInstant()
        )
        log.info("Patchet $sykepengesoknadId til $offsetDateTime. Resultat $resultat")
    }

    val osloZone = ZoneId.of("Europe/Oslo")
    fun LocalDateTime.tilOsloZone(): OffsetDateTime = this.atZone(osloZone).toOffsetDateTime()
}
