package no.nav.helse.flex.service

import no.nav.helse.flex.domain.Soknad
import no.nav.helse.flex.oppgave.lagBeskrivelse
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val oppgaveDato: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

fun lagOppgaveRequest(
    aktorId: String,
    journalpostId: String,
    soknad: Soknad,
    behandlingstemaOgType: BehandlingstemaOgType,
): OppgaveRequest =
    OppgaveRequest(
        opprettetAvEnhetsnr = "9999",
        aktoerId = aktorId,
        journalpostId = journalpostId,
        beskrivelse = lagBeskrivelse(soknad),
        tema = "SYK",
        oppgavetype = "SOK",
        aktivDato = LocalDate.now().format(oppgaveDato),
        fristFerdigstillelse = omTreUkedager(LocalDate.now()).format(oppgaveDato),
        prioritet = "NORM",
        behandlingstema = behandlingstemaOgType.behandlingstema,
        behandlingstype = behandlingstemaOgType.behandlingstype,
    )

fun omTreUkedager(idag: LocalDate) =
    when (idag.dayOfWeek) {
        DayOfWeek.SUNDAY -> idag.plusDays(4)
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY -> idag.plusDays(3)
        else -> idag.plusDays(5)
    }
