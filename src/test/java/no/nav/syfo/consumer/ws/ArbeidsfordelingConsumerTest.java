package no.nav.syfo.consumer.ws;

import no.nav.syfo.domain.dto.Soknadstype;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.ArbeidsfordelingV1;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.FinnBehandlendeEnhetListeUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.informasjon.*;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeRequest;
import no.nav.tjeneste.virksomhet.arbeidsfordeling.v1.meldinger.WSFinnBehandlendeEnhetListeResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static no.nav.syfo.consumer.ws.ArbeidsfordelingConsumer.BEHANDLINGSTEMA_OPPHOLD_UTLAND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ArbeidsfordelingConsumerTest {

    @Mock
    private ArbeidsfordelingV1 arbeidsfordelingV1;

    @InjectMocks
    private ArbeidsfordelingConsumer arbeidsfordelingConsumer;

    @Before
    public void setup() throws FinnBehandlendeEnhetListeUgyldigInput {
        when(arbeidsfordelingV1.finnBehandlendeEnhetListe(any()))
                .thenReturn(new WSFinnBehandlendeEnhetListeResponse().withBehandlendeEnhetListe(singletonList(
                        new WSOrganisasjonsenhet()
                                .withStatus(WSEnhetsstatus.AKTIV)
                                .withEnhetId("enhetsId"))));
    }

    @Test
    public void hentBehandlendeEnhet() throws FinnBehandlendeEnhetListeUgyldigInput {
        GeografiskTilknytning geografiskTilknytning = GeografiskTilknytning.builder()
                .geografiskTilknytning("2017")
                .diskresjonskode(null)
                .build();

        arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.SELVSTENDIGE_OG_FRILANSERE);

        ArgumentCaptor<WSFinnBehandlendeEnhetListeRequest> captor = ArgumentCaptor.forClass(WSFinnBehandlendeEnhetListeRequest.class);

        verify(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture());

        WSArbeidsfordelingKriterier kriterier = captor.getValue().getArbeidsfordelingKriterier();

        assertThat(kriterier.getDiskresjonskode()).isNull();
        assertThat(kriterier.getBehandlingstema()).isNull();
        assertThat(kriterier.getGeografiskTilknytning()).isEqualTo(new WSGeografi().withValue("2017"));
        assertThat(kriterier.getTema()).isEqualTo(new WSTema().withValue("SYK"));
    }

    @Test
    public void hentBehandlendeEnhetKode6GirDiskresjonskode() throws FinnBehandlendeEnhetListeUgyldigInput {
        GeografiskTilknytning geografiskTilknytning = GeografiskTilknytning.builder()
                .geografiskTilknytning("2017")
                .diskresjonskode("SPSF")
                .build();

        arbeidsfordelingConsumer.finnBehandlendeEnhet(geografiskTilknytning, Soknadstype.OPPHOLD_UTLAND);

        ArgumentCaptor<WSFinnBehandlendeEnhetListeRequest> captor = ArgumentCaptor.forClass(WSFinnBehandlendeEnhetListeRequest.class);

        verify(arbeidsfordelingV1).finnBehandlendeEnhetListe(captor.capture());

        WSArbeidsfordelingKriterier kriterier = captor.getValue().getArbeidsfordelingKriterier();

        assertThat(kriterier.getDiskresjonskode()).isEqualTo(new WSDiskresjonskoder().withValue("SPSF"));
        assertThat(kriterier.getBehandlingstema()).isEqualTo(new WSBehandlingstema().withValue(BEHANDLINGSTEMA_OPPHOLD_UTLAND));
        assertThat(kriterier.getGeografiskTilknytning()).isEqualTo(new WSGeografi().withValue("2017"));
        assertThat(kriterier.getTema()).isEqualTo(new WSTema().withValue("SYK"));
    }
}