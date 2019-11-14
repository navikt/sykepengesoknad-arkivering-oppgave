package no.nav.syfo.consumer.ws

import no.nav.syfo.domain.dto.Soknadstype
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.binding.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Behandlingstema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Enhetsstatus
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Geografi
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Organisasjonsenhet
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.Tema
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.FinnBehandlendeEnhetListeResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner


@RunWith(MockitoJUnitRunner::class)
class ArbeidsfordelingConsumerTest {

    @Mock
    private val arbeidsfordelingV1: ArbeidsfordelingV1? = null

    @InjectMocks
    private val arbeidsfordelingConsumer: ArbeidsfordelingConsumer? = null

    @Before
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun setup() {
        `when`(arbeidsfordelingV1!!.finnBehandlendeEnhetListe(any()))
            .thenReturn(FinnBehandlendeEnhetListeResponse().apply {
                behandlendeEnhetListe.add(Organisasjonsenhet().apply {
                    status = Enhetsstatus.AKTIV
                    enhetId = "enhetsId"
                })
            })
    }

    @Test
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun hentBehandlendeEnhet() {
        val geografiskTilknytning = GeografiskTilknytning("2017", null)

        arbeidsfordelingConsumer!!.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.SELVSTENDIGE_OG_FRILANSERE)

        val captor = ArgumentCaptor.forClass(FinnBehandlendeEnhetListeRequest::class.java)

        verify<ArbeidsfordelingV1>(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture())

        val kriterier = captor.value.arbeidsfordelingKriterier

        assertThat(kriterier.diskresjonskode).isNull()
        assertThat(kriterier.behandlingstema).isNull()
        assertThat(kriterier.geografiskTilknytning.value).isEqualTo("2017")
        assertThat(kriterier.tema.value).isEqualTo("SYK")
    }

    @Test
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun hentBehandlendeEnhetKode6GirDiskresjonskode() {
        val geografiskTilknytning = GeografiskTilknytning("2017", "SPSF")

        arbeidsfordelingConsumer!!.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.OPPHOLD_UTLAND)

        val captor = ArgumentCaptor.forClass(FinnBehandlendeEnhetListeRequest::class.java)

        verify<ArbeidsfordelingV1>(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture())

        val kriterier = captor.value.arbeidsfordelingKriterier

        assertThat(kriterier.diskresjonskode.value).isEqualTo("SPSF")
        assertThat(kriterier.behandlingstema.value).isEqualTo(BEHANDLINGSTEMA_OPPHOLD_UTLAND)
        assertThat(kriterier.geografiskTilknytning.value).isEqualTo("2017")
        assertThat(kriterier.tema.value).isEqualTo("SYK")
    }
}
