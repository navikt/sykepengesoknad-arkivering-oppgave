package no.nav.syfo.kafka

import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.Innsending
import no.nav.syfo.kafka.interfaces.Soknad
import no.nav.syfo.kafka.soknad.dto.SoknadDTO
import no.nav.syfo.kafka.sykepengesoknad.dto.*
import no.nav.syfo.service.BehandleFeiledeSoknaderService
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.TopicPartition
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.springframework.kafka.core.ConsumerFactory
import java.time.LocalDate
import java.time.LocalDateTime

@RunWith(MockitoJUnitRunner::class)
class RebehandleSoknadListenerTest {
    @Mock
    private lateinit var behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService
    @Mock
    private lateinit var innsendingDAO: InnsendingDAO
    @Mock
    private lateinit var consumerFactory: ConsumerFactory<String, Soknad>

    private lateinit var rebehandleSoknadListener: RebehandleSoknadListener

    private lateinit var consumer: Consumer<String, Soknad>

    @Before
    @Suppress("unchecked_cast")
    fun setUp() {
        consumer = mock(Consumer::class.java) as Consumer<String, Soknad>
        `when`(consumerFactory.createConsumer(anyString(), anyString())).thenReturn(consumer)

        rebehandleSoknadListener = RebehandleSoknadListener(
                behandleFeiledeSoknaderService,
                innsendingDAO,
                "test",
                consumerFactory
        )
    }

    @Test
    fun rebehandleSoknadIkkeSykepengesoknadDTOEllerSoknadDTO() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(emptyList())

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2", 1, 1, "key", object : Soknad {}))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()
    }

    @Test
    fun rebehandleSoknadSoknadDTO() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .soknadstype("SELVSTENDIGE_OG_FRILANSERE")
                .status("SENDT")
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettetDato(LocalDate.now())
                .innsendtDato(LocalDate.now())
                .arbeidsgiver("arbeidsgiver")
                .arbeidssituasjon("FRILANSER")
                .startSykeforlop(LocalDate.now())
                .sykmeldingUtskrevet(LocalDate.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadPerioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTO() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOIkkeSendt() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.NY)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOIkkeSendtTilNav() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(null)
                .sendtArbeidsgiver(LocalDateTime.now())
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOIkkeFeilet() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(emptyList())

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOEttersendtTilArbeidsgiver() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(LocalDateTime.now().plusDays(1))
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOEttersendtTilNav() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(LocalDateTime.now().minusDays(1))
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOToSoknaderISammeRecords() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key1",
                                        soknad
                                ),
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        2,
                                        "key2",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOToSoknaderIToRecords() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records1 = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key1",
                                        soknad
                                ))))
        val records2 = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        2,
                                        "key2",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records1, records2, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadDTOToSoknaderIToPartisjoner() {
        val innsending = Innsending("innsending", "id")
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
                innsending
        ))

        val soknad = SykepengesoknadDTO.builder()
                .id("id")
                .sykmeldingId("sykmeldingId")
                .aktorId("aktorId")
                .type(SoknadstypeDTO.ARBEIDSTAKERE)
                .status(SoknadsstatusDTO.SENDT)
                .fom(LocalDate.now())
                .tom(LocalDate.now())
                .opprettet(LocalDateTime.now())
                .sendtNav(LocalDateTime.now())
                .sendtArbeidsgiver(null)
                .arbeidsgiver(ArbeidsgiverDTO.builder().navn("arbeidsgiver").orgnummer("orgnummer").build())
                .arbeidssituasjon(ArbeidssituasjonDTO.ARBEIDSTAKER)
                .startSyketilfelle(LocalDate.now())
                .sykmeldingSkrevet(LocalDateTime.now())
                .korrigertAv(null)
                .korrigerer(null)
                .soknadsperioder(emptyList())
                .sporsmal(emptyList())
                .build()

        val records = ConsumerRecords<String, Soknad>(mapOf(
                TopicPartition("syfo-soknad-v2", 1) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        1,
                                        1,
                                        "key1",
                                        soknad
                                )),
                TopicPartition("syfo-soknad-v2", 2) to
                        listOf(
                                ConsumerRecord<String, Soknad>(
                                        "syfo-soknad-v2",
                                        2,
                                        1,
                                        "key2",
                                        soknad
                                ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }
}