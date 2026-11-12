## Session 9: Naechster-Schritt für fahrplan.md

*Projekt: bestellsystem · ID `8bd24007-f342-496d-9d38-8dc24eb26ea4` · 2026-11-12 10:21 – 2026-11-12 10:33 UTC*

### ❯ Nutzer

> /clear

### ❯ Nutzer

> /naechster-schritt

**⏺ Bash**

```bash
cat docs/fahrplan.md
```

````
# Fahrplan

Ein Schritt pro Sitzung, von oben nach unten. Start: neue Sitzung (oder `/clear`), dann `/naechster-schritt`.
Abgehakt wird erst, wenn **alle** "Fertig wenn"-Punkte erfüllt sind. Hinter dem Haken stehen Datum und Commit.

Werkzeug: **feature-dev** = Design offen, Architektur-Freigabe nötig · **goal** = Ziel eindeutig prüfbar, reine Logik.

---

## [x] 0 Vorbereitung (2026-11-12)

**Werkzeug:** von Hand
**Ziel:** Repo und Rahmen stehen.

- [x] `CLAUDE.md` angelegt
- [x] `git init`, `.gitignore` (Java, Node, `.env`, IDE), `.gitattributes` (LF)
- [x] Paketname bestätigt: `de.bestellsystem` (groupId), `bestellsystem-backend` (artifactId)
- [x] Geprüft: Java 21.0.13, Maven 3.8.7 (nur Wrapper nutzen), Node 22.20 / npm 11.6, Docker 29.9 + Compose 5.7, Daemon läuft; Angular CLI über `npx`

**Fertig wenn:** erster Commit mit Doku und `CLAUDE.md` existiert.

---

## [x] 1 Fundament Backend (E00) (2026-11-12, 9aa6671)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E00-*.md`, `docs/01-entscheidungen.md` (D-08, D-11, D-13), `CLAUDE.md`

```
/feature-dev Setze Schritt 1 aus docs/fahrplan.md um: Backend-Fundament (Epic E00).
Spring-Boot-Gerüst unter backend/ nach CLAUDE.md, docker-compose.yml (PostgreSQL, Mailpit),
Flyway, Testcontainers, Wertobjekte Geld und Anschrift, Clock-Bean, ProblemDetail-Fehlerbehandlung,
springdoc-openapi, Bestellnummern-Generator, Benachrichtigungs-Port mit Mail-Adapter.
Noch keine Fachentities außer diesen Bausteinen. Kein Frontend.
```

**Fertig wenn:**
- `./gradlew build` grün, inklusive eines Testcontainers-Tests
- `Geld`-Tests: Addition, Multiplikation, Rundung, keine Gleitkommafehler
- Swagger UI erreichbar
… [gekürzt, 202 Zeilen gesamt]
````

**⏺ Bash**

```bash
ls stories/ epics/ && git status --short
```

```
epics/:
E00-domaenenkern-plattform.md
E01-kundenkonto-adressbuch.md
E02-produktkatalog-lagerbestand.md
E03-merkliste.md
E04-bestellung-checkout.md
E05-zahlungen-erstattungen.md
E06-mahnwesen.md
E07-versand-sendungsverfolgung.md
E08-gutscheine-aktionen.md

stories/:
US-01.1-registrierung-mit-lieferadresse.md
US-01.2-weitere-adresse-hinzufuegen.md
US-01.3-adresse-aendern-loeschen.md
US-02.1-verfuegbarkeit-pruefen.md
US-03.1-produkt-merken.md
US-04.1-bestellung-aufgeben.md
US-04.2-bestellhistorie-einsehen.md
US-05.1-zahlungsstatus-pruefen.md
US-06.1-automatische-mahnpruefung.md
US-08.1-gutschein-einloesen.md
?? backend/bin/
```

### ● Claude

Schritt 5 (Zahlungen, E05) ist dran. Die Schritte 0 bis 4b sind alle abgehakt. Laut Fahrplan ist das Werkzeug **goal**, und `/goal` startest du selbst. Ich habe deshalb nichts gebaut und auch nichts committet, weil es keine Änderungen gibt.

Zum Starten diesen Befehl eingeben:

```
/goal Alle Akzeptanzkriterien aus stories/US-05.1-zahlungsstatus-pruefen.md sind als Tests
umgesetzt und grün, dazu Endpunkte für Zahlungsstatus-Änderung und Erstattung, ./gradlew build grün,
docs/ARCHITEKTUR.md aktualisiert, Schritt 5 in docs/fahrplan.md abgehakt.
```

Zwei Punkte vorher:

1. **Das Goal deckt nicht alles ab.** „Fertig wenn“ verlangt auch: „Bezahlstatus und offener Betrag in der Bestellansicht sichtbar“. Das ist Frontend, steht aber nicht im Goal-Text. Dann würde der Schritt abgehakt, obwohl ein Punkt offen ist. Du kannst entweder den Goal-Text um diese Frontend-Anzeige (mit `npm test` grün) erweitern, oder das Frontend danach mit feature-dev bauen und erst dann abhaken.
2. **`backend/bin/` ist untracked.** Das ist wahrscheinlich IDE-Build-Output (Eclipse/VS Code) und gehört in `.gitignore`. Soll ich das mit einem eigenen Commit erledigen?

### ❯ Nutzer

> Ok go

**⏺ Bash**

```bash
ls backend/bin | head; find backend/bin -type f | head -5; cat .gitignore
```

```
main
test
backend/bin/main/application.yml
backend/bin/main/de/bestellsystem/BestellsystemApplication.class
backend/bin/main/db/migration/V2__produkt.sql
backend/bin/main/db/migration/V1__bestellnummer_sequenz.sql
backend/bin/main/db/migration/V3__kunde.sql
# Umgebung und Geheimnisse
.env
.env.*
!.env.example
*.pem
*.key

# Betriebssystem
.DS_Store
Thumbs.db

# IDEs
.idea/
*.iml
.vscode/*
!.vscode/extensions.json
.project
.classpath
.settings/
.factorypath
*.swp

# Claude Code (lokale Einstellungen; Skills und CLAUDE.md werden versioniert)
.claude/settings.local.json

# Backend (Gradle / Java)
backend/build/
backend/.gradle/
*.class
*.log
hs_err_pid*
replay_pid*

… [gekürzt, 54 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -i 's|^backend/.gradle/$|backend/.gradle/\nbackend/bin/|' .gitignore && git add .gitignore && git status --short && git commit -q -m "Ignore IDE build output in backend/bin

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -1
```

```
M  .gitignore
311fc7d Ignore IDE build output in backend/bin
```

### ● Claude

Ich habe `backend/bin/` in `.gitignore` aufgenommen und das als Commit `311fc7d` („Ignore IDE build output in backend/bin“) committet. Der Arbeitsbaum ist jetzt sauber. Build und Tests habe ich dafür nicht laufen lassen, weil sich nur die Ignore-Liste geändert hat.

Schritt 5 kann ich nicht selbst starten, denn `/goal` startest du. Ich empfehle den erweiterten Text, damit auch der Frontend-Punkt aus „Fertig wenn“ abgedeckt ist:

```
/goal Alle Akzeptanzkriterien aus stories/US-05.1-zahlungsstatus-pruefen.md sind als Tests
umgesetzt und grün, dazu Endpunkte für Zahlungsstatus-Änderung und Erstattung, Bezahlstatus und
offener Betrag in der Bestellansicht des Frontends sichtbar, ./gradlew build und npm test grün,
docs/ARCHITEKTUR.md aktualisiert, Schritt 5 in docs/fahrplan.md abgehakt.
```

Falls ich Schritt 5 stattdessen direkt von Hand umsetzen soll, sag Bescheid.

### ❯ Nutzer

> Mach du das

**⏺ Bash**

```bash
cat epics/E05-*.md stories/US-05.1-*.md && cat docs/01-entscheidungen.md
```

```
# E05 Zahlungen & Erstattungen

**Priorität:** MVP
**Akteure:** System (Zahlungsanbieter), Shop-Mitarbeiter (Erstattung)
**Abhängig von:** E04

## Ziel

Alle Geldflüsse einer Bestellung werden lückenlos gebucht, und für jede Bestellung ist jederzeit feststellbar, ob sie vollständig bezahlt ist.

## Umfang

- Zahlungsstatus aktualisieren (`AUSSTEHEND` → `ERFOLGREICH` / `FEHLGESCHLAGEN` / `STORNIERT`). → D-05
- Teilzahlungen als weitere Buchungen.
- Erstattungen als Buchung mit `buchungsart = ERSTATTUNG`. → D-05
- `bezahlteSumme()`, `offenerBetrag()`, `istVollstaendigBezahlt()`. → D-05

## Nicht im Umfang

- Echte Anbindung an Zahlungsanbieter (Webhooks), Rechnungs-PDF, Buchhaltungsexport.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-05.1](../stories/US-05.1-zahlungsstatus-pruefen.md) | Prüfen, ob eine Bestellung vollständig bezahlt ist | ausformuliert |
| US-05.2 | Zahlungsstatus vom Anbieter übernehmen | Kandidat |
| US-05.3 | Erstattung buchen | Kandidat |
# US-05.1 Prüfen, ob eine Bestellung vollständig bezahlt ist

**Epic:** E05 Zahlungen & Erstattungen
**Entscheidungen:** D-02, D-05, D-11

## Story

**Als** Shop-Mitarbeiter
**möchte ich** für jede Bestellung sehen, ob sie vollständig bezahlt ist und wie viel noch offen ist,
**damit** ich nur bezahlte Ware versende und offene Posten erkenne.

## Akzeptanzkriterien
… [gekürzt, 327 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/ARCHITEKTUR.md && grep -n -i -A30 "zahlung" docs/02-domaenenmodell.md | head -150
```

````
# Architektur

Stand: Schritt 4b (Checkout im Frontend, Bestellhistorie US-04.2). Wird mit jedem Schritt im selben Commit nachgezogen.

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
| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen); setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`) |
… [gekürzt, 364 Zeilen gesamt]
````

### ❯ Nutzer

> [Request interrupted by user]

### ❯ Nutzer

> /goal Alle Akzeptanzkriterien aus stories/US-05.1-zahlungsstatus-pruefen.md sind als Tests
> umgesetzt und grün, dazu Endpunkte für Zahlungsstatus-Änderung und Erstattung, Bezahlstatus und
> offener Betrag in der Bestellansicht des Frontends sichtbar, ./gradlew build und npm test grün,
> docs/ARCHITEKTUR.md aktualisiert, Schritt 5 in docs/fahrplan.md abgehakt.

**⏺ Bash**

```bash
cat stories/US-05.1-*.md epics/E05-*.md && grep -n "D-0[1-5]" -A12 docs/01-entscheidungen.md | head -120
```

```
# US-05.1 Prüfen, ob eine Bestellung vollständig bezahlt ist

**Epic:** E05 Zahlungen & Erstattungen
**Entscheidungen:** D-02, D-05, D-11

## Story

**Als** Shop-Mitarbeiter
**möchte ich** für jede Bestellung sehen, ob sie vollständig bezahlt ist und wie viel noch offen ist,
**damit** ich nur bezahlte Ware versende und offene Posten erkenne.

## Akzeptanzkriterien

Gegeben jeweils eine Bestellung mit Gesamtwert **100,00 €**.

1. **Nur ausstehende Zahlung**
   Gegeben eine Zahlung 100,00 € mit Status AUSSTEHEND,
   dann ist die Bestellung nicht vollständig bezahlt, offener Betrag 100,00 €.

2. **Vollständig bezahlt**
   Gegeben eine Zahlung 100,00 € mit Status ERFOLGREICH,
   dann ist die Bestellung vollständig bezahlt, offener Betrag 0,00 €.

3. **Teilzahlungen**
   Gegeben Zahlungen 40,00 € und 60,00 €, beide ERFOLGREICH,
   dann ist die Bestellung vollständig bezahlt.
   Gegeben nur die Zahlung über 40,00 € ist ERFOLGREICH,
   dann ist der offene Betrag 60,00 €.

4. **Fehlgeschlagene und stornierte Zahlungen zählen nicht**
   Gegeben eine Zahlung 100,00 € FEHLGESCHLAGEN und eine 100,00 € STORNIERT,
   dann ist der offene Betrag 100,00 €.

5. **Erstattung reduziert die bezahlte Summe**
   Gegeben eine Zahlung 100,00 € ERFOLGREICH und eine Erstattung 30,00 € ERFOLGREICH,
   dann ist die bezahlte Summe 70,00 € und die Bestellung nicht vollständig bezahlt.

6. **Überzahlung**
   Gegeben Zahlungen 100,00 € und 20,00 €, beide ERFOLGREICH,
   dann ist die Bestellung vollständig bezahlt, offener Betrag 0,00 € (nicht negativ).
… [gekürzt, 172 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/ARCHITEKTUR.md && find backend/src -path '*zahlung*' -o -path '*bestellung*' | grep java$
```

````
# Architektur

Stand: Schritt 4b (Checkout im Frontend, Bestellhistorie US-04.2). Wird mit jedem Schritt im selben Commit nachgezogen.

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
| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen); setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`) |
… [gekürzt, 274 Zeilen gesamt]
````

**⏺ Bash**

```bash
sed -n 67,100p docs/01-entscheidungen.md; grep -n "^## D-" docs/01-entscheidungen.md; cd backend/src/main/java/de/bestellsystem && cat zahlung/domain/*.java zahlung/api/ZahlungDto.java bestellung/domain/Bestellung.java
```

````
## D-05 Erstattungen und Transaktionsstatus

**Problem:** Erstattungen werden erwähnt, aber nicht modelliert. Unklar, welche Zahlungen zur "Summe aller geleisteten Zahlungen" zählen.

**Entscheidung:**
- Zahlung erhält `buchungsart ∈ {ZAHLUNG, ERSTATTUNG}`. Der Betrag ist **immer positiv**; die Buchungsart bestimmt das Vorzeichen.
- `transaktionsstatus ∈ {AUSSTEHEND, ERFOLGREICH, FEHLGESCHLAGEN, STORNIERT}`.
- Es gilt:

```
bezahlteSumme          = Σ ERFOLGREICH-ZAHLUNG − Σ ERFOLGREICH-ERSTATTUNG
offenerBetrag          = max(0, gesamtwert − bezahlteSumme)
istVollstaendigBezahlt = bezahlteSumme ≥ gesamtwert
```

- Überzahlung ist erlaubt (offenerBetrag = 0), die Rückzahlung erfolgt als Erstattung.

---

## D-06 Liefer- und Rechnungsadressen

**Problem:** Wie wird zwischen Liefer- und Rechnungsadresse unterschieden? Wie wird "mindestens eine Lieferadresse" erzwungen?

**Entscheidung:**
- Adresse erhält zwei Kennzeichen: `istLieferadresse` und `istRechnungsadresse`. Mindestens eines ist wahr; eine Adresse darf beides sein.
- **Invariante:** Ein Kunde hat stets mindestens eine Adresse mit `istLieferadresse = wahr`.
  - Die Registrierung verlangt eine Lieferadresse.
  - Das Löschen der letzten Lieferadresse oder das Entfernen ihres Kennzeichens wird abgelehnt.
- Adressen sind Teil des Kunden (Komposition): Wird der Kunde gelöscht, werden seine Adressen gelöscht.

**Nicht im Umfang:** Rechnungsadresse in der Bestellung (der Text verlangt nur den Lieferadress-Snapshot). Leicht nachrüstbar über D-08.

---

7:## D-01 Gesamtwert: gespeichert oder berechnet?
26:## D-02 Zahlung "mindestens eine" vs. offene Beträge
36:## D-03 Zahlungsrückstand braucht eine Fälligkeit
51:## D-04 Gutschein: Art des Rabatts und Wirkung
67:## D-05 Erstattungen und Transaktionsstatus
86:## D-06 Liefer- und Rechnungsadressen
… [gekürzt, 431 Zeilen gesamt]
````

**⏺ Bash**

```bash
cat bestellung/application/BestellungService.java bestellung/api/BestellungController.java bestellung/api/BestellungDto.java bestellung/api/BestellungUebersichtDto.java bestellung/persistence/BestellungRepository.java
```

```
package de.bestellsystem.bestellung.application;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.BestellnummernGenerator;
import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.application.GutscheinService;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BestellungService {

    private final BestellungRepository repository;
    private final KundeService kundeService;
    private final ProduktService produktService;
    private final GutscheinService gutscheinService;
    private final BestellnummernGenerator bestellnummernGenerator;
    private final FachlicheEinstellungen einstellungen;
    private final Clock uhr;

    BestellungService(
            BestellungRepository repository,
            KundeService kundeService,
… [gekürzt, 354 Zeilen gesamt]
```

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Zahlungsstatus ändern: Welche Übergänge sind erlaubt? (US-05.2 ist noch Kandidat, E05 nennt nur AUSSTEHEND → ERFOLGREICH/FEHLGESCHLAGEN/STORNIERT.)",
   "header": "Übergänge",
   "multiSelect": false,
   "options": [
    {
     "label": "Nur aus AUSSTEHEND (Recommended)",
     "description": "AUSSTEHEND → ERFOLGREICH/FEHLGESCHLAGEN/STORNIERT. Endzustände sind endgültig; alles andere 422. Zahlungszeitpunkt aus Clock, nur bei ERFOLGREICH gesetzt."
    },
    {
     "label": "Auch ERFOLGREICH → STORNIERT",
     "description": "Wie oben, zusätzlich darf eine erfolgreiche Buchung storniert werden (Rückbuchung ohne Erstattung)."
    }
   ]
  },
  {
   "question": "Erstattung buchen (US-05.3 Kandidat): Wie entsteht sie?",
   "header": "Erstattung",
   "multiSelect": false,
   "options": [
    {
     "label": "AUSSTEHEND, dann Status (Recommended)",
     "description": "POST legt ERSTATTUNG mit Status AUSSTEHEND an; Abschluss über denselben Status-Endpunkt wie Zahlungen. Einheitlicher Ablauf."
    },
    {
     "label": "Sofort ERFOLGREICH",
     "description": "Mitarbeiter bucht Erstattung direkt als erfolgreich, Zeitpunkt sofort gesetzt."
    }
… [gekürzt, 68 Zeilen gesamt]
```

```
Your questions have been answered: "Zahlungsstatus ändern: Welche Übergänge sind erlaubt? (US-05.2 ist noch Kandidat, E05 nennt nur AUSSTEHEND → ERFOLGREICH/FEHLGESCHLAGEN/STORNIERT.)"="Nur aus AUSSTEHEND (Recommended)", "Erstattung buchen (US-05.3 Kandidat): Wie entsteht sie?"="AUSSTEHEND, dann Status (Recommended)", "Obergrenze und Zahlungsart einer Erstattung?"="≤ erstattbar, Art wie Zahlung (Recommended)", "Frontend-Umfang in Schritt 5?"="Anzeige + Status in Historie (Recommended)". You can now continue with these answers in mind.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && wc -l bestellung/*/*.java && sed -n 1,120p bestellung/domain/BestellungTest.java
```

```
  590 bestellung/api/BestellungApiIT.java
  153 bestellung/application/BestellungNebenlaeufigkeitIT.java
  327 bestellung/domain/BestellungTest.java
 1070 total
package de.bestellsystem.bestellung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class BestellungTest {

    private static final LocalDate ZWOELFTER_NOVEMBER = LocalDate.of(2026, 11, 12);
    private static final Period ZAHLUNGSZIEL = Period.ofDays(14);
    private static final Anschrift HAUPTSTRASSE =
            new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE");
    private static final UUID KUNDE = UUID.randomUUID();

    private Produkt buch;
    private Produkt lampe;

    @BeforeEach
… [gekürzt, 124 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 1,140p bestellung/api/BestellungApiIT.java; grep -n "void \|DisplayName\|Nested\|class " bestellung/api/BestellungApiIT.java | sed -n 1,200p
```

```
package de.bestellsystem.bestellung.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import jakarta.persistence.EntityManagerFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
… [gekürzt, 214 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 500,590p bestellung/api/BestellungApiIT.java
```

```
        }

        @Test
        @DisplayName("US-04.2: Historie braucht unabhängig von der Bestellanzahl 2 Abfragen")
        void historieOhneNPlusEins() throws Exception {
            gibAuf(einBuch(), null).andExpect(status().isCreated());
            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);

            gibAuf(zweiBuecherEineLampe(), "HERBST10").andExpect(status().isCreated());
            gibAuf(einBuch(), null).andExpect(status().isCreated());
            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("Fahrplan 4a: Bestellabruf braucht unabhängig von der Positionszahl 2 Abfragen")
    void bestellabrufOhneNPlusEins() throws Exception {
        String klein = bestellnummer(gibAuf(einBuch(), "HERBST10"));
        StringBuilder viele = new StringBuilder(einBuch());
        for (int i = 0; i < 4; i++) {
            Produkt produkt = produkte.save(new Produkt("Artikel " + i, Geld.ausCent(100), 5));
            viele.append(",").append(position(produkt, 1));
        }
        String gross = bestellnummer(gibAuf(viele.toString(), "HERBST10"));

        assertThat(abfragenFuerAbruf(klein)).isEqualTo(2);
        assertThat(abfragenFuerAbruf(gross)).isEqualTo(2);
    }

    private long abfragenFuerAbruf(String bestellnummer) throws Exception {
        return abfragen(get(BESTELLUNG, kundenId, bestellnummer));
    }

    private long abfragen(RequestBuilder anfrage) throws Exception {
        Statistics statistik = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        statistik.setStatisticsEnabled(true);
        statistik.clear();
        try {
            mockMvc.perform(anfrage).andExpect(status().isOk());
            return statistik.getPrepareStatementCount();
… [gekürzt, 91 Zeilen gesamt]
```

### ● Claude

Entscheidung zuerst als D-19 festhalten.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat >> docs/01-entscheidungen.md <<'EOF'

---

## D-19 Zahlungsstatus ändern und Erstattung buchen

**Problem:** E05 nennt Statuswechsel und Erstattungen, die Stories US-05.2 und US-05.3 sind aber nur Kandidaten. Offen waren erlaubte Übergänge, Ablauf und Obergrenze einer Erstattung sowie der Umfang im Frontend.

**Entscheidung:**
- Ein Status lässt sich nur **aus `AUSSTEHEND`** ändern, und zwar nach `ERFOLGREICH`, `FEHLGESCHLAGEN` oder `STORNIERT`. Diese drei sind endgültig; jeder andere Wechsel (auch `AUSSTEHEND` → `AUSSTEHEND`) wird mit 422 abgelehnt. Das gilt für Zahlungen und Erstattungen gleichermaßen.
- Der `zahlungszeitpunkt` wird aus der Uhr gesetzt, wenn eine Buchung `ERFOLGREICH` wird, und bleibt bei `FEHLGESCHLAGEN` und `STORNIERT` leer (präzisiert D-17).
- Eine Erstattung entsteht mit Status `AUSSTEHEND` und wird über denselben Statuswechsel abgeschlossen. Zahlungsart und Betrag gibt der Aufrufer an.
- Betrag einer Erstattung: > 0 (sonst 400) und ≤ **erstattbarer Betrag** = `bezahlteSumme − Σ AUSSTEHEND-ERSTATTUNG` (sonst 422). So kann `bezahlteSumme` nie negativ werden.
- Statuswechsel und Erstattung erhöhen die Version der Bestellung (`OPTIMISTIC_FORCE_INCREMENT`); gleichzeitige Buchungen auf derselben Bestellung scheitern für die zweite mit 409.
- Teilzahlungen sind weitere Buchungen `ZAHLUNG` mit Status `AUSSTEHEND` (Domäne); ein Endpunkt dafür folgt mit US-05.2.
- API: `PUT …/bestellungen/{nr}/zahlungen/{zahlungId}/status`, `POST …/bestellungen/{nr}/erstattungen` (201 + `Location` auf `GET …/zahlungen/{zahlungId}`). Bestellung und Historie liefern `bezahlteSumme`, `offenerBetrag` und `istVollstaendigBezahlt`.
- Frontend: Bestellansicht zeigt Bezahlstatus, bezahlte Summe und offenen Betrag; die Historie zeigt je Bestellung „bezahlt“ oder den offenen Betrag. Bedienelemente für Statuswechsel und Erstattung gibt es noch nicht.

**Nicht im Umfang (Schritt 5):** Zahlungsanbieter-Anbindung, Endpunkt für Teilzahlungen, Bedienung im Frontend.
EOF
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && grep -n "public" gemeinsam/domain/Geld.java gemeinsam/domain/*Exception.java; grep -rn "OPTIMISTIC_FORCE_INCREMENT" --include=*.java . | head
```

```
gemeinsam/domain/FachlicheAblehnungException.java:4:public class FachlicheAblehnungException extends RuntimeException {
gemeinsam/domain/FachlicheAblehnungException.java:6:    public FachlicheAblehnungException(String meldung) {
gemeinsam/domain/NichtGefundenException.java:4:public class NichtGefundenException extends RuntimeException {
gemeinsam/domain/NichtGefundenException.java:6:    public NichtGefundenException(String meldung) {
gemeinsam/domain/Geld.java:15:public record Geld(long cent) implements Comparable<Geld> {
gemeinsam/domain/Geld.java:17:    public static final String WAEHRUNG = "EUR";
gemeinsam/domain/Geld.java:18:    public static final Geld NULL = new Geld(0);
gemeinsam/domain/Geld.java:22:    public static Geld ausCent(long cent) {
gemeinsam/domain/Geld.java:26:    public String waehrung() {
gemeinsam/domain/Geld.java:30:    public Geld addiere(Geld andere) {
gemeinsam/domain/Geld.java:34:    public Geld subtrahiere(Geld andere) {
gemeinsam/domain/Geld.java:38:    public Geld multipliziere(int menge) {
gemeinsam/domain/Geld.java:43:    public Geld prozentsatz(BigDecimal prozent) {
gemeinsam/domain/Geld.java:50:    public boolean istNegativ() {
gemeinsam/domain/Geld.java:55:    public int compareTo(Geld andere) {
gemeinsam/domain/Geld.java:60:    public String toString() {
gemeinsam/domain/KonfliktException.java:4:public class KonfliktException extends RuntimeException {
gemeinsam/domain/KonfliktException.java:6:    public KonfliktException(String meldung) {
(eval):1: no matches found: --include=*.java
```

**⏺ Bash**

```bash
grep -rn "FORCE_INCREMENT\|LockModeType" . | head; grep -n "toString" -A4 gemeinsam/domain/Geld.java
```

```
katalog/persistence/ProduktRepository.java:4:import jakarta.persistence.LockModeType;
katalog/persistence/ProduktRepository.java:18:    @Lock(LockModeType.PESSIMISTIC_WRITE)
kunde/application/KundeService.java:11:import jakarta.persistence.LockModeType;
kunde/application/KundeService.java:103:        entityManager.lock(kunde, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
60:    public String toString() {
61-        String vorzeichen = cent < 0 ? "-" : "";
62-        long betrag = Math.abs(cent);
63-        return "%s%d,%02d %s".formatted(vorzeichen, betrag / 100, betrag % 100, WAEHRUNG);
64-    }
```

### ● Claude

Now the domain test (test first).

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/domain/BezahlstatusTest.java
package de.bestellsystem.bestellung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** US-05.1 und D-19; jede Bestellung hat einen Gesamtwert von 100,00 €. */
class BezahlstatusTest {
… [gekürzt, 336 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/domain/BezahlstatusTest.java (file state is current in your context — no need to Read it back)
```

### ● Claude

Now domain code. `Zahlung`:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/zahlung/domain && python3 - <<'EOF'
p='Zahlung.java'
s=open(p).read()
s=s.replace('''import de.bestellsystem.gemeinsam.domain.Geld;
''','''import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
''')
s=s.replace('''        return new Zahlung(zahlungsart, Buchungsart.ZAHLUNG, Transaktionsstatus.AUSSTEHEND, betrag);
    }
''','''        return new Zahlung(zahlungsart, Buchungsart.ZAHLUNG, Transaktionsstatus.AUSSTEHEND, betrag);
    }

    /** Teilzahlung oder Erstattung (D-19): immer AUSSTEHEND, Betrag größer 0. */
    public static Zahlung ausstehend(Zahlungsart zahlungsart, Buchungsart buchungsart, Geld betrag) {
        if (buchungsart == null) {
            throw new IllegalArgumentException("buchungsart ist erforderlich");
        }
        if (betrag == null || betrag.compareTo(Geld.NULL) <= 0) {
            throw new IllegalArgumentException("betrag muss größer 0 sein");
        }
        return new Zahlung(zahlungsart, buchungsart, Transaktionsstatus.AUSSTEHEND, betrag);
    }

    /**
     * Nur aus AUSSTEHEND in einen Endzustand (D-19); der Zeitpunkt wird nur bei Erfolg gesetzt.
     */
    public void aendereStatus(Transaktionsstatus neuerStatus, Instant zeitpunkt) {
        if (neuerStatus == null) {
            throw new IllegalArgumentException("transaktionsstatus ist erforderlich");
        }
… [gekürzt, 56 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Now `Bestellung`:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/bestellung/domain && python3 - <<'EOF'
p='Bestellung.java'
s=open(p).read()
s=s.replace('''import de.bestellsystem.gemeinsam.domain.KonfliktException;
''','''import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
''')
s=s.replace('''import de.bestellsystem.zahlung.domain.Zahlung;
''','''import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
''')
s=s.replace('''import java.time.LocalDate;
''','''import java.time.Instant;
import java.time.LocalDate;
''')
s=s.replace('''import java.util.UUID;
''','''import java.util.UUID;
import java.util.function.Predicate;
''')
s=s.replace('''    public String getBestellnummer() {''','''    /** {@code Σ ERFOLGREICH-ZAHLUNG − Σ ERFOLGREICH-ERSTATTUNG} (D-05). */
    public Geld berechneBezahlteSumme() {
        return summe(zahlung -> zahlung.istErfolgreich() && !zahlung.istErstattung())
                .subtrahiere(summe(zahlung -> zahlung.istErfolgreich() && zahlung.istErstattung()));
    }

    /** {@code max(0, gesamtwert − bezahlteSumme)} (D-05). */
    public Geld berechneOffenenBetrag() {
        Geld offen = berechneGesamtwert().subtrahiere(berechneBezahlteSumme());
        return offen.istNegativ() ? Geld.NULL : offen;
… [gekürzt, 93 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
grep -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' build/test-results/test/TEST-*Bezahlstatus*.xml
```

```
build/test-results/test/TEST-de.bestellsystem.bestellung.domain.BezahlstatusTest$ErstattungBuchen.xml:tests="8" skipped="0" failures="0" errors="0"
build/test-results/test/TEST-de.bestellsystem.bestellung.domain.BezahlstatusTest$StatusAendern.xml:tests="13" skipped="0" failures="0" errors="0"
build/test-results/test/TEST-de.bestellsystem.bestellung.domain.BezahlstatusTest.xml:tests="1" skipped="0" failures="0" errors="0"
build/test-results/test/TEST-de.bestellsystem.bestellung.domain.BezahlstatusTest$VollstaendigBezahlt.xml:tests="8" skipped="0" failures="0" errors="0"
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && cat gemeinsam/api/GeldDto.java; grep -rn "GeldDto" --include=*.java . | grep -v "GeldDto.von\|import" | head; sed -n 90,115p kunde/application/KundeService.java
```

```
package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.Geld;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(name = "Geld", description = "Geldbetrag in ganzen Cent (D-11)")
public record GeldDto(
        @Schema(description = "Betrag in Cent", example = "4999") @NotNull @PositiveOrZero
                Long cent,
        @Schema(description = "Währung, immer EUR", example = "EUR", allowableValues = "EUR")
                @NotNull
                @Pattern(regexp = Geld.WAEHRUNG, message = "muss EUR sein")
                String waehrung) {

    public static GeldDto von(Geld geld) {
        return new GeldDto(geld.cent(), geld.waehrung());
    }

    public Geld alsGeld() {
        return Geld.ausCent(cent);
    }
}
(eval):1: no matches found: --include=*.java
        return zumAendern(kundenId)
                .aendereAdresse(adressId, anschrift, istLieferadresse, istRechnungsadresse);
    }

    @Transactional
    public void entferneAdresse(UUID kundenId, UUID adressId) {
        zumAendern(kundenId).entferneAdresse(adressId);
    }

    // Erhöht die Version des Kunden beim Festschreiben, auch wenn sich nur eine Adresse ändert;
    // so scheitert eine gleichzeitige zweite Änderung am Adressbuch (D-16).
    private Kunde zumAendern(UUID kundenId) {
        Kunde kunde = finde(kundenId);
        entityManager.lock(kunde, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
… [gekürzt, 52 Zeilen gesamt]
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='bestellung/persistence/BestellungRepository.java'
s=open(p).read()
s=s.replace('''import de.bestellsystem.bestellung.domain.Bestellung;
''','''import de.bestellsystem.bestellung.domain.Bestellung;
import jakarta.persistence.LockModeType;
''')
s=s.replace('''import org.springframework.data.jpa.repository.JpaRepository;
''','''import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
''')
s=s.rstrip()[:-1]+'''
    /** Zahlungen aller Bestellungen des Kunden in denselben Persistenzkontext (US-04.2, D-19). */
    @Query("select b from Bestellung b join fetch b.zahlungen where b.kundeId = :kundeId")
    List<Bestellung> findeZahlungenDesKunden(@Param("kundeId") UUID kundeId);

    // Erhöht die Version beim Festschreiben, auch wenn sich nur der Status einer Zahlung ändert;
    // so scheitert eine gleichzeitige zweite Buchung auf derselben Bestellung (D-19).
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    @Query(
            """
            select b from Bestellung b
              join fetch b.zahlungen
            where b.bestellnummer = :bestellnummer and b.kundeId = :kundeId
            """)
    Optional<Bestellung> findeZumBuchen(
            @Param("kundeId") UUID kundeId, @Param("bestellnummer") String bestellnummer);
}
'''
open(p,'w').write(s)
… [gekürzt, 117 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='bestellung/application/BestellungService.java'
s=open(p).read()
s=s.replace('''        return repository
                .findeMitPositionen(kundenId, bestellnummer)
                .flatMap(bestellung -> repository.findeZahlungen(bestellnummer))
                .orElseThrow''','''        return repository
                .findeMitZahlungen(kundenId, bestellnummer)
                .orElseThrow''')
s=s.replace('''        Zahlung erstattung = zumBuchen(kundenId, bestellnummer).bucheErstattung(zahlungsart, betrag);
        repository.flush();
        return erstattung;''','''        return zumBuchen(kundenId, bestellnummer).bucheErstattung(zahlungsart, betrag);''')
open(p,'w').write(s)
p='bestellung/persistence/BestellungRepository.java'
s=open(p).read()
s=s.replace('''    // Erhöht die Version''','''    @Query(
            """
            select b from Bestellung b
              join fetch b.zahlungen
            where b.bestellnummer = :bestellnummer and b.kundeId = :kundeId
            """)
    Optional<Bestellung> findeMitZahlungen(
            @Param("kundeId") UUID kundeId, @Param("bestellnummer") String bestellnummer);

    // Erhöht die Version''')
open(p,'w').write(s)
EOF
mkdir -p zahlung/api && cat > zahlung/api/StatusAenderungDto.java <<'EOF'
package de.bestellsystem.zahlung.api;

… [gekürzt, 59 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cat > zahlung/api/ZahlungController.java <<'EOF'
package de.bestellsystem.zahlung.api;

import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.zahlung.domain.Zahlung;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping(ZahlungController.BESTELLUNG)
@Tag(name = "Zahlungen", description = "Zahlungsstatus und Erstattungen (E05, D-19)")
class ZahlungController {

… [gekürzt, 193 Zeilen gesamt]
```

```
package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.api.GeldDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
        name = "BestellungUebersicht",
        description = "Eintrag der Bestellhistorie (US-04.2)",
        requiredProperties = {
            "bestellnummer",
            "bestelldatum",
            "anzahlPositionen",
            "gesamtwert",
            "offenerBetrag",
            "istVollstaendigBezahlt"
        })
public record BestellungUebersichtDto(
        @Schema(example = "B-2026-000123") String bestellnummer,
        LocalDate bestelldatum,
        @Schema(description = "Anzahl der Positionen, nicht der Stückzahl") int anzahlPositionen,
        @Schema(description = "max(0, warenwert − rabattBetrag), berechnet (D-01)")
                GeldDto gesamtwert,
        @Schema(description = "max(0, gesamtwert − bezahlteSumme) (D-05)") GeldDto offenerBetrag,
```

### ● Claude

Zwischenstand: Domäne fertig (30 Tests grün), Endpunkte und DTOs geschrieben. Jetzt API-Integrationstests.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && ls; cat Datenbestand.java | sed -n 1,40p
```

```
BestellsystemApplicationIT.java
bestellung
Datenbestand.java
FesteUhrKonfiguration.java
gemeinsam
gutschein
katalog
kunde
TestcontainersKonfiguration.java
package de.bestellsystem;

import org.springframework.jdbc.core.simple.JdbcClient;

/** Leert die Fachtabellen in Abhängigkeitsreihenfolge, damit sich Testklassen nicht stören. */
public final class Datenbestand {

    private Datenbestand() {}

    public static void leere(JdbcClient jdbc) {
        jdbc.sql("DELETE FROM bestellung").update();
        jdbc.sql("DELETE FROM gutschein").update();
        jdbc.sql("DELETE FROM kunde").update();
        jdbc.sql("DELETE FROM produkt").update();
    }
}
```

**⏺ Bash**

```bash
sed -n 1,200p kunde/application/KundeNebenlaeufigkeitIT.java | head -120; cat FesteUhrKonfiguration.java | grep -n "Clock\|Instant"
```

```
package de.bestellsystem.kunde.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.kunde.domain.Adresse;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.kunde.persistence.KundeRepository;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
@Import(TestcontainersKonfiguration.class)
class KundeNebenlaeufigkeitIT {

    private static final Anschrift BERLIN = new Anschrift("Hauptstraße 1", "Berlin", "10115", "DE");
    private static final Anschrift WIEN = new Anschrift("Ring 2", "Wien", "1010", "AT");
    private static final long WARTEZEIT_SEKUNDEN = 10;

    @Autowired private KundeService service;
    @Autowired private KundeRepository repository;
    @Autowired private TransactionTemplate transaktion;
    @Autowired private JdbcClient jdbc;

… [gekürzt, 125 Zeilen gesamt]
```

**⏺ Bash**

```bash
mkdir -p /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/zahlung/api && cat > /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/zahlung/api/ZahlungApiIT.java <<'EOF'
package de.bestellsystem.zahlung.api;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
… [gekürzt, 382 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/zahlung/api && python3 - <<'EOF'
p='ZahlungApiIT.java'
s=open(p).read()
s=s.replace('''    private KundeService kundenService() {
        return kundeService;
    }

''','')
s=s.replace('''                    kundenService().registriere("Bob", "B", "bob@example.de", HAUPTSTRASSE, false)
                            .getId();''','''                    kundeService
                            .registriere("Bob", "Beispiel", "bob@example.de", HAUPTSTRASSE, false)
                            .getId();''')
open(p,'w').write(s)
EOF
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && sed -i 's/@DisplayName("US-04.2: Historie braucht unabhängig von der Bestellanzahl 2 Abfragen")/@DisplayName("US-04.2, D-19: Historie braucht unabhängig von der Bestellanzahl 3 Abfragen")/' bestellung/api/BestellungApiIT.java && python3 - <<'EOF'
p='bestellung/api/BestellungApiIT.java'
s=open(p).read()
old='''            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);

            gibAuf(zweiBuecherEineLampe(), "HERBST10").andExpect(status().isCreated());
            gibAuf(einBuch(), null).andExpect(status().isCreated());
            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(2);'''
assert old in s
s=s.replace(old, old.replace('isEqualTo(2)','isEqualTo(3)'))
open(p,'w').write(s)
EOF
grep -n "Historie braucht" -A3 bestellung/api/BestellungApiIT.java
```

```
503:        @DisplayName("US-04.2, D-19: Historie braucht unabhängig von der Bestellanzahl 3 Abfragen")
504-        void historieOhneNPlusEins() throws Exception {
505-            gibAuf(einBuch(), null).andExpect(status().isCreated());
506-            assertThat(abfragen(get(BESTELLUNGEN, kundenId))).isEqualTo(3);
[This command modified 1 file you've previously read: bestellung/api/BestellungApiIT.java. Call Read before editing.]
```

### ● Claude

Nebenläufigkeitstest für Erstattungen:

**⏺ Bash**

```bash
mkdir -p /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/zahlung/application && cat > /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/zahlung/application/ErstattungNebenlaeufigkeitIT.java <<'EOF'
package de.bestellsystem.zahlung.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.bestellung.application.Positionswunsch;
import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
… [gekürzt, 134 Zeilen gesamt]
```

```
5:public record Positionswunsch(UUID produktId, int menge) {}
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew spotlessApply -q && ./gradlew test --tests '*ZahlungApiIT' --tests '*ErstattungNebenlaeufigkeitIT' --tests '*BestellungApiIT' -q 2>&1 | grep -v "^\s*at " | head -60
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T11:29:22.253+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:29:22.254+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T11:29:22.256+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-11-12T11:29:22.520+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:29:22.521+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-11-12T11:29:22.524+01:00  INFO 1393679 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.

48 tests completed, 14 failed

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':test'.
> There were failing tests. See the report at: file:///home/mschachner/Projects/bestellsystem/backend/build/reports/tests/test/index.html

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 17s
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/build/test-results/test && python3 - <<'EOF'
import glob,re
import xml.etree.ElementTree as ET
for f in glob.glob('*.xml'):
    for tc in ET.parse(f).getroot().iter('testcase'):
        for fl in list(tc.findall('failure'))+list(tc.findall('error')):
            print(tc.get('classname').split('.')[-1], tc.get('name'), '::', (fl.get('message') or '')[:300])
EOF
```

```
ErstattungNebenlaeufigkeitIT D-19: zwei gleichzeitige Erstattungen über je 60,00 €, nur eine gelingt :: org.springframework.orm.jpa.JpaSystemException: [OPTIMISTIC_FORCE_INCREMENT] not supported for non-versioned entities [de.bestellsystem.zahlung.domain.Zahlung]
ZahlungApiIT$Bezahlstatus US-05.1 AK4: fehlgeschlagene Zahlung zählt nicht, offen 100,00 € :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$Bezahlstatus D-19: Historie zeigt je Bestellung offenen Betrag und Bezahlstatus :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$Bezahlstatus US-05.1 AK5: Erstattung 30,00 € ergibt bezahlte Summe 70,00 € :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$Bezahlstatus US-05.1 AK2: Zahlung erfolgreich, vollständig bezahlt, offen 0,00 € :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$Bezahlstatus US-05.1 AK7: Rabatt 10,00 €, erfolgreiche Zahlung 90,00 € genügt :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$StatusAendern D-19: Endzustand ist endgültig, zweiter Wechsel ergibt 422 :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$StatusAendern D-19: unbekannte Zahlung ergibt 404 :: java.lang.AssertionError: Status expected:<404> but was:<500>
ZahlungApiIT$StatusAendern D-19: AUSSTEHEND als Ziel ergibt 422 :: java.lang.AssertionError: Status expected:<422> but was:<500>
ZahlungApiIT$StatusAendern D-19: Zahlung einer anderen Bestellung ergibt 404 :: java.lang.AssertionError: Status expected:<404> but was:<500>
ZahlungApiIT$ErstattungBuchen D-19: Betrag 0, fehlende Zahlungsart oder fremde Währung ergeben 400 :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$ErstattungBuchen D-19: Erstattung ergibt 201 mit Location, Status AUSSTEHEND :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$ErstattungBuchen D-19: unbekannte Bestellung ergibt 404 :: java.lang.AssertionError: Status expected:<200> but was:<500>
ZahlungApiIT$ErstattungBuchen D-19: mehr als erstattbar ergibt 422, auch mit ausstehender Erstattung :: java.lang.AssertionError: Status expected:<200> but was:<500>
```

### ● Claude

Lock-Hint greift auch auf Zahlungen. Wie bei `KundeService` per `entityManager.lock` nur auf Bestellung sperren:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && grep -n "EntityManager" kunde/application/KundeService.java | head -3; python3 - <<'EOF'
p='bestellung/persistence/BestellungRepository.java'
s=open(p).read()
i=s.index('    // Erhöht die Version')
s=s[:i].rstrip()+'\n}\n'
s=s.replace('import jakarta.persistence.LockModeType;\n','').replace('import org.springframework.data.jpa.repository.Lock;\n','')
open(p,'w').write(s)
p='bestellung/application/BestellungService.java'
s=open(p).read()
s=s.replace('''    private Bestellung zumBuchen(UUID kundenId, String bestellnummer) {
        return repository
                .findeZumBuchen(kundenId, bestellnummer)
                .orElseThrow(() -> nichtGefunden(bestellnummer));
    }''','''    // Erhöht die Version beim Festschreiben, auch wenn sich nur der Status einer Zahlung ändert;
    // so scheitert eine gleichzeitige zweite Buchung auf derselben Bestellung (D-19).
    private Bestellung zumBuchen(UUID kundenId, String bestellnummer) {
        Bestellung bestellung =
                repository
                        .findeMitZahlungen(kundenId, bestellnummer)
                        .orElseThrow(() -> nichtGefunden(bestellnummer));
        entityManager.lock(bestellung, LockModeType.OPTIMISTIC_FORCE_INCREMENT);
        return bestellung;
    }''')
s=s.replace('''import java.time.Clock;''','''import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Clock;''')
s=s.replace('''    private final Clock uhr;

''','''    private final Clock uhr;
    private final EntityManager entityManager;
… [gekürzt, 49 Zeilen gesamt]
```

```
10:import jakarta.persistence.EntityManager;
23:    private final EntityManager entityManager;
25:    KundeService(KundeRepository repository, EntityManager entityManager) {
    List<Bestellung> findeHistorie(@Param("kundeId") UUID kundeId);

    /** Zahlungen aller Bestellungen des Kunden in denselben Persistenzkontext (US-04.2, D-19). */
    @Query("select b from Bestellung b join fetch b.zahlungen where b.kundeId = :kundeId")
    List<Bestellung> findeZahlungenDesKunden(@Param("kundeId") UUID kundeId);

    @Query(
            """
            select b from Bestellung b
              join fetch b.zahlungen
            where b.bestellnummer = :bestellnummer and b.kundeId = :kundeId
            """)
    Optional<Bestellung> findeMitZahlungen(
            @Param("kundeId") UUID kundeId, @Param("bestellnummer") String bestellnummer);
}
```

**⏺ Bash**

```bash
grep -ho 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' TEST-*Zahlung*.xml TEST-*Erstattung*.xml TEST-*BestellungApiIT*.xml | sort | uniq -c
```

```
      1 tests="15" skipped="0" failures="0" errors="0"
      2 tests="1" skipped="0" failures="0" errors="0"
      2 tests="4" skipped="0" failures="0" errors="0"
      2 tests="6" skipped="0" failures="0" errors="0"
      1 tests="7" skipped="0" failures="0" errors="0"
      1 tests="8" skipped="0" failures="0" errors="0"
```

### ● Claude

Backend grün. OpenAPI exportieren, Client erzeugen:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew aktualisiereOpenApi -q 2>&1 | grep -iv "warning\|INFO" | tail -5; cd ../frontend && npm run generate:api 2>&1 | tail -3 && git -C .. status --short
```

```
[[bestellsystem] openapi.json] ############################################################################################
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,supportsES6=true" exited with code 0
[bestellsystem] openapi.json
 M backend/src/main/java/de/bestellsystem/bestellung/api/BestellungDto.java
 M backend/src/main/java/de/bestellsystem/bestellung/api/BestellungUebersichtDto.java
 M backend/src/main/java/de/bestellsystem/bestellung/application/BestellungService.java
 M backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java
 M backend/src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java
 M backend/src/main/java/de/bestellsystem/zahlung/domain/Zahlung.java
 M backend/src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java
 M docs/01-entscheidungen.md
 M frontend/openapi.json
 M frontend/src/app/api/.openapi-generator/FILES
 M frontend/src/app/api/api/api.ts
 M frontend/src/app/api/model/bestellung-uebersicht.ts
 M frontend/src/app/api/model/bestellung.ts
 M frontend/src/app/api/model/models.ts
?? backend/src/main/java/de/bestellsystem/zahlung/api/ErstattungBuchenDto.java
?? backend/src/main/java/de/bestellsystem/zahlung/api/StatusAenderungDto.java
?? backend/src/main/java/de/bestellsystem/zahlung/api/ZahlungController.java
?? backend/src/test/java/de/bestellsystem/bestellung/domain/BezahlstatusTest.java
?? backend/src/test/java/de/bestellsystem/zahlung/
?? frontend/src/app/api/api/zahlungen.service.ts
?? frontend/src/app/api/model/erstattung-buchen.ts
?? frontend/src/app/api/model/status-aenderung.ts
```

### ● Claude

Now frontend: Bestellansicht und Historie.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app/bestellung && ls -R . | head -30; cat bestellung-detail/bestellung-detail.html bestellung-detail/bestellung-detail.ts bestellhistorie/bestellhistorie.html bezeichnungen.ts
```

```
Exit code 1
.:
bestellhistorie
bestellung-detail
bezeichnungen.ts
checkout
warenkorb.spec.ts
warenkorb.ts

./bestellhistorie:
bestellhistorie.spec.ts
bestellhistorie.ts

./bestellung-detail:
bestellung-detail.css
bestellung-detail.html
bestellung-detail.spec.ts
bestellung-detail.ts

./checkout:
checkout.css
checkout.html
checkout.spec.ts
checkout.ts
<p><a [routerLink]="['/kunden', kundenId()]">← Zum Kunden</a></p>

@switch (zustand()) {
  @case ('laedt') {
    <p class="hinweis" role="status">Bestellung wird geladen …</p>
  }
  @case ('fehler') {
    <div class="hinweis fehler" role="alert">
      <p class="meldung">{{ fehlermeldung() }}</p>
      <button type="button" (click)="laden()">Erneut versuchen</button>
    </div>
  }
  @default {
    @if (bestellung(); as bestellung) {
      @if (aufgegeben()) {
        <p class="hinweis erfolg" role="status">
… [gekürzt, 192 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app/bestellung && cat bestellhistorie/bestellhistorie.ts bestellung-detail/bestellung-detail.spec.ts bestellung-detail/bestellung-detail.css; sed -n 1,60p bestellhistorie/bestellhistorie.spec.ts
```

```
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BestellungUebersicht, BestellungenService } from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Bestellhistorie eines Kunden, Reihenfolge vom Backend (US-04.2). */
@Component({
  selector: 'app-bestellhistorie',
  imports: [RouterLink, DatePipe, GeldPipe],
  template: `
    <h2>Bestellungen</h2>
    @switch (zustand()) {
      @case ('laedt') {
        <p class="hinweis" role="status">Bestellungen werden geladen …</p>
      }
      @case ('fehler') {
        <div class="hinweis fehler" role="alert">
          <p class="meldung">{{ fehlermeldung() }}</p>
          <button type="button" (click)="laden()">Erneut versuchen</button>
        </div>
      }
      @default {
        @if (bestellungen().length === 0) {
          <p class="hinweis">Noch keine Bestellungen.</p>
        } @else {
          <table>
            <thead>
              <tr>
                <th scope="col">Bestellnummer</th>
                <th scope="col">Bestelldatum</th>
                <th scope="col" class="zahl">Positionen</th>
                <th scope="col" class="zahl">Gesamtwert</th>
              </tr>
            </thead>
            <tbody>
… [gekürzt, 345 Zeilen gesamt]
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='bestellung-detail/bestellung-detail.html'
s=open(p).read()
old='''      <h2>Zahlung</h2>
      <ul class="zahlungen">
        @for (zahlung of bestellung.zahlungen; track zahlung.id) {
          <li>
            {{ zahlungsartName(zahlung.zahlungsart) }}: {{ zahlung.betrag | geld }},
            {{ transaktionsstatusName(zahlung.transaktionsstatus) }}
          </li>
        }
      </ul>'''
new='''      <h2>Zahlung</h2>
      <dl class="eckdaten bezahlstatus">
        <dt>Bezahlstatus</dt>
        <dd class="status" [class.bezahlt]="bestellung.istVollstaendigBezahlt">
          {{ bestellung.istVollstaendigBezahlt ? 'vollständig bezahlt' : 'offen' }}
        </dd>
        <dt>Bezahlt</dt>
        <dd class="bezahlt-summe">{{ bestellung.bezahlteSumme | geld }}</dd>
        <dt>Offener Betrag</dt>
        <dd class="offen">{{ bestellung.offenerBetrag | geld }}</dd>
      </dl>
      <ul class="zahlungen">
        @for (zahlung of bestellung.zahlungen; track zahlung.id) {
          <li>
            @if (zahlung.buchungsart === 'ERSTATTUNG') {
              Erstattung
            }
            {{ zahlungsartName(zahlung.zahlungsart) }}: {{ zahlung.betrag | geld }},
… [gekürzt, 75 Zeilen gesamt]
```

```
2:  --text: #1d1f21;
7:  --gut: #1e7a3c;
10:  color: var(--text);
```

### ● Claude

Now frontend tests updaten/ergänzen:

**⏺ Bash**

```bash
python3 - <<'EOF'
p='bestellung-detail/bestellung-detail.spec.ts'
s=open(p).read()
s=s.replace('''      transaktionsstatus: 'AUSSTEHEND',
    },
  ],
};''','''      transaktionsstatus: 'AUSSTEHEND',
    },
  ],
  bezahlteSumme: euro(0),
  offenerBetrag: euro(4950),
  istVollstaendigBezahlt: false,
};''')
old='''  it('zeigt ohne Gutschein keine Rabattzeile','''
new='''  it('US-05.1 AK1: zeigt offenen Bezahlstatus und offenen Betrag vom Backend', async () => {
    await erzeugen();
    http.expectOne(URL).flush(BESTELLUNG);
    await fixture.whenStable();

    expect(element.querySelector('.bezahlstatus .status')!.textContent).toContain('offen');
    expect(element.querySelector('.bezahlstatus .status')!.classList).not.toContain('bezahlt');
    expect(element.querySelector('.bezahlt-summe')!.textContent).toContain('0,00 €');
    expect(element.querySelector('.offen')!.textContent).toContain('49,50 €');
  });

  it('US-05.1 AK5: zeigt Erstattung, bezahlte Summe und Restbetrag', async () => {
    await erzeugen();
    http.expectOne(URL).flush({
      ...BESTELLUNG,
      zahlungen: [
… [gekürzt, 102 Zeilen gesamt]
```

```
Exit code 1
Traceback (most recent call last):
  File "<stdin>", line 97, in <module>
AssertionError
(eval):102: no matches found: --include=*.spec.ts
```

**⏺ Bash**

```bash
git diff --stat . ; grep -n "49,50" bestellhistorie/bestellhistorie.spec.ts
```

```
 .../bestellung/bestellhistorie/bestellhistorie.ts  | 10 +++-
 .../bestellung-detail/bestellung-detail.css        |  9 ++++
 .../bestellung-detail/bestellung-detail.html       | 13 +++++
 .../bestellung-detail/bestellung-detail.spec.ts    | 57 ++++++++++++++++++++++
 .../bestellung-detail/bestellung-detail.ts         |  5 +-
 5 files changed, 92 insertions(+), 2 deletions(-)
58:    expect(zeilen[1].textContent).toContain('49,50 €');
```

**⏺ Bash**

```bash
sed -n 56,64p bestellhistorie/bestellhistorie.spec.ts | cat -A | cut -c1-80
```

```
    expect(zeilen[1].textContent).toContain('12.11.2026');$
    expect(zeilen[1].textContent).toContain('2');$
    expect(zeilen[1].textContent).toContain('49,50M-BM- M-bM-^BM-,');$
  });$
$
  it('US-04.2 AK7: verlinkt jede Bestellung auf ihre Ansicht', async () => {$
    http.expectOne(URL).flush([MIT_GUTSCHEIN]);$
    await fixture.whenStable();$
$
```

### ● Claude

Spec nutzt geschütztes Leerzeichen vor `€`. Anpassen:

**⏺ Bash**

```bash
python3 - <<'EOF'
import re
N=' '
p='bestellung-detail/bestellung-detail.spec.ts'
s=open(p).read()
i=s.index("  it('US-05.1 AK1")
j=s.index("  it('zeigt ohne Gutschein keine Rabattzeile'")
s=s[:i]+s[i:j].replace(' €',N+'€')+s[j:]
open(p,'w').write(s)

p='bestellhistorie/bestellhistorie.spec.ts'
s=open(p).read()
s=s.replace('''  gesamtwert: { cent: 1250, waehrung: 'EUR' },
};''','''  gesamtwert: { cent: 1250, waehrung: 'EUR' },
  offenerBetrag: { cent: 0, waehrung: 'EUR' },
  istVollstaendigBezahlt: true,
};''')
s=s.replace('''  gesamtwert: { cent: 4950, waehrung: 'EUR' },
};''','''  gesamtwert: { cent: 4950, waehrung: 'EUR' },
  offenerBetrag: { cent: 4950, waehrung: 'EUR' },
  istVollstaendigBezahlt: false,
};''')
old="    expect(zeilen[1].textContent).toContain('49,50"+N+"€');\n  });"
assert old in s
new=old+'''

  it('D-19: zeigt je Bestellung bezahlt oder den offenen Betrag', async () => {
    http.expectOne(URL).flush([NEUESTE, MIT_GUTSCHEIN]);
    await fixture.whenStable();

… [gekürzt, 38 Zeilen gesamt]
```

```
src/app/bestellung/bestellhistorie/bestellhistorie.spec.ts
src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts

[31m⎯⎯⎯⎯⎯⎯⎯[39m[1m[41m Failed Tests 1 [49m[22m[31m⎯⎯⎯⎯⎯⎯⎯[39m

[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts[2m > [22mBestellungDetail[2m > [22mUS-05.1 AK5: zeigt Erstattung, bezahlte Summe und Restbetrag
[31m[1mAssertionError[22m: expected ' Erstattung  Rechnung: 10,00 €, erfol…' to contain 'Erstattung Rechnung: 10,00 €, erfolgr…'[39m

Expected: "Erstattung Rechnung: 10,00 €, erfolgreich"
Received: " Erstattung  Rechnung: 10,00 €, erfolgreich "

[36m [2m❯[22m src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts:[2m142:38[22m[39m
    [90m140|[39m     [35mconst[39m zahlungen [33m=[39m element[33m.[39m[34mquerySelectorAll[39m([32m'.zahlungen li'[39m)[33m;[39m
    [90m141|[39m     expect(zahlungen[0].textContent).toContain('Rechnung: 49,50 €, erf…
    [90m142|[39m     expect(zahlungen[1].textContent).toContain('Erstattung Rechnung: 1…
    [90m   |[39m                                      [31m^[39m
    [90m143|[39m     expect(element.querySelector('.bezahlt-summe')!.textContent).toCon…
    [90m144|[39m     expect(element.querySelector('.offen')!.textContent).toContain('10…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[1/1]⎯[22m[39m


[2m Test Files [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m12 passed[39m[22m[90m (13)[39m
[2m      Tests [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m75 passed[39m[22m[90m (76)[39m
[2m   Start at [22m 11:30:58
[2m   Duration [22m 2.56s[2m (transform 823ms, setup 5.89s, import 2.31s, tests 2.01s, environment 13.43s)[22m
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='src/app/bestellung/bestellung-detail/bestellung-detail.html'
s=open(p).read()
old='''            @if (zahlung.buchungsart === 'ERSTATTUNG') {
              Erstattung
            }
            {{ zahlungsartName(zahlung.zahlungsart) }}'''
new='''            {{ zahlung.buchungsart === 'ERSTATTUNG' ? 'Erstattung' : '' }}
            {{ zahlungsartName(zahlung.zahlungsart) }}'''
s=s.replace(old,new)
open(p,'w').write(s)
p='src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts'
s=open(p).read()
s=s.replace("expect(zahlungen[1].textContent).toContain('Erstattung Rechnung:","expect(zahlungen[1].textContent!.replace(/\\s+/g, ' ')).toContain('Erstattung Rechnung:")
open(p,'w').write(s)
EOF
grep -n "Erstattung Rechnung" src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts; npm test 2>&1 | tail -5; npx prettier --check src/app/bestellung 2>&1 | tail -3
```

```
142:    expect(zahlungen[1].textContent!.replace(/\s+/g, ' ')).toContain('Erstattung Rechnung: 10,00 €, erfolgreich');
[2m Test Files [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m12 passed[39m[22m[90m (13)[39m
[2m      Tests [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m75 passed[39m[22m[90m (76)[39m
[2m   Start at [22m 11:31:09
[2m   Duration [22m 2.52s[2m (transform 1.18s, setup 6.55s, import 3.70s, tests 2.16s, environment 11.78s)[22m

Checking formatting...
[warn] src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts
[warn] Code style issues found in the above file. Run Prettier with --write to fix.
```

**⏺ Bash**

```bash
sed -i "142s#replace(/\\\\s+/g, ' ')#replace(/[ \\\\n]+/g, ' ')#" src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts && sed -n 142p src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts && npx prettier --write src/app/bestellung >/dev/null && npm test 2>&1 | tail -5
```

```
    expect(zahlungen[1].textContent!.replace(/[ \n]+/g, ' ')).toContain('Erstattung Rechnung: 10,00 €, erfolgreich');
[2m Test Files [22m [1m[32m13 passed[39m[22m[90m (13)[39m
[2m      Tests [22m [1m[32m76 passed[39m[22m[90m (76)[39m
[2m   Start at [22m 11:31:19
[2m   Duration [22m 2.52s[2m (transform 949ms, setup 6.24s, import 2.59s, tests 2.01s, environment 13.76s)[22m
```

### ● Claude

Frontend grün. Jetzt voller Backend-Build und Frontend-Build:

**⏺ Bash**

```bash
npm run build 2>&1 | tail -4; cd ../backend && ./gradlew build -q 2>&1 | grep -v "INFO\|WARN\|warning\|^\s*$" | tail -15; echo "exit ${pipestatus[1]}"
```

```
Application bundle generation complete. [2.559 seconds] - 2026-11-12T10:31:28.271Z

Output location: /home/mschachner/Projects/bestellsystem/frontend/dist/frontend

exit 0
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/build/test-results/test && python3 -c "
import glob,xml.etree.ElementTree as ET
t=f=s=0
for x in glob.glob('*.xml'):
  r=ET.parse(x).getroot(); t+=int(r.get('tests')); f+=int(r.get('failures'))+int(r.get('errors')); s+=int(r.get('skipped'))
print(t,f,s)"; ls -la --time-style=+%T TEST-*BestellsystemApplicationIT.xml | awk '{print $6}'
```

```
240 0 0
11:31:52
```

### ● Claude

240 Backend-Tests grün, 0 übersprungen. Kurzer End-to-End-Check gegen echte App:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && docker compose up -d 2>&1 | tail -2; cd backend && (./gradlew bootRun > /tmp/bootrun.log 2>&1 &) ; for i in $(seq 60); do curl -s localhost:8080/v3/api-docs >/dev/null && break; sleep 2; done; curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/v3/api-docs
```

```
 Container bestellsystem-mailpit-1 Running 
 Container bestellsystem-postgres-1 Running 
200
```

**⏺ Bash**

```bash
B=localhost:8080/api/v1; J='Content-Type: application/json'
P=$(curl -s -H "$J" -d '{"titel":"E2E Lampe","listenpreis":{"cent":10000,"waehrung":"EUR"},"lagerbestand":5}' $B/produkte | jq -r .id)
K=$(curl -s -H "$J" -d "{\"vorname\":\"Test\",\"nachname\":\"Zahlung\",\"email\":\"e2e-$RANDOM@example.de\",\"lieferadresse\":{\"strasse\":\"Hauptstr. 1\",\"stadt\":\"Berlin\",\"postleitzahl\":\"10115\",\"land\":\"DE\"},\"auchRechnungsadresse\":false}" $B/kunden)
echo "$K" | head -c 300; echo
KID=$(echo "$K" | jq -r .id); A=$(echo "$K" | jq -r '.adressen[0].id')
NR=$(curl -s -H "$J" -d "{\"positionen\":[{\"produktId\":\"$P\",\"menge\":1}],\"lieferadresseId\":\"$A\",\"zahlungsart\":\"RECHNUNG\"}" $B/kunden/$KID/bestellungen | jq -r .bestellnummer)
Z=$(curl -s $B/kunden/$KID/bestellungen/$NR | jq -r '.zahlungen[0].id')
curl -s -X PUT -H "$J" -d '{"transaktionsstatus":"ERFOLGREICH"}' $B/kunden/$KID/bestellungen/$NR/zahlungen/$Z/status | jq -c .
curl -s -i -H "$J" -d '{"zahlungsart":"RECHNUNG","betrag":{"cent":3000,"waehrung":"EUR"}}' $B/kunden/$KID/bestellungen/$NR/erstattungen | grep -i "^HTTP\|^location"
E=$(curl -s $B/kunden/$KID/bestellungen/$NR | jq -r '.zahlungen[] | select(.buchungsart=="ERSTATTUNG") | .id')
curl -s -X PUT -H "$J" -d '{"transaktionsstatus":"ERFOLGREICH"}' $B/kunden/$KID/bestellungen/$NR/zahlungen/$E/status >/dev/null
curl -s $B/kunden/$KID/bestellungen/$NR | jq -c '{gesamtwert:.gesamtwert.cent,bezahlteSumme:.bezahlteSumme.cent,offenerBetrag:.offenerBetrag.cent,istVollstaendigBezahlt}'
curl -s $B/kunden/$KID/bestellungen | jq -c '.[0] | {bestellnummer,offenerBetrag:.offenerBetrag.cent,istVollstaendigBezahlt}'
echo "KID=$KID NR=$NR"
```

```
{"id":"acc88d57-b104-4905-ae33-00dcc2b5178a","vorname":"Test","nachname":"Zahlung","email":"e2e-11547@example.de","adressen":[{"id":"0c0aa584-651e-44e9-8edc-40f15c9ac434","anschrift":{"strasse":"Hauptstr. 1","stadt":"Berlin","postleitzahl":"10115","land":"DE"},"istLieferadresse":true,"istRechnungsad
{"type":"about:blank","title":"Not Found","status":404,"detail":"No static resource api/v1/kunden/acc88d57-b104-4905-ae33-00dcc2b5178a/bestellungen/B-2026-000009/zahlungen/7dc3f2c5-d7c3-40d4-bf66-f15efec78ad7/status.","instance":"/api/v1/kunden/acc88d57-b104-4905-ae33-00dcc2b5178a/bestellungen/B-2026-000009/zahlungen/7dc3f2c5-d7c3-40d4-bf66-f15efec78ad7/status"}
HTTP/1.1 404 
{"gesamtwert":10000,"bezahlteSumme":null,"offenerBetrag":null,"istVollstaendigBezahlt":null}
{"bestellnummer":"B-2026-000009","offenerBetrag":null,"istVollstaendigBezahlt":null}
KID=acc88d57-b104-4905-ae33-00dcc2b5178a NR=B-2026-000009
```

### ● Claude

Läuft alter Backend-Prozess auf :8080. Prüfen:

**⏺ Bash**

```bash
grep -i "already in use\|Started\|APPLICATION FAILED" /tmp/bootrun.log | head; ss -ltnp | grep 8080
```

```
2026-11-12T11:32:02.551+01:00  INFO 1414263 --- [bestellsystem] [           main] d.b.BestellsystemApplication             : Starting BestellsystemApplication using Java 21.0.13.0.3 with PID 1414263 (/home/mschachner/Projects/bestellsystem/backend/build/classes/java/main started by mschachner in /home/mschachner/Projects/bestellsystem/backend)
APPLICATION FAILED TO START
Web server failed to start. Port 8080 was already in use.
LISTEN 0      100                     *:8080             *:*    users:(("java",pid=1222768,fd=90))
```

**⏺ Bash**

```bash
ps -o pid,lstart,args -p 1222768 | cut -c1-250
```

```
    PID                  STARTED COMMAND
1222768  Do Nov 12 10:58:45 2026 /usr/lib/jvm/java-21-openjdk-amd64/bin/java -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp /home/mschachner/Projects/bestellsystem/backend/build/classes/java/main
```

### ● Claude

Fremder Prozess (seit 10:58) belegt :8080, lasse ihn in Ruhe. Starte neues Backend auf :8081:

**⏺ Bash**

```bash
(SERVER_PORT=8081 ./gradlew bootRun > /tmp/bootrun.log 2>&1 &) ; for i in $(seq 60); do curl -s localhost:8081/v3/api-docs >/dev/null && break; sleep 2; done
B=localhost:8081/api/v1; J='Content-Type: application/json'
P=$(curl -s -H "$J" -d '{"titel":"E2E Lampe","listenpreis":{"cent":10000,"waehrung":"EUR"},"lagerbestand":5}' $B/produkte | jq -r .id)
K=$(curl -s -H "$J" -d "{\"vorname\":\"Test\",\"nachname\":\"Zahlung\",\"email\":\"e2e-$RANDOM@example.de\",\"lieferadresse\":{\"strasse\":\"Hauptstr. 1\",\"stadt\":\"Berlin\",\"postleitzahl\":\"10115\",\"land\":\"DE\"},\"auchRechnungsadresse\":false}" $B/kunden)
KID=$(echo "$K" | jq -r .id); A=$(echo "$K" | jq -r '.adressen[0].id')
NR=$(curl -s -H "$J" -d "{\"positionen\":[{\"produktId\":\"$P\",\"menge\":1}],\"lieferadresseId\":\"$A\",\"zahlungsart\":\"RECHNUNG\"}" $B/kunden/$KID/bestellungen | jq -r .bestellnummer)
Z=$(curl -s $B/kunden/$KID/bestellungen/$NR | jq -r '.zahlungen[0].id')
curl -s -X PUT -H "$J" -d '{"transaktionsstatus":"ERFOLGREICH"}' $B/kunden/$KID/bestellungen/$NR/zahlungen/$Z/status | jq -c .
curl -s -i -H "$J" -d '{"zahlungsart":"RECHNUNG","betrag":{"cent":3000,"waehrung":"EUR"}}' $B/kunden/$KID/bestellungen/$NR/erstattungen | grep -i "^HTTP\|^location"
E=$(curl -s $B/kunden/$KID/bestellungen/$NR | jq -r '.zahlungen[] | select(.buchungsart=="ERSTATTUNG") | .id')
curl -s -X PUT -H "$J" -d '{"transaktionsstatus":"ERFOLGREICH"}' $B/kunden/$KID/bestellungen/$NR/zahlungen/$E/status >/dev/null
curl -s -X PUT -H "$J" -d '{"transaktionsstatus":"STORNIERT"}' $B/kunden/$KID/bestellungen/$NR/zahlungen/$E/status | jq -c '{status,detail}'
curl -s $B/kunden/$KID/bestellungen/$NR | jq -c '{gesamtwert:.gesamtwert.cent,bezahlteSumme:.bezahlteSumme.cent,offenerBetrag:.offenerBetrag.cent,istVollstaendigBezahlt}'
curl -s $B/kunden/$KID/bestellungen | jq -c '.[0] | {bestellnummer,offenerBetrag:.offenerBetrag.cent,istVollstaendigBezahlt}'
echo "KID=$KID NR=$NR"
```

```
{"id":"d3f74956-ea14-42f3-9bba-eb44702838ee","zahlungsart":"RECHNUNG","buchungsart":"ZAHLUNG","transaktionsstatus":"ERFOLGREICH","betrag":{"cent":10000,"waehrung":"EUR"},"zahlungszeitpunkt":"2026-11-12T10:32:31.483269254Z"}
HTTP/1.1 201 
Location: http://localhost:8081/api/v1/kunden/b5b076fb-aa04-44ec-a12d-060a607dbf74/bestellungen/B-2026-000010/zahlungen/9350638a-2f3b-40a4-86b9-66c80975f254
{"status":422,"detail":"Der Status einer Buchung kann nicht von ERFOLGREICH nach STORNIERT wechseln."}
{"gesamtwert":10000,"bezahlteSumme":7000,"offenerBetrag":3000,"istVollstaendigBezahlt":false}
{"bestellnummer":"B-2026-000010","offenerBetrag":3000,"istVollstaendigBezahlt":false}
KID=b5b076fb-aa04-44ec-a12d-060a607dbf74 NR=B-2026-000010
```

### ● Claude

API End-to-End korrekt (AK5-Szenario: bezahlt 70 €, offen 30 €; 422 bei Endzustand). Eigenes Backend stoppen:

**⏺ Bash**

```bash
pkill -f "SERVER_PORT=8081" ; P=$(ss -ltnp | grep ':8081' | grep -o 'pid=[0-9]*' | cut -d= -f2); [ -n "$P" ] && kill $P; sleep 2; ss -ltn | grep -c ':8081'
```

```
Exit code 144
```

**⏺ Bash**

```bash
ss -ltn | grep ':808'
```

```
LISTEN 0      100                     *:8080             *:*          
LISTEN 0      100                     *:8081             *:*
```

**⏺ Bash**

```bash
P=$(ss -ltnp | grep ':8081 ' | grep -o 'pid=[0-9]*' | cut -d= -f2); echo $P; ps -o lstart= -p $P; kill $P; sleep 3; ss -ltn | grep ':808'
```

```
1417525
 Do Nov 12 11:32:24 2026
LISTEN 0      100                     *:8080             *:*
```

### ● Claude

Doku nachziehen. Epic-Status-Konvention prüfen:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && grep -h "^| \[US" epics/*.md
```

```
| [US-01.1](../stories/US-01.1-registrierung-mit-lieferadresse.md) | Registrierung mit Lieferadresse | ausformuliert |
| [US-01.2](../stories/US-01.2-weitere-adresse-hinzufuegen.md) | Weitere Liefer- oder Rechnungsadresse hinzufügen | ausformuliert |
| [US-01.3](../stories/US-01.3-adresse-aendern-loeschen.md) | Adresse ändern oder löschen (letzte Lieferadresse geschützt) | ausformuliert |
| [US-03.1](../stories/US-03.1-produkt-merken.md) | Produkt merken und beobachtete Artikel abrufen | ausformuliert |
| [US-02.1](../stories/US-02.1-verfuegbarkeit-pruefen.md) | Verfügbarkeit eines Produkts prüfen | ausformuliert |
| [US-08.1](../stories/US-08.1-gutschein-einloesen.md) | Gutschein bei der Bestellung einlösen | ausformuliert |
| [US-04.1](../stories/US-04.1-bestellung-aufgeben.md) | Bestellung aufgeben | ausformuliert |
| [US-04.2](../stories/US-04.2-bestellhistorie-einsehen.md) | Bestellhistorie einsehen | ausformuliert |
| [US-05.1](../stories/US-05.1-zahlungsstatus-pruefen.md) | Prüfen, ob eine Bestellung vollständig bezahlt ist | ausformuliert |
| [US-06.1](../stories/US-06.1-automatische-mahnpruefung.md) | Automatische Mahnprüfung für ein Kundenkonto | ausformuliert |
```

**⏺ Bash**

````bash
python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
def r(a,b):
    global s
    assert a in s, a[:60]
    s=s.replace(a,b,1)
r('Stand: Schritt 4b (Checkout im Frontend, Bestellhistorie US-04.2).','Stand: Schritt 5 (Zahlungen E05: Bezahlstatus US-05.1, Statuswechsel und Erstattung D-19).')
r('''| `zahlung.domain` | `Zahlung` (Entity, UUID, Teil der Bestellung; Fabrik `ausstehend`), Enums `Zahlungsart`, `Buchungsart`, `Transaktionsstatus` |
| `zahlung.api` | `ZahlungDto` |''','''| `zahlung.domain` | `Zahlung` (Entity, UUID, Teil der Bestellung; Fabriken `ausstehend` für erste Zahlung und für Teilzahlung/Erstattung mit Betrag > 0): `aendereStatus` (nur aus `AUSSTEHEND`, 422; Zeitpunkt nur bei `ERFOLGREICH`, D-19), `istErfolgreich`, `istAusstehend`, `istErstattung`; Enums `Zahlungsart`, `Buchungsart`, `Transaktionsstatus` |
| `zahlung.api` | `ZahlungController` (Statuswechsel, Erstattung, Buchung abrufen; nutzt `BestellungService`, da `Zahlung` Teil des Bestellungs-Aggregats ist), DTOs `ZahlungDto`, `StatusAenderungDto`, `ErstattungBuchenDto` |''')
r('''Fabrik `gibAuf`, `berechneWarenwert`, `berechneGesamtwert`.''','''Fabrik `gibAuf`, `berechneWarenwert`, `berechneGesamtwert`; Bezahlstatus (US-05.1, D-05) `berechneBezahlteSumme`, `berechneOffenenBetrag`, `istVollstaendigBezahlt`, `berechneErstattbarenBetrag` (D-19); Buchungen `bucheZahlung`, `bucheErstattung` (422 über erstattbar), `aendereZahlungsstatus`, `zahlung(id)` (404 bei fremder Buchung).''')
r('''`findeHistorie` (`JOIN FETCH` Positionen, Datum und Nummer absteigend, D-18) |''','''`findeHistorie` (`JOIN FETCH` Positionen, Datum und Nummer absteigend, D-18), `findeZahlungenDesKunden` (Zahlungen der Historie), `findeMitZahlungen` (Kunde und Nummer, für Buchungen) |''')
r('''`historie` (zwei Abfragen, US-04.2); `Positionswunsch` |''','''`historie` (drei Abfragen, US-04.2, D-19), `zahlung`, `aendereZahlungsstatus` (Zeitpunkt aus `Clock`), `bucheErstattung`; Buchungen sperren die Bestellung mit `OPTIMISTIC_FORCE_INCREMENT` (D-19); `Positionswunsch` |''')
r('''| GET | `/api/v1/kunden/{kundenId}/bestellungen` | Bestellhistorie (US-04.2): Nummer, Datum, Anzahl Positionen, Gesamtwert; neueste zuerst; 404 unbekannter Kunde |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}` | Bestellung mit Positionen, Rabatt, Gesamtwert, Zahlungen; 404 auch bei fremder Bestellung |''','''| GET | `/api/v1/kunden/{kundenId}/bestellungen` | Bestellhistorie (US-04.2): Nummer, Datum, Anzahl Positionen, Gesamtwert, offener Betrag, `istVollstaendigBezahlt`; neueste zuerst; 404 unbekannter Kunde |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}` | Bestellung mit Positionen, Rabatt, Gesamtwert, Zahlungen, `bezahlteSumme`, `offenerBetrag`, `istVollstaendigBezahlt` (US-05.1); 404 auch bei fremder Bestellung |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/zahlungen/{zahlungId}` | eine Buchung; 404 bei fremder Bestellung oder Buchung |
| PUT | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/zahlungen/{zahlungId}/status` | `{transaktionsstatus}`: 200, 400, 404, 409 Versionskonflikt, 422 (nicht mehr `AUSSTEHEND` oder Ziel `AUSSTEHEND`, D-19) |
| POST | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/erstattungen` | `{zahlungsart, betrag}`: 201 + `Location` (Buchung), 400 (Betrag 0, Pflichtfelder), 404, 409, 422 über erstattbar (D-19) |''')
r('''Jede Ablehnung wirft vor dem Commit; die Transaktion rollt zurück, kein Bestand ändert sich.
''','''Jede Ablehnung wirft vor dem Commit; die Transaktion rollt zurück, kein Bestand ändert sich.

## Datenfluss (Zahlungsstatus und Erstattung, D-19)

```
PUT …/zahlungen/{id}/status ─ ZahlungController (@Valid StatusAenderungDto)
  → BestellungService.aendereZahlungsstatus (@Transactional)
      findeMitZahlungen (404) → lock OPTIMISTIC_FORCE_INCREMENT
      Bestellung.aendereZahlungsstatus → Zahlung.aendereStatus(status, Instant aus Clock)   422
… [gekürzt, 58 Zeilen gesamt]
````

```
 docs/01-entscheidungen.md | 18 ++++++++++++++++
 docs/ARCHITEKTUR.md       | 53 +++++++++++++++++++++++++++++++++++------------
 2 files changed, 58 insertions(+), 13 deletions(-)
```

### ● Claude

Commit:

**⏺ Bash**

```bash
git add docs/01-entscheidungen.md docs/ARCHITEKTUR.md backend/src frontend/openapi.json frontend/src/app/api frontend/src/app/bestellung && git status --short && git commit -q -F - <<'EOF'
Add payment status, refunds and paid amount display (E05, US-05.1)

Bestellung computes bezahlteSumme, offenerBetrag and
istVollstaendigBezahlt from its bookings (D-05). New endpoints change
a payment's status (only from AUSSTEHEND, D-19) and book refunds up to
the refundable amount. Order view and history show the payment status.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1
```

```
M  backend/src/main/java/de/bestellsystem/bestellung/api/BestellungDto.java
M  backend/src/main/java/de/bestellsystem/bestellung/api/BestellungUebersichtDto.java
M  backend/src/main/java/de/bestellsystem/bestellung/application/BestellungService.java
M  backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java
M  backend/src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java
A  backend/src/main/java/de/bestellsystem/zahlung/api/ErstattungBuchenDto.java
A  backend/src/main/java/de/bestellsystem/zahlung/api/StatusAenderungDto.java
A  backend/src/main/java/de/bestellsystem/zahlung/api/ZahlungController.java
M  backend/src/main/java/de/bestellsystem/zahlung/domain/Zahlung.java
M  backend/src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java
A  backend/src/test/java/de/bestellsystem/bestellung/domain/BezahlstatusTest.java
A  backend/src/test/java/de/bestellsystem/zahlung/api/ZahlungApiIT.java
A  backend/src/test/java/de/bestellsystem/zahlung/application/ErstattungNebenlaeufigkeitIT.java
M  docs/01-entscheidungen.md
M  docs/ARCHITEKTUR.md
M  frontend/openapi.json
M  frontend/src/app/api/.openapi-generator/FILES
M  frontend/src/app/api/api/api.ts
A  frontend/src/app/api/api/zahlungen.service.ts
M  frontend/src/app/api/model/bestellung-uebersicht.ts
M  frontend/src/app/api/model/bestellung.ts
A  frontend/src/app/api/model/erstattung-buchen.ts
M  frontend/src/app/api/model/models.ts
A  frontend/src/app/api/model/status-aenderung.ts
M  frontend/src/app/bestellung/bestellhistorie/bestellhistorie.spec.ts
M  frontend/src/app/bestellung/bestellhistorie/bestellhistorie.ts
M  frontend/src/app/bestellung/bestellung-detail/bestellung-detail.css
M  frontend/src/app/bestellung/bestellung-detail/bestellung-detail.html
M  frontend/src/app/bestellung/bestellung-detail/bestellung-detail.spec.ts
M  frontend/src/app/bestellung/bestellung-detail/bestellung-detail.ts
826fd9a Add payment status, refunds and paid amount display (E05, US-05.1)
```

### ● Claude

Fahrplan abhaken:

**⏺ Bash**

```bash
sed -i 's/^## \[ \] 5 Zahlungen (E05)$/## [x] 5 Zahlungen (E05) (2026-11-12, 826fd9a)/' docs/fahrplan.md && grep -n "^## \[x\] 5" docs/fahrplan.md && git add docs/fahrplan.md && git commit -q -m "Hake Schritt 5 im Fahrplan ab

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -2 && git status --short
```

```
121:## [x] 5 Zahlungen (E05) (2026-11-12, 826fd9a)
ad2514c Hake Schritt 5 im Fahrplan ab
826fd9a Add payment status, refunds and paid amount display (E05, US-05.1)
[This command modified 1 file you've previously read: docs/fahrplan.md. Call Read before editing.]
```

### ● Claude

Schritt 5 (Zahlungen, E05) ist fertig, alle Tests grün und abgehakt. Die Bestellansicht zeigt jetzt Bezahlstatus, bezahlte Summe und offenen Betrag.

**Vorab entschieden (D-19, mit dir abgestimmt):**
- Ein Zahlungsstatus lässt sich nur aus AUSSTEHEND ändern, nach ERFOLGREICH, FEHLGESCHLAGEN oder STORNIERT. Diese Endzustände sind endgültig; jeder andere Wechsel ergibt 422.
- Der Zahlungszeitpunkt wird nur bei ERFOLGREICH gesetzt.
- Eine Erstattung entsteht als AUSSTEHEND und wird über denselben Statuswechsel abgeschlossen.
- Erstattet werden kann höchstens die bezahlte Summe abzüglich noch ausstehender Erstattungen, sonst 422.

**Gebaut:**
- **Fachlogik:** Die Bestellung berechnet bezahlte Summe, offenen Betrag und „vollständig bezahlt“ bei jedem Abruf aus ihren Buchungen; gespeichert wird davon nichts.
- **Neue Endpunkte:**
  - `PUT …/zahlungen/{id}/status` ändert den Status einer Buchung.
  - `POST …/erstattungen` bucht eine Erstattung und liefert 201 mit `Location`.
  - `GET …/zahlungen/{id}` liefert eine einzelne Buchung.
- **Gleichzeitige Buchungen:** Statuswechsel und Erstattung erhöhen die Version der Bestellung. Bei zwei gleichzeitigen Buchungen bekommt die zweite 409.
- **Bestellung und Historie** liefern zusätzlich `bezahlteSumme`, `offenerBetrag` und `istVollstaendigBezahlt`. Die Historie braucht dafür jetzt 3 statt 2 Datenbankabfragen, unabhängig von der Anzahl der Bestellungen.
- **Frontend:** Die Bestellansicht zeigt Bezahlstatus, bezahlte Summe, offenen Betrag und kennzeichnet Erstattungen. Die Historie zeigt je Bestellung „bezahlt“ oder „offen: Betrag“.
- OpenAPI-Datei und generierter Client sind aktualisiert; `docs/ARCHITEKTUR.md` ist nachgezogen.

**Tests:**
- `./gradlew build`: 240 Tests, alle grün, keiner übersprungen.
  - Alle 7 Akzeptanzkriterien von US-05.1 als Unit-Tests ohne Spring.
  - Die meisten davon auch über HTTP gegen PostgreSQL.
  - Ein Test mit zwei gleichzeitigen Erstattungen über je 60 € auf 100 €: genau eine gelingt.
- `npm test`: 76 grün; `npm run build` läuft durch.
- Gegen das laufende Backend durchgespielt: Zahlung erfolgreich, Erstattung über 30 €, Ergebnis bezahlt 70 €, offen 30 €. Der Versuch, die abgeschlossene Erstattung danach zu stornieren, ergibt 422.

**Commits:** `826fd9a` (Arbeit), `ad2514c` (Fahrplan abgehakt)

**Offen bzw. bewusst nicht gebaut:**
- Die Anzeige im Browser habe ich nicht angeschaut. Sie ist nur über die Komponententests abgedeckt, die genau diese Elemente prüfen.
- Für Teilzahlungen gibt es die Fachlogik, aber noch keinen Endpunkt; der kommt mit US-05.2. Im Frontend gibt es noch keine Knöpfe für Statuswechsel oder Erstattung.
- Auf Port 8080 läuft noch ein Backend, das um 10:58 gestartet wurde. Es enthält diese Änderungen nicht. Ich habe es nicht angefasst und für den Test eine eigene Instanz auf 8081 gestartet und wieder beendet. Wenn du die neue Anzeige im Browser sehen willst, musst du es neu starten.

**Nächster Schritt:** 6, Mahnwesen (E06), mit feature-dev. Vorher `/clear`, dann `/naechster-schritt`.

