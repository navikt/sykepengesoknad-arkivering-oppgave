package no.nav.syfo.consumer.ws

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.nav.syfo.TestApplication
import no.nav.syfo.controller.PDFRestController
import no.nav.syfo.domain.Soknad
import no.nav.syfo.domain.dto.Sykepengesoknad
import no.nav.tjeneste.virksomhet.behandlejournal.v2.BehandleJournalV2
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseRequest
import no.nav.tjeneste.virksomhet.behandlejournal.v2.meldinger.WSJournalfoerInngaaendeHenvendelseResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.IOException

@RunWith(MockitoJUnitRunner::class)
class BehandleJournalConsumerTest {

    @Mock
    private val behandleJournalV2: BehandleJournalV2? = null
    @Mock
    private val personConsumer: PersonConsumer? = null
    @Mock
    private val pdfRestController: PDFRestController? = null

    @InjectMocks
    private val behandleJournalConsumer: BehandleJournalConsumer? = null

    private val objectMapper = ObjectMapper()
            .registerModule(JavaTimeModule())
            .registerModule(KotlinModule())

    @Test
    @Throws(IOException::class)
    fun opprettJournalpost() {
        `when`(behandleJournalV2!!.journalfoerInngaaendeHenvendelse(any<WSJournalfoerInngaaendeHenvendelseRequest>()))
                .thenReturn(WSJournalfoerInngaaendeHenvendelseResponse().withJournalpostId("id"))

        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "22026900623", "Kjersti Glad")
        val id = behandleJournalConsumer!!.opprettJournalpost(soknad, "saksId")

        assertThat(id).isEqualTo("id")
    }

    @Test
    @Throws(IOException::class)
    fun opprettJournalpostTaklerFeil() {
        `when`(behandleJournalV2!!.journalfoerInngaaendeHenvendelse(any<WSJournalfoerInngaaendeHenvendelseRequest>())).thenThrow(RuntimeException("test"))

        val sykepengesoknad = objectMapper.readValue(TestApplication::class.java.getResource("/soknadSelvstendigMedNeisvar.json"), Sykepengesoknad::class.java)
        val soknad = Soknad.lagSoknad(sykepengesoknad, "22026900623", "Kjersti Glad")

        try {
            behandleJournalConsumer!!.opprettJournalpost(soknad, "saksid")
        } catch (e: RuntimeException) {
            assertThat(e).hasMessage("Kunne ikke behandle journalpost for søknad med id daa8b4b5-ece8-4e6d-ab7e-c7354958201a og saks id: saksid")
        }

    }
}
