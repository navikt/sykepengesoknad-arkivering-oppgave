# Sykepengesoknad-arkivering-oppgave
sykepengesoknad-arkivering-oppgave er ansvarlig for å arkivere alle sendete søknader i dokarkiv og opprette oppgaver i gosys på søknader som går til NAV og ikke behandles i speil.

## Generell flyt

Når en sykepengesøknad kommer inn på Kafka vil søknaden gå gjennom samme flyt som arbeidstakersøknader,
det er noen sjekker som splitter disse av fra flyten til arbeidstakersøknader.

1. Det sjekkes om søknaden har status `SENDT`, og om søknaden **ikke** er ettersendt til arbeidsgiver.
    * En søknad som blir ansett som ettersendt til arbeidsgiver har `sendtArbeidsgiver`-feltet satt, og
      `sendtNav`-feltet inneholder en dato tidligere enn datoen i `sendtArbeidsgiver`-feltet.
    * Med andre ord vil alle sendte sykepengesøknader som ikke er arbeidstakersøknader gå gjennom her,
      for `sendtArbeidsgiver` vil ikke være satt.
2. Søknaden blir journalført i Gosys med PDF fra [`flex-sykepengesoknad-pdfgen`](flex-sykepengesoknad-pdfgen.md).
3. Herfra skjer følgende.
    * Det at søknaden skal behandles av Nav er bare en sjekk på om `sendtNav`-feltet er satt med en dato.
    * Dersom dette skjer, vil den gå gjennom flyten beskrevet i [oppgavestyring mot spei](#oppgavestyring-mot-speil).
4. Saksbehandlingssystemet internt i `sykepengesoknad-arkivering-oppgave` vil anse saken tilknyttet denne søknaden som ferdigbehandlet.

### Rebehandling
Hvis det på noe som helst steg feiler, vil saksbehandlingssystemet internt i `sykepengesoknad-arkivering-oppgave` legge søknaden på en intern Kafka-topic som
vil føre til forsøk på rebehandling. En rebehandling betyr at den beskrevne flyten forsøkes på nytt for søknaden.

## Oppgavestyring mot Speil

Vanligvis kreves det to ting for at en saksbehandler skal kunne behandle en søknad;
journalføring av søknaden (med PDF), samt en oppgave i Gsak/Gosys.
Når en arbeidstakersøknad kommer inn på Kafka (flex.sykepengesoknad), vil søknaden bli journalført men
oppgave for saksbehandlere blir ikke opprettet med det første. Herfra refererer vi til denne typen
oppgave som sak for saksbehandlere, mens oppgave vil mene noe internt i `sykepengesoknad-arkivering-oppgave`.

Applikasjonen til teamet som har ansvar for å utvikle løsninger for automatisering av saker, `spedisjon`,
leser også fra flex.sykepengesoknad. For hver søknad som kommer inn dit, vil `spedisjon` sende hendelser på sitt
eget topic (aapen-helse-spre-oppgaver). Disse hendelsene kan være en av tre typer.

* `Utsett` betyr at `spedisjon` ønsker at `sykepengesoknad-arkivering-oppgave` skal vente med å opprette en sak for saksbehandlere.
  En sånn hendelse har også en timeout knyttet til den, denne definerer hvor lenge `sykepengesoknad-arkivering-oppgave` skal vente.
  Stort sett betyr dette at automatiseringssystemet ikke har fått alt de trenger for å behandle en søknad enda,
  for eksempel at inntektsmelding mangler.
* `Ferdigbehandlet` betyr at søknaden har fått et vedtak gjennom automatiseringssystemet, at
  en saksbehandler ikke trenger å vedta saken, og at `sykepengesoknad-arkivering-oppgave` ikke trenger å opprette sak.
* `Opprett` betyr at søknaden ikke har fått et vedtak gjennom automatiseringssystemet. `sykepengesoknad-arkivering-oppgave`
  må opprette en sak for saksbehandlere slik at de kan se på søknaden i stedet.

`sykepengesoknad-arkivering-oppgave` leser på aapen-helse-spre-oppgaver for å bestemme om den skal opprette saker eller ikke.
Dette gjøres i tre steg:

1. Hendelsen fra `spedisjon` har kommet inn, denne lagres i databasen sammen med søknads-id. Dersom en annen
   hendelse kommer inn i etterkant med samme søknads-id, vil databasen oppdateres med ny status.
   Det skal sies at denne ikke er strengt tatt nødvendig, og at `sykepengesoknad-arkivering-oppgave` vil fungere i et vakum.
2. `sykepengesoknad-arkivering-oppgave` må ha selv sett søknaden på flex.sykepengesoknad, dette signaliseres ved at
   databaseinnslaget knyttet til søknads-id får et felt kalt `avstemt` satt til 1.
   Dette er for å hindre at vi gjør noe dersom `spedisjon` sender hendelser knyttet til en søknad som ikke finnes.
   I tillegg, dersom et databaseinnlegg knyttet til søknads-id ikke finnes allerede, vil `sykepengesoknad-arkivering-oppgave` selv sette inn
   dette med status `Utsett`, med en time frem i tid. Dette er hvor lang tid `spedisjon` har til å selv publisere
   at de har sett søknaden.
3. Til sist, en jobb som kjører regelmessig henter opp alle oppgaver som er `avstemt` == 1,
   samt status `Opprett` eller `Utsett` med en timeout som har gått ut (altså, at timeout-tiden ligger i fortiden).

Når jobben finner søknader som oppfyller kriteriene, vil den for hver oppgave opprette en sak for saksbehandlere.
Deretter vil hver oppgave knyttet til søknads-id få status `Opprettet` i databasen, slik at jobben ikke henter
disse oppgavene neste gang den kjører.

Dersom `spedisjon` ikke svarer før `sykepengesoknad-arkivering-oppgave` har sett søknaden"
I noen tilfeller kan det hende at `sykepengesoknad-arkivering-oppgave` kommer `spedisjon` i forkjøpet og finner en søknad
som ikke har blitt behandlet i noen form av `spedisjon` enda. I dette tilfellet oppretter `sykepengesoknad-arkivering-oppgave`
sin egen `Utsett`, og venter i 48 timer på svar fra `spedisjon`. Dette blir da en oppgave som
følger samme logikk som over.

En del søknader har ikke spedisjon et forhold til, da setter vi timeout til 1 minutt slik at vi ikke venter for lenge på spedisjon.

## Behandlingstype Medlemskap

Når en sykepengesøknad mottas på Kafaka og feltet `medlemskapsvurdering` er satt, lagres verdien i databasen. Når det 
eventuelt opprettes en Gosys-oppgave på den samme søknaden brukes verdien(e) til å avgjøre om oppgaven skal inneholde informasjon
om bruker er medlems i folketrygden eller ikke, eller om dette må avklares. Følgende scenarios kan oppstå: 

| Inngående Vurdering              | Endelig Vurdering           | Behandlingstype  | Gosys-oppgave                          |
|----------------------------------|-----------------------------|------------------|----------------------------------------|
| null (ingen inngående vurdering) | null (sjekker ikke)         | SYKEPENGER       | null (ingen informasjon)               |
| UAVKLART (brukerspørsmål)        | UAVKLART                    | MEDLEMSKAP       | UAVKLART (uavklart-tekst + brukersvar) |
| UAVKLART (brukerspørsmål)        | null (feil fra LovMe)       | MEDLEMSKAP       | UAVKLART (uavklart-tekst + brukersvar) |
| UAVKLART (brukerspørsmål)        | NEI                         | MEDLEMSKAP       | NEI (nei-tekst + brukersvar)           |
| NEI                              | null (sjekker ikke)         | MEDLEMSKAP       | NEI (nei-tekst)                        |
| UAVKLART (brukerspørsmål)        | JA                          | SYKEPENGER       | null (ingen informasjon)               |
| UAVKLART                         | null (sjekker ikke)         | SYKEPENGER       | null (ingen informasjon)               |
| JA                               | null (sjekker ikke)         | SYKEPENGER       | null (ingen informasjon)               |
                                  |

## Data
Applikasjonen har en database i GCP. 

Tabellen `innsending` lenker sykepengesøknad id med journalpostid og oppgaveid. Det slettes ikke data fra tabellen.

Tabellen `oppgavestyring` holder informasjon om en sykepengesøknad skal få sin oppgave oppretta i gosys eller om denne oppgaven blir behandlet i ny løsning.
Det slettes ikke data fra tabellen. Dataene kopieres også nattlig over til et bigquery datasett.


# Komme i gang

Bygges med gradle. Standard Spring Boot oppsett.

---

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan stilles til flex@nav.no

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #flex.
