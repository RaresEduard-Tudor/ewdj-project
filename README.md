# FIFA Wereldbeker 2026 — Voorspellingsapp

Spring Boot + Thymeleaf webapplicatie waarin gebruikers wedstrijduitslagen voorspellen voor het WK 2026, in teams strijden om punten en de publieke ranglijst beklimmen.

---

## ⚠ Belangrijk vóór het opstarten

`spring.jpa.hibernate.ddl-auto=update` staat **bewust aan** in `src/main/resources/application.properties`.

**Waarom:**
- Bij de eerste start op een lege `worldcup`-databank maakt Hibernate automatisch alle tabellen aan — geen handmatige SQL-scripts nodig
- `update` is niet-destructief: bestaande data blijft behouden bij elke herstart
- `none` zou crashen op een verse databank (geen tabellen = `SQLException`); `create` of `create-drop` zouden bij elke herstart alle data wissen
- De `DataSeeder` is idempotent (controleert eerst of admin/wedstrijden al bestaan) — herstarten is dus altijd veilig

Kortom: gewoon laten staan. App start, tabellen verschijnen, admin + 24 wedstrijden worden geseed.

---

## Functionaliteiten

- **Voorspellingen**: scores voorspellen voor alle 24 groepsfasewedstrijden; aanpasbaar tot 1 uur voor aftrap
- **Automatische puntenberekening**: exacte score, juiste uitslag, bonuspunten als enige voorspeller binnen het team
- **Teambeheer**: teams aanmaken, lid worden via uitnodigingscode, leden en instellingen beheren
- **Groepsstanden**: live tabel met W/G/V/DS/Pnt voor alle 12 WK-groepen
- **Publieke Top-10**: ranglijst van de beste teams, zichtbaar voor iedereen (ook gasten)
- **Adminpaneel**: wedstrijden toevoegen/bewerken, einduitslagen invoeren, auditlog voor alle adminacties
- **Spring Security**: rolgebaseerde toegang (USER / ADMIN)
- **Meertalig**: volledig vertaald naar Nederlands en Engels (`?lang=nl` / `?lang=en`)

---

## Vereisten

| Onderdeel | Versie |
|---|---|
| Java JDK | 21 |
| Maven | 3.9+ (of gebruik de meegeleverde `mvnw`) |
| MySQL | 8 |
| IntelliJ IDEA | 2023.3+ (optioneel, bundelt eigen Maven) |

---

## Setup

### 1. MySQL starten

Optie A — via Docker (aanbevolen):

```bash
docker run -d \
  --name worldcup-mysql \
  -e MYSQL_ROOT_PASSWORD=admin123 \
  -e MYSQL_DATABASE=worldcup \
  -p 3306:3306 \
  mysql:8
```

Als de container al bestaat van een vorige run:

```bash
docker start worldcup-mysql
```

Optie B — eigen lokale MySQL: maak handmatig een database `worldcup` aan en zorg dat gebruiker `root` / wachtwoord `admin123` toegang heeft, of pas `src/main/resources/application.properties` aan (regels 2–4).

### 2. Applicatie starten

Vanaf de commandline:

```bash
./mvnw spring-boot:run
```

(Windows: `mvnw.cmd spring-boot:run`)

Of in IntelliJ: open de map als Maven-project en draai `WorldcupApplication.main()`.

### 3. Openen in browser

```
http://localhost:9001
```

### Standaard adminaccount

| Gebruikersnaam | Wachtwoord |
|---|---|
| `admin` | `admin123` |

De `DataSeeder` draait bij opstart en maakt automatisch het adminaccount + alle 24 WK-groepsfasewedstrijden aan (alleen als de databank leeg is).

---

## Tests uitvoeren

Tests gebruiken een in-memory H2-databank — Docker/MySQL is **niet** nodig om te testen.

```bash
./mvnw test
```

Verwacht resultaat: **166 tests, 0 failures**.

---

## Projectstructuur

```
src/main/java/com/worldcup/
├── config/          SecurityConfig, WebConfig, PasswordEncoderConfig
├── controller/      Home, Auth, Match, Team, Prediction, Scoreboard, Groups
│   └── admin/       AdminMatchController
├── rest/            MatchRestController (REST API)
├── client/          StadiumCapacityClient (reactieve WebClient)
├── domain/          User, Team, Match, Prediction
├── dto/             RegistrationDto, MatchDto, MatchResponseDto, ...
├── exception/       GlobalExceptionHandler + custom exceptions
├── interceptor/     AdminAuditInterceptor
├── repository/      JPA repositories
├── service/         User, Team, Match, Prediction, Scoring
├── validation/      6 custom Jakarta-annotaties + validators
├── DataSeeder.java
└── WorldcupApplication.java
```

---

## Puntensysteem

Geconfigureerd in `messages.properties` (resource bundle) en geladen via `MessageSource` in `ScoringService`:

| Gebeurtenis | Punten |
|---|---|
| Exacte score | 10 |
| Juiste uitslag (winst/gelijk) | 5 |
| Bonus: enige met exacte score in team | +5 |
| Bonus: enige met juiste uitslag in team | +3 |

---

## Custom validators

| Annotatie | Doel |
|---|---|
| `@ValidEmail` | Controleert e-mailformaat |
| `@ValidPasswords` | Controleert of wachtwoord en bevestiging gelijk zijn |
| `@ValidChecksum` | Verifieert checksum = stadiumCode % 97 |
| `@ValidCountries` | Land A ≠ Land B |
| `@SameGroupCountries` | Land A en Land B in dezelfde groep |
| `@ValidMatchDate` | Datum binnen WK-periode (11 juni – 19 juli 2026) |

---

## Pre-zip checklist

Voordat je het project inlevert:

- [ ] **Tests groen**: `./mvnw test` → `BUILD SUCCESS` (166/166)
- [ ] **`ddl-auto`** in `src/main/resources/application.properties` staat op `update` (veilig voor een verse databank) — niet op `create`
- [ ] **Geen geheimen** in `application.properties` buiten de standaard dev-credentials
- [ ] **Project schoonmaken**: `./mvnw clean` om `target/` te verwijderen
- [ ] **Zip-inhoud nakijken** — moet bevatten:
  - `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `README.md`, `guidelines.md`
- [ ] **Zip-inhoud nakijken** — mag NIET bevatten:
  - `target/`, `.idea/`, `*.iml`, `.git/`, `.DS_Store`
- [ ] **Zip-naam**: `Klasgroep_Naam_Voornaam.zip`
- [ ] **Deadline**: zaterdag 23 mei, 23:59

Snelle zip vanuit de bovenliggende map (Linux/Mac):

```bash
cd ..
zip -r Klasgroep_Tudor_Rares.zip ewdj-project \
  -x 'ewdj-project/target/*' \
  -x 'ewdj-project/.idea/*' \
  -x 'ewdj-project/.git/*' \
  -x '*.iml' \
  -x '.DS_Store'
```

---

## Opmerkingen

- `spring.jpa.open-in-view=false`: lazy-collecties worden expliciet geladen via `Hibernate.initialize()` in de controllers
- Alle zichtbare teksten staan in `messages.properties` (EN) en `messages_nl.properties` (NL)
- De REST-API (`/api/matches/**`) wordt door de app zelf geconsumeerd via `StadiumCapacityClient` (geen externe afhankelijkheid)
- `DataSeeder` seed alleen als de databank leeg is (idempotent — herstart is veilig)
