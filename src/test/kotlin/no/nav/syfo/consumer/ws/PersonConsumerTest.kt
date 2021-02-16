package no.nav.syfo.consumer.ws

import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentGeografiskTilknytningSikkerhetsbegrensing
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Bydel
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Diskresjonskoder
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentGeografiskTilknytningResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class PersonConsumerTest {

    @Mock
    private val personV3: PersonV3? = null

    @InjectMocks
    private val personConsumer: PersonConsumer? = null

    @Test
    @Throws(HentGeografiskTilknytningSikkerhetsbegrensing::class, HentGeografiskTilknytningPersonIkkeFunnet::class)
    fun hentGeografiskTilknytning() {

        `when`(personV3!!.hentGeografiskTilknytning(any())).thenReturn(
            HentGeografiskTilknytningResponse()
                .withDiskresjonskode(null)
                .withGeografiskTilknytning(
                    Bydel()
                        .withGeografiskTilknytning("2017")
                )
        )

        val geografiskTilknytning = personConsumer!!.hentGeografiskTilknytning("fnr")
        assertThat(geografiskTilknytning.geografiskTilknytning).isEqualTo("2017")
        assertThat(geografiskTilknytning.diskresjonskode).isNull()
    }

    @Test
    @Throws(HentGeografiskTilknytningSikkerhetsbegrensing::class, HentGeografiskTilknytningPersonIkkeFunnet::class)
    fun hentGeografiskTilknytningMapperOmDiskresjonskode() {

        `when`(personV3!!.hentGeografiskTilknytning(any())).thenReturn(
            HentGeografiskTilknytningResponse()
                .withDiskresjonskode(Diskresjonskoder().withValue("SPSF"))
                .withGeografiskTilknytning(
                    Bydel()
                        .withGeografiskTilknytning("2017")
                )
        )

        val geografiskTilknytning = personConsumer!!.hentGeografiskTilknytning("fnr")
        assertThat(geografiskTilknytning.geografiskTilknytning).isEqualTo("2017")
        assertThat(geografiskTilknytning.diskresjonskode).isEqualTo("SPSF")
    }

    @Test
    fun taklerAtGeografiskTilknytningErNull() {
        `when`(personV3!!.hentGeografiskTilknytning(any())).thenReturn(
            HentGeografiskTilknytningResponse()
                .withDiskresjonskode(Diskresjonskoder().withValue("SPSF"))
        )

        val geografiskTilknytning = personConsumer!!.hentGeografiskTilknytning("fnr")

        assertThat(geografiskTilknytning.geografiskTilknytning).isNull()
        assertThat(geografiskTilknytning.diskresjonskode).isEqualTo("SPSF")
    }
}
