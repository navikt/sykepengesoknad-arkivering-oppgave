package no.nav.helse.flex.oppgavefordeling

import no.nav.helse.flex.client.pdl.PdlClient
import no.nav.helse.flex.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class GeografiskTilknytning(
    private val oppgavefordelingRepository: OppgavefordelingRepository,
    private val pdlClient: PdlClient,
) {

    val log = logger()

    @Scheduled(initialDelay = 120, fixedDelay = 1, timeUnit = TimeUnit.SECONDS)
    fun fyllMedGeografiskTilknytning() {
        val utenGt = listOf(
            "f28ead00-0f67-4d4e-89ad-a1fc82747641",
            "efd9b7ed-aa24-496f-87b1-d199e2a0e8e7",
            "f15a048d-4a44-4510-ad10-abd43f18c850",
            "f5f7dc87-f4b4-45ed-adaa-9ca3da274cb1",
            "f5fca44a-c728-4b3e-ac3e-eb4c3e6da266",
            "eff5eafe-653a-4bea-88ab-d0143d91c257",
            "b76255a0-1e90-45c9-bf6a-eac24d23b187",
            "f3b32ed4-c74b-4d64-884e-8131acf14a60",
            "f71bfac0-88a6-46d2-abbe-1ea66e6ac303",
            "f15cf5af-92d6-463b-9f3b-202889d42d47",
            "968bbead-ca85-4d85-ab1f-1164c821d06e",
            "f161a704-766c-48e1-a6ac-4498ad1d60b8",
            "f3b4e4b5-0b9d-47df-a328-a5b87caa9ebb",
            "f4ca97fc-7e94-4222-b4ea-8a743d7c7bfd",
            "f4c80967-92fd-4952-93f1-31da68a87b5f",
            "f4d2e8f1-4753-4c78-b167-750d6eab0e92",
            "f15f4328-a4b3-4003-bfd5-95e8108dc05c",
            "f5f9b71f-a80c-4142-a756-213084e25520",
            "f1686603-e2fd-42fb-8f86-49e7d5d2ed9c",
            "7cfb059f-ab77-44d5-baaf-fd6bd2da70c2",
            "f5f1b3a8-b3dd-4162-9092-094cead374fe",
            "f732b705-605a-41b6-ad42-ef262ba0f21c",
            "f5f3fe8b-0af6-44b4-a5d5-6b8140a4ee16",
            "f72fd89b-1e47-45ce-96cf-e45973a95f2a",
            "f4d4d957-7e90-4fe0-8b4d-2d9ba9a146d9",
            "d3183d5d-62f0-4b49-aac2-c9faaf64c266",
            "f73e1d65-ef5c-4195-b87f-e9160bc715ba",
            "effb8498-448f-438f-9ed6-31028fd43e82",
            "f7172043-41f4-40f4-aec7-b10bb65acce6",
            "f4d1af9d-d2ba-4a42-8851-061ce4c6abeb",
            "effbccf2-042e-4f92-83fd-fc5cc33deb49",
            "f5ee51eb-ba5e-4768-983f-0f222570b34c",
            "f73e1b81-49a6-4db8-b9e4-07701c30f4a2",
            "f826aac0-70d5-47b2-a1a7-d60b121de32b",
            "f730dca8-578c-4157-88b7-d47fa879b2ad",
            "f71b7197-e92a-49e4-b38e-21b0249e1838",
            "f3b590b0-6f6e-4420-9666-498ac6b71ea1",
            "ee189116-7564-4623-9289-256768d2488b",
            "f4d639c3-4d03-4777-958d-dce3615cc627",
            "f71b931a-fcd0-40dd-b6fc-a733c5699158",
            "efefa1c8-41f3-44ff-810f-1f752f049c0b",
            "f5fb4a7c-bf65-4f6e-ab33-c405e5f195d7",
            "f71f91e7-03a9-4039-82e8-3ffbcaed6148",
            "f5f7670d-3768-4ee5-a230-6fa8b9baf4b9",
            "f5f30efd-642f-4c51-92c6-7426cd7efa5d",
            "f4d62e31-be3b-4369-84c4-53599c7ee168",
            "f28c89de-34a0-4a69-b600-e4ae816f658b",
            "f15ede97-369c-4336-b280-0f809f5c86d0",
            "f2829256-3c14-4815-826a-94316f808156",
            "f5f0a4ac-1272-460e-ad60-ff788c7be653",
            "f3b11d65-8fad-4d29-b516-8c991c5dd565",
            "f3b45bda-25c5-4c55-bce2-efe11530b1c3",
            "f734b5b7-775f-4886-852f-5fb17ce672b0",
            "efee1de5-51bf-4965-925f-8dd30d68f598",
            "efd5f468-ef00-44e7-b299-77f4064cf6c4",
            "f28c7c51-49c2-4c88-a8b1-96095cfcd635",
            "f5f9f4fb-074c-4cc2-b37b-8d3f30cb504e",
            "f8268c74-c70c-4f23-9642-a266baa4a28b",
            "f165d7de-eb16-417d-b155-27cd208dfe29",
            "f3afb8ad-cf22-4bc9-982d-2bbe0caa3cd5"
        )

        utenGt.forEach {
            val fnr = oppgavefordelingRepository.findFnrBySykepengesoknadId(it)
            try {
                val gt = pdlClient.hentGeografiskTilknytning(fnr!!).hentGeografiskTilknytning

                oppgavefordelingRepository.lagreGeografiskTilknytning(
                    sykepengesoknadId = it,
                    kommune = gt.gtKommune,
                    bydel = gt.gtBydel,
                    land = gt.gtLand,
                )
                log.info("Hentet GT for sykepengesoknad $it with type: ${gt.gtType}")
            } catch (e: Exception) {
                log.warn("Klarte ikke hent GT for sykepengesoknad $it - ${e.message}")
            }
        }
    }
}
