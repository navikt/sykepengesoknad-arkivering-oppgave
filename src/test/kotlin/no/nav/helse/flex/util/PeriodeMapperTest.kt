package no.nav.helse.flex.util

import io.kotest.matchers.shouldBe
import no.nav.helse.flex.util.PeriodeMapper.jsonTilPeriode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

class PeriodeMapperTest {
    @Test
    fun `Gyldig periode-json mappes til Periode`() {
        val periode = jsonTilPeriode("""{"fom":"2024-01-01","tom":"2024-01-31"}""")

        periode.fom shouldBe LocalDate.of(2024, 1, 1)
        periode.tom shouldBe LocalDate.of(2024, 1, 31)
    }

    @Test
    fun `Json som ikke lar seg parse gir IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> { jsonTilPeriode("ikke gyldig json") }

        exception.message shouldBe "Mapping av periode-json feiler"
    }

    @Test
    fun `Json uten paakrevde felter gir IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> { jsonTilPeriode("""{"fom":"2024-01-01"}""") }

        exception.message shouldBe "Mapping av periode-json feiler"
    }

    @Test
    fun `Periode med fom etter tom gir IllegalArgumentException`() {
        val exception = assertThrows<IllegalArgumentException> { jsonTilPeriode("""{"fom":"2024-01-31","tom":"2024-01-01"}""") }

        exception.message shouldBe "Mapping av periode-json feiler"
    }
}
