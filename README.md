# Boligregnskap

En fullstack-webapplikasjon for å holde styr på økonomien i én eller flere utleieboliger: leieinntekter, utgifter, forfallsdatoer og en årsrapport med overskudd og skatteestimat - pluss et automatisk estimat på forventet leieinntekt for en adresse.

Bygget som et personlig prosjekt for å håndtere regnskapet for egne utleieboliger.

## Funksjonalitet

- Registrere flere eiendommer/adresser, hver med egne felt (boligtype, areal, rom, byggeår m.m.)
- Registrere leieinntekter og utgifter (felleskostnader, vedlikehold, forsikring m.m.) per eiendom
- Oversikt over kommende forfallsdatoer som påminnelse
- Årsrapport: sum inntekter, sum utgifter, overskudd og grovt skatteestimat
- Automatisk estimat på forventet netto leieinntekt for en adresse, basert på SSBs offentlige leiemarkedsstatistikk
- Data lagres i database og persisteres mellom økter

## Teknologi

**Backend**
- Java 21
- Spring Boot 3 (Spring Web, Spring Data JPA)
- H2-database (fil-basert)
- REST-API
- Kartverkets adresse-API (geokoding) og SSBs PxWebApi (leiemarkedsstatistikk) - begge nøkkelfrie, offentlige API-er

**Frontend**
- HTML, CSS og vanilla JavaScript
- Kommuniserer med backend via `fetch` mot REST-API-et

## Arkitektur

```
src/main/java/no/nicolay/boligregnskap/
├── model/          Transaction, TransactionType, Eiendom, Boligtype
├── repository/     TransactionRepository, EiendomRepository (Spring Data JPA)
├── controller/     TransactionController, EiendomController (REST-endepunkter)
├── service/        AdresseService (geokoding), LeieestimatService (leieprisestimat)
├── migration/       EiendomMigrationRunner (engangs-backfill av gamle transaksjoner)
├── util/           SkatteUtil (delt skattekonvensjon)
└── BoligregnskapApplication.java
src/main/resources/
├── static/index.html   Frontend
└── application.properties
```

## API-endepunkter

| Metode | Endepunkt | Beskrivelse |
|---|---|---|
| `GET` | `/api/eiendommer` | Alle eiendommer |
| `POST` | `/api/eiendommer` | Ny eiendom (geokodes automatisk mot Kartverket) |
| `DELETE` | `/api/eiendommer/{id}` | Slett eiendom (409 hvis den har transaksjoner) |
| `GET` | `/api/eiendommer/{id}/leieestimat` | Estimert brutto/netto leieinntekt for eiendommen |
| `GET` | `/api/transactions?eiendomId=` | Transaksjoner (evt. filtrert på eiendom) |
| `POST` | `/api/transactions?eiendomId=` | Ny transaksjon knyttet til en eiendom |
| `DELETE` | `/api/transactions/{id}` | Slett transaksjon |
| `GET` | `/api/transactions/upcoming?eiendomId=` | Kommende forfallsdatoer |
| `GET` | `/api/transactions/report/{year}?eiendomId=` | Årsrapport for gitt år |

## Kjøre prosjektet

Krever Java 21 og Maven.

```bash
mvn spring-boot:run
```

Eller dobbeltklikk `run.bat` (rydder selv opp gamle prosesser på port 8080 og åpner nettleseren automatisk).

Åpne deretter `http://localhost:8080` i nettleseren.

## Merknad om leieestimatet

Leieestimatet er basert på SSBs leiemarkedsundersøkelse (gjennomsnittlig leie per kvadratmeter for en sone: Oslo/Bærum, Bergen, Trondheim, Stavanger, Akershus for øvrig, eller hele landet) og antall rom - det er et regionalt/nasjonalt snitt, ikke et adressespesifikt tall. Automatisert uthenting fra Finn.no er bevisst unngått, siden dette er i strid med Finns brukervilkår.

## Videre arbeid

- Innlogging
- Grafisk visning av inntekter/utgifter over tid
- Eksport av årsrapport til PDF
- Enhetstester av rapport-, skatte- og leieestimatberegning

## Merknad om skatteberegning

Skatteestimatet er en forenklet beregning (22 % av positivt overskudd) og er ikke en offisiell eller nøyaktig skatteberegning. Bruk Skatteetatens egne verktøy for faktisk skattemelding.
