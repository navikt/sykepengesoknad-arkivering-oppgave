package no.nav.helse.flex.util

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDate

class DatoUtilTest {
    @Test
    fun `aarMaaned formatter skal formatere til norsk måned og år`() {
        val dato = LocalDate.of(2025, 1, 15)
        val formatted = dato.format(DatoUtil.aarMaaned)

        formatted `should be equal to` "januar 2025"
    }
}
