package no.nav.syfo.consumer.aktor

data class Ident(
    val ident: String,
    val identgruppe: String,
    val gjeldende: Boolean
)
