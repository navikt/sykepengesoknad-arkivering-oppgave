package no.nav.syfo.client.pdl

import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("test")
class PdlClient {
    fun hentIdenter(ident: String) = HentIdenterResponseData(
        HentIdenter(
            listOf(
                PdlIdent(AKTORID, "298374918"),
                PdlIdent(FOLKEREGISTERIDENT, "20950340984")
            )
        )
    )

    fun hentFormattertNavn(fnr: String) =
        Navn(
            fornavn = "Tom",
            mellomnavn = null,
            etternavn = "Eke"
        ).format()

    fun hentGeografiskTilknytning(fnr: String) = HentGeografiskTilknytningResponseData(
        HentGeografiskTilknytning(
            gtType = "KOMMUNE",
            gtKommune = "0301",
            gtBydel = null,
            gtLand = null
        ),
        HentPerson(
            listOf(
                Adressebeskyttelse(
                    gradering = Gradering.UGRADERT
                )
            )
        )
    )
}
