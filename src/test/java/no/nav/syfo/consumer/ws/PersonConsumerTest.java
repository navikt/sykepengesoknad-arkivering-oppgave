package no.nav.syfo.consumer.ws;

import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing;
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bydel;
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PersonConsumerTest {

    @Mock
    private PersonV3 personV3;

    @InjectMocks
    private PersonConsumer personConsumer;

    @Test
    public void hentGeografiskTilknytning() throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {

        when(personV3.hentGeografiskTilknytning(any())).thenReturn(new HentGeografiskTilknytningResponse()
                .withDiskresjonskode(null)
                .withGeografiskTilknytning(new Bydel()
                        .withGeografiskTilknytning("2017")
                ));

        GeografiskTilknytning geografiskTilknytning = personConsumer.hentGeografiskTilknytning("fnr");
        assertThat(geografiskTilknytning.geografiskTilknytning).isEqualTo("2017");
        assertThat(geografiskTilknytning.diskresjonskode).isEqualTo(null);
    }

    @Test
    public void hentGeografiskTilknytningMapperOmDiskresjonskode() throws HentGeografiskTilknytningSikkerhetsbegrensing, HentGeografiskTilknytningPersonIkkeFunnet {

        when(personV3.hentGeografiskTilknytning(any())).thenReturn(new HentGeografiskTilknytningResponse()
                .withDiskresjonskode(new Diskresjonskoder().withValue("SPSF"))
                .withGeografiskTilknytning(new Bydel()
                        .withGeografiskTilknytning("2017")
                ));

        GeografiskTilknytning geografiskTilknytning = personConsumer.hentGeografiskTilknytning("fnr");
        assertThat(geografiskTilknytning.geografiskTilknytning).isEqualTo("2017");
        assertThat(geografiskTilknytning.diskresjonskode).isEqualTo("SPSF");
    }
}