package no.nav.syfo.consumer.ws

import no.nav.syfo.domain.dto.Soknadstype
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.*
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse
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
                .thenReturn(WSFinnBehandlendeEnhetListeResponse().withBehandlendeEnhetListe(listOf(WSOrganisasjonsenhet()
                        .withStatus(WSEnhetsstatus.AKTIV)
                        .withEnhetId("enhetsId"))))
    }

    @Test
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun hentBehandlendeEnhet() {
        val geografiskTilknytning = GeografiskTilknytning("2017", null)

        arbeidsfordelingConsumer!!.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.SELVSTENDIGE_OG_FRILANSERE)

        val captor = ArgumentCaptor.forClass(WSFinnBehandlendeEnhetListeRequest::class.java)

        verify<ArbeidsfordelingV1>(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture())

        val kriterier = captor.value.arbeidsfordelingKriterier

        assertThat(kriterier.diskresjonskode).isNull()
        assertThat(kriterier.behandlingstema).isNull()
        assertThat(kriterier.geografiskTilknytning).isEqualTo(WSGeografi().withValue("2017"))
        assertThat(kriterier.tema).isEqualTo(WSTema().withValue("SYK"))
    }

    @Test
    @Throws(FinnBehandlendeEnhetListeUgyldigInput::class)
    fun hentBehandlendeEnhetKode6GirDiskresjonskode() {
        val geografiskTilknytning = GeografiskTilknytning("2017", "SPSF")

        arbeidsfordelingConsumer!!.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.OPPHOLD_UTLAND)

        val captor = ArgumentCaptor.forClass(WSFinnBehandlendeEnhetListeRequest::class.java)

        verify<ArbeidsfordelingV1>(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture())

        val kriterier = captor.value.arbeidsfordelingKriterier

        assertThat(kriterier.diskresjonskode).isEqualTo(WSDiskresjonskoder().withValue("SPSF"))
        assertThat(kriterier.behandlingstema).isEqualTo(WSBehandlingstema().withValue(BEHANDLINGSTEMA_OPPHOLD_UTLAND))
        assertThat(kriterier.geografiskTilknytning).isEqualTo(WSGeografi().withValue("2017"))
        assertThat(kriterier.tema).isEqualTo(WSTema().withValue("SYK"))
    }
}