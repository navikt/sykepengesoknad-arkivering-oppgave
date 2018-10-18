package no.nav.syfo.consumer.ws;

import no.nav.tjeneste.virksomhet.oppgavebehandling.v3.OppgavebehandlingV3;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.time.DayOfWeek.*;
import static java.time.LocalDate.now;
import static java.time.temporal.TemporalAdjusters.next;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class OppgavebehandlingConsumerTest {

    @Mock
    private OppgavebehandlingV3 oppgavebehandlingV3;

    @InjectMocks
    private OppgavebehandlingConsumer oppgavebehandlingConsumer;

    @Test
    public void innsendingLordagOgSondagGirSammeFristSomMandag() {
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(SATURDAY))).getDayOfWeek()).isEqualTo(THURSDAY);
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(SUNDAY))).getDayOfWeek()).isEqualTo(THURSDAY);
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(MONDAY))).getDayOfWeek()).isEqualTo(THURSDAY);
    }

    @Test
    public void fristSettesOmTreDagerUtenomHelg() {
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(MONDAY))).getDayOfWeek()).isEqualTo(THURSDAY);
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(TUESDAY))).getDayOfWeek()).isEqualTo(FRIDAY);
    }

    @Test
    public void toDagerLeggesTilOverHelg() {
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(WEDNESDAY))).getDayOfWeek()).isEqualTo(MONDAY);
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(THURSDAY))).getDayOfWeek()).isEqualTo(TUESDAY);
        assertThat(oppgavebehandlingConsumer.omTreUkedager(now().with(next(FRIDAY))).getDayOfWeek()).isEqualTo(WEDNESDAY);
    }

}