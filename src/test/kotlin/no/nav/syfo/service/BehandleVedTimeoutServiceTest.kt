package no.nav.syfo.service

import com.nhaarman.mockitokotlin2.whenever
import no.nav.syfo.config.unleash.ToggleImpl
import no.nav.syfo.consumer.repository.OppgavestyringDAO
import no.nav.syfo.consumer.syfosoknad.SyfosoknadConsumer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class BehandleVedTimeoutServiceTest {
    @Mock
    lateinit var saksbehandlingsService: SaksbehandlingsService

    @Mock
    lateinit var toggle: ToggleImpl

    @Mock
    lateinit var oppgavestyringDAO: OppgavestyringDAO

    @Mock
    lateinit var syfosoknadConsumer: SyfosoknadConsumer

    @Before
    fun setup() {
        whenever(toggle.isNotProduction()).thenReturn(true)
    }

    @Test
    fun `behandleTimeout`() {
        
    }
}
