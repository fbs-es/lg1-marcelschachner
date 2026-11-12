# Architektur

Stand: Schritt 3 (Kundenkonto und Adressbuch E01). Wird mit jedem Schritt im selben Commit nachgezogen.

## Überblick

```
Browser (Angular 21, :4200, Proxy /api → :8080)
   │  HTTP/JSON, /api/v1
   ▼
Spring Boot 3.5 (Java 21, Gradle)  ──SMTP──▶  Mailpit (lokal) / Mailserver
   │  JPA/Hibernate, JDBC
   ▼
PostgreSQL 16 (Schema per Flyway)
```

- Build: Gradle 9.9.1 über `./gradlew`, Kotlin DSL ([ADR 0001](adr/0001-gradle-als-build-werkzeug.md)).
- `./gradlew build` = kompilieren, alle Tests (Unit + Testcontainers), Spotless-Formatprüfung.
- CI: `.github/workflows/ci.yml` mit zwei Jobs auf `ubuntu-latest`:
  - `backend`: `./gradlew build` (Docker für Testcontainers vorhanden)
  - `frontend`: `npm ci`, `generate:api` plus `git diff --exit-code`, `npm test`, `npm run build`

## Pakete

Schichten je Fachpaket: `api` → `application` → `domain`; `persistence` und `infrastruktur` implementieren Ports aus `application`.

| Paket | Inhalt |
|---|---|
| `de.bestellsystem` | `BestellsystemApplication` (inkl. `@ConfigurationPropertiesScan`) |
| `gemeinsam.domain` | `Geld` (Cent `long`, EUR, `@Embeddable` Record), `Anschrift` (`@Embeddable` Record, ISO-3166-Alpha-2, Längen nach D-16), fachliche Ausnahmen `NichtGefundenException` (404), `KonfliktException` (409), `FachlicheAblehnungException` (422) |
| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung`, `BestellnummernGenerator` + Port `BestellnummernZaehler`, `FachlicheEinstellungen` (`bestellsystem.*`) |
| `gemeinsam.persistence` | `SequenzBestellnummernZaehler` (`nextval('bestellnummer_seq')` über `JdbcClient`) |
| `gemeinsam.infrastruktur` | `UhrKonfiguration` (`Clock`-Bean in konfigurierter Zeitzone), `MailBenachrichtigungsversand` (`JavaMailSender`), `MailEinstellungen` (`bestellsystem.mail.*`), `OpenApiKonfiguration` |
| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`; prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`; setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`) |
| `kunde.application` | `KundeService`: `registriere` (409 bei vergebener E-Mail, auch über Unique-Constraint), `alle`, `finde`, Adressbuch-Operationen mit `OPTIMISTIC_FORCE_INCREMENT` auf den Kunden (D-16) |
| `kunde.api` | `KundeController`, `AdressbuchController`, DTOs `RegistrierungDto`, `AnschriftDto`, `AdresseEingabeDto`, `AdresseDto`, `KundeDto`, `KundeUebersichtDto` |

## REST-API

OpenAPI unter `/v3/api-docs`, Swagger UI unter `/swagger-ui.html`. Exportiert nach `frontend/openapi.json` ([ADR 0002](adr/0002-openapi-spezifikation-als-datei.md)).

| Methode | Pfad | Ergebnis |
|---|---|---|
| POST | `/api/v1/produkte` | 201 + `Location`, 400 (D-15) |
| GET | `/api/v1/produkte` | alle Produkte nach Titel |
| GET | `/api/v1/produkte/{id}` | Produkt mit `istVerfuegbar`, 404 |
| PUT | `/api/v1/produkte/{id}/lagerbestand` | Bestand absolut setzen, 400 bei < 0, 404, 409 Versionskonflikt |
| GET | `/api/v1/produkte/{id}/verfuegbarkeit?menge=n` | `{produktId, menge, istVerfuegbar}`, 400 bei menge < 1, 404 |
| POST | `/api/v1/kunden` | Registrierung mit erster Lieferadresse: 201 + `Location`, 400, 409 E-Mail vergeben |
| GET | `/api/v1/kunden` | Übersicht ohne Adressbuch, nach Nachname und Vorname |
| GET | `/api/v1/kunden/{kundenId}` | Kunde mit Adressbuch (Anlagereihenfolge), 404 |
| POST | `/api/v1/kunden/{kundenId}/adressen` | 201 + `Location`, 400 (auch I-2), 404, 409 Versionskonflikt |
| GET | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | eine Adresse, 404 auch bei fremder Adresse |
| PUT | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | vollständig ersetzen: 200, 400, 404, 409, 422 (I-1) |
| DELETE | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | 204, 404, 409, 422 letzte Lieferadresse (I-1) |

## Datenfluss (Beispiel Produkt anlegen)

```
ProduktAnlegen (Angular) ─ ProdukteService (generiert) ─ POST /api/v1/produkte
  → ProduktController (@Valid ProduktAnlegenDto) → ProduktService.legeAn (@Transactional)
  → new Produkt(...) prüft Invarianten → ProduktRepository.save → INSERT produkt
  ← 201 ProduktDto ← Router zurück zur Liste ← GET /api/v1/produkte
```

## Querschnitt

### Geld (D-11)

- Ganze Cent als `long`, Währung fest `EUR`; nie Gleitkomma.
- `addiere`, `subtrahiere`, `multipliziere(menge)`, `prozentsatz(BigDecimal)` mit `RoundingMode.HALF_UP` (kaufmännisch).
- Überlauf wirft `ArithmeticException` (`Math.*Exact`).
- Darf negativ sein (Zwischenergebnisse); Nichtnegativität (I-7) prüfen die Entities.
- In Entities mit `@AttributeOverride` auf eine sprechende Spalte (`..._cent`) abbilden.

### Fehler (RFC 9457)

| Auslöser | Status | `title` |
|---|---|---|
| Bean Validation (`@Valid`) | 400 | Ungültige Eingabe, plus `fehler: [{feld, meldung}]` |
| `IllegalArgumentException` (verletzte Wertobjekt-Regel) | 400 | Ungültige Eingabe |
| unlesbares JSON, unbekannter Pfad, falsche Methode | 400/404/405 | Spring-Standard als ProblemDetail |
| `NichtGefundenException` | 404 | Nicht gefunden |
| `KonfliktException` | 409 | Konflikt |
| `ObjectOptimisticLockingFailureException` | 409 | Versionskonflikt |
| `FachlicheAblehnungException` | 422 | Fachlich abgelehnt |
| alles andere | 500 | Interner Fehler (ohne interne Details, geloggt) |

### Nebenläufigkeit im Adressbuch (D-16)

- `Kunde` trägt `@Version`. Jede Adressbuch-Operation sperrt ihn mit `OPTIMISTIC_FORCE_INCREMENT`, damit auch eine reine Feldänderung an einer `Adresse` die Version erhöht.
- Zwei gleichzeitige Änderungen, die zusammen I-1 verletzen würden (z. B. beide Lieferadressen löschen), scheitern für die zweite mit 409 Versionskonflikt.
- Gleichzeitige Registrierung derselben E-Mail: der Unique-Constraint `kunde_email_eindeutig` greift, `saveAndFlush` übersetzt die Verletzung in 409.

### Sprache

- `spring.web.locale=de` mit festem `LocaleResolver`: Bean-Validation-Meldungen sind immer deutsch.

### Zeit

- Einzige Zeitquelle ist die `Clock`-Bean (`Europe/Berlin`, `bestellsystem.zeitzone`).
- Tests legen Zeit per `FesteUhrKonfiguration` (`@Primary Clock.fixed`) oder direkt mit `Clock.fixed` fest.

### Bestellnummer (D-13, D-14)

- Format `B-<Jahr des Bestelldatums>-<Zähler, mind. 6-stellig>`, z. B. `B-2026-000123`.
- Zähler global über PostgreSQL-Sequenz, nebenläufig eindeutig, Lücken zulässig.

### Benachrichtigung (D-03)

- Port `Benachrichtigungsversand.sende(Benachrichtigung)`; Adapter verschickt Text-E-Mail per SMTP.
- Lokal landet jede Mail in Mailpit (http://localhost:8025).

### Konfiguration

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `bestellsystem.zeitzone` | `Europe/Berlin` | Zeitzone der Uhr |
| `bestellsystem.zahlungsziel` | `14d` | Bestelldatum bis Fälligkeit (D-03) |
| `bestellsystem.mahnintervall` | `7d` | Mindestabstand zweier Mahnungen (D-03) |
| `bestellsystem.mail.absender` | `shop@bestellsystem.local` | Absender der E-Mails |

- Zugangsdaten nur über Umgebungsvariablen (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_ABSENDER`).
- Lokal: `.env` im Repo-Wurzelverzeichnis (Vorlage `.env.example`), gelesen von Docker Compose und vom Backend (`spring.config.import`).
- `spring.jpa.open-in-view=false`, `ddl-auto=validate`.

## Datenbank

### Tabellen und Sequenzen

| Objekt | Zweck |
|---|---|
| Sequenz `bestellnummer_seq` | Zähler der Bestellnummer |
| Tabelle `produkt` | `id` UUID, `version`, `titel` (≤ 200), `listenpreis_cent` (≥ 0), `lagerbestand` (≥ 0); Index auf `titel` |
| Tabelle `kunde` | `id` UUID, `version`, `vorname`, `nachname` (≤ 100), `email` (≤ 254, normalisiert, UNIQUE `kunde_email_eindeutig`); Index auf `(nachname, vorname)` |
| Tabelle `adresse` | `id` UUID, `kunde_id` (FK, `ON DELETE CASCADE`, Index), `reihenfolge` (`@OrderColumn`, nullable, da von Hibernate nachgetragen), `strasse`, `stadt`, `postleitzahl`, `land`, `ist_lieferadresse`, `ist_rechnungsadresse`; CHECK mindestens ein Kennzeichen (I-2) |

### Migrationen

| Version | Datei | Inhalt |
|---|---|---|
| V1 | `V1__bestellnummer_sequenz.sql` | Sequenz `bestellnummer_seq` |
| V2 | `V2__produkt.sql` | Tabelle `produkt` mit CHECK-Constraints |
| V3 | `V3__kunde.sql` | Tabellen `kunde` und `adresse` |

## Lokale Infrastruktur

`docker-compose.yml`:

| Dienst | Image | Ports |
|---|---|---|
| `postgres` | `postgres:16-alpine` | 5432 |
| `mailpit` | `axllent/mailpit:v1.33.1` | 1025 (SMTP), 8025 (Web) |

## Frontend

Angular 21: Standalone Components, Signals, Reactive Forms, zoneless; Tests mit Vitest/jsdom.

| Pfad | Inhalt |
|---|---|
| `src/app/app.ts`, `app.routes.ts`, `app.config.ts` | Layout (Kopfzeile, Navigation), Routen mit `withComponentInputBinding`, `provideApi('')`, `LOCALE_ID de-DE`, `DEFAULT_CURRENCY_CODE EUR` |
| `src/app/api/` | generierter Client (`npm run generate:api`, nie von Hand ändern) |
| `src/app/gemeinsam/` | `GeldPipe` (Cent → `49,99 €`), `euroInCent` + Validator `euroBetrag` (ohne Gleitkomma), `problemMeldung` (ProblemDetail → Text) |
| `src/app/katalog/produkt-liste` | `/produkte`: Laden, Fehler mit Wiederholen, Leerzustand, Tabelle mit Verfügbarkeit |
| `src/app/katalog/produkt-anlegen` | `/produkte/neu`: Formular, Feld- und Serverfehler, danach zurück zur Liste |
| `src/app/kunde/kunden-liste` | `/kunden`: Laden, Fehler mit Wiederholen, Leerzustand, Tabelle mit Link zum Adressbuch |
| `src/app/kunde/kunde-registrieren` | `/kunden/neu`: Stammdaten, Lieferadresse, "auch Rechnungsadresse", danach zum Kunden |
| `src/app/kunde/kunde-detail` | `/kunden/:kundenId`: Stammdaten und Adressbuch; hinzufügen, bearbeiten, löschen; 422/409 als Meldung |
| `src/app/kunde/adresse-formular`, `anschrift-felder` | wiederverwendbares Adressformular (Kennzeichen-Validator I-2) und Anschriftfelder mit Regeln aus D-16 |
| `src/styles.css` | globale Formular- und Tabellenstile (`.formular`, `.feldfehler`, `.aktionen`, `table`) |

- `proxy.conf.json` leitet `/api` an `http://localhost:8080` weiter (`npm start`).
- `openapitools.json` pinnt den Generator auf 7.27.0 (`typescript-angular`).

## Tests

| Art | Werkzeug | Beispiele |
|---|---|---|
| Domain-Unit | JUnit 5, AssertJ | `GeldTest`, `AnschriftTest`, `BestellnummernGeneratorTest`, `BenachrichtigungTest` |
| Adapter-Unit | Mockito an der Grenze (`JavaMailSender`) | `MailBenachrichtigungsversandTest` |
| Web | `@WebMvcTest`, MockMvc | `ZentraleFehlerbehandlungTest` (über Test-Controller `FehlerAusloesenderController`) |
| Domain-Unit Katalog | JUnit 5, AssertJ | `ProduktTest` (US-02.1 AK1–5, D-15) |
| Domain-Unit Kunde | JUnit 5, AssertJ | `KundeTest` (US-01.1 bis US-01.3), `EmailTest` (D-13, D-16) |
| API-Integration Kunde | MockMvc, PostgreSQL | `KundeApiIT` (alle AK US-01.1 bis US-01.3 über HTTP) |
| Nebenläufigkeit | zwei Transaktionen per `TransactionTemplate`, Latches | `KundeNebenlaeufigkeitIT` (US-01.3 AK8, gleichzeitige Registrierung) |
| Integration | `@SpringBootTest`, Testcontainers (`TestcontainersKonfiguration`, `@ServiceConnection`) | `BestellsystemApplicationIT`, `SequenzBestellnummernZaehlerIT` |
| API-Integration | `@SpringBootTest` + MockMvc gegen echten Service und PostgreSQL | `ProduktApiIT` (US-02.1 AK1–5 über HTTP, Statuscodes, ProblemDetail) |
| Spec-Drift | `OpenApiSpezifikationIT`; `./gradlew aktualisiereOpenApi` schreibt die Datei neu | ADR 0002 |
| Frontend | Vitest, `TestBed`, `HttpTestingController` | `produkt-liste.spec.ts`, `produkt-anlegen.spec.ts`, `kunden-liste.spec.ts`, `kunde-registrieren.spec.ts`, `kunde-detail.spec.ts`, `geld.pipe.spec.ts`, `euro-eingabe.spec.ts`, `problem-meldung.spec.ts` |
