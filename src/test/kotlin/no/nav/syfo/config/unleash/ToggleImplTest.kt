package no.nav.syfo.config.unleash

import no.finn.unleash.Unleash
import no.nav.syfo.config.unleash.FeatureToggle.SKAL_LESE_SOKNADER_FRA_KOE
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ToggleImplTest {
    @Mock
    private val unleash: Unleash? = null

    @Test
    fun isEnabledPEndepunkterForSoknad() {
        val toggle = ToggleImpl(unleash!!, "p")
        `when`(unleash.isEnabled(anyString())).thenReturn(true)
        assertThat(toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)).isTrue()
        verify(unleash).isEnabled(anyString())
    }

    @Test
    fun isDisabledPEndepunkterForSoknad() {
        val toggle = ToggleImpl(unleash!!, "p")
        `when`(unleash.isEnabled(anyString())).thenReturn(false)
        assertThat(toggle.isEnabled(SKAL_LESE_SOKNADER_FRA_KOE)).isFalse()
        verify(unleash).isEnabled(anyString())
    }

    @Test
    fun isTestEnvironmentFalseForProd() {
        val toggle = ToggleImpl(unleash!!, "p")
        assertThat(toggle.isNotProduction()).isFalse()
    }

    @Test
    fun isTestEnvironmentTrueForTest() {
        val toggle = ToggleImpl(unleash!!, "q1")
        assertThat(toggle.isNotProduction()).isTrue()
    }
}
