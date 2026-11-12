# Bestellsystem

Bestellsystem für einen Onlineshop: Kunden mit Adressbuch, Produktkatalog mit Lagerbestand, Merkliste, Checkout mit Gutscheinen, Zahlungen und Erstattungen, automatisches Mahnwesen per E-Mail und Versandpakete mit Lieferstatus.

- **Backend:** Java 21, Spring Boot 3.5 (REST, JPA/Hibernate, Flyway), Gradle
- **Datenbank:** PostgreSQL 16
- **Frontend:** Angular 21 mit aus der OpenAPI generiertem Client
- **Lokal:** Docker Compose mit PostgreSQL und Mailpit (E-Mail-Test)

Ausgangspunkt war die [Anforderung](docs/00-anforderung.md); Lücken und Widersprüche sind in den [Entscheidungen D-01 bis D-22](docs/01-entscheidungen.md) aufgelöst.

## Voraussetzungen

| Werkzeug | Version | Wofür |
|---|---|---|
| Java (JDK) | 21 | Backend; auch der OpenAPI-Generator im Frontend |
| Node.js / npm | 22 / 11 | Frontend |
| Docker mit Compose | aktuell, Daemon läuft | PostgreSQL und Mailpit; Testcontainers in den Backend-Tests |

Gradle und Angular CLI müssen nicht installiert sein: das Backend baut über den Wrapper `./gradlew`, das Frontend über die npm-Skripte (`npx` für die Angular CLI).

## Schnellstart

```bash
# 1. Zugangsdaten anlegen (.env wird nicht versioniert)
cp .env.example .env            # DB_PASSWORD setzen

# 2. PostgreSQL und Mailpit starten
docker compose up -d

# 3. Backend starten: API auf :8080, Flyway legt das Schema an
cd backend && ./gradlew bootRun

# 4. Frontend starten (zweites Terminal): http://localhost:4200
cd frontend && npm ci && npm start
```

| Adresse | Inhalt |
|---|---|
| http://localhost:4200 | Oberfläche (Proxy `/api` → `:8080`) |
| http://localhost:8080/swagger-ui.html | Swagger UI, API ausprobieren |
| http://localhost:8080/v3/api-docs | OpenAPI zur Laufzeit; exportiert unter [docs/api/openapi.yaml](docs/api/openapi.yaml) |
| http://localhost:8025 | Mailpit: alle versendeten E-Mails (z. B. Mahnungen) |

Bedienung: [Benutzerhandbuch](docs/benutzerhandbuch.md). Gutscheine gibt es im MVP nur per SQL; das Handbuch enthält ein Beispiel.

Stoppen: `docker compose down` (Daten bleiben im Volume `postgres-daten`), `docker compose down -v` löscht sie.

## Konfiguration

Backend und Docker Compose lesen `.env` im Wurzelverzeichnis (Vorlage [.env.example](.env.example)):

| Variable | Standard | Bedeutung |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` | `localhost`, `5432`, `bestellsystem`, `bestellsystem` | Datenbank |
| `DB_PASSWORD` | – (Pflicht) | Datenbankpasswort |
| `MAIL_HOST`, `MAIL_PORT` | `localhost`, `1025` | SMTP (lokal Mailpit) |
| `MAIL_ABSENDER` | `shop@bestellsystem.local` | Absender der E-Mails |

Fachliche Einstellungen in `backend/src/main/resources/application.yml` unter `bestellsystem.*`: Zeitzone (`Europe/Berlin`), Zahlungsziel (14 Tage), Mahnintervall (7 Tage), Zeitplan des Mahnlaufs (täglich 06:00, `-` schaltet ab).

## Tests ausführen

```bash
cd backend && ./gradlew build                   # alle Backend-Tests + Formatprüfung (Docker muss laufen)
cd backend && ./gradlew test --tests '*KundeTest' # einzelne Testklasse
cd backend && ./gradlew spotlessApply           # Formatierung korrigieren
cd frontend && npm test -- --watch=false        # Frontend-Tests (Vitest), einmalig
cd frontend && npm test                         # Frontend-Tests im Watch-Modus
```

- Backend-Integrationstests starten PostgreSQL per **Testcontainers**; die laufende Compose-Datenbank wird nicht benutzt.
- Jedes Akzeptanzkriterium einer Story ist ein Test mit der Story im Namen, z. B. `US-04.1 AK3: nicht genug Bestand lehnt ganze Bestellung ab`. Testberichte: `backend/build/reports/tests/test/index.html`.
- CI ([.github/workflows/ci.yml](.github/workflows/ci.yml)) führt `./gradlew build` sowie im Frontend Client-Drift-Prüfung, Tests und Build aus.

### API ändern

Die OpenAPI-Spezifikation ist versioniert ([ADR 0002](docs/adr/0002-openapi-spezifikation-als-datei.md)). Nach einer API-Änderung:

```bash
cd backend && ./gradlew aktualisiereOpenApi   # schreibt frontend/openapi.json und docs/api/openapi.yaml
cd frontend && npm run generate:api           # erzeugt src/app/api/ neu
```

Beides wird committet; `./gradlew build` schlägt fehl, wenn die Dateien veraltet sind.

## Projektstruktur

```
backend/    Spring Boot, Pakete nach Fachlichkeit (de.bestellsystem.{kunde, katalog, …})
frontend/   Angular, Ordner spiegeln die Fachpakete; src/app/api/ ist generiert
docs/       Anforderung, Entscheidungen, Domänenmodell, Architektur, ADRs, Handbuch, API
epics/      Epics E00 bis E08
stories/    User Stories mit Akzeptanzkriterien
```

## Dokumentation

| Datei | Inhalt |
|---|---|
| [docs/00-anforderung.md](docs/00-anforderung.md) | Originaltext der Anforderung |
| [docs/01-entscheidungen.md](docs/01-entscheidungen.md) | Fachliche Entscheidungen D-01 bis D-22 (verbindlich) |
| [docs/02-domaenenmodell.md](docs/02-domaenenmodell.md) | UML-Klassendiagramm, Invarianten I-1 bis I-11, Formeln |
| [docs/arc42.md](docs/arc42.md) | Architekturüberblick nach arc42: Kontext, Bausteine, Laufzeit, Verteilung, Querschnitt, Risiken |
| [docs/ARCHITEKTUR.md](docs/ARCHITEKTUR.md) | Detailreferenz: alle Pakete, Endpunkte, Tabellen, Migrationen, Tests |
| [docs/adr/](docs/adr/README.md) | Technische Entscheidungen (ADR 0001 bis 0011) mit Bezug zu D-xx |
| [docs/benutzerhandbuch.md](docs/benutzerhandbuch.md) | Bedienung der wichtigsten Abläufe |
| [docs/api/openapi.yaml](docs/api/openapi.yaml) | OpenAPI-Spezifikation der REST-API |
| [docs/fahrplan.md](docs/fahrplan.md) | Umsetzungsschritte 0 bis 9 |
| [CLAUDE.md](CLAUDE.md) | Arbeitsregeln für Claude Code (Stack, Struktur, Tests) |

## Akteure

- **Kunde**: registriert sich, pflegt Adressbuch und Merkliste, bestellt.
- **Shop-Mitarbeiter**: pflegt Katalog, Lagerbestand, Gutscheine, Versandpakete.
- **System**: Zahlungsanbieter (meldet Zahlungsstatus), zeitgesteuerter Mahnlauf, E-Mail-Versand.

## Epics und Stories

| ID | Epic | Umgesetzte Stories |
|---|---|---|
| E00 | [Domänenkern & Plattform](epics/E00-domaenenkern-plattform.md) | (technisch) |
| E01 | [Kundenkonto & Adressbuch](epics/E01-kundenkonto-adressbuch.md) | [US-01.1](stories/US-01.1-registrierung-mit-lieferadresse.md), [US-01.2](stories/US-01.2-weitere-adresse-hinzufuegen.md), [US-01.3](stories/US-01.3-adresse-aendern-loeschen.md) |
| E02 | [Produktkatalog & Lagerbestand](epics/E02-produktkatalog-lagerbestand.md) | [US-02.1](stories/US-02.1-verfuegbarkeit-pruefen.md) |
| E03 | [Merkliste](epics/E03-merkliste.md) | [US-03.1](stories/US-03.1-produkt-merken.md), [US-03.2](stories/US-03.2-produkt-entfernen.md) |
| E04 | [Bestellung & Checkout](epics/E04-bestellung-checkout.md) | [US-04.1](stories/US-04.1-bestellung-aufgeben.md), [US-04.2](stories/US-04.2-bestellhistorie-einsehen.md) |
| E05 | [Zahlungen & Erstattungen](epics/E05-zahlungen-erstattungen.md) | [US-05.1](stories/US-05.1-zahlungsstatus-pruefen.md) |
| E06 | [Mahnwesen](epics/E06-mahnwesen.md) | [US-06.1](stories/US-06.1-automatische-mahnpruefung.md), [US-06.2](stories/US-06.2-taeglicher-mahnlauf.md) |
| E07 | [Versand & Sendungsverfolgung](epics/E07-versand-sendungsverfolgung.md) | [US-07.1](stories/US-07.1-versandpaket-anlegen.md), [US-07.2](stories/US-07.2-lieferstatus-aktualisieren.md), [US-07.3](stories/US-07.3-pakete-einer-bestellung-sehen.md) |
| E08 | [Gutscheine & Aktionen](epics/E08-gutscheine-aktionen.md) | [US-08.1](stories/US-08.1-gutschein-einloesen.md) |

Nicht im MVP: Login, Storno, Versandkosten, Steuern, Rechnungsadresse in der Bestellung, Mahnstufen und Gebühren, Gutschein-Nutzungslimits, echte Zahlungsanbieter, Tracking-APIs.
