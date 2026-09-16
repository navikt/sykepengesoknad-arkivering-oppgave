package no.nav.helse.flex

import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Jackson 2 ligger fortsatt på classpath transitivt etter oppgraderingen til Jackson 3, så
 * importer fra `com.fasterxml.jackson.core` og `com.fasterxml.jackson.databind` kompilerer
 * uten feil - de blir bare aldri brukt av Jackson 3 i runtime. Det har allerede gitt to
 * stille feil: `@JsonSerialize` i Soknad og døde catch-blokker i PeriodeMapper.
 *
 * Annotasjoner fra `com.fasterxml.jackson.annotation` er derimot korrekte, siden
 * `tools.jackson.core:jackson-databind` fortsatt avhenger av `jackson-annotations`.
 */
class JacksonImportTest {
    private val ulovligImport = Regex("""^import com\.fasterxml\.jackson\.(core|databind)\.""", RegexOption.MULTILINE)

    @Test
    fun `Ingen kildefiler importerer fra Jackson 2 core eller databind`() {
        val treff =
            listOf(File("src/main/kotlin"), File("src/test/kotlin"))
                .flatMap { it.walkTopDown().filter { fil -> fil.extension == "kt" } }
                .flatMap { fil ->
                    ulovligImport.findAll(fil.readText()).map { "${fil.path}: ${it.value.trim()}" }
                }

        treff shouldBeEqualTo emptyList()
    }
}
