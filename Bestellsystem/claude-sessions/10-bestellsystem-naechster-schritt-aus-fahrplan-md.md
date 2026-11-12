## Session 10: Nächster Schritt aus fahrplan.md

*Projekt: bestellsystem · ID `9f2765e8-dd62-4437-8e7a-16898b327170` · 2026-11-12 10:36 – 2026-11-12 10:50 UTC*

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
ls epics stories docs; cat epics/E06-*.md stories/US-06*.md; grep -n -A12 "D-03" docs/01-entscheidungen.md | head -40
```

```
docs:
00-anforderung.md
01-entscheidungen.md
02-domaenenmodell.md
adr
ARCHITEKTUR.md
fahrplan.md

epics:
E00-domaenenkern-plattform.md
E01-kundenkonto-adressbuch.md
E02-produktkatalog-lagerbestand.md
E03-merkliste.md
E04-bestellung-checkout.md
E05-zahlungen-erstattungen.md
E06-mahnwesen.md
E07-versand-sendungsverfolgung.md
E08-gutscheine-aktionen.md

stories:
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
# E06 Mahnwesen

**Priorität:** MVP
**Akteur:** System (zeitgesteuerter Mahnlauf), Kunde (Empfänger)
**Abhängig von:** E04, E05, E00 (Benachrichtigungsversand)

## Ziel

Für ein Kundenkonto wird automatisch geprüft, ob Bestellungen überfällig und nicht vollständig bezahlt sind. Der Kunde erhält dann eine Mahnung mit dem jeweils offenen Betrag.

… [gekürzt, 131 Zeilen gesamt]
```

**⏺ Skill**

```
{
 "skill": "feature-dev:feature-dev",
 "args": "Setze Schritt 6 aus docs/fahrplan.md um: Mahnwesen (E06).\nKunde.fuehreMahnpruefungDurch, Mahnung-Entity, täglicher @Scheduled-Lauf (US-06.2,\nKriterien vorher mit mir klären), E-Mail über den Benachrichtigungs-Port."
}
```

```
Launching skill: feature-dev:feature-dev
```

**⏺ Bash**

```bash
cat docs/ARCHITEKTUR.md; find backend/src -name "*.java" | sort; ls backend/src/main/resources/db/migration; cat backend/src/main/resources/application*.yml backend/src/main/resources/application*.properties 2>/dev/null
```

````
Exit code 1
# Architektur

Stand: Schritt 5 (Zahlungen E05: Bezahlstatus US-05.1, Statuswechsel und Erstattung D-19). Wird mit jedem Schritt im selben Commit nachgezogen.

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
… [gekürzt, 119 Zeilen gesamt]
````

**⏺ Bash**

```bash
sed -n 40,200p docs/ARCHITEKTUR.md
```

```
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`) |
| `kunde.application` | `KundeService`: `registriere` (409 bei vergebener E-Mail, auch über Unique-Constraint), `alle`, `finde`, `pruefeVorhanden` (404 ohne Laden), Adressbuch-Operationen mit `OPTIMISTIC_FORCE_INCREMENT` auf den Kunden (D-16) |
| `kunde.api` | `KundeController`, `AdressbuchController`, DTOs `RegistrierungDto`, `AdresseEingabeDto`, `AdresseDto`, `KundeDto`, `KundeUebersichtDto` |
| `gutschein.domain` | `Gutschein` (Entity, UUID; Fabriken `prozent`, `festbetrag`): `normalisiere(code)` (getrimmt, groß), `istGueltigAm(datum)` (inklusive), `berechneRabatt(warenwert)` (HALF_UP, höchstens Warenwert); `RabattArt` |
| `gutschein.persistence` | `GutscheinRepository.findByCode` |
| `gutschein.application` | `GutscheinService.zumEinloesen(code)` (422 bei unbekanntem Code) |
| `zahlung.domain` | `Zahlung` (Entity, UUID, Teil der Bestellung; Fabriken `ausstehend` für erste Zahlung und für Teilzahlung/Erstattung mit Betrag > 0): `aendereStatus` (nur aus `AUSSTEHEND`, 422; Zeitpunkt nur bei `ERFOLGREICH`, D-19), `istErfolgreich`, `istAusstehend`, `istErstattung`; Enums `Zahlungsart`, `Buchungsart`, `Transaktionsstatus` |
| `zahlung.api` | `ZahlungController` (Statuswechsel, Erstattung, Buchung abrufen; nutzt `BestellungService`, da `Zahlung` Teil des Bestellungs-Aggregats ist), DTOs `ZahlungDto`, `StatusAenderungDto`, `ErstattungBuchenDto` |
| `bestellung.domain` | `Bestellung` (Aggregatwurzel, `bestellnummer` als ID, `@Version`, `kundeId`, `@Embedded lieferanschrift`, Gutschein 0..1 LAZY, eingefrorener `rabattBetrag`, Positionen und Zahlungen `cascade ALL`): Fabrik `gibAuf`, `berechneWarenwert`, `berechneGesamtwert`; Bezahlstatus (US-05.1, D-05) `berechneBezahlteSumme`, `berechneOffenenBetrag`, `istVollstaendigBezahlt`, `berechneErstattbarenBetrag` (D-19); Buchungen `bucheZahlung`, `bucheErstattung` (422 über erstattbar), `aendereZahlungsstatus`, `zahlung(id)` (404 bei fremder Buchung). `Bestellposition` (eingefrorener `einzelpreis`, Produkt LAZY, `berechneZwischenbetrag`). `Bestellwunsch` (Produkt + Menge vor dem Zusammenfassen) |
| `bestellung.persistence` | `BestellungRepository`: `findeMitPositionen` (`JOIN FETCH` Positionen, Produkt, Gutschein), `findeZahlungen` (`JOIN FETCH` Zahlungen), `findeHistorie` (`JOIN FETCH` Positionen, Datum und Nummer absteigend, D-18), `findeZahlungenDesKunden` (Zahlungen der Historie), `findeMitZahlungen` (Kunde und Nummer, für Buchungen) |
| `bestellung.application` | `BestellungService`: `gibAuf` (eine Transaktion), `finde` (zwei Abfragen), `historie` (drei Abfragen, US-04.2, D-19), `zahlung`, `aendereZahlungsstatus` (Zeitpunkt aus `Clock`), `bucheErstattung`; Buchungen sperren die Bestellung mit `OPTIMISTIC_FORCE_INCREMENT` (D-19); `Positionswunsch` |
| `bestellung.api` | `BestellungController`, DTOs `BestellungAufgebenDto`, `PositionswunschDto`, `BestellungDto`, `BestellpositionDto`, `BestellungUebersichtDto` |

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
| POST | `/api/v1/kunden/{kundenId}/adressen` | 
… [gekürzt, 161 Zeilen gesamt]
```

### ● Claude

Lese Schritt-6-Eingaben und bestehenden Code (Kunde, Bestellung, Benachrichtigung). Danach Fragen zu US-06.2.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && cat kunde/domain/Kunde.java gemeinsam/application/Benachrichtigung*.java gemeinsam/application/FachlicheEinstellungen.java bestellung/domain/Bestellung.java | head -500; grep -n -i "mahn\|Rueckstand\|faellig" -r . ../../../../../../docs/02-domaenenmodell.md | head -40
```

```
package de.bestellsystem.kunde.domain;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Kundenkonto mit Adressbuch; Aggregatwurzel, die I-1 und I-2 durchsetzt (D-06, D-16). */
@Entity
@Table(name = "kunde")
public class Kunde {

    public static final int NAME_MAXIMALE_LAENGE = 100;

    @Id private UUID id;

    // Wird bei jeder Adressbuch-Änderung erhöht, damit gleichzeitige Änderungen I-1 nicht
    // gemeinsam verletzen (D-16).
    @Version private Long version;

    private String vorname;

    private String nachname;

    @Embedded
… [gekürzt, 517 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && sed -n 1,30p docs/02-domaenenmodell.md; sed -n 90,175p docs/02-domaenenmodell.md; grep -n "^## D-" docs/01-entscheidungen.md; grep -n -i "mahn" docs/00-anforderung.md docs/01-entscheidungen.md | head; cat backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversand.java; ls backend/src/main/resources/db/migration; cat backend/src/test/java/de/bestellsystem/Datenbestand.java backend/src/test/java/de/bestellsystem/FesteUhrKonfiguration.java
```

````
# Domänenmodell

Enthält alle Entscheidungen aus `01-entscheidungen.md`. Abgeleitete Attribute sind mit `/` markiert.

## Klassendiagramm

```mermaid
classDiagram
  direction LR

  class Kunde {
    kundenId: UUID
    vorname: String
    nachname: String
    email: Email «eindeutig»
    getBeobachteteArtikel() List~Produkt~
    fuehreMahnpruefungDurch(stichtag: Datum) Mahnbenachrichtigung?
  }

  class Adresse {
    adressId: UUID
    strasse: String
    stadt: String
    postleitzahl: String
    land: Laendercode
    istLieferadresse: Boolean
    istRechnungsadresse: Boolean
  }

  class Produkt {
    rabattArt: RabattArt
    rabattWert: Dezimal
    ablaufdatum: Datum
    istGueltigAm(datum: Datum) Boolean
    berechneRabatt(warenwert: Geld) Geld
  }

  class Mahnung {
    mahnungId: UUID
    erstelltAm: Datum
… [gekürzt, 213 Zeilen gesamt]
````

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Lücke in D-03, wird D-20: Bei Annas Mahnprüfung ist B-1 überfällig und wurde vor 3 Tagen gemahnt (noch im Intervall); B-5 ist neu überfällig. Was steht in der E-Mail?",
   "header": "Teilmahnung",
   "multiSelect": false,
   "options": [
    {
     "label": "Nur fällige (Recommended)",
     "description": "Mail listet nur die Bestellungen, die jetzt gemahnt werden (B-5). Nur diese bekommen eine Mahnung. Die Intervalle der Bestellungen bleiben unabhängig voneinander."
    },
    {
     "label": "Alle überfälligen",
     "description": "Ist mindestens eine Bestellung fällig, listet die Mail jede überfällige Bestellung (B-1 und B-5). Mahnung nur für B-5, B-1 bleibt im Intervall."
    },
    {
     "label": "Alle + Intervall neu",
     "description": "Mail listet B-1 und B-5, beide bekommen eine Mahnung. Damit startet das Intervall von B-1 neu."
    }
   ]
  },
  {
   "question": "US-06.2: Wie wird der Mahnlauf außer per Zeitplan ausgelöst (auch damit die Mail in Mailpit nachweisbar ist)?",
   "header": "Auslöser",
   "multiSelect": false,
   "options": [
    {
     "label": "Beide Endpunkte (Recommended)",
     "description": "POST /api/v1/kunden/{id}/mahnpruefung (US-06.1, ein Kunde) und POST /api/v1/mahnlaeufe (alle Kunden). Stichtag ist immer heute laut Clock. Ohne Login sind beide offen, wie der Rest im MVP."
    },
… [gekürzt, 72 Zeilen gesamt]
```

```
Your questions have been answered: "Lücke in D-03, wird D-20: Bei Annas Mahnprüfung ist B-1 überfällig und wurde vor 3 Tagen gemahnt (noch im Intervall); B-5 ist neu überfällig. Was steht in der E-Mail?"="Nur fällige (Recommended)", "US-06.2: Wie wird der Mahnlauf außer per Zeitplan ausgelöst (auch damit die Mail in Mailpit nachweisbar ist)?"="Beide Endpunkte (Recommended)", "US-06.2: Schlägt der Versand bei einem Kunden fehl, was passiert mit den übrigen Kunden im selben Lauf?"="Weiter, je Kunde (Recommended)", "Mahnungen im Frontend anzeigen?"="Kein Frontend (Recommended)". You can now continue with these answers in mind.
```

### ● Claude

Antworten notiert. Das Design ist überschaubar und folgt den bestehenden Mustern (wie `Zahlung` an `Bestellung`). Daher ein Entwurf statt drei Architektur-Agenten:

**Domain**
- `mahnwesen.domain.Mahnung` (Entity, UUID, `erstelltAm`, eingefrorener `gemahnterBetrag`), Komposition an `Bestellung` (`cascade ALL`).
- `Bestellung`:
  - `istUeberfaellig(stichtag)`: offen > 0 und `stichtag > faelligAm` (AK6).
  - `istMahnungFaellig(stichtag, intervall)`: überfällig und entweder noch keine Mahnung oder `letzte + intervall ≤ stichtag` (AK4).
  - `mahne(stichtag)` legt eine `Mahnung` an.
- `Kunde.fuehreMahnpruefungDurch(stichtag, bestellungen, intervall)` → `Optional<Mahnbenachrichtigung>`. Mahnt nur fällige Bestellungen (D-20) und lehnt fremde `kundeId` ab.
- `Mahnbenachrichtigung` (E-Mail, Liste aus Nummer, fällig am, offen) wird zu `Benachrichtigung`:
  - Betreff: „Zahlungserinnerung“.
  - Text: eine Zeile je Bestellung mit Betrag in `de-DE` und der Summe.

**Application / Persistenz**
- `MahnService.pruefe(kundeId, stichtag)` (`@Transactional`):
  - Lädt den Kunden und seine Bestellungen mit `faelligAm < stichtag`: Positionen, Zahlungen und Mahnungen, drei Abfragen ohne N+1.
  - Mahnt, sendet dann über den Port.
  - Wirft der Port: Rollback, nichts protokolliert (AK7), Antwort 503 als ProblemDetail.
- Bestellungen bekommen beim Mahnen `OPTIMISTIC_FORCE_INCREMENT` (wie D-19).
- `Mahnlauf.fuehreDurch(stichtag)`:
  - Kandidaten sind `DISTINCT kunde_id` mit überfälliger Bestellung.
  - Je Kunde eigene Transaktion; Fehler werden geloggt und gezählt, der Lauf geht weiter.
  - Ergebnis: Anzahl gemahnter und fehlgeschlagener Kunden.
- Zeitplan: `@Scheduled(cron = bestellsystem.mahnlauf.cron)`, Standard `0 0 6 * * *`, Zone `bestellsystem.zeitzone`. In Tests mit `-` deaktiviert.
- Idempotent: Ein Intervall ≥ 1 Tag verhindert eine zweite Mahnung am selben Tag.
- `V5__mahnung.sql`.

**API** (Stichtag = heute laut `Clock`)
- `POST /api/v1/kunden/{id}/mahnpruefung`: 200 `{gemahnt, mahnungen[{bestellnummer, faelligAm, offenerBetrag}]}`, 404, 503.
- `POST /api/v1/mahnlaeufe`: 200 `{stichtag, gemahnteKunden, fehlgeschlageneKunden}`.

**Tests**
- Unit: AK1–6 an `Kunde`/`Bestellung` mit den Beispieldaten der Story.
- IT mit Testcontainers und einer zählenden Port-Attrappe, die auch fehlschlagen kann: AK1–7, Lauf mit einem fehlschlagenden Kunden, Wiederholung am selben Tag.
- MockMvc für Statuscodes.
- Danach Mailpit-Nachweis per `bootRun` und `curl`.

**Doku:** D-20 (Teilmahnung, Lauf, 503) in `01-entscheidungen.md`, US-06.2 in `stories/`, `ARCHITEKTUR.md`, OpenAPI-Export plus `generate:api`.

Freigabe zum Implementieren?

### ❯ Nutzer

> Ja

### ● Claude

Lese restliche Muster-Dateien.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && cat src/main/java/de/bestellsystem/zahlung/domain/Zahlung.java src/main/resources/db/migration/V4__gutschein_bestellung_zahlung.sql src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java src/main/java/de/bestellsystem/bestellung/application/BestellungService.java; ls src/main/resources src/test/resources 2>/dev/null; cat src/main/resources/application.yml src/test/resources/*.yml 2>/dev/null
```

```
Exit code 1
package de.bestellsystem.zahlung.domain;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Zahlungsbuchung einer Bestellung (D-02, D-05); Teil der Bestellung. */
@Entity
@Table(name = "zahlung")
public class Zahlung {

    @Id private UUID id;

    @Enumerated(EnumType.STRING)
    private Zahlungsart zahlungsart;

    @Enumerated(EnumType.STRING)
    private Buchungsart buchungsart;

    @Enumerated(EnumType.STRING)
    private Transaktionsstatus transaktionsstatus;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "betrag_cent"))
    private Geld betrag;

    // Bleibt leer, bis Geld tatsächlich fließt (D-17).
    private Instant zahlungszeitpunkt;
… [gekürzt, 269 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && sed -n 1,200p src/main/resources/db/migration/V4__gutschein_bestellung_zahlung.sql | sed -n 15,200p; cat src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java src/main/resources/application.yml; ls src/test/resources 2>/dev/null
```

```
Exit code 2
CREATE TABLE bestellung (
    bestellnummer       VARCHAR(30)  PRIMARY KEY,
    version             BIGINT       NOT NULL,
    kunde_id            UUID         NOT NULL REFERENCES kunde (id),
    bestelldatum        DATE         NOT NULL,
    faellig_am          DATE         NOT NULL,
    -- Snapshot der Lieferadresse, keine Referenz auf adresse (D-08)
    liefer_strasse      VARCHAR(200) NOT NULL,
    liefer_stadt        VARCHAR(100) NOT NULL,
    liefer_postleitzahl VARCHAR(20)  NOT NULL,
    liefer_land         VARCHAR(2)   NOT NULL,
    gutschein_id        UUID         REFERENCES gutschein (id),
    rabatt_betrag_cent  BIGINT       NOT NULL CHECK (rabatt_betrag_cent >= 0)
);

CREATE INDEX bestellung_kunde_idx ON bestellung (kunde_id);
CREATE INDEX bestellung_gutschein_idx ON bestellung (gutschein_id);

CREATE TABLE bestellposition (
    id               UUID        PRIMARY KEY,
    bestellnummer    VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    zeilennummer     INTEGER     NOT NULL CHECK (zeilennummer >= 1),
    produkt_id       UUID        NOT NULL REFERENCES produkt (id),
    menge            INTEGER     NOT NULL CHECK (menge >= 1),
    einzelpreis_cent BIGINT      NOT NULL CHECK (einzelpreis_cent >= 0),
    CONSTRAINT bestellposition_zeile_eindeutig UNIQUE (bestellnummer, zeilennummer),
    CONSTRAINT bestellposition_produkt_eindeutig UNIQUE (bestellnummer, produkt_id)
);

CREATE INDEX bestellposition_produkt_idx ON bestellposition (produkt_id);

CREATE TABLE zahlung (
    id                 UUID        PRIMARY KEY,
    bestellnummer      VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    zahlungsart        VARCHAR(20) NOT NULL
        CHECK (zahlungsart IN ('KREDITKARTE', 'PAYPAL', 'LASTSCHRIFT', 'RECHNUNG', 'VORKASSE')),
    buchungsart        VARCHAR(20) NOT NULL CHECK (buchungsart IN ('ZAHLUNG', 'ERSTATTUNG')),
    transaktionsstatus VARCHAR(20) NOT NULL
        CHECK (transaktionsstatus IN ('AUSSTEHEND', 'ERFOLGREICH', 'FEHLGESCHLAGEN', 'STORNIERT')),
… [gekürzt, 134 Zeilen gesamt]
```

### ● Claude

Jetzt Test-Muster (IT-Setup, Testcontainers, Benachrichtigungs-Attrappe?).

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && sed -n 1,140p src/test/java/de/bestellsystem/zahlung/api/ZahlungApiIT.java; cat src/test/java/de/bestellsystem/TestcontainersKonfiguration.java; grep -rn "Benachrichtigungsversand\|MockitoBean\|scheduling" src/test | head; cat src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java | sed -n 1,200p
```

```
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/** US-05.1 und D-19 über HTTP; jede Bestellung hat einen Gesamtwert von 100,00 €. */
… [gekürzt, 250 Zeilen gesamt]
```

**⏺ Bash**

```bash
grep -n "public " src/main/java/de/bestellsystem/gemeinsam/domain/Geld.java; sed -n 1,80p src/test/java/de/bestellsystem/bestellung/domain/BezahlstatusTest.java; cat src/main/java/de/bestellsystem/bestellung/api/BestellungController.java | sed -n 1,80p; cat src/main/java/de/bestellsystem/kunde/application/KundeService.java | grep -n "public"
```

```
15:public record Geld(long cent) implements Comparable<Geld> {
17:    public static final String WAEHRUNG = "EUR";
18:    public static final Geld NULL = new Geld(0);
22:    public static Geld ausCent(long cent) {
26:    public String waehrung() {
30:    public Geld addiere(Geld andere) {
34:    public Geld subtrahiere(Geld andere) {
38:    public Geld multipliziere(int menge) {
43:    public Geld prozentsatz(BigDecimal prozent) {
50:    public boolean istNegativ() {
55:    public int compareTo(Geld andere) {
60:    public String toString() {
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
… [gekürzt, 181 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 55,70p src/main/java/de/bestellsystem/gemeinsam/domain/Geld.java; cat src/main/java/de/bestellsystem/gemeinsam/api/GeldDto.java | grep -v import; sed -n 55,70p src/main/java/de/bestellsystem/kunde/application/KundeService.java; sed -n 1,40p src/main/java/de/bestellsystem/bestellung/api/BestellungUebersichtDto.java
```

```
    public int compareTo(Geld andere) {
        return Long.compare(cent, andere.cent);
    }

    @Override
    public String toString() {
        String vorzeichen = cent < 0 ? "-" : "";
        long betrag = Math.abs(cent);
        return "%s%d,%02d %s".formatted(vorzeichen, betrag / 100, betrag % 100, WAEHRUNG);
    }
}
package de.bestellsystem.gemeinsam.api;


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
        return repository.findAllByOrderByNachnameAscVornameAsc();
    }

    public Kunde finde(UUID kundenId) {
        return repository.findeMitAdressen(kundenId).orElseThrow(() -> nichtGefunden(kundenId));
    }

    /** Wirft 404, wenn der Kunde nicht existiert, ohne ihn zu laden. */
    public void pruefeVorhanden(UUID kundenId) {
… [gekürzt, 84 Zeilen gesamt]
```

### ● Claude

Muster klar. Noch `Email`, `KundeRepository`, Mail-Test prüfen.

**⏺ Bash**

```bash
grep -n "record\|public static" src/main/java/de/bestellsystem/kunde/domain/Email.java; cat src/main/java/de/bestellsystem/kunde/persistence/KundeRepository.java src/test/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversandTest.java | grep -v "^import"; grep -rn "@Enable\|class .*Konfiguration" src/main/java | head; grep -n "aktualisiereOpenApi" -A15 build.gradle.kts | head -30
```

```
9:public record Email(String wert) {
11:    public static final int MAXIMALE_LAENGE = 254;
package de.bestellsystem.kunde.persistence;


public interface KundeRepository extends JpaRepository<Kunde, UUID> {

    boolean existsByEmail(Email email);

    List<Kunde> findAllByOrderByNachnameAscVornameAsc();

    @Query("select k from Kunde k left join fetch k.adressen where k.id = :id")
    Optional<Kunde> findeMitAdressen(@Param("id") UUID id);
}
package de.bestellsystem.gemeinsam.infrastruktur;



class MailBenachrichtigungsversandTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
    private final MailBenachrichtigungsversand versand =
            new MailBenachrichtigungsversand(
                    mailSender, new MailEinstellungen("shop@bestellsystem.local"));

    @Test
    void versendetBenachrichtigungAlsTextMail() {
        versand.sende(new Benachrichtigung("kunde@example.org", "Zahlungserinnerung", "Hallo"));

        ArgumentCaptor<SimpleMailMessage> nachricht =
                ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(nachricht.capture());
        assertThat(nachricht.getValue().getFrom()).isEqualTo("shop@bestellsystem.local");
        assertThat(nachricht.getValue().getTo()).containsExactly("kunde@example.org");
        assertThat(nachricht.getValue().getSubject()).isEqualTo("Zahlungserinnerung");
        assertThat(nachricht.getValue().getText()).isEqualTo("Hallo");
    }
}
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/UhrKonfiguration.java:9:class UhrKonfiguration {
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiKonfiguration.java:11:class OpenApiKonfiguration {
… [gekürzt, 50 Zeilen gesamt]
```

### ● Claude

Los. Zuerst Doku: D-20 und US-06.2.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && sed -n 254,400p docs/01-entscheidungen.md; cat stories/US-04.2-*.md | head -30
```

```
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
# US-04.2 Bestellhistorie einsehen

**Epic:** E04 Bestellung & Checkout
**Entscheidungen:** D-01, D-13, D-16

## Story

**Als** Kunde
**möchte ich** meine bisherigen Bestellungen in einer Liste sehen,
**damit** ich nachvollziehen kann, was ich wann bestellt habe.

## Beispieldaten

| Bestellnummer | Bestelldatum | Positionen | Gutschein | Gesamtwert |
|---|---|---|---|---|
| B-2026-000001 | 10.11.2026 | 1 × Buch | – | 12,50 € |
| B-2026-000002 | 12.11.2026 | 2 × Buch, 1 × Lampe | HERBST10 | 49,50 € |
| B-2026-000003 | 12.11.2026 | 1 × Buch | – | 12,50 € |

## Akzeptanzkriterien

1. **Liste der eigenen Bestellungen**
   Gegeben ich habe die drei Bestellungen aus den Beispieldaten aufgegeben,
   wenn ich meine Bestellhistorie abrufe,
   dann sehe ich je Bestellung Bestellnummer, Bestelldatum, Anzahl der Positionen und Gesamtwert,
… [gekürzt, 45 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat >> docs/01-entscheidungen.md <<'EOF'

## D-20 Mahnprüfung im Detail und täglicher Mahnlauf

**Problem:** D-03 legt fest, dass eine Mahnung je Bestellung höchstens einmal pro Intervall entsteht und die E-Mail „jede überfällige Bestellung“ auflistet. Offen war, was in der E-Mail steht, wenn nur ein Teil der überfälligen Bestellungen wieder gemahnt werden darf. Ebenfalls offen waren Auslöser und Fehlerverhalten des täglichen Laufs (US-06.2).

**Entscheidung:**
- Die E-Mail listet nur die Bestellungen, die **in dieser Prüfung gemahnt** werden: Sie sind überfällig, und ihre letzte Mahnung liegt mindestens ein Mahnintervall zurück (`stichtag ≥ letzteMahnung + mahnintervall`) oder fehlt. Die Intervalle der Bestellungen sind voneinander unabhängig. Präzisiert D-03.
- Eine Mahnung friert Datum (Stichtag) und gemahnten Betrag (offener Betrag am Stichtag) ein.
- Das Mahnintervall muss mindestens einen Tag betragen. Damit versendet eine Wiederholung am selben Tag nichts (idempotent).
- Der Versand läuft innerhalb der Transaktion der Prüfung. Scheitert er, wird nichts protokolliert (US-06.1 AK7), und die manuelle Prüfung antwortet mit **503**. Scheitert erst der Commit nach erfolgreichem Versand, kann die nächste Prüfung erneut mahnen: Die Zustellung gilt mindestens einmal.
- Gleichzeitige Prüfungen desselben Kunden laufen nacheinander (Zeilensperre auf dem Kunden). Die zweite sieht die Mahnungen der ersten.
- **Täglicher Mahnlauf:** zeitgesteuert über `bestellsystem.mahnlauf.cron`, Standard `0 0 6 * * *` in `bestellsystem.zeitzone`, mit `-` abschaltbar. Stichtag ist das heutige Datum laut Uhr. Geprüft werden alle Kunden mit mindestens einer Bestellung, deren Fälligkeit vor dem Stichtag liegt.
- Jeder Kunde wird in einer eigenen Transaktion geprüft. Ein Fehler bei einem Kunden wird geloggt und gezählt, der Lauf geht weiter. Das Ergebnis meldet die Anzahl gemahnter und fehlgeschlagener Kunden.
- Manuelle Auslösung, Stichtag immer heute: `POST /api/v1/kunden/{kundenId}/mahnpruefung` (ein Kunde) und `POST /api/v1/mahnlaeufe` (alle Kunden). Ohne Login sind beide offen wie die übrige API im MVP.

**Nicht im Umfang (Schritt 6):** Anzeige der Mahnungen im Frontend, Nachholen ausgefallener Läufe, Sperre gegen parallele Läufe auf mehreren Instanzen.
EOF
cat > stories/US-06.2-taeglicher-mahnlauf.md <<'EOF'
# US-06.2 Täglicher Mahnlauf über alle Kunden

**Epic:** E06 Mahnwesen
**Entscheidungen:** D-03, D-20

## Story

**Als** Shop-Betreiber
**möchte ich**, dass die Mahnprüfung jeden Tag automatisch für alle Kunden läuft,
**damit** kein Rückstand übersehen wird und niemand den Lauf von Hand anstoßen muss.

… [gekürzt, 71 Zeilen gesamt]
```

```
| [US-06.1](../stories/US-06.1-automatische-mahnpruefung.md) | Automatische Mahnprüfung für ein Kundenkonto | ausformuliert |
| [US-06.2](../stories/US-06.2-taeglicher-mahnlauf.md) | Täglicher Mahnlauf über alle Kunden | ausformuliert |
```

### ● Claude

Domain-Tests zuerst (rot erwartet).

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java
package de.bestellsystem.kunde.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.bestellung.domain.Bestellwunsch;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import de.bestellsystem.mahnwesen.domain.Mahnposition;
import de.bestellsystem.mahnwesen.domain.Mahnung;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** US-06.1 mit den Beispieldaten der Story; Zahlungsziel 14 Tage, Mahnintervall 7 Tage. */
class MahnpruefungTest {

    private static final Period ZAHLUNGSZIEL = Period.ofDays(14);
… [gekürzt, 203 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java (file state is current in your context — no need to Read it back)
```

### ● Claude

Jetzt Domain-Code.

**⏺ Bash**

```bash
mkdir -p /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/mahnwesen/{domain,application,api} && cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/mahnwesen && cat > domain/Mahnung.java <<'EOF'
package de.bestellsystem.mahnwesen.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** Protokoll einer Mahnung je Bestellung (D-03); Datum und Betrag sind eingefroren (D-20). */
@Entity
@Table(name = "mahnung")
public class Mahnung {

    @Id private UUID id;

    private LocalDate erstelltAm;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "gemahnter_betrag_cent"))
    private Geld gemahnterBetrag;

    protected Mahnung() {}

    public Mahnung(LocalDate erstelltAm, Geld gemahnterBetrag) {
… [gekürzt, 99 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Jetzt `Bestellung` erweitern.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
p='src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java'
s=open(p).read()
s=s.replace("import de.bestellsystem.katalog.domain.Produkt;\n","import de.bestellsystem.katalog.domain.Produkt;\nimport de.bestellsystem.mahnwesen.domain.Mahnung;\n")
s=s.replace("import java.util.Collections;\n","import java.util.Collections;\nimport java.util.Comparator;\n")
s=s.replace("""    private List<Zahlung> zahlungen = new ArrayList<>();

    protected Bestellung() {}""","""    private List<Zahlung> zahlungen = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "bestellnummer", nullable = false)
    @OrderBy("erstelltAm")
    private List<Mahnung> mahnungen = new ArrayList<>();

    protected Bestellung() {}""")
s=s.replace("""    /** Weitere Zahlung, z. B. Teilzahlung""","""    /** Rückstand: {@code offenerBetrag > 0 ∧ stichtag > faelligAm} (D-03). */
    public boolean istUeberfaellig(LocalDate stichtag) {
        return berechneOffenenBetrag().compareTo(Geld.NULL) > 0 && stichtag.isAfter(faelligAm);
    }

    /** Überfällig und die letzte Mahnung liegt mindestens ein Mahnintervall zurück (D-20). */
    public boolean istMahnungFaellig(LocalDate stichtag, Period mahnintervall) {
        return istUeberfaellig(stichtag)
                && letzteMahnung()
                        .map(letzte -> !stichtag.isBefore(letzte.getErstelltAm().plus(mahnintervall)))
                        .orElse(true);
    }

    /** Protokolliert eine Mahnung über den offenen Betrag am Stichtag (D-03, D-20). */
    public Mahnung mahne(LocalDate stichtag, Period mahnintervall) {
… [gekürzt, 56 Zeilen gesamt]
```

```
10:import de.bestellsystem.mahnwesen.domain.Mahnung;
93:    private List<Mahnung> mahnungen = new ArrayList<>();
221:    /** Überfällig und die letzte Mahnung liegt mindestens ein Mahnintervall zurück (D-20). */
222:    public boolean istMahnungFaellig(LocalDate stichtag, Period mahnintervall) {
224:                && letzteMahnung()
229:    /** Protokolliert eine Mahnung über den offenen Betrag am Stichtag (D-03, D-20). */
230:    public Mahnung mahne(LocalDate stichtag, Period mahnintervall) {
231:        if (!istMahnungFaellig(stichtag, mahnintervall)) {
235:        Mahnung mahnung = new Mahnung(stichtag, berechneOffenenBetrag());
240:    public Optional<Mahnung> letzteMahnung() {
```

### ● Claude

Jetzt `Kunde.fuehreMahnpruefungDurch`.

**⏺ Bash**

```bash
python3 - <<'EOF'
p='src/main/java/de/bestellsystem/kunde/domain/Kunde.java'
s=open(p).read()
s=s.replace("package de.bestellsystem.kunde.domain;\n\n","package de.bestellsystem.kunde.domain;\n\nimport de.bestellsystem.bestellung.domain.Bestellung;\n",1)
s=s.replace("import de.bestellsystem.gemeinsam.domain.NichtGefundenException;\n","import de.bestellsystem.gemeinsam.domain.NichtGefundenException;\nimport de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;\nimport de.bestellsystem.mahnwesen.domain.Mahnposition;\nimport de.bestellsystem.mahnwesen.domain.Mahnung;\n")
s=s.replace("import java.util.ArrayList;\n","import java.time.LocalDate;\nimport java.time.Period;\nimport java.util.ArrayList;\n")
s=s.replace("import java.util.Collections;\n","import java.util.Collections;\nimport java.util.Comparator;\n")
s=s.replace("import java.util.Objects;\n","import java.util.Objects;\nimport java.util.Optional;\n")
s=s.replace("""    private boolean istEinzigeLieferadresse""","""    /**
     * Mahnt jede Bestellung, deren Mahnung am Stichtag fällig ist, und liefert eine Nachricht mit
     * allen gemahnten Bestellungen; leer ohne Rückstand (US-06.1, D-03, D-20).
     *
     * @param bestellungen Bestellungen dieses Kunden, mit Positionen, Zahlungen und Mahnungen
     */
    public Optional<Mahnbenachrichtigung> fuehreMahnpruefungDurch(
            LocalDate stichtag, List<Bestellung> bestellungen, Period mahnintervall) {
        if (!stichtag.plus(mahnintervall).isAfter(stichtag)) {
            throw new IllegalArgumentException("Das Mahnintervall muss mindestens einen Tag betragen");
        }
        bestellungen.stream()
                .filter(bestellung -> !bestellung.getKundeId().equals(id))
                .findFirst()
                .ifPresent(fremde -> {
                    throw new IllegalArgumentException(
                            "Bestellung %s gehört nicht zum Kunden %s"
                                    .formatted(fremde.getBestellnummer(), id));
                });
        List<Mahnposition> positionen =
                bestellungen.stream()
                        .filter(bestellung -> bestellung.istMahnungFaellig(stichtag, mahnintervall))
… [gekürzt, 49 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Domain grün. Persistenz: Migration, Abfragen, Kundensperre.

**⏺ Bash**

```bash
cat > src/main/resources/db/migration/V5__mahnung.sql <<'EOF'
-- E06: Mahnung je Bestellung mit eingefrorenem Datum und Betrag (D-03, D-20)
CREATE TABLE mahnung (
    id                    UUID        PRIMARY KEY,
    bestellnummer         VARCHAR(30) NOT NULL REFERENCES bestellung (bestellnummer) ON DELETE CASCADE,
    erstellt_am           DATE        NOT NULL,
    gemahnter_betrag_cent BIGINT      NOT NULL CHECK (gemahnter_betrag_cent > 0)
);

CREATE INDEX mahnung_bestellnummer_idx ON mahnung (bestellnummer);

-- Mahnlauf sucht Kunden mit überfälligen Bestellungen (D-20)
CREATE INDEX bestellung_faellig_am_idx ON bestellung (faellig_am);
EOF
python3 - <<'EOF'
p='src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java'
s=open(p).read()
s=s.replace("import java.util.List;","import java.time.LocalDate;\nimport java.util.List;")
s=s.rstrip()[:-1]+'''
    // Drei Abfragen statt einer (MultipleBagFetchException), alle in denselben Persistenzkontext.
    @Query(
            """
            select b from Bestellung b
              join fetch b.positionen
            where b.kundeId = :kundeId and b.faelligAm < :stichtag
            """)
    List<Bestellung> findeFaelligeMitPositionen(
            @Param("kundeId") UUID kundeId, @Param("stichtag") LocalDate stichtag);

    @Query(
… [gekürzt, 102 Zeilen gesamt]
```

```
117:    private static NichtGefundenException nichtGefunden(UUID kundenId) {
```

### ● Claude

Port-Fehler, Adapter, 503.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && cat > gemeinsam/application/BenachrichtigungNichtVersendetException.java <<'EOF'
package de.bestellsystem.gemeinsam.application;

/** Der Versand einer Benachrichtigung ist gescheitert; Vertrag des Ports (D-20). */
public class BenachrichtigungNichtVersendetException extends RuntimeException {

    public BenachrichtigungNichtVersendetException(String nachricht, Throwable ursache) {
        super(nachricht, ursache);
    }
}
EOF
python3 - <<'EOF'
p='gemeinsam/application/Benachrichtigungsversand.java'
s=open(p).read()
s=s.replace("    void sende(Benachrichtigung benachrichtigung);","""    /**
     * @throws BenachrichtigungNichtVersendetException wenn die Nachricht nicht zugestellt werden
     *     konnte
     */
    void sende(Benachrichtigung benachrichtigung);""")
open(p,'w').write(s)
p='gemeinsam/infrastruktur/MailBenachrichtigungsversand.java'
s=open(p).read()
s=s.replace("import de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;","import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;\nimport de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;\nimport org.springframework.mail.MailException;")
s=s.replace("        mailSender.send(nachricht);","""        try {
            mailSender.send(nachricht);
        } catch (MailException fehler) {
            throw new BenachrichtigungNichtVersendetException(
                    "E-Mail an " + benachrichtigung.empfaenger() + " nicht versendet", fehler);
        }""")
open(p,'w').write(s)
… [gekürzt, 65 Zeilen gesamt]
```

```
package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

class MailBenachrichtigungsversandTest {

    private final JavaMailSender mailSender = mock(JavaMailSender.class);
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
p='src/test/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversandTest.java'
s=open(p).read()
s=s.replace("import static org.assertj.core.api.Assertions.assertThat;\n","import static org.assertj.core.api.Assertions.assertThat;\nimport static org.assertj.core.api.Assertions.assertThatThrownBy;\nimport static org.mockito.ArgumentMatchers.any;\nimport static org.mockito.Mockito.doThrow;\n")
s=s.replace("import de.bestellsystem.gemeinsam.application.Benachrichtigung;\n","import de.bestellsystem.gemeinsam.application.Benachrichtigung;\nimport de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;\n")
s=s.replace("import org.springframework.mail.SimpleMailMessage;\n","import org.springframework.mail.MailSendException;\nimport org.springframework.mail.SimpleMailMessage;\n")
open(p,'w').write(s)
EOF
```

```
(Bash completed with no output)
```

### ● Claude

Application-Schicht: Mahnschreiben, MahnService, Mahnlauf.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && cat > mahnwesen/application/Mahnschreiben.java <<'EOF'
package de.bestellsystem.mahnwesen.application;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Formuliert die E-Mail zu einer Mahnbenachrichtigung (D-03). */
final class Mahnschreiben {

    static final String BETREFF = "Zahlungserinnerung";

    private static final DateTimeFormatter DATUM = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Mahnschreiben() {}

    static Benachrichtigung von(Mahnbenachrichtigung mahnung) {
        String zeilen =
                mahnung.positionen().stream()
                        .map(position ->
                                "- %s, fällig am %s: offen %s"
                                        .formatted(
                                                position.bestellnummer(),
                                                DATUM.format(position.faelligAm()),
                                                position.offenerBetrag()))
                        .collect(Collectors.joining("\n"));
        String text =
                """
                Guten Tag,
… [gekürzt, 198 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

API-Schicht.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/mahnwesen/api && cat > MahnpositionDto.java <<'EOF'
package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.mahnwesen.domain.Mahnposition;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
        name = "Mahnposition",
        description = "Gemahnte Bestellung mit offenem Betrag am Stichtag (D-20)",
        requiredProperties = {"bestellnummer", "faelligAm", "offenerBetrag"})
public record MahnpositionDto(
        @Schema(example = "B-2026-000123") String bestellnummer,
        LocalDate faelligAm,
        GeldDto offenerBetrag) {

    static MahnpositionDto von(Mahnposition position) {
        return new MahnpositionDto(
                position.bestellnummer(),
                position.faelligAm(),
                GeldDto.von(position.offenerBetrag()));
    }
}
EOF
cat > MahnpruefungDto.java <<'EOF'
package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.mahnwesen.domain.Mahnbenachrichtigung;
import io.swagger.v3.oas.annotations.media.Schema;
… [gekürzt, 164 Zeilen gesamt]
```

```
/home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/mahnwesen/application/MahnService.java:41:    public Optional<Mahnbenachrichtigung> pruefeHeute(UUID kundenId) {
```

### ● Claude

`pruefeHeute` ungenutzt, raus.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
p='src/main/java/de/bestellsystem/mahnwesen/application/MahnService.java'
s=open(p).read()
s=s.replace("""    /** Prüfung mit dem heutigen Datum laut Uhr als Stichtag. */
    @Transactional
    public Optional<Mahnbenachrichtigung> pruefeHeute(UUID kundenId) {
        return pruefe(kundenId, LocalDate.now(uhr));
    }

""","")
s=s.replace("import java.time.Clock;\n","")
s=s.replace("    private final Clock uhr;\n","")
s=s.replace(",\n            Clock uhr) {",") {")
s=s.replace("        this.uhr = uhr;\n","")
open(p,'w').write(s)
EOF
./gradlew spotlessApply compileJava compileTestJava -q 2>&1 | tail -20
```

```
(Bash completed with no output)
```

### ● Claude

Integrationstests: Port-Attrappe und Testdaten-Helfer.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && mkdir -p mahnwesen/application mahnwesen/api && cat > BenachrichtigungsAttrappe.java <<'EOF'
package de.bestellsystem;

import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import de.bestellsystem.gemeinsam.application.Benachrichtigungsversand;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Merkt sich versendete Nachrichten und lässt auf Wunsch einzelne Empfänger scheitern. */
public class BenachrichtigungsAttrappe implements Benachrichtigungsversand {

    private final List<Benachrichtigung> versendet = new CopyOnWriteArrayList<>();
    private final Set<String> scheiternde = ConcurrentHashMap.newKeySet();

    @Override
    public void sende(Benachrichtigung benachrichtigung) {
        if (scheiternde.contains(benachrichtigung.empfaenger())) {
            throw new BenachrichtigungNichtVersendetException(
                    "Attrappe: Versand an " + benachrichtigung.empfaenger() + " scheitert",
                    new IllegalStateException("SMTP nicht erreichbar"));
        }
        versendet.add(benachrichtigung);
    }

… [gekürzt, 123 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cat > mahnwesen/application/MahnServiceIT.java <<'EOF'
package de.bestellsystem.mahnwesen.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.BenachrichtigungsAttrappe;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.gemeinsam.application.Benachrichtigung;
import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.mahnwesen.Mahnbestand;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;

/** US-06.1 mit Datenbank und Port-Attrappe; Beispieldaten der Story. */
@SpringBootTest
@Import({TestcontainersKonfiguration.class, BenachrichtigungsAttrappe.Konfiguration.class})
… [gekürzt, 287 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cat > mahnwesen/api/MahnwesenApiIT.java <<'EOF'
package de.bestellsystem.mahnwesen.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.BenachrichtigungsAttrappe;
import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.bestellung.persistence.BestellungRepository;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.mahnwesen.Mahnbestand;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
… [gekürzt, 125 Zeilen gesamt]
```

```
> Task :compileJava UP-TO-DATE
> Task :processResources UP-TO-DATE
> Task :classes UP-TO-DATE
> Task :compileTestJava
> Task :processTestResources
> Task :testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T11:46:32.442+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:46:32.443+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T11:46:32.448+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-11-12T11:46:32.715+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:46:32.716+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-11-12T11:46:32.719+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.
2026-11-12T11:46:32.944+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:46:32.945+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Shutdown initiated...
2026-11-12T11:46:32.946+01:00  INFO 1492345 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Shutdown completed.
> Task :test

[Incubating] Problems report is available at: file:///home/mschachner/Projects/bestellsystem/backend/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.9.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 15s
5 actionable tasks: 3 executed, 2 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.9.1/userguide/configuration_cache_enabling.html
```

### ● Claude

Prüfen, ob alle Mahn-Tests wirklich liefen:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/build/test-results/test && for f in *Mahn*.xml *MailBenach*.xml; do grep -o 'testsuite name="[^"]*" tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' $f; done
```

```
testsuite name="de.bestellsystem.kunde.domain.MahnpruefungTest" tests="10" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.mahnwesen.api.MahnwesenApiIT" tests="4" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.mahnwesen.application.MahnlaufIT" tests="4" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.mahnwesen.application.MahnlaufZeitplanIT" tests="1" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.mahnwesen.application.MahnServiceIT" tests="6" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.gemeinsam.infrastruktur.MailBenachrichtigungsversandTest" tests="2" skipped="0" failures="0" errors="0"
```

### ● Claude

Alles grün. Jetzt Gesamt-Build, OpenAPI-Export und Client neu erzeugen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew aktualisiereOpenApi -q 2>&1 | grep -v "INFO\|WARN\|^$" | tail -5; ./gradlew build -q 2>&1 | grep -iE "FAIL|error|BUILD" | head; echo "build exit ${pipestatus[1]}"; cd ../frontend && npm run generate:api --silent 2>&1 | tail -3; git -C .. status --short
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
build exit 0
[[bestellsystem] openapi.json] ############################################################################################
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,supportsES6=true" exited with code 0
[bestellsystem] openapi.json
 M backend/src/main/java/de/bestellsystem/bestellung/application/BestellungService.java
 M backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java
 M backend/src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java
 M backend/src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java
 M backend/src/main/java/de/bestellsystem/gemeinsam/application/Benachrichtigungsversand.java
 M backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversand.java
 M backend/src/main/java/de/bestellsystem/kunde/application/KundeService.java
 M backend/src/main/java/de/bestellsystem/kunde/domain/Kunde.java
 M backend/src/main/java/de/bestellsystem/kunde/persistence/KundeRepository.java
 M backend/src/main/resources/application.yml
 M backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversandTest.java
 M docs/01-entscheidungen.md
 M epics/E06-mahnwesen.md
 M frontend/openapi.json
 M frontend/src/app/api/.openapi-generator/FILES
 M frontend/src/app/api/api/api.ts
 M frontend/src/app/api/model/models.ts
?? backend/src/main/java/de/bestellsystem/gemeinsam/application/BenachrichtigungNichtVersendetException.java
?? backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/ZeitplanKonfiguration.java
?? backend/src/main/java/de/bestellsystem/mahnwesen/
?? backend/src/main/resources/db/migration/V5__mahnung.sql
?? backend/src/test/java/de/bestellsystem/BenachrichtigungsAttrappe.java
?? backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java
?? backend/src/test/java/de/bestellsystem/mahnwesen/
?? backend/src/test/resources/
?? frontend/src/app/api/api/mahnwesen.service.ts
?? frontend/src/app/api/model/mahnlauf.ts
?? frontend/src/app/api/model/mahnposition.ts
?? frontend/src/app/api/model/mahnpruefung.ts
?? stories/US-06.2-taeglicher-mahnlauf.md
```

### ● Claude

Frontend-Tests und Mailpit-Nachweis. Starte Infrastruktur und Backend.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && docker compose up -d 2>&1 | tail -3 && cd backend && (nohup ./gradlew bootRun > /tmp/bootrun-mahn.log 2>&1 &) ; cd ../frontend && npm test 2>&1 | tail -8
```

```
 Container bestellsystem-postgres-1 Running 
 Container bestellsystem-mailpit-1 Running 
[1m[30m[46m RUN [49m[39m[22m [36mv4.1.12 [39m[90m/home/mschachner/Projects/bestellsystem/frontend[39m


[2m Test Files [22m [1m[32m13 passed[39m[22m[90m (13)[39m
[2m      Tests [22m [1m[32m76 passed[39m[22m[90m (76)[39m
[2m   Start at [22m 11:47:33
[2m   Duration [22m 2.95s[2m (transform 1.11s, setup 6.50s, import 3.06s, tests 2.37s, environment 15.08s)[22m
```

### ● Claude

Frontend 76/76 grün (IDE-Diagnosen betreffen unveränderte Specs, sind IDE-Typing, nicht Build). Warte auf Backend.

**⏺ Bash**

```bash
for i in $(seq 1 60); do curl -sf localhost:8080/v3/api-docs >/dev/null && break; sleep 2; done; grep -E "Started|Migrating|ERROR" /tmp/bootrun-mahn.log | tail -5
```

```
2026-11-12T11:47:33.468+01:00  INFO 1501868 --- [bestellsystem] [           main] o.f.core.internal.command.DbMigrate      : Migrating schema "public" to version "5 - mahnung"
2026-11-12T11:47:37.246+01:00 ERROR 1501868 --- [bestellsystem] [           main] o.s.b.d.LoggingFailureAnalysisReporter   :
```

**⏺ Bash**

```bash
grep -A15 "APPLICATION FAILED" /tmp/bootrun-mahn.log | head -25
```

```
APPLICATION FAILED TO START
***************************

Description:

Web server failed to start. Port 8080 was already in use.

Action:

Identify and stop the process that's listening on port 8080 or configure this application to listen on another port.


> Task :bootRun FAILED

[Incubating] Problems report is available at: file:///home/mschachner/Projects/bestellsystem/backend/build/reports/problems/problems-report.html
```

### ● Claude

Port 8080 belegt. Schaue, was läuft:

**⏺ Bash**

```bash
ss -ltnp 2>/dev/null | grep 8080; ps -o pid,lstart,cmd -p $(ss -ltnp 2>/dev/null | grep 8080 | grep -o 'pid=[0-9]*' | cut -d= -f2 | head -1) 2>/dev/null | cut -c1-200
```

```
LISTEN 0      100                     *:8080             *:*    users:(("java",pid=1222768,fd=90))           
    PID                  STARTED CMD
1222768  Do Nov 12 10:58:45 2026 /usr/lib/jvm/java-21-openjdk-amd64/bin/java -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp /home/mschachner/Proj
```

### ● Claude

Alte Backend-Instanz (seit 10:58) läuft noch, alter Code. Nicht anfassen; starte neue Instanz auf :8081.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && (nohup ./gradlew bootRun --args='--server.port=8081' > /tmp/bootrun-mahn.log 2>&1 &); for i in $(seq 1 60); do curl -sf localhost:8081/v3/api-docs >/dev/null && break; sleep 2; done; grep -E "Started Bestell|ERROR" /tmp/bootrun-mahn.log | tail -3
```

```
2026-11-12T11:47:54.280+01:00  INFO 1505215 --- [bestellsystem] [           main] d.b.BestellsystemApplication             : Started BestellsystemApplication in 3.197 seconds (process running for 3.36)
```

### ● Claude

Testdaten über API anlegen, Fälligkeit in lokaler DB zurückdatieren, Mahnprüfung auslösen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && B=localhost:8081/api/v1; TS=$(date +%s)
K=$(curl -s -X POST $B/kunden -H 'Content-Type: application/json' -d "{\"vorname\":\"Mahn\",\"nachname\":\"Test\",\"email\":\"mahntest-$TS@example.de\",\"lieferadresse\":{\"strasse\":\"Hauptstr. 1\",\"stadt\":\"Berlin\",\"postleitzahl\":\"10115\",\"land\":\"DE\"},\"istAuchRechnungsadresse\":true}"); echo "$K" | head -c 400; echo
```

```
{"id":"2260b770-c2ec-4a6d-b70b-82768a786348","vorname":"Mahn","nachname":"Test","email":"mahntest-1794480480@example.de","adressen":[{"id":"e391f380-486e-4519-b483-a0e339799d67","anschrift":{"strasse":"Hauptstr. 1","stadt":"Berlin","postleitzahl":"10115","land":"DE"},"istLieferadresse":true,"istRechnungsadresse":true}]}
```

**⏺ Bash**

```bash
B=localhost:8081/api/v1; KID=2260b770-c2ec-4a6d-b70b-82768a786348; AID=e391f380-486e-4519-b483-a0e339799d67
grep -n "record ProduktAnlegenDto" -A6 backend/src/main/java/de/bestellsystem/katalog/api/ProduktAnlegenDto.java | grep -o "[A-Za-z]* [a-z][A-Za-z]*[,)]" ; grep -o "[A-Za-z<>]* [a-z][A-Za-z]*[,)]" backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java backend/src/main/java/de/bestellsystem/bestellung/api/PositionswunschDto.java
```

```
String titel,
GeldDto listenpreis,
Integer lagerbestand)
backend/src/main/java/de/bestellsystem/bestellung/api/PositionswunschDto.java:UUID produktId,
backend/src/main/java/de/bestellsystem/bestellung/api/PositionswunschDto.java:Integer menge)
backend/src/main/java/de/bestellsystem/bestellung/api/PositionswunschDto.java: menge)
backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java:PositionswunschDto> positionen,
backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java:UUID lieferadresseId,
backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java:Zahlungsart zahlungsart,
backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java:String gutscheincode)
```

**⏺ Bash**

```bash
B=localhost:8081/api/v1; KID=2260b770-c2ec-4a6d-b70b-82768a786348; AID=e391f380-486e-4519-b483-a0e339799d67
P=$(curl -s -X POST $B/produkte -H 'Content-Type: application/json' -d '{"titel":"Mahntest-Lampe","listenpreis":{"cent":4999,"waehrung":"EUR"},"lagerbestand":5}' | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
NR=$(curl -s -X POST $B/kunden/$KID/bestellungen -H 'Content-Type: application/json' -d "{\"positionen\":[{\"produktId\":\"$P\",\"menge\":1}],\"lieferadresseId\":\"$AID\",\"zahlungsart\":\"RECHNUNG\"}" | grep -o '"bestellnummer":"[^"]*"' | cut -d'"' -f4); echo "Bestellung $NR"
source .env 2>/dev/null; docker compose exec -T postgres psql -U "${DB_USER:-bestellsystem}" -d "${DB_NAME:-bestellsystem}" -c "UPDATE bestellung SET bestelldatum = DATE '2026-10-01', faellig_am = DATE '2026-10-15' WHERE bestellnummer = '$NR'"
echo; curl -s -X POST $B/kunden/$KID/mahnpruefung; echo; curl -s -X POST $B/kunden/$KID/mahnpruefung; echo; curl -s -X POST $B/mahnlaeufe; echo
curl -s "localhost:8025/api/v1/search?query=to:mahntest-1794480480@example.de" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["messages_count"]); [print(m["Subject"], m["To"][0]["Address"], m["ID"]) for m in d["messages"]]'
```

```
Bestellung B-2026-000012
UPDATE 1

{"stichtag":"2026-11-12","istGemahnt":true,"mahnpositionen":[{"bestellnummer":"B-2026-000012","faelligAm":"2026-10-15","offenerBetrag":{"cent":4999,"waehrung":"EUR"}}]}
{"stichtag":"2026-11-12","istGemahnt":false,"mahnpositionen":[]}
{"stichtag":"2026-11-12","gemahnteKunden":0,"fehlgeschlageneKunden":0}
1
Zahlungserinnerung mahntest-1794480480@example.de 1BmgsadZlVxBlyFjZoRvAM
```

**⏺ Bash**

```bash
curl -s localhost:8025/api/v1/message/1BmgsadZlVxBlyFjZoRvAM | python3 -c 'import json,sys; print(json.load(sys.stdin)["Text"])'; pkill -f "server.port=8081" ; sleep 2; ss -ltn | grep -c 8081
```

```
Exit code 144
Guten Tag,

zu folgenden Bestellungen ist die Zahlung überfällig (Stand 12.11.2026):

- B-2026-000012, fällig am 15.10.2026: offen 49,99 EUR

Summe offen: 49,99 EUR

Bitte begleichen Sie den offenen Betrag. Haben Sie bereits bezahlt, ist diese Nachricht gegenstandslos.
```

**⏺ Bash**

```bash
ss -ltn | grep -E ":808[01]" ; pgrep -af "server.port=8081" | cut -c1-120
```

```
LISTEN 0      100                     *:8080             *:*          
1506371 /usr/bin/zsh -c source /home/mschachner/.claude/shell-snapshots/snapshot-zsh-1794477029925-0eyh9h.sh 2>/dev/null
```

### ● Claude

Mailpit-Nachweis erbracht. Danach Testinstanz auf :8081 gestoppt; die alte Instanz auf :8080 läuft unberührt weiter. Jetzt Phase 6: unabhängiger Review des Diffs.

**⏺ Agent**

```
{
 "description": "Review dunning diff",
 "subagent_type": "feature-dev:code-reviewer",
 "prompt": "Review the uncommitted changes in /home/mschachner/Projects/bestellsystem (run `git diff` and `git status`; untracked new files under backend/src/main/java/de/bestellsystem/mahnwesen/, backend/src/test/java/de/bestellsystem/mahnwesen/, backend/src/test/java/de/bestellsystem/BenachrichtigungsAttrappe.java, backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java, backend/src/main/resources/db/migration/V5__mahnung.sql, gemeinsam/application/BenachrichtigungNichtVersendetException.java, gemeinsam/infrastruktur/ZeitplanKonfiguration.java, backend/src/test/resources/config/application.yml). Ignore generated frontend/src/app/api and frontend/openapi.json.\n\nFeature: dunning (Mahnwesen). Spec: stories/US-06.1-automatische-mahnpruefung.md, stories/US-06.2-taeglicher-mahnlauf.md, decisions D-03 and D-20 in docs/01-entscheidungen.md. Project rules: CLAUDE.md (rich domain model, layering api→application→domain, Clock only, Geld value object, Flyway, LAZY + JOIN FETCH, no N+1, ProblemDetail errors, German domain naming).\n\nFocus: functional bugs vs. the acceptance criteria and D-20 (interval semantics, transaction/rollback when mail sending fails, per-customer transaction in the run, locking/concurrency, Hibernate fetching/persistence-context pitfalls with the three fetch queries in BestellungService.faelligeZumMahnen, scheduling config), and violations of CLAUDE.md conventions. All tests currently pass (`./gradlew build` green). Report only high-confidence, concrete issues with file:line and a failure scenario; no style nits."
}
```

```
Async agent launched successfully. (This tool result is internal metadata — never quote or paste any part of it, including the agentId below, into a user-facing reply.)
agentId: af528c6f11b9bdbcb (internal ID - do not mention to user. Use SendMessage with to: 'af528c6f11b9bdbcb', summary: '<5-10 word recap>' to continue this agent.)
The agent is working in the background. You will be notified automatically when it completes. You know nothing about its results until that notification arrives — do not report, assume, or predict them; continue other work or respond to the user in the meantime.
Do not duplicate this agent's work — avoid working with the same files or topics it is using.
output_file: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/af528c6f11b9bdbcb.output
Do NOT Read or tail this file via the shell tool — it is the full subagent JSONL transcript and reading it will overflow your context. If the user asks for progress, say the agent is still running; you'll get a completion notification.
```

### ● Claude

Während Review läuft: `ARCHITEKTUR.md` nachziehen.

**⏺ Bash**

````bash
python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
s=s.replace("Stand: Schritt 5 (Zahlungen E05: Bezahlstatus US-05.1, Statuswechsel und Erstattung D-19).","Stand: Schritt 6 (Mahnwesen E06: Mahnprüfung US-06.1, täglicher Mahnlauf US-06.2, D-20).")
s=s.replace("| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung`,","| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung` (Fehler des Ports: `BenachrichtigungNichtVersendetException`),")
s=s.replace("`MailBenachrichtigungsversand` (`JavaMailSender`),","`MailBenachrichtigungsversand` (`JavaMailSender`, übersetzt `MailException`), `ZeitplanKonfiguration` (`@EnableScheduling`),")
s=s.replace("`lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen); setzt I-1","`lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen), `fuehreMahnpruefungDurch(stichtag, bestellungen, mahnintervall)` (US-06.1, D-20: mahnt fällige Bestellungen, liefert `Optional<Mahnbenachrichtigung>`); setzt I-1")
s=s.replace("`findeMitAdressen` (`JOIN FETCH`) |","`findeMitAdressen` (`JOIN FETCH`), `sperreZumMahnen` (`PESSIMISTIC_WRITE`, D-20) |")
s=s.replace("`pruefeVorhanden` (404 ohne Laden),","`pruefeVorhanden` (404 ohne Laden), `sperreZumMahnen` (404),")
s=s.replace("`zahlung(id)` (404 bei fremder Buchung).","`zahlung(id)` (404 bei fremder Buchung); Mahnwesen (D-03, D-20) `istUeberfaellig(stichtag)`, `istMahnungFaellig(stichtag, intervall)`, `mahne(stichtag, intervall)`, `letzteMahnung`, Mahnungen `cascade ALL`.")
s=s.replace("`findeMitZahlungen` (Kunde und Nummer, für Buchungen) |","`findeMitZahlungen` (Kunde und Nummer, für Buchungen), `findeFaelligeMitPositionen` / `findeZahlungenFaelliger` / `findeMahnungenFaelliger` (fällig vor Stichtag, drei Abfragen), `findeKundenMitFaelligenBestellungen` |")
s=s.replace("`bucheErstattung`; Buchungen sperren","`bucheErstattung`, `faelligeZumMahnen` (drei Abfragen), `kundenMitFaelligenBestellungen`; Buchungen sperren")
s=s.replace("| `bestellung.api` |","""| `mahnwesen.domain` | `Mahnung` (Entity, UUID, Teil der Bestellung; eingefroren `erstelltAm`, `gemahnterBetrag` > 0), `Mahnbenachrichtigung` (Empfänger, Stichtag, Positionen, `summe`), `Mahnposition` |
| `mahnwesen.application` | `MahnService.pruefe(kundenId, stichtag)` (`@Transactional`: Kunde sperren, Bestellungen laden, `Kunde.fuehreMahnpruefungDurch`, Versand vor dem Commit), `Mahnlauf` (`@Scheduled`, `fuehreDurch(stichtag)`: je Kunde eigene Transaktion, Fehler gezählt), `Mahnlaufergebnis`, `Mahnschreiben` (Text der E-Mail) |
| `mahnwesen.api` | `MahnwesenController`, DTOs `MahnpruefungDto`, `MahnpositionDto`, `MahnlaufDto` |
| `bestellung.api` |""",1)
s=s.replace("""| POST | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/erstattungen` | `{zahlungsart, betrag}`: 201 + `Location` (Buchung), 400 (Betrag 0, Pflichtfelder), 404, 409, 422 über erstattbar (D-19) |""","""| POST | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/erstattungen` | `{zahlungsart, betrag}`: 201 + `Location` (Buchung), 400 (Betrag 0, Pflichtfelder), 404, 409, 422 über erstattbar (D-19) |
| POST | `/api/v1/kunden/{kundenId}/mahnpruefung` | Mahnprüfung, Stichtag heute: 200 `{stichtag, istGemahnt, mahnpositionen[{bestellnummer, faelligAm, offenerBetrag}]}`, 404, 409, 503 Versand gescheitert (D-20) |
| POST | `/api/v1/mahnlaeufe` | Mahnlauf über alle Kunden, Stichtag heute: 200 `{stichtag, gemahnteKunden, fehlgeschlageneKunden}` (US-06.2) |""")
s=s.replace("""## Querschnitt
""","""## Datenfluss (Mahnprüfung und Mahnlauf, D-20)

```
@Scheduled(bestellsystem.mahnlauf.cron) oder POST /mahnlaeufe
  → Mahnlauf.fuehreDurch(heute aus Clock)
      BestellungService.kundenMitFaelligenBestellungen(stichtag)   DISTINCT kunde_id, faellig_am < stichtag
      je Kunde: MahnService.pruefe (eigene Transaktion), Fehler loggen und zählen
  ← Mahnlaufergebnis {stichtag, gemahnteKunden, fehlgeschlageneKunden}

POST /kunden/{id}/mahnpruefung oder Mahnlauf
… [gekürzt, 49 Zeilen gesamt]
````

```
232:### Tabellen und Sequenzen
245:### Migrationen
251:| V3 | `V3__kunde.sql` | Tabellen `kunde` und `adresse` |
252:| V4 | `V4__gutschein_bestellung_zahlung.sql` | Tabellen `gutschein`, `bestellung`, `bestellposition`, `zahlung` |
283:| `src/styles.css` | globale Formular- und Tabellenstile (`.formular`, `.feldfehler`, `.aktionen`, `table`, `.zahl`, `.unsichtbar`) |
```

**⏺ Bash**

```bash
sed -n 228,256p docs/ARCHITEKTUR.md; grep -c "mahnwesen" docs/ARCHITEKTUR.md
```

```
- `spring.jpa.open-in-view=false`, `ddl-auto=validate`.

## Datenbank

### Tabellen und Sequenzen

| Objekt | Zweck |
|---|---|
| Sequenz `bestellnummer_seq` | Zähler der Bestellnummer |
| Tabelle `produkt` | `id` UUID, `version`, `titel` (≤ 200), `listenpreis_cent` (≥ 0), `lagerbestand` (≥ 0); Index auf `titel` |
| Tabelle `kunde` | `id` UUID, `version`, `vorname`, `nachname` (≤ 100), `email` (≤ 254, normalisiert, UNIQUE `kunde_email_eindeutig`); Index auf `(nachname, vorname)` |
| Tabelle `adresse` | `id` UUID, `kunde_id` (FK, `ON DELETE CASCADE`, Index), `reihenfolge` (`@OrderColumn`, nullable, da von Hibernate nachgetragen), `strasse`, `stadt`, `postleitzahl`, `land`, `ist_lieferadresse`, `ist_rechnungsadresse`; CHECK mindestens ein Kennzeichen (I-2) |
| Tabelle `gutschein` | `id` UUID, `code` (≤ 50, normalisiert, UNIQUE `gutschein_code_eindeutig`), `rabatt_art`, `prozent` NUMERIC(5,2) oder `festbetrag_cent`, `ablaufdatum`; CHECK passend zur Rabattart (D-04) |
| Tabelle `bestellung` | `bestellnummer` PK, `version`, `kunde_id` (FK, Index), `bestelldatum`, `faellig_am`, `liefer_strasse`/`_stadt`/`_postleitzahl`/`_land` (Snapshot, D-08), `gutschein_id` (FK, nullable), `rabatt_betrag_cent` (≥ 0); kein gespeicherter Gesamtwert (D-01) |
| Tabelle `bestellposition` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`), `zeilennummer` (≥ 1), `produkt_id` (FK, Index), `menge` (≥ 1), `einzelpreis_cent` (≥ 0); UNIQUE je Bestellung auf Zeilennummer und Produkt (I-6) |
| Tabelle `zahlung` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`, Index), `zahlungsart`, `buchungsart`, `transaktionsstatus` (je CHECK auf Enum-Werte), `betrag_cent` (≥ 0), `zahlungszeitpunkt` (nullable, gesetzt bei `ERFOLGREICH`, D-17, D-19) |

### Migrationen

| Version | Datei | Inhalt |
|---|---|---|
| V1 | `V1__bestellnummer_sequenz.sql` | Sequenz `bestellnummer_seq` |
| V2 | `V2__produkt.sql` | Tabelle `produkt` mit CHECK-Constraints |
| V3 | `V3__kunde.sql` | Tabellen `kunde` und `adresse` |
| V4 | `V4__gutschein_bestellung_zahlung.sql` | Tabellen `gutschein`, `bestellung`, `bestellposition`, `zahlung` |

## Lokale Infrastruktur

`docker-compose.yml`:
3
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
s=s.replace("`rabatt_betrag_cent` (≥ 0); kein gespeicherter Gesamtwert (D-01) |","`rabatt_betrag_cent` (≥ 0); Index auf `faellig_am` (Mahnlauf); kein gespeicherter Gesamtwert (D-01) |")
s=s.replace("""`zahlungszeitpunkt` (nullable, gesetzt bei `ERFOLGREICH`, D-17, D-19) |""","""`zahlungszeitpunkt` (nullable, gesetzt bei `ERFOLGREICH`, D-17, D-19) |
| Tabelle `mahnung` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`, Index), `erstellt_am`, `gemahnter_betrag_cent` (> 0) (D-03, D-20) |""")
s=s.replace("""| V4 | `V4__gutschein_bestellung_zahlung.sql` | Tabellen `gutschein`, `bestellung`, `bestellposition`, `zahlung` |""","""| V4 | `V4__gutschein_bestellung_zahlung.sql` | Tabellen `gutschein`, `bestellung`, `bestellposition`, `zahlung` |
| V5 | `V5__mahnung.sql` | Tabelle `mahnung`, Index `bestellung_faellig_am_idx` |""")
open(p,'w').write(s)
EOF
git diff --stat docs/ARCHITEKTUR.md
```

```
 docs/ARCHITEKTUR.md | 49 +++++++++++++++++++++++++++++++++++++++----------
 1 file changed, 39 insertions(+), 10 deletions(-)
```

### ● Claude

Doku fertig. Warte auf Review-Ergebnis.

### ❯ Nutzer

> <task-notification>
> <task-id>af528c6f11b9bdbcb</task-id>
> <tool-use-id>toolu_01HPgQqaRfocL1Ws3r6vT9kA</tool-use-id>
> <output-file>/tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/af528c6f11b9bdbcb.output</output-file>
> <status>completed</status>
> <summary>Agent "Review dunning diff" finished</summary>
> <note>A task-notification fires each time this agent stops with no live background children of its own. The user can send it another message and resume it, so the same task-id may notify more than once.</note>
> <result>I found no issues at confidence 80 or above in the dunning (Mahnwesen) changes.
>
> I couldn't run `git diff` because this environment has no shell tool. I read the changed and new files directly instead. That covered `Kunde`, `Bestellung`, `BestellungRepository`, `BestellungService`, `KundeService` and `KundeRepository`. It also covered `MahnService`, `Mahnlauf`, `Mahnschreiben`, `MahnwesenController`, `Mahnung`, `ZentraleFehlerbehandlung`, `MailBenachrichtigungsversand`, `ZeitplanKonfiguration`, `V5__mahnung.sql`, `application.yml` and the test `config/application.yml`. I did not read the test classes, `Mahnbenachrichtigung`, `Mahnposition`, the DTOs, or the diffs of `docs/` and `epics/`.
>
> **Checked and found correct**
> - **Interval rule:** `Bestellung.istMahnungFaellig` uses `!stichtag.isBefore(letzte + intervall)`, which matches D-20. 01.11. gives nothing on 05.11. and re-dunning on 08.11. (AK4). The due-date day itself is not dunned (AK6), and the SQL filter `faelligAm &lt; :stichtag` agrees with `istUeberfaellig`.
> - **Same-day idempotence:** `Kunde.fuehreMahnpruefungDurch` rejects an interval under one day, so a repeat run on the same day sends nothing.
> - **Rollback on mail failure:** `BenachrichtigungNichtVersendetException` is a `RuntimeException` and is thrown inside `MahnService.pruefe`'s `@Transactional`, so the transaction rolls back (US-06.1 AK7). The exception handler maps it to 503.
> - **Per-customer transactions:** `Mahnlauf` is a separate bean that calls the `MahnService` proxy without its own transaction. Each customer therefore gets a fresh transaction, and a failure is counted and logged without stopping the run.
> - **Locking:** the pessimistic write lock on `Kunde` does not touch `@Version`, because `Kunde` is not modified. Under READ COMMITTED the second concurrent check sees the first one's `Mahnung` rows.
> - **Fetch queries:** the three queries in `BestellungService.faelligeZumMahnen` initialize the bag collections on the same managed `Bestellung` instances. That is the same pattern as the existing `findeZahlungen` and `findeZahlungenDesKunden`. The `Bestellung` and `Bestellposition` computations don't touch the lazy `produkt`, so there is no lazy-load failure or N+1.
> - **Scheduling:** `cron: "-"` with a `zone` placeholder is fine in Spring. The test `config/application.yml` switches the schedule off, and `ZeitplanKonfiguration` enables scheduling.
> - **Schema:** the `V5__mahnung.sql` foreign key type matches `bestellung.bestellnummer`, which is `VARCHAR(30)`. A new migration was added instead of editing an old one, and the `Geld` column uses an `AttributeOverride`.
> - **Mail at-least-once:** the at-least-once delivery on commit failure is documented in D-20 and accepted there.
>
> **Below the reporting threshold (about 55–60 confidence)**
> - **Unvalidated `bestellsystem.mahnintervall`** (`/home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/gemeinsam/application/FachlicheEinstellungen.java:19`): nothing checks the interval at startup, so `0d` or a negative value starts fine. Every customer in the daily run then fails with an `IllegalArgumentException`, which is only logged as a warning and counted in `fehlgeschlagen`. The manual `POST .../mahnpruefung` returns 400, which blames the caller for a configuration error. A `@Validated` check on the record, or a startup assertion, would fail fast. This is not an acceptance-criteria violation, since D-20 only requires the minimum in the domain.</result>
> <usage><subagent_tokens>59156</subagent_tokens><tool_uses>27</tool_uses><duration_ms>73990</duration_ms></usage>
> </task-notification>

### ● Claude

Review: kein Befund ab Konfidenz 80. Ein Randpunkt darunter: `mahnintervall` wird beim Start nicht validiert. Bei `0d` scheitert dann jeder Kunde im Lauf; das wird nur geloggt. Biete ich unten an, kein AK betroffen. Build war nach letzter Codeänderung grün, danach nur Doku. Commit:

**⏺ Bash**

```bash
git add backend/src/main/java/de/bestellsystem/mahnwesen backend/src/test/java/de/bestellsystem/mahnwesen backend/src/test/resources/config/application.yml backend/src/test/java/de/bestellsystem/BenachrichtigungsAttrappe.java backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java backend/src/main/resources/db/migration/V5__mahnung.sql backend/src/main/java/de/bestellsystem/gemeinsam/application/BenachrichtigungNichtVersendetException.java backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/ZeitplanKonfiguration.java backend/src/main/java/de/bestellsystem/bestellung backend/src/main/java/de/bestellsystem/gemeinsam backend/src/main/java/de/bestellsystem/kunde backend/src/main/resources/application.yml backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversandTest.java docs/01-entscheidungen.md docs/ARCHITEKTUR.md epics/E06-mahnwesen.md stories/US-06.2-taeglicher-mahnlauf.md frontend/openapi.json frontend/src/app/api && git status --short && git commit -q -F - <<'EOF'
Add dunning check, daily dunning run and Mahnung (E06)

US-06.1 and US-06.2: Kunde.fuehreMahnpruefungDurch dunns overdue orders
once per interval, Mahnung entity (V5), mail via the notification port
before commit so a send failure records nothing (503). Daily @Scheduled
run with one transaction per customer, manual endpoints for a single
customer and for all customers. D-20 settles partial dunning and the run.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1
```

```
M  backend/src/main/java/de/bestellsystem/bestellung/application/BestellungService.java
M  backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java
M  backend/src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java
M  backend/src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java
A  backend/src/main/java/de/bestellsystem/gemeinsam/application/BenachrichtigungNichtVersendetException.java
M  backend/src/main/java/de/bestellsystem/gemeinsam/application/Benachrichtigungsversand.java
M  backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversand.java
A  backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/ZeitplanKonfiguration.java
M  backend/src/main/java/de/bestellsystem/kunde/application/KundeService.java
M  backend/src/main/java/de/bestellsystem/kunde/domain/Kunde.java
M  backend/src/main/java/de/bestellsystem/kunde/persistence/KundeRepository.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/api/MahnlaufDto.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/api/MahnpositionDto.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/api/MahnpruefungDto.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/api/MahnwesenController.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/application/MahnService.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/application/Mahnlauf.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/application/Mahnlaufergebnis.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/application/Mahnschreiben.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/domain/Mahnbenachrichtigung.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/domain/Mahnposition.java
A  backend/src/main/java/de/bestellsystem/mahnwesen/domain/Mahnung.java
M  backend/src/main/resources/application.yml
A  backend/src/main/resources/db/migration/V5__mahnung.sql
A  backend/src/test/java/de/bestellsystem/BenachrichtigungsAttrappe.java
M  backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversandTest.java
A  backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java
A  backend/src/test/java/de/bestellsystem/mahnwesen/Mahnbestand.java
A  backend/src/test/java/de/bestellsystem/mahnwesen/api/MahnwesenApiIT.java
A  backend/src/test/java/de/bestellsystem/mahnwesen/application/MahnServiceIT.java
A  backend/src/test/java/de/bestellsystem/mahnwesen/application/MahnlaufIT.java
A  backend/src/test/java/de/bestellsystem/mahnwesen/application/MahnlaufZeitplanIT.java
A  backend/src/test/resources/config/application.yml
M  docs/01-entscheidungen.md
M  docs/ARCHITEKTUR.md
M  epics/E06-mahnwesen.md
M  frontend/openapi.json
M  frontend/src/app/api/.openapi-generator/FILES
M  frontend/src/app/api/api/api.ts
A  frontend/src/app/api/api/mahnwesen.service.ts
… [gekürzt, 46 Zeilen gesamt]
```

### ● Claude

All "Fertig wenn" erfüllt → abhaken.

**⏺ Bash**

```bash
sed -i 's/^## \[ \] 6 Mahnwesen (E06)$/## [x] 6 Mahnwesen (E06) (2026-11-12, 874b481)/' docs/fahrplan.md && grep -n "^## \[x\] 6" docs/fahrplan.md && git add docs/fahrplan.md && git commit -q -m "Hake Schritt 6 im Fahrplan ab

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -2 && git status --short
```

```
138:## [x] 6 Mahnwesen (E06) (2026-11-12, 874b481)
0c85420 Hake Schritt 6 im Fahrplan ab
874b481 Add dunning check, daily dunning run and Mahnung (E06)
[This command modified 1 file you've previously read: docs/fahrplan.md. Call Read before editing.]
```

### ● Claude

Schritt 6 (Mahnwesen) ist fertig, abgehakt und committet. Backend-Build mit allen Tests grün, Frontend-Tests 76/76 grün, Mahn-E-Mail in Mailpit nachgewiesen.

**Gebaut** (nach deinen vier Antworten)
- **Mahnung:** neue Tabelle per Migration `V5`. Jede Mahnung gehört zu einer Bestellung und hält Datum und gemahnten Betrag fest.
- **`Kunde.fuehreMahnpruefungDurch`:** mahnt jede Bestellung, die überfällig ist und deren letzte Mahnung mindestens 7 Tage zurückliegt. Der Kunde bekommt eine E-Mail mit genau diesen Bestellungen.
- **Versandfehler:** Der Versand läuft vor dem Commit. Scheitert er, wird nichts protokolliert und die Antwort ist 503.
  - Nachteil: Scheitert erst der Commit nach erfolgreichem Versand, kann die nächste Prüfung erneut mahnen. Diese doppelte Zustellung ist in D-20 als zulässig festgehalten.
- **Täglicher Lauf:** standardmäßig 06:00 Europe/Berlin, über `bestellsystem.mahnlauf.cron` einstellbar und mit `-` abschaltbar. Jeder Kunde läuft in einer eigenen Transaktion; ein Fehler bei einem Kunden wird gezählt und hält den Lauf nicht auf.
- **Endpunkte:** `POST /api/v1/kunden/{id}/mahnpruefung` für einen Kunden und `POST /api/v1/mahnlaeufe` für alle, Stichtag jeweils heute.
- **Doku:**
  - neue Entscheidung D-20 (welche Bestellungen in der E-Mail stehen, Ablauf des Laufs, 503)
  - Story US-06.2 neu in `stories/`, Epic E06 aktualisiert
  - `ARCHITEKTUR.md` nachgezogen
  - OpenAPI und generierter Client aktualisiert; kein Frontend-Code, wie gewählt.

**Fertig wenn**
- **US-06.1 AK1–7 als Tests grün, mit fester Uhr:** sowohl als Domain-Test als auch gegen die Datenbank mit einer Attrappe für den Versand. Dazu Tests für US-06.2, die Endpunkte und den Zeitplan.
- **Mahn-E-Mail in Mailpit sichtbar:** Testkunde angelegt, Fälligkeit in der lokalen DB zurückdatiert, E-Mail „Zahlungserinnerung“ mit Bestellung, Fälligkeit 15.10.2026 und 49,99 EUR offen kam an.
- **Wiederholung am selben Tag idempotent:** die zweite Prüfung lieferte `istGemahnt:false`, der folgende Lauf `gemahnteKunden:0`. Zusätzlich als Test abgedeckt.

**Hinweise**
- Auf Port 8080 lief schon eine ältere Backend-Instanz (gestartet 10:58). Die habe ich nicht angefasst, sondern zum Nachweis eine eigene auf 8081 gestartet und danach wieder beendet. Die alte Instanz läuft noch mit altem Code; Migration `V5` ist in der lokalen DB aber schon eingespielt.
- In der lokalen DB liegen jetzt Testdaten: Kunde „Mahn Test“, Produkt „Mahntest-Lampe“, Bestellung B-2026-000012 mit Mahnung.
- Der Review fand keinen echten Fehler. Ein Randpunkt bleibt offen: Ein Mahnintervall von `0d` in der Konfiguration fällt erst im Lauf auf, und dann scheitert jeder Kunde mit nur einer Warnung im Log. Eine Prüfung beim Start ist klein; soll ich sie einbauen?

**Commits:** `874b481` (Arbeit), `0c85420` (Fahrplan abgehakt).

**Nächster Schritt:** 7, Merkliste (E03). Vorher `/clear`, dann `/naechster-schritt`.

