package no.nav.syfo.kafka

import no.nav.syfo.consumer.repository.InnsendingDAO
import no.nav.syfo.domain.Innsending
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SoknadsstatusDTO.NY
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SoknadsstatusDTO.SENDT
import no.nav.syfo.kafka.sykepengesoknadarbeidsledig.dto.SykepengesoknadArbeidsledigDTO
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
class RebehandleSoknadarbeidsledigListenerTest {
    @Mock
    private lateinit var behandleFeiledeSoknaderService: BehandleFeiledeSoknaderService
    @Mock
    private lateinit var innsendingDAO: InnsendingDAO
    @Mock
    private lateinit var consumerFactory: ConsumerFactory<String, SykepengesoknadArbeidsledigDTO>

    private lateinit var rebehandleSoknadarbeidsledigListener: RebehandleSoknadarbeidsledigListener

    private lateinit var consumer: Consumer<String, SykepengesoknadArbeidsledigDTO>

    @Before
    @Suppress("unchecked_cast")
    fun setUp() {
        consumer = mock(Consumer::class.java) as Consumer<String, SykepengesoknadArbeidsledigDTO>
        `when`(consumerFactory.createConsumer(anyString(), anyString())).thenReturn(consumer)

        rebehandleSoknadarbeidsledigListener = RebehandleSoknadarbeidsledigListener(
            behandleFeiledeSoknaderService,
            innsendingDAO,
            "test",
            consumerFactory
        )
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTO() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOIkkeSendt() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = NY,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOIkkeSendtTilNav() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = null,
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOIkkeFeilet() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(emptyList())

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, never()).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOToSoknaderISammeRecords() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key1",
                        soknad
                    ),
                    ConsumerRecord(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        2,
                        "key2",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOToSoknaderIToRecords() {
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            Innsending("innsending", "id")
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records1 = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key1",
                        soknad
                    ))))
        val records2 = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        2,
                        "key2",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records1, records2, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }

    @Test
    fun rebehandleSoknadSykepengesoknadArbeidsledigDTOToSoknaderIToPartisjoner() {
        val innsending = Innsending("innsending", "id")
        `when`(innsendingDAO.hentFeilendeInnsendinger()).thenReturn(listOf(
            innsending
        ))

        val soknad = SykepengesoknadArbeidsledigDTO(
            id = "id",
            sykmeldingId = "sykmeldingId",
            aktorId = "aktorId",
            status = SENDT,
            fom = LocalDate.now(),
            tom = LocalDate.now(),
            opprettet = LocalDateTime.now(),
            sendtNav = LocalDateTime.now(),
            startSyketilfelle = LocalDate.now(),
            sykmeldingSkrevet = LocalDateTime.now(),
            korrigertAv = null,
            korrigerer = null,
            soknadsperioder = emptyList(),
            sporsmal = emptyList())

        val records = ConsumerRecords<String, SykepengesoknadArbeidsledigDTO>(mapOf(
            TopicPartition("syfo-soknad-arbeidsledig-v1", 1) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        1,
                        1,
                        "key1",
                        soknad
                    )),
            TopicPartition("syfo-soknad-arbeidsledig-v1", 2) to
                listOf(
                    ConsumerRecord<String, SykepengesoknadArbeidsledigDTO>(
                        "syfo-soknad-arbeidsledig-v1",
                        2,
                        1,
                        "key2",
                        soknad
                    ))))
        `when`(consumer.poll(1000L)).thenReturn(records, ConsumerRecords.empty())

        rebehandleSoknadarbeidsledigListener.listen()

        verify(behandleFeiledeSoknaderService, times(2)).behandleFeiletSoknad(no.nav.syfo.any(), no.nav.syfo.any())
    }
}
