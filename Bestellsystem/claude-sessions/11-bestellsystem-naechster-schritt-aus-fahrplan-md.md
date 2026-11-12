## Session 11: /naechster-schritt aus fahrplan.md

*Projekt: bestellsystem · ID `a6c9b6d1-e2ec-497f-a5b0-6f67b5e3cd63` · 2026-11-12 10:50 – 2026-11-12 11:00 UTC*

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
cat epics/E03-*.md; echo ======; ls stories; echo =====; cat stories/US-03*; echo =====; grep -n -A12 "D-07" docs/01-entscheidungen.md; echo ====; grep -n -i -B2 -A15 "merkl" docs/02-domaenenmodell.md | head -80
```

```
Exit code 1
# E03 Merkliste

**Priorität:** MVP (unabhängig, parallel umsetzbar)
**Akteur:** Kunde
**Abhängig von:** E01, E02

## Ziel

Kunden merken sich Produkte für später und sehen jederzeit ihre aktuell beobachteten Artikel.

## Umfang

- Produkt auf die Merkliste setzen; Datum wird festgehalten. → D-07
- Produkt von der Merkliste entfernen.
- Beobachtete Artikel abrufen, neueste zuerst, mit aktueller Verfügbarkeit und aktuellem Preis. → D-07, D-09
- Doppeltes Merken ist ein No-op. → I-4

## Nicht im Umfang

- Benachrichtigung bei Preissenkung oder Wiederverfügbarkeit, mehrere Listen, Teilen.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-03.1](../stories/US-03.1-produkt-merken.md) | Produkt merken und beobachtete Artikel abrufen | ausformuliert |
| US-03.2 | Produkt von der Merkliste entfernen | Kandidat |
(eval):1: ===== not found
```

**⏺ Bash**

```bash
ls stories; echo "-----"; cat stories/US-03*; echo "-----"; grep -n -A12 "D-07" docs/01-entscheidungen.md; echo "----"; grep -n -i -B2 -A15 "merkl" docs/02-domaenenmodell.md | head -80; grep -n "I-4" -A3 docs/*.md | head
```

```
US-01.1-registrierung-mit-lieferadresse.md
US-01.2-weitere-adresse-hinzufuegen.md
US-01.3-adresse-aendern-loeschen.md
US-02.1-verfuegbarkeit-pruefen.md
US-03.1-produkt-merken.md
US-04.1-bestellung-aufgeben.md
US-04.2-bestellhistorie-einsehen.md
US-05.1-zahlungsstatus-pruefen.md
US-06.1-automatische-mahnpruefung.md
US-06.2-taeglicher-mahnlauf.md
US-08.1-gutschein-einloesen.md
-----
# US-03.1 Produkt merken und beobachtete Artikel abrufen

**Epic:** E03 Merkliste
**Entscheidungen:** D-07, D-09

## Story

**Als** Kunde
**möchte ich** Produkte auf meine Merkliste setzen und diese jederzeit abrufen,
**damit** ich interessante Artikel später wiederfinde und kaufen kann.

## Akzeptanzkriterien

1. **Produkt merken**
   Gegeben meine leere Merkliste,
   wenn ich am 12.11.2026 das Produkt "Kopfhörer" merke,
   dann enthält meine Merkliste einen Eintrag "Kopfhörer" mit Datum 12.11.2026.

2. **Doppeltes Merken ändert nichts**
   Gegeben "Kopfhörer" steht seit 12.11.2026 auf meiner Merkliste,
   wenn ich es am 20.11.2026 erneut merke,
   dann steht es weiterhin genau einmal auf der Liste, mit Datum 12.11.2026.

3. **Beobachtete Artikel abrufen**
   Gegeben ich habe "Kopfhörer" (12.11.) und "Kabel" (15.11.) gemerkt,
   wenn ich meine beobachteten Artikel abrufe,
   dann sehe ich "Kabel" vor "Kopfhörer", jeweils mit Merkdatum, aktuellem Listenpreis und Verfügbarkeit.

… [gekürzt, 131 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/ARCHITEKTUR.md; cat stories/US-06.2*; git log --oneline | head -20
```

````
# Architektur

Stand: Schritt 6 (Mahnwesen E06: Mahnprüfung US-06.1, täglicher Mahnlauf US-06.2, D-20). Wird mit jedem Schritt im selben Commit nachgezogen.

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
| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung` (Fehler des Ports: `BenachrichtigungNichtVersendetException`), `BestellnummernGenerator` + Port `BestellnummernZaehler`, `FachlicheEinstellungen` (`bestellsystem.*`) |
| `gemeinsam.persistence` | `SequenzBestellnummernZaehler` (`nextval('bestellnummer_seq')` über `JdbcClient`) |
| `gemeinsam.infrastruktur` | `UhrKonfiguration` (`Clock`-Bean in konfigurierter Zeitzone), `MailBenachrichtigungsversand` (`JavaMailSender`, übersetzt `MailException`), `ZeitplanKonfiguration` (`@EnableScheduling`), `MailEinstellungen` (`bestellsystem.mail.*`), `OpenApiKonfiguration` |
| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen), `fuehreMahnpruefungDurch(stichtag, bestellungen, mahnintervall)` (US-06.1, D-20: mahnt fällige Bestellungen, liefert `Optional<Mahnbenachrichtigung>`); setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`), `sperreZumMahnen` (`PESSIMISTIC_WRITE`, D-20) |
… [gekürzt, 377 Zeilen gesamt]
````

### ● Claude

Schritt 7 Merkliste dran. Werkzeug feature-dev → Skill starten.

**⏺ Skill**

```
{
 "skill": "feature-dev:feature-dev",
 "args": "Setze Schritt 7 aus docs/fahrplan.md um: Merkliste (E03), Backend und Frontend.\nUS-03.1 und US-03.2 (Kriterien vorher mit mir klären)."
}
```

```
Launching skill: feature-dev:feature-dev
```

**⏺ Bash**

```bash
find backend/src -name "*.java" -path "*katalog*" -o -name "*.java" -path "*kunde*" | sort; ls backend/src/main/resources/db/migration; ls frontend/src/app/*/; cat frontend/src/app/app.routes.ts
```

```
backend/src/main/java/de/bestellsystem/katalog/api/LagerbestandDto.java
backend/src/main/java/de/bestellsystem/katalog/api/ProduktAnlegenDto.java
backend/src/main/java/de/bestellsystem/katalog/api/ProduktController.java
backend/src/main/java/de/bestellsystem/katalog/api/ProduktDto.java
backend/src/main/java/de/bestellsystem/katalog/api/VerfuegbarkeitDto.java
backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
backend/src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java
backend/src/main/java/de/bestellsystem/kunde/api/AdressbuchController.java
backend/src/main/java/de/bestellsystem/kunde/api/AdresseDto.java
backend/src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java
backend/src/main/java/de/bestellsystem/kunde/api/KundeController.java
backend/src/main/java/de/bestellsystem/kunde/api/KundeDto.java
backend/src/main/java/de/bestellsystem/kunde/api/KundeUebersichtDto.java
backend/src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java
backend/src/main/java/de/bestellsystem/kunde/application/KundeService.java
backend/src/main/java/de/bestellsystem/kunde/domain/Adresse.java
backend/src/main/java/de/bestellsystem/kunde/domain/Email.java
backend/src/main/java/de/bestellsystem/kunde/domain/Kunde.java
backend/src/main/java/de/bestellsystem/kunde/persistence/KundeRepository.java
backend/src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java
backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
backend/src/test/java/de/bestellsystem/kunde/api/KundeApiIT.java
backend/src/test/java/de/bestellsystem/kunde/application/KundeNebenlaeufigkeitIT.java
backend/src/test/java/de/bestellsystem/kunde/domain/EmailTest.java
backend/src/test/java/de/bestellsystem/kunde/domain/KundeTest.java
backend/src/test/java/de/bestellsystem/kunde/domain/MahnpruefungTest.java
V1__bestellnummer_sequenz.sql
V2__produkt.sql
V3__kunde.sql
V4__gutschein_bestellung_zahlung.sql
V5__mahnung.sql
frontend/src/app/api/:
api
api.base.service.ts
api.module.ts
configuration.ts
encoder.ts
git_push.sh
index.ts
… [gekürzt, 118 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main && cat java/de/bestellsystem/katalog/domain/Produkt.java java/de/bestellsystem/katalog/application/ProduktService.java java/de/bestellsystem/katalog/api/ProduktDto.java java/de/bestellsystem/katalog/persistence/ProduktRepository.java java/de/bestellsystem/kunde/application/KundeService.java resources/db/migration/V5__mahnung.sql resources/db/migration/V3__kunde.sql
```

```
package de.bestellsystem.katalog.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Objects;
import java.util.UUID;

/** Artikel im Katalog mit Listenpreis und Lagerbestand (E02, D-09, D-15). */
@Entity
@Table(name = "produkt")
public class Produkt {

    public static final int TITEL_MAXIMALE_LAENGE = 200;

    @Id private UUID id;

    // Wrapper-Typ: null kennzeichnet für Spring Data ein neues Produkt trotz vergebener UUID.
    @Version private Long version;

    private String titel;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "listenpreis_cent"))
    private Geld listenpreis;

    private int lagerbestand;

    protected Produkt() {}

    public Produkt(String titel, Geld listenpreis, int lagerbestand) {
        this.id = UUID.randomUUID();
        this.titel = pruefeTitel(titel);
        this.listenpreis = pruefeListenpreis(listenpreis);
… [gekürzt, 402 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src && cat main/java/de/bestellsystem/mahnwesen/api/MahnwesenController.java main/java/de/bestellsystem/kunde/api/AdressbuchController.java main/java/de/bestellsystem/mahnwesen/domain/Mahnung.java; ls test/java/de/bestellsystem/*/ test/java/de/bestellsystem/*/*/; sed -n 1,90p test/java/de/bestellsystem/kunde/api/KundeApiIT.java
```

```
package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.mahnwesen.application.MahnService;
import de.bestellsystem.mahnwesen.application.Mahnlauf;
import de.bestellsystem.mahnwesen.application.Mahnlaufergebnis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Mahnwesen", description = "Mahnprüfung und Mahnlauf (E06)")
class MahnwesenController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final MahnService mahnService;
    private final Mahnlauf mahnlauf;
    private final Clock uhr;

    MahnwesenController(MahnService mahnService, Mahnlauf mahnlauf, Clock uhr) {
        this.mahnService = mahnService;
        this.mahnlauf = mahnlauf;
        this.uhr = uhr;
    }

    @PostMapping("/kunden/{kundenId}/mahnpruefung")
    @Operation(
            operationId = "pruefeMahnungen",
… [gekürzt, 506 Zeilen gesamt]
```

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "US-03.2 (Merkliste): Was passiert, wenn ein Kunde ein Produkt entfernt, das gar nicht auf seiner Merkliste steht? (Rest des Entwurfs: Entfernen, erneutes Merken setzt neues Datum, Listen persönlich, unbekannter Kunde/Produkt 404)",
   "header": "Entfernen",
   "multiSelect": false,
   "options": [
    {
     "label": "204, No-op (Recommended)",
     "description": "DELETE idempotent, passt zu 'doppeltes Merken ist No-op'. Unbekannter Kunde bleibt 404."
    },
    {
     "label": "404",
     "description": "Fehler 'Produkt steht nicht auf der Merkliste'."
    }
   ]
  },
  {
   "question": "Merkliste 'neueste zuerst' (D-07): Domänenmodell hat hinzugefuegtAm als Datum. Zwei Einträge am selben Tag sind dann gleichrangig. Wie sortieren?",
   "header": "Reihenfolge",
   "multiSelect": false,
   "options": [
    {
     "label": "Zeitpunkt speichern (Recommended)",
     "description": "Spalte hinzugefuegt_am als Zeitpunkt (Instant aus Clock); Sortierung exakt neueste zuerst; API liefert Datum (Europe/Berlin). Neue D-21."
    },
    {
     "label": "Datum + Titel",
     "description": "Nur Datum speichern wie im Modell; bei gleichem Datum nach Titel aufsteigend. Neue D-21."
    }
… [gekürzt, 68 Zeilen gesamt]
```

```
Your questions have been answered: "US-03.2 (Merkliste): Was passiert, wenn ein Kunde ein Produkt entfernt, das gar nicht auf seiner Merkliste steht? (Rest des Entwurfs: Entfernen, erneutes Merken setzt neues Datum, Listen persönlich, unbekannter Kunde/Produkt 404)"="204, No-op (Recommended)", "Merkliste 'neueste zuerst' (D-07): Domänenmodell hat hinzugefuegtAm als Datum. Zwei Einträge am selben Tag sind dann gleichrangig. Wie sortieren?"="Zeitpunkt speichern (Recommended)", "Merkliste-REST: Wie soll das Merken eines Produkts aussehen?"="PUT …/merkliste/{produktId} (Recommended)", "Merkliste im Frontend: Ohne Login gibt es keinen 'aktiven Kunden'. Wo wird gemerkt und entfernt?"="Eigene Merkliste-Seite (Recommended)". You can now continue with these answers in mind.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && grep -n "^## D-" docs/01-entscheidungen.md | tail -4; sed -n '/## D-20/,$p' docs/01-entscheidungen.md; ls frontend/src/app/bestellung/bestellhistorie frontend/src/app/kunde/kunde-detail; cat frontend/src/app/bestellung/bestellhistorie/bestellhistorie.ts frontend/src/app/bestellung/bestellhistorie/bestellhistorie.html
```

```
Exit code 1
216:## D-17 Checkout: Ablehnungen, Gutscheincode, erste Zahlung
237:## D-18 Checkout im Frontend und Bestellhistorie
254:## D-19 Zahlungsstatus ändern und Erstattung buchen
270:## D-20 Mahnprüfung im Detail und täglicher Mahnlauf
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
frontend/src/app/bestellung/bestellhistorie:
bestellhistorie.spec.ts
bestellhistorie.ts

frontend/src/app/kunde/kunde-detail:
kunde-detail.css
kunde-detail.html
kunde-detail.spec.ts
kunde-detail.ts
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { BestellungUebersicht, BestellungenService } from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Bestellhistorie eines Kunden mit Bezahlstatus, Reihenfolge vom Backend (US-04.2, D-19). */
… [gekürzt, 137 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && ls; cat FesteUhrKonfiguration.java 2>/dev/null; grep -rln "Clock" . | head; grep -rn "class Datenbestand" -A30 . | head -50
```

```
BenachrichtigungsAttrappe.java
BestellsystemApplicationIT.java
bestellung
Datenbestand.java
FesteUhrKonfiguration.java
gemeinsam
gutschein
katalog
kunde
mahnwesen
TestcontainersKonfiguration.java
zahlung
package de.bestellsystem;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Legt Datum und Uhrzeit für Integrationstests fest. */
@TestConfiguration(proxyBeanMethods = false)
public class FesteUhrKonfiguration {

    public static final Instant JETZT = Instant.parse("2026-11-12T09:30:00Z");
    public static final ZoneId ZEITZONE = ZoneId.of("Europe/Berlin");

    @Bean
    @Primary
    Clock festeUhr() {
        return Clock.fixed(JETZT, ZEITZONE);
    }
}
FesteUhrKonfiguration.java
BestellsystemApplicationIT.java
Datenbestand.java:6:public final class Datenbestand {
Datenbestand.java-7-
Datenbestand.java-8-    private Datenbestand() {}
Datenbestand.java-9-
… [gekürzt, 47 Zeilen gesamt]
```

### ● Claude

**Phase 4 — Architektur.** Klein, eine klare Lösung; Alternativen (Merkliste als Collection im `Kunde`-Aggregat; `findById`+`save` mit 409 bei Gleichzeitigkeit) verworfen: erste lädt ganzen Kunden bei jeder Änderung, zweite verletzt „doppelt merken = No-op“ bei Doppelklick.

**Backend** `de.bestellsystem.merkliste`
- `V6__merklisteneintrag.sql`: PK `(kunde_id, produkt_id)` (I-4), `hinzugefuegt_am TIMESTAMPTZ NOT NULL`, beide FKs `ON DELETE CASCADE` (D-07: Produkt weg → Einträge weg), Index `produkt_id`.
- `domain.Merklisteneintrag`: `@EmbeddedId MerklisteneintragId(kundeId, produktId)`, `@ManyToOne(LAZY)` Produkt (read-only Join-Spalte), `hinzugefuegtAm` Instant ohne Setter, `merkdatum(ZoneId)` → `LocalDate`.
- `persistence.MerklisteneintragRepository`: `merke` = native `INSERT … ON CONFLICT DO NOTHING` (liefert 1 neu / 0 vorhanden; nebenläufig sicher, Datum bleibt), `findeMerkliste(kundeId)` mit `JOIN FETCH produkt`, sortiert `hinzugefuegtAm desc, titel`, `findeEintrag`, `entferne` (JPQL delete).
- `application.MerklisteService`: `merke` (404 Kunde/Produkt, Zeit aus `Clock`, liefert Eintrag + `istNeu`), `merkliste` (404 Kunde), `eintrag` (404), `entferne` (404 Kunde, sonst No-op).
- `api.MerklisteController`:
  - `GET /api/v1/kunden/{kundenId}/merkliste` → `[{produktId, titel, listenpreis, istVerfuegbar, hinzugefuegtAm(Datum)}]`
  - `PUT …/merkliste/{produktId}` → 201 + `Location` neu, 200 vorhanden
  - `GET …/merkliste/{produktId}` → Eintrag oder 404 (Ziel der Location)
  - `DELETE …/merkliste/{produktId}` → 204 immer (404 nur unbekannter Kunde)
- Tests: `MerklisteApiIT` (alle AK US-03.1/03.2 über HTTP, verstellbare Test-Uhr für 12./15./20.11., Abfragenzahl Liste = 1 + Kundenprüfung), `MerklisteneintragTest` (Merkdatum in Zeitzone, Mitternachtsgrenze).

**Frontend**
- `src/app/merkliste/merkliste.ts`, Route `/kunden/:kundenId/merkliste`, Link aus Kundendetail.
- Tabelle Produkt, gemerkt am, Preis, Verfügbarkeit, „Entfernen“; darunter Produktauswahl + „Merken“. Laden/Fehler/Leer, ProblemDetail-Meldungen.
- `merkliste.spec.ts`; Client neu generieren.

**Doku:** `stories/US-03.2-…md`, Epic-Tabelle, `D-21` (Zeitpunkt statt Datum, PUT/DELETE-Semantik, Frontend-Ort), ARCHITEKTUR.md.

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Architektur Merkliste (Schritt 7): eigenes Paket merkliste, Entity Merklisteneintrag mit EmbeddedId, Merken per INSERT ON CONFLICT DO NOTHING (PUT 201/200), DELETE immer 204, Liste mit JOIN FETCH, Frontend-Seite /kunden/:id/merkliste, D-21 + US-03.2 in docs. Freigeben?",
   "header": "Freigabe",
   "multiSelect": false,
   "options": [
    {
     "label": "Freigeben (Recommended)",
     "description": "So umsetzen wie beschrieben."
    },
    {
     "label": "Änderungen",
     "description": "Erst anpassen (bitte unter 'Other' beschreiben)."
    }
   ]
  }
 ]
}
```

```
Your questions have been answered: "Architektur Merkliste (Schritt 7): eigenes Paket merkliste, Entity Merklisteneintrag mit EmbeddedId, Merken per INSERT ON CONFLICT DO NOTHING (PUT 201/200), DELETE immer 204, Liste mit JOIN FETCH, Frontend-Seite /kunden/:id/merkliste, D-21 + US-03.2 in docs. Freigeben?"="Freigeben (Recommended)". You can now continue with these answers in mind.
```

### ● Claude

Freigegeben. Erst Doku (D-21, Story), dann Tests, dann Code.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && tail -5 docs/01-entscheidungen.md | cat -A | tail -3; grep -n "D-07\|US-03" docs/*.md epics/*.md | grep -v "^docs/01" 
```

```
- Manuelle AuslM-CM-6sung, Stichtag immer heute: `POST /api/v1/kunden/{kundenId}/mahnpruefung` (ein Kunde) und `POST /api/v1/mahnlaeufe` (alle Kunden). Ohne Login sind beide offen wie die M-CM-<brige API im MVP.$
$
**Nicht im Umfang (Schritt 6):** Anzeige der Mahnungen im Frontend, Nachholen ausgefallener LM-CM-$ufe, Sperre gegen parallele LM-CM-$ufe auf mehreren Instanzen.$
docs/02-domaenenmodell.md:175:| I-4 | Merklisteneintrag eindeutig je (Kunde, Produkt) | D-07 |
epics/E03-merkliste.md:13:- Produkt auf die Merkliste setzen; Datum wird festgehalten. → D-07
epics/E03-merkliste.md:15:- Beobachtete Artikel abrufen, neueste zuerst, mit aktueller Verfügbarkeit und aktuellem Preis. → D-07, D-09
epics/E03-merkliste.md:26:| [US-03.1](../stories/US-03.1-produkt-merken.md) | Produkt merken und beobachtete Artikel abrufen | ausformuliert |
epics/E03-merkliste.md:27:| US-03.2 | Produkt von der Merkliste entfernen | Kandidat |
docs/fahrplan.md:159:**Eingaben:** `epics/E03-*.md`, `stories/US-03.1-*.md`, D-07
docs/fahrplan.md:163:US-03.1 und US-03.2 (Kriterien vorher mit mir klären).
docs/fahrplan.md:167:- US-03.1 und US-03.2 als Tests grün
```

**⏺ Bash**

```bash
cat >> docs/01-entscheidungen.md <<'EOF'

---

## D-21 Merkliste im Detail: Zeitpunkt, Merken, Entfernen

**Problem:** D-07 legt `hinzugefuegtAm` als Datum fest und verlangt „neueste zuerst“. Zwei am selben Tag gemerkte Produkte wären damit gleichrangig. Offen waren außerdem die Antwort beim Entfernen eines nicht gemerkten Produkts, die Form der Endpunkte und der Ort im Frontend ohne Login.

**Entscheidung:**
- Gespeichert wird der **Zeitpunkt** des Merkens (aus der Uhr). Die API liefert daraus das Merkdatum in `bestellsystem.zeitzone`. Sortiert wird nach Zeitpunkt absteigend, bei Gleichstand nach Titel. Präzisiert D-07.
- Merken: `PUT /api/v1/kunden/{kundenId}/merkliste/{produktId}`. Neu gemerkt: **201** + `Location`. Schon gemerkt: **200**, Datum unverändert (I-4). Auch gleichzeitiges doppeltes Merken ergibt genau einen Eintrag.
- Entfernen: `DELETE` auf denselben Pfad, immer **204**, auch wenn das Produkt nicht gemerkt war. Erneutes Merken danach setzt ein neues Datum.
- Unbekannter Kunde: **404** bei allen Merklisten-Endpunkten. Unbekanntes Produkt beim Merken: **404**.
- Frontend: eigene Seite `/kunden/:kundenId/merkliste`, verlinkt aus der Kundenansicht. Dort werden Produkte über eine Auswahl gemerkt und entfernt. Die Produktliste bleibt unverändert.

**Nicht im Umfang (Schritt 7):** „Merken“-Knopf in der Produktliste, Übernahme in den Warenkorb.
EOF
cat > stories/US-03.2-produkt-entfernen.md <<'EOF'
# US-03.2 Produkt von der Merkliste entfernen

**Epic:** E03 Merkliste
**Entscheidungen:** D-07, D-21

## Story

**Als** Kunde
**möchte ich** Produkte von meiner Merkliste entfernen,
**damit** sie nur Artikel enthält, die mich noch interessieren.

## Akzeptanzkriterien
… [gekürzt, 59 Zeilen gesamt]
```

```
 docs/01-entscheidungen.md         | 15 +++++++++++++++
 epics/E03-merkliste.md            |  4 ++--
 stories/US-03.1-produkt-merken.md |  2 +-
 3 files changed, 18 insertions(+), 3 deletions(-)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && sed -n 1,140p src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java; grep -n "Statistics\|statistik" -i src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java | head; cat src/test/resources/config/application.yml; ls src/test/resources
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
… [gekürzt, 151 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 525,545p src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java; cat src/main/java/de/bestellsystem/gemeinsam/api/GeldDto.java; cat src/main/java/de/bestellsystem/bestellung/api/BestellungUebersichtDto.java
```

```
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
        } finally {
            statistik.setStatisticsEnabled(false);
        }
    }

    private ResultActions gibAuf(String positionen, String gutscheincode) throws Exception {
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
… [gekürzt, 83 Zeilen gesamt]
```

### ● Claude

Jetzt Tests zuerst.

**⏺ Bash**

```bash
mkdir -p src/test/java/de/bestellsystem/merkliste/api && cat > src/test/java/de/bestellsystem/merkliste/api/VerstellbareUhrKonfiguration.java <<'EOF'
package de.bestellsystem.merkliste.api;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/** Uhr, die ein Test auf verschiedene Tage stellen kann (Merkdatum US-03.1, US-03.2). */
@TestConfiguration(proxyBeanMethods = false)
class VerstellbareUhrKonfiguration {

    @Bean
    @Primary
    VerstellbareUhr verstellbareUhr() {
        return new VerstellbareUhr(ZoneId.of("Europe/Berlin"));
    }

    static final class VerstellbareUhr extends Clock {

        private final ZoneId zone;
        private volatile Instant jetzt = Instant.parse("2026-11-12T09:30:00Z");

        VerstellbareUhr(ZoneId zone) {
            this.zone = zone;
        }

        void stelle(Instant zeitpunkt) {
… [gekürzt, 399 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
f=src/test/java/de/bestellsystem/merkliste/api/MerklisteApiIT.java && sed -i 's/assertThat(java.util.List.of(/assertThat(List.of(/; s/^import java.util.UUID;/import java.util.List;\nimport java.util.UUID;/' $f && grep -n "List" $f | head -3
B=src/main/java/de/bestellsystem/merkliste; mkdir -p $B/domain $B/persistence $B/application $B/api
cat > src/main/resources/db/migration/V6__merklisteneintrag.sql <<'EOF'
-- E03: Merkliste als Assoziationsklasse Kunde *–* Produkt (D-07, D-21, I-4)
CREATE TABLE merklisteneintrag (
    kunde_id        UUID        NOT NULL REFERENCES kunde (id) ON DELETE CASCADE,
    -- Produkt aus dem Katalog entfernt: Eintrag verschwindet (D-07)
    produkt_id      UUID        NOT NULL REFERENCES produkt (id) ON DELETE CASCADE,
    -- Zeitpunkt statt Datum, damit "neueste zuerst" auch innerhalb eines Tages gilt (D-21)
    hinzugefuegt_am TIMESTAMPTZ NOT NULL,
    CONSTRAINT merklisteneintrag_pk PRIMARY KEY (kunde_id, produkt_id)
);

CREATE INDEX merklisteneintrag_produkt_idx ON merklisteneintrag (produkt_id);
EOF
cat > $B/domain/MerklisteneintragId.java <<'EOF'
package de.bestellsystem.merkliste.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

/** Ein Produkt steht höchstens einmal auf der Merkliste eines Kunden (I-4). */
@Embeddable
public record MerklisteneintragId(
        @Column(name = "kunde_id") UUID kundeId, @Column(name = "produkt_id") UUID produktId) {

    public MerklisteneintragId {
        Objects.requireNonNull(kundeId, "kundeId ist erforderlich");
… [gekürzt, 214 Zeilen gesamt]
```

```
22:import java.util.List;
141:            assertThat(List.of(erster.get(), zweiter.get()))
149:            "US-03.1 AK3: neueste zuerst, mit Merkdatum, aktuellem Listenpreis und Verfügbarkeit")
```

**⏺ Bash**

```bash
B=src/main/java/de/bestellsystem/merkliste && python3 - <<'EOF'
p='src/main/java/de/bestellsystem/merkliste/application/MerklisteService.java'
s=open(p).read()
s=s.replace("""        return new Merkergebnis(eintrag(kundenId, produktId), istNeu);""","""        return new Merkergebnis(findeEintrag(kundenId, produktId), istNeu);""")
s=s.replace("""    public Merklisteneintrag eintrag(UUID kundenId, UUID produktId) {
        kundeService.pruefeVorhanden(kundenId);
        return repository""","""    public Merklisteneintrag eintrag(UUID kundenId, UUID produktId) {
        kundeService.pruefeVorhanden(kundenId);
        return findeEintrag(kundenId, produktId);
    }

    private Merklisteneintrag findeEintrag(UUID kundenId, UUID produktId) {
        return repository""")
open(p,'w').write(s)
EOF
cat > $B/api/MerklisteneintragDto.java <<'EOF'
package de.bestellsystem.merkliste.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.merkliste.domain.Merklisteneintrag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Schema(
        name = "Merklisteneintrag",
        description = "Gemerktes Produkt mit aktuellem Preis und aktueller Verfügbarkeit (D-07)",
        requiredProperties = {
… [gekürzt, 176 Zeilen gesamt]
```

```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :compileTestJava
> Task :processTestResources UP-TO-DATE
> Task :testClasses
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T11:55:50.414+01:00  INFO 1548693 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:55:50.417+01:00  INFO 1548693 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T11:55:50.425+01:00  INFO 1548693 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
> Task :test

[Incubating] Problems report is available at: file:///home/mschachner/Projects/bestellsystem/backend/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.9.1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 9s
5 actionable tasks: 4 executed, 1 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.9.1/userguide/configuration_cache_enabling.html
```

**⏺ Bash**

```bash
ls build/test-results/test/ | grep -i merk; grep -o 'tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' build/test-results/test/TEST-de.bestellsystem.merkliste.api.MerklisteApiIT.xml
```

```
TEST-de.bestellsystem.merkliste.api.MerklisteApiIT.xml
tests="17" skipped="0" failures="0" errors="0"
```

### ● Claude

17 Tests grün. OpenAPI exportieren, dann voller Build.

**⏺ Bash**

```bash
./gradlew aktualisiereOpenApi -q 2>&1 | tail -5; ./gradlew build 2>&1 | grep -E "FAIL|BUILD|tests completed" ; cd ../frontend && npm run generate:api 2>&1 | tail -2 && git status --short
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T11:56:08.137+01:00  INFO 1549809 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T11:56:08.139+01:00  INFO 1549809 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T11:56:08.145+01:00  INFO 1549809 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
BUILD SUCCESSFUL in 51s
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,supportsES6=true" exited with code 0
[bestellsystem] openapi.json
 M ../docs/01-entscheidungen.md
 M ../epics/E03-merkliste.md
 M openapi.json
 M src/app/api/.openapi-generator/FILES
 M src/app/api/api/api.ts
 M src/app/api/model/models.ts
 M ../stories/US-03.1-produkt-merken.md
?? ../backend/src/main/java/de/bestellsystem/merkliste/
?? ../backend/src/main/resources/db/migration/V6__merklisteneintrag.sql
?? ../backend/src/test/java/de/bestellsystem/merkliste/
?? src/app/api/api/merkliste.service.ts
?? src/app/api/model/merklisteneintrag.ts
?? ../stories/US-03.2-produkt-entfernen.md
```

**⏺ Bash**

```bash
grep -n "public \w*(" src/app/api/api/merkliste.service.ts src/app/api/api/produkte.service.ts | grep -v "observe\|options" ; cat src/app/api/model/merklisteneintrag.ts | sed -n '/export/,$p'; cat src/app/kunde/kunde-detail/kunde-detail.html | head -40; cat src/app/bestellung/bestellhistorie/bestellhistorie.spec.ts
```

```
src/app/api/api/merkliste.service.ts:56:  public entferneVonMerkliste(
src/app/api/api/merkliste.service.ts:67:  public entferneVonMerkliste(
src/app/api/api/merkliste.service.ts:78:  public entferneVonMerkliste(
src/app/api/api/merkliste.service.ts:89:  public entferneVonMerkliste(
src/app/api/api/merkliste.service.ts:157:  public findeMerklisteneintrag(
src/app/api/api/merkliste.service.ts:168:  public findeMerklisteneintrag(
src/app/api/api/merkliste.service.ts:179:  public findeMerklisteneintrag(
src/app/api/api/merkliste.service.ts:190:  public findeMerklisteneintrag(
src/app/api/api/merkliste.service.ts:258:  public listeMerkliste(
src/app/api/api/merkliste.service.ts:268:  public listeMerkliste(
src/app/api/api/merkliste.service.ts:278:  public listeMerkliste(
src/app/api/api/merkliste.service.ts:288:  public listeMerkliste(
src/app/api/api/merkliste.service.ts:351:  public merkeProdukt(
src/app/api/api/merkliste.service.ts:362:  public merkeProdukt(
src/app/api/api/merkliste.service.ts:373:  public merkeProdukt(
src/app/api/api/merkliste.service.ts:384:  public merkeProdukt(
src/app/api/api/produkte.service.ts:60:  public findeProdukt(
src/app/api/api/produkte.service.ts:70:  public findeProdukt(
src/app/api/api/produkte.service.ts:80:  public findeProdukt(
src/app/api/api/produkte.service.ts:90:  public findeProdukt(
src/app/api/api/produkte.service.ts:151:  public legeProduktAn(
src/app/api/api/produkte.service.ts:161:  public legeProduktAn(
src/app/api/api/produkte.service.ts:171:  public legeProduktAn(
src/app/api/api/produkte.service.ts:181:  public legeProduktAn(
src/app/api/api/produkte.service.ts:250:  public listeProdukte(
src/app/api/api/produkte.service.ts:259:  public listeProdukte(
src/app/api/api/produkte.service.ts:268:  public listeProdukte(
src/app/api/api/produkte.service.ts:277:  public listeProdukte(
src/app/api/api/produkte.service.ts:331:  public pruefeVerfuegbarkeit(
src/app/api/api/produkte.service.ts:342:  public pruefeVerfuegbarkeit(
src/app/api/api/produkte.service.ts:353:  public pruefeVerfuegbarkeit(
src/app/api/api/produkte.service.ts:364:  public pruefeVerfuegbarkeit(
src/app/api/api/produkte.service.ts:443:  public setzeLagerbestand(
src/app/api/api/produkte.service.ts:454:  public setzeLagerbestand(
src/app/api/api/produkte.service.ts:465:  public setzeLagerbestand(
src/app/api/api/produkte.service.ts:476:  public setzeLagerbestand(
export interface Merklisteneintrag {
  /**
   * Merkdatum in der Zeitzone des Shops (D-21)
   */
… [gekürzt, 200 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 1,60p src/app/kunde/kunde-detail/kunde-detail.ts; grep -n "bestellhistorie\|app-" src/app/kunde/kunde-detail/kunde-detail.html; cat src/app/kunde/kunde-detail/kunde-detail.css | head -30; grep -n "select\|formular" src/app/bestellung/checkout/checkout.html 2>/dev/null | head; ls src/app/bestellung/checkout
```

```
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable } from 'rxjs';

import { Adresse, AdresseEingabe, AdressbuchService, Kunde, KundenService } from '../../api';
import { Bestellhistorie } from '../../bestellung/bestellhistorie/bestellhistorie';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { AdresseFormular } from '../adresse-formular/adresse-formular';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** `null`: kein Formular offen, `'neu'`: Adresse hinzufügen, sonst die ID der bearbeiteten Adresse. */
type Bearbeitung = null | 'neu' | string;

@Component({
  selector: 'app-kunde-detail',
  imports: [RouterLink, AdresseFormular, Bestellhistorie],
  templateUrl: './kunde-detail.html',
  styleUrl: './kunde-detail.css',
})
export class KundeDetail implements OnInit {
  private readonly kundenService = inject(KundenService);
  private readonly adressbuchService = inject(AdressbuchService);

  readonly kundenId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly kunde = signal<Kunde | undefined>(undefined);
  protected readonly fehlermeldung = signal('');
  protected readonly bearbeitung = signal<Bearbeitung>(null);
  protected readonly sendet = signal(false);
  protected readonly aktionsfehler = signal('');

  ngOnInit(): void {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
    this.kundenService.findeKunde(this.kundenId()).subscribe({
… [gekürzt, 102 Zeilen gesamt]
```

### ● Claude

Spec zuerst, dann Komponente.

**⏺ Bash**

```bash
mkdir -p src/app/merkliste && cat > src/app/merkliste/merkliste.spec.ts <<'EOF'
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Merklisteneintrag, Produkt, provideApi } from '../api';
import { Merkliste } from './merkliste';

const MERKLISTE = '/api/v1/kunden/k1/merkliste';
const PRODUKTE = '/api/v1/produkte';

const KABEL: Merklisteneintrag = {
  produktId: 'p-kabel',
  titel: 'Kabel',
  listenpreis: { cent: 999, waehrung: 'EUR' },
  istVerfuegbar: false,
  hinzugefuegtAm: '2026-11-15',
};
const KOPFHOERER: Merklisteneintrag = {
  produktId: 'p-kopf',
  titel: 'Kopfhörer',
  listenpreis: { cent: 7999, waehrung: 'EUR' },
  istVerfuegbar: true,
  hinzugefuegtAm: '2026-11-12',
};
const PRODUKT_LISTE: Produkt[] = [
  {
    id: 'p-kabel',
    titel: 'Kabel',
… [gekürzt, 178 Zeilen gesamt]
```

```
import { Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { EMPTY, catchError, map, of, switchMap, tap } from 'rxjs';

import {
  Adresse,
  BestellungAufgeben,
  BestellungenService,
  KundeUebersicht,
  KundenService,
} from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';
import { ZAHLUNGSARTEN } from '../bezeichnungen';
import { Warenkorb, Warenkorbeintrag } from '../warenkorb';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Warenkorb und Kasse (US-04.1, US-08.1): Kunde, Lieferadresse, Zahlungsart, Gutschein. */
@Component({
  selector: 'app-checkout',
  imports: [ReactiveFormsModule, RouterLink, GeldPipe],
  templateUrl: './checkout.html',
  styleUrl: './checkout.css',
})
export class Checkout {
  private readonly kundenService = inject(KundenService);
  private readonly bestellungenService = inject(BestellungenService);
  private readonly router = inject(Router);
  private readonly formBuilder = inject(NonNullableFormBuilder);

  protected readonly warenkorb = inject(Warenkorb);
  protected readonly zahlungsarten = ZAHLUNGSARTEN;

  protected readonly formular = this.formBuilder.group({
    kundenId: ['', Validators.required],
    lieferadresseId: ['', Validators.required],
    zahlungsart: [
… [gekürzt, 66 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat > src/app/merkliste/merkliste.ts <<'EOF'
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';

import { MerklisteService, Merklisteneintrag, Produkt, ProdukteService } from '../api';
import { GeldPipe } from '../gemeinsam/geld.pipe';
import { problemMeldung } from '../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

/** Merkliste eines Kunden: ansehen, merken, entfernen (US-03.1, US-03.2, D-21). */
@Component({
  selector: 'app-merkliste',
  imports: [ReactiveFormsModule, RouterLink, DatePipe, GeldPipe],
  templateUrl: './merkliste.html',
  styleUrl: './merkliste.css',
})
export class Merkliste implements OnInit {
  private readonly merklisteService = inject(MerklisteService);
  private readonly produkteService = inject(ProdukteService);

  readonly kundenId = input.required<string>();

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly eintraege = signal<Merklisteneintrag[]>([]);
  protected readonly produkte = signal<Produkt[]>([]);
  protected readonly fehlermeldung = signal('');
… [gekürzt, 197 Zeilen gesamt]
```

```
17:      <p><a [routerLink]="['/kunden', kunde.id, 'merkliste']">Merkliste</a></p>
    [90m   |[39m                                 [31m^[39m
    [90m122|[39m   })[33m;[39m
    [90m123|[39m

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[3/4]⎯[22m[39m

[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/merkliste/merkliste.spec.ts[2m > [22mMerkliste[2m > [22mzeigt Ladefehler verständlich und lädt erneut
[31m[1mError[22m: Cannot flush a cancelled request.[39m
[90m [2m❯[22m TestRequest.flush ../darwin_arm64-fastbuild-ST-fdfa778d11ba/bin/packages/common/http/testing/src/request.ts:[2m80:12[22m[39m
[36m [2m❯[22m src/app/merkliste/merkliste.spec.ts:[2m165:30[22m[39m
    [90m163|[39m         { status[33m:[39m [34m404[39m[33m,[39m statusText[33m:[39m [32m'Not Found'[39m }[33m,[39m
    [90m164|[39m       )[33m;[39m
    [90m165|[39m     http[33m.[39m[34mexpectOne[39m([33mPRODUKTE[39m)[33m.[39m[34mflush[39m([33mPRODUKT_LISTE[39m)[33m;[39m
    [90m   |[39m                              [31m^[39m
    [90m166|[39m     [35mawait[39m fixture[33m.[39m[34mwhenStable[39m()[33m;[39m
    [90m167|[39m     expect(element.querySelector('[role="alert"]')!.textContent).toCon…

[31m[2m⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯[4/4]⎯[22m[39m


[2m Test Files [22m [1m[31m1 failed[39m[22m[2m | [22m[1m[32m13 passed[39m[22m[90m (14)[39m
[2m      Tests [22m [1m[31m4 failed[39m[22m[2m | [22m[1m[32m79 passed[39m[22m[90m (83)[39m
[2m   Start at [22m 11:57:57
[2m   Duration [22m 2.59s[2m (transform 1.12s, setup 5.62s, import 2.98s, tests 2.68s, environment 14.59s)[22m
```

**⏺ Bash**

```bash
npm test 2>&1 | grep -E "FAIL|Error|expected" | head -20
```

```
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/merkliste/merkliste.spec.ts[2m > [22mMerkliste[2m > [22mUS-03.1 AK3: zeigt Einträge in Backend-Reihenfolge mit Datum, Preis und Verfügbarkeit
[31m[1mAssertionError[22m: expected 'Kabel15.11.20269,99 €nicht verfügbar …' to contain '9,99 €'[39m
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/merkliste/merkliste.spec.ts[2m > [22mMerkliste[2m > [22mUS-03.1 AK1: merkt das gewählte Produkt und lädt die Liste neu
[31m[1mError[22m: Expected one matching request for criteria "Match URL: /api/v1/kunden/k1/merkliste/p-kopf", found none.[39m
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/merkliste/merkliste.spec.ts[2m > [22mMerkliste[2m > [22mmerken ohne Auswahl sendet nichts
[31m[1mAssertionError[22m: expected '← Zum KundenMerklisteProduktBitte wäh…' to contain 'Bitte ein Produkt wählen.'[39m
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/merkliste/merkliste.spec.ts[2m > [22mMerkliste[2m > [22mzeigt Ladefehler verständlich und lädt erneut
[31m[1mError[22m: Cannot flush a cancelled request.[39m
```

**⏺ Bash**

```bash
grep -c $' ' src/app/bestellung/bestellhistorie/bestellhistorie.spec.ts; python3 - <<'EOF'
p='src/app/merkliste/merkliste.spec.ts'
s=open(p).read()
s=s.replace("'9,99 €'","'9,99 €'").replace("'79,99 €'","'79,99 €'")
s=s.replace("""        { status: 404, statusText: 'Not Found' },
      );
    http.expectOne(PRODUKTE).flush(PRODUKT_LISTE);
    await fixture.whenStable();""","""        { status: 404, statusText: 'Not Found' },
      );
    expect(http.expectOne(PRODUKTE).cancelled).toBe(true);
    await fixture.whenStable();""")
open(p,'w').write(s)
p='src/app/merkliste/merkliste.ts'
s=open(p).read()
s=s.replace("import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';","import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';")
s=s.replace("""  protected readonly produktId = new FormControl('', {
    nonNullable: true,
    validators: Validators.required,
  });
""","""  protected readonly formular = new FormGroup({
    produktId: new FormControl('', { nonNullable: true, validators: Validators.required }),
  });
  protected readonly produktId = this.formular.controls.produktId;
""")
s=s.replace("() => this.produktId.reset(),","() => this.formular.reset(),")
open(p,'w').write(s)
p='src/app/merkliste/merkliste.html'
s=open(p).read()
s=s.replace('<form class="merken" (ngSubmit)="merken()" novalidate>','<form class="merken" [formGroup]="formular" (ngSubmit)="merken()" novalidate>')
s=s.replace('<select id="produktId" [formControl]="produktId">','<select id="produktId" formControlName="produktId">')
… [gekürzt, 33 Zeilen gesamt]
```

```
2
[2m      Tests [22m [1m[32m83 passed[39m[22m[90m (83)[39m
```

### ● Claude

Frontend grün (83). Build prüfen, dann Browser-Nachweis.

**⏺ Bash**

```bash
npm run build 2>&1 | tail -4; cd .. && docker compose up -d 2>&1 | tail -2; (curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/produkte; curl -s -o /dev/null -w "%{http_code}\n" localhost:4200) 
```

```
Application bundle generation complete. [2.677 seconds] - 2026-11-12T10:58:30.997Z

Output location: /home/mschachner/Projects/bestellsystem/frontend/dist/frontend

 Container bestellsystem-postgres-1 Running 
 Container bestellsystem-mailpit-1 Running 
200
200
```

**⏺ Bash**

```bash
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/kunden/00000000-0000-0000-0000-000000000000/merkliste; ps -eo pid,lstart,args | grep -E "bootRun|BestellsystemApplication|ng serve" | grep -v grep | cut -c1-160
```

```
404
1170632  Do Nov 12 10:50:31 2026 sh -c ng serve --proxy-config proxy.conf.json
1170633  Do Nov 12 10:50:31 2026 ng serve --proxy-config proxy.conf.json (frontend)
1222375  Do Nov 12 10:58:43 2026 /usr/bin/zsh -c source /home/mschachner/.claude/shell-snapshots/snapshot-zsh-1794477029925-0eyh9h.sh 2>/dev/null || true && set
1222376  Do Nov 12 10:58:43 2026 java -Xmx64m -Xms64m -Dorg.gradle.appname=gradlew -jar /home/mschachner/Projects/bestellsystem/backend/gradle/wrapper/gradle-wr
1222768  Do Nov 12 10:58:45 2026 /usr/lib/jvm/java-21-openjdk-amd64/bin/java -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en
```

**⏺ Bash**

```bash
curl -s localhost:8080/api/v1/kunden/00000000-0000-0000-0000-000000000000/merkliste; echo; curl -s localhost:8080/api/v1/kunden | head -c 400
```

```
{"type":"about:blank","title":"Not Found","status":404,"detail":"No static resource api/v1/kunden/00000000-0000-0000-0000-000000000000/merkliste.","instance":"/api/v1/kunden/00000000-0000-0000-0000-000000000000/merkliste"}
[{"id":"9df8828b-8161-412a-9368-876e0c897dc9","vorname":"Anna","nachname":"Browser","email":"browser-1794475430333@example.de"},{"id":"2260b770-c2ec-4a6d-b70b-82768a786348","vorname":"Mahn","nachname":"Test","email":"mahntest-1794480480@example.de"},{"id":"acc88d57-b104-4905-ae33-00dcc2b5178a","vorname":"Test","nachname":"Zahlung","email":"e2e-11547@example.de"},{"id":"b5b076fb-aa04-44ec-a12d-060a
```

### ● Claude

Laufendes Backend ist alter Stand (aus früherer Sitzung, ohne Merkliste). Neu starten.

**⏺ Bash**

```bash
kill 1222375 1222376 1222768 2>/dev/null; sleep 3; ps -p 1222768 >/dev/null && echo still || echo stopped
```

```
stopped
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew bootRun > /tmp/bootrun-merkliste.log 2>&1
```

```
Command running in background with ID: b6lqd6d2u. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/b6lqd6d2u.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ ToolSearch**

```
{
 "query": "select:mcp__claude-in-chrome__tabs_context_mcp,mcp__claude-in-chrome__navigate,mcp__claude-in-chrome__computer,mcp__claude-in-chrome__read_page,mcp__claude-in-chrome__tabs_create_mcp,mcp__claude-in-chrome__find,mcp__claude-in-chrome__form_input,mcp__claude-in-chrome__get_page_text",
 "max_results": 8
}
```

```
[tool_reference]
[tool_reference]
[tool_reference]
[tool_reference]
[tool_reference]
[tool_reference]
[tool_reference]
[tool_reference]
```

**⏺ Bash**

```bash
for i in $(seq 1 40); do curl -s -o /dev/null localhost:8080/api/v1/produkte && break; sleep 2; done; curl -s localhost:8080/api/v1/kunden | head -c 150; echo; curl -s localhost:8080/api/v1/produkte | head -c 300
```

```
[{"id":"9df8828b-8161-412a-9368-876e0c897dc9","vorname":"Anna","nachname":"Browser","email":"browser-1794475430333@example.de"},{"id":"2260b770-c2ec-4
[{"id":"cb3b0de5-c224-478b-bc12-3d65a032e538","titel":"Buch 4b","listenpreis":{"cent":1250,"waehrung":"EUR"},"lagerbestand":4,"istVerfuegbar":true},{"id":"4feef0af-b2a0-43de-b7a1-7f35fc88b27b","titel":"E2E Lampe","listenpreis":{"cent":10000,"waehrung":"EUR"},"lagerbestand":4,"istVerfuegbar":true},{"
```

**⏺ mcp__claude-in-chrome__tabs_context_mcp**

```
{
 "createIfEmpty": true
}
```

```
Browser extension is not connected. Please ensure the Claude browser extension is installed and running (https://claude.ai/chrome), and that you are logged into claude.ai with the same account as Claude Code. If this is your first time connecting to Chrome, you may need to restart Chrome for the installation to take effect. If you continue to experience issues, please report a bug: https://github.com/anthropics/claude-code/issues/new?labels=bug,claude-in-chrome
```

### ● Claude

Chrome-Erweiterung nicht verbunden. Ersatz: Durchstich über Angular-Dev-Server-Proxy (:4200 → :8080) gegen echte DB.

**⏺ Bash**

```bash
K=9df8828b-8161-412a-9368-876e0c897dc9; P=cb3b0de5-c224-478b-bc12-3d65a032e538; P2=4feef0af-b2a0-43de-b7a1-7f35fc88b27b; U=localhost:4200/api/v1/kunden/$K/merkliste
curl -s -o /dev/null -w "PUT1 %{http_code}\n" -X PUT $U/$P; curl -s -o /dev/null -w "PUT2 %{http_code}\n" -X PUT $U/$P2; curl -s -o /dev/null -w "PUT1 again %{http_code}\n" -X PUT $U/$P
curl -s $U; echo; curl -s -o /dev/null -w "DELETE %{http_code}\n" -X DELETE $U/$P; curl -s $U; echo; curl -s -o /dev/null -w "page %{http_code}\n" localhost:4200/kunden/$K/merkliste
```

```
PUT1 201
PUT2 201
PUT1 again 200
[{"produktId":"4feef0af-b2a0-43de-b7a1-7f35fc88b27b","titel":"E2E Lampe","listenpreis":{"cent":10000,"waehrung":"EUR"},"istVerfuegbar":true,"hinzugefuegtAm":"2026-11-12"},{"produktId":"cb3b0de5-c224-478b-bc12-3d65a032e538","titel":"Buch 4b","listenpreis":{"cent":1250,"waehrung":"EUR"},"istVerfuegbar":true,"hinzugefuegtAm":"2026-11-12"}]
DELETE 204
[{"produktId":"4feef0af-b2a0-43de-b7a1-7f35fc88b27b","titel":"E2E Lampe","listenpreis":{"cent":10000,"waehrung":"EUR"},"istVerfuegbar":true,"hinzugefuegtAm":"2026-11-12"}]
page 200
```

### ● Claude

Stack läuft, API über Proxy korrekt. Jetzt Phase 6 Review.

**⏺ Agent**

```
{
 "description": "Review Merkliste changes",
 "subagent_type": "feature-dev:code-reviewer",
 "prompt": "Review uncommitted changes in /home/mschachner/Projects/bestellsystem (run `git status` and `git diff`; new untracked files: backend/src/main/java/de/bestellsystem/merkliste/**, backend/src/main/resources/db/migration/V6__merklisteneintrag.sql, backend/src/test/java/de/bestellsystem/merkliste/**, frontend/src/app/merkliste/**, stories/US-03.2-produkt-entfernen.md). Ignore generated code in frontend/src/app/api/ and frontend/openapi.json.\n\nFeature: Merkliste (wishlist) per customer. Spec: stories/US-03.1-produkt-merken.md, stories/US-03.2-produkt-entfernen.md, decisions D-07 and D-21 in docs/01-entscheidungen.md. Project rules in CLAUDE.md (read it). Design is approved: PUT merken via native INSERT ON CONFLICT DO NOTHING (201 new / 200 existing), DELETE always 204, list with JOIN FETCH sorted by timestamp desc then title, own frontend page /kunden/:kundenId/merkliste.\n\nCheck: correctness bugs (JPA mapping of record @EmbeddedId + read-only @ManyToOne on same column, persistence-context staleness after native insert/JPQL delete, transaction boundaries, timezone conversion), violations of CLAUDE.md conventions, test gaps against acceptance criteria, frontend issues (state handling, error display). All tests currently pass (backend ./gradlew build, frontend npm test). Report only high-confidence issues, each as `path:line: severity: problem. fix.` Max ~10 findings. Do not edit files."
}
```

```
Async agent launched successfully. (This tool result is internal metadata — never quote or paste any part of it, including the agentId below, into a user-facing reply.)
agentId: a4a3dfe058211ab5c (internal ID - do not mention to user. Use SendMessage with to: 'a4a3dfe058211ab5c', summary: '<5-10 word recap>' to continue this agent.)
The agent is working in the background. You will be notified automatically when it completes. You know nothing about its results until that notification arrives — do not report, assume, or predict them; continue other work or respond to the user in the meantime.
Do not duplicate this agent's work — avoid working with the same files or topics it is using.
output_file: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/a4a3dfe058211ab5c.output
Do NOT Read or tail this file via the shell tool — it is the full subagent JSONL transcript and reading it will overflow your context. If the user asks for progress, say the agent is still running; you'll get a completion notification.
```

### ● Claude

Meanwhile ARCHITEKTUR.md nachziehen.

**⏺ Bash**

````bash
python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
def rep(a,b):
    global s
    assert a in s, a[:60]
    s=s.replace(a,b,1)
rep("Stand: Schritt 6 (Mahnwesen E06: Mahnprüfung US-06.1, täglicher Mahnlauf US-06.2, D-20).",
    "Stand: Schritt 7 (Merkliste E03: US-03.1, US-03.2, D-21).")
rep("| `bestellung.api` | `BestellungController`",
"""| `merkliste.domain` | `Merklisteneintrag` (Entity, Assoziationsklasse Kunde *–* Produkt, D-07): `@EmbeddedId MerklisteneintragId` (Record `kundeId`, `produktId`, I-4), Produkt LAZY (nur lesend auf `produkt_id`), `hinzugefuegtAm` als `Instant`, `merkdatum(zeitzone)` (D-21); wird nie über `persist` angelegt |
| `merkliste.persistence` | `MerklisteneintragRepository`: `merke` (native `INSERT … ON CONFLICT DO NOTHING`, 1 = neu, 0 = schon gemerkt), `findeMerkliste` (`JOIN FETCH` Produkt, Zeitpunkt absteigend, dann Titel), `findeEintrag`, `entferne` (JPQL-Delete, No-op ohne Eintrag) |
| `merkliste.application` | `MerklisteService`: `merke` (404 Kunde/Produkt, Zeitpunkt aus `Clock`, liefert `Merkergebnis(eintrag, istNeu)`), `merkliste`, `eintrag` (404), `entferne` (404 nur für unbekannten Kunden) |
| `merkliste.api` | `MerklisteController`, DTO `MerklisteneintragDto` (`produktId`, `titel`, aktueller `listenpreis`, `istVerfuegbar`, Merkdatum `hinzugefuegtAm` in `bestellsystem.zeitzone`) |
| `bestellung.api` | `BestellungController`""")
rep("| POST | `/api/v1/mahnlaeufe` |",
"""| GET | `/api/v1/kunden/{kundenId}/merkliste` | beobachtete Artikel, neueste zuerst, mit Merkdatum, aktuellem Preis und Verfügbarkeit; leere Liste; 404 unbekannter Kunde (US-03.1) |
| PUT | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | merken: 201 + `Location` neu, 200 schon gemerkt (Datum bleibt, I-4), 404 Kunde oder Produkt (D-21) |
| GET | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | ein Eintrag; 404 Kunde unbekannt oder nicht gemerkt |
| DELETE | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | 204, auch wenn nicht gemerkt; 404 unbekannter Kunde (US-03.2, D-21) |
| POST | `/api/v1/mahnlaeufe` |""")
rep("## Querschnitt\n",
"""## Datenfluss (Merkliste, D-21)

```
PUT /kunden/{id}/merkliste/{produktId} ─ MerklisteController
  → MerklisteService.merke (@Transactional)
      KundeService.pruefeVorhanden, ProduktService.finde                 404
      MerklisteneintragRepository.merke(…, Instant aus Clock)            INSERT … ON CONFLICT DO NOTHING
      findeEintrag (JOIN FETCH Produkt)
… [gekürzt, 55 Zeilen gesamt]
````

```
 docs/ARCHITEKTUR.md | 31 +++++++++++++++++++++++++++++--
 1 file changed, 29 insertions(+), 2 deletions(-)
```

### ● Claude

Warte auf Review-Ergebnis.

### ❯ Nutzer

> <task-notification>
> <task-id>a4a3dfe058211ab5c</task-id>
> <tool-use-id>toolu_01EpHZcPZr8eRuhguUxziiQK</tool-use-id>
> <output-file>/tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/a4a3dfe058211ab5c.output</output-file>
> <status>completed</status>
> <summary>Agent "Review Merkliste changes" finished</summary>
> <note>A task-notification fires each time this agent stops with no live background children of its own. The user can send it another message and resume it, so the same task-id may notify more than once.</note>
> <result>I reviewed the uncommitted Merkliste change: the backend in `merkliste/{domain,persistence,application,api}`, the migration `V6__merklisteneintrag.sql`, `MerklisteApiIT`, `VerstellbareUhrKonfiguration`, and the frontend component with its spec and routing. I did not run the tests; I read the code against US-03.1, US-03.2, D-07, D-21 and CLAUDE.md. I found no issues at confidence 80 or higher.
>
> What I checked and found sound:
>
> - **Mapping:** the record `@EmbeddedId` with a read-only `@ManyToOne(LAZY)` on `produkt_id` (`insertable=false, updatable=false`) is valid. The entity is never persisted through Hibernate, so there is no conflict between the two mappings on that column.
> - **Persistence-context staleness:**
>   - In `MerklisteService.merke` the entity is only loaded after the native insert, by the JOIN FETCH query.
>   - In `entferne` no `Merklisteneintrag` has been loaded in that transaction, so the JPQL delete cannot leave a stale entity.
>   - `@Modifying` without `clearAutomatically` is therefore fine.
> - **Concurrency (D-21):** `INSERT … ON CONFLICT DO NOTHING` under READ COMMITTED lets the second transaction block until the first commits. It then returns 0, so the 201/200 split holds, and `gleichzeitigesMerken` covers it.
> - **Transactions:** `merke` and `entferne` are write transactions, and the class-level `readOnly` covers the reads.
> - **Timezone:** `hinzugefuegtAm` is stored as `Instant` in a `TIMESTAMPTZ` column. The DTO converts it with `Clock.getZone()`, which production builds from `bestellsystem.zeitzone` in `UhrKonfiguration`. The test with 23:30Z giving 15.11 covers the conversion.
> - **Acceptance criteria:** every acceptance criterion of US-03.1 (AK1–5) and US-03.2 (AK1–5) has a named test. There are also tests for the same-day ordering, the N+1 query count, the cascade on product deletion (D-07), and 400 on an invalid ID.
> - **Frontend:** it shows loading, error with retry, and empty states. Action errors show the ProblemDetail message. `sendet` guards against double submits, and the list is reloaded from the backend after each action.
> - **Documentation:** `docs/ARCHITEKTUR.md` already describes Merkliste, and D-21 is present.
>
> Below-threshold notes, not findings:
>
> - `/home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/merkliste/application/MerklisteService.java:41`: if the product or customer is deleted between the existence check and the insert, the foreign key fails and the response is a 500 instead of a 404. This race is rare, and the repository has no mapping for it.
> - `/home/mschachner/Projects/bestellsystem/docs/fahrplan.md:156`: step 7 is still `[ ]`. CLAUDE.md says a step is done only when it is ticked off and committed, so tick it off in the commit.
> - `/home/mschachner/Projects/bestellsystem/frontend/src/app/kunde/kunde-detail/kunde-detail.html:17`: the new Merkliste link has no test in `kunde-detail.spec.ts`.</result>
> <usage><subagent_tokens>48068</subagent_tokens><tool_uses>21</tool_uses><duration_ms>45584</duration_ms></usage>
> </task-notification>

### ● Claude

Review: keine Befunde über Schwelle. Kleinen Link-Test in Kundendetail nachziehen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend && grep -n "it(\|href\|flush" src/app/kunde/kunde-detail/kunde-detail.spec.ts | head -20
```

```
61:    http.expectOne('/api/v1/kunden/k1').flush(kunde);
63:    http.expectOne('/api/v1/kunden/k1/bestellungen').flush([]);
86:  it('zeigt Laden, dann Stammdaten und Adressbuch mit Kennzeichen', async () => {
99:  it('zeigt einen unbekannten Kunden als Fehler', async () => {
102:      .flush(
112:  it('US-01.2 AK1: fügt eine Adresse hinzu und zeigt sie im Adressbuch', async () => {
132:    anfrage.flush(WIEN, { status: 201, statusText: 'Created' });
139:  it('US-01.2 AK2: sendet keine Adresse ohne Kennzeichen', async () => {
154:  it('US-01.3 AK1: ändert eine Adresse mit vorbelegtem Formular', async () => {
167:    anfrage.flush({ ...WIEN, anschrift: { ...WIEN.anschrift, stadt: 'Graz' } });
172:  it('US-01.3 AK2: zeigt die Ablehnung beim Entziehen des Lieferkennzeichens im Formular', async () => {
180:    http.expectOne('/api/v1/kunden/k1/adressen/a1').flush(
194:  it('US-01.3 AK4: löscht eine Adresse aus dem Adressbuch', async () => {
200:    anfrage.flush(null, { status: 204, statusText: 'No Content' });
205:  it('US-01.3 AK5: zeigt die Ablehnung beim Löschen der letzten Lieferadresse', async () => {
209:    http.expectOne('/api/v1/kunden/k1/adressen/a1').flush(
```

**⏺ Bash**

```bash
sed -n 55,98p src/app/kunde/kunde-detail/kunde-detail.spec.ts
```

```
  async function absenden(): Promise<void> {
    element.querySelector('form')!.dispatchEvent(new Event('submit'));
    await fixture.whenStable();
  }

  async function geladen(kunde: Kunde = ANNA): Promise<void> {
    http.expectOne('/api/v1/kunden/k1').flush(kunde);
    await fixture.whenStable();
    http.expectOne('/api/v1/kunden/k1/bestellungen').flush([]);
    await fixture.whenStable();
  }

  beforeEach(async () => {
    TestBed.configureTestingModule({
      imports: [KundeDetail],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(KundeDetail);
    fixture.componentRef.setInput('kundenId', 'k1');
    element = fixture.nativeElement;
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('zeigt Laden, dann Stammdaten und Adressbuch mit Kennzeichen', async () => {
    expect(element.textContent).toContain('Kunde wird geladen');
    await geladen();

    expect(element.querySelector('h1')!.textContent).toContain('Anna Muster');
    expect(element.textContent).toContain('anna@example.de');
    expect(karten().length).toBe(2);
    expect(karten()[0].textContent).toContain('10115 Berlin');
    expect(karten()[0].textContent).toContain('Lieferadresse');
… [gekürzt, 43 Zeilen gesamt]
```

**⏺ Bash**

```bash
grep -n "ANNA: Kunde\|id: 'k1'" src/app/kunde/kunde-detail/kunde-detail.spec.ts | head -3; python3 - <<'EOF'
p='src/app/kunde/kunde-detail/kunde-detail.spec.ts'
s=open(p).read()
s=s.replace("""  it('zeigt einen unbekannten Kunden als Fehler'""","""  it('US-03.1: verlinkt die Merkliste des Kunden', async () => {
    await geladen();

    const link = [...element.querySelectorAll('a')].find((a) => a.textContent === 'Merkliste');
    expect(link!.getAttribute('href')).toBe('/kunden/k1/merkliste');
  });

  it('zeigt einen unbekannten Kunden als Fehler'""",1)
open(p,'w').write(s)
EOF
npm test 2>&1 | grep -E "FAIL|Error|Tests "
```

```
21:const ANNA: Kunde = {
22:  id: 'k1',
[2m      Tests [22m [1m[32m84 passed[39m[22m[90m (84)[39m
```

### ● Claude

Frontend 84 grün. Letzter Backend-Build, dann Commit.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew build 2>&1 | grep -E "FAIL|BUILD"; cd .. && git add docs/01-entscheidungen.md docs/ARCHITEKTUR.md epics/E03-merkliste.md stories/US-03.1-produkt-merken.md stories/US-03.2-produkt-entfernen.md backend/src/main/java/de/bestellsystem/merkliste backend/src/main/resources/db/migration/V6__merklisteneintrag.sql backend/src/test/java/de/bestellsystem/merkliste frontend/openapi.json frontend/src/app/api frontend/src/app/merkliste frontend/src/app/app.routes.ts frontend/src/app/kunde/kunde-detail && git status --short
```

```
BUILD SUCCESSFUL in 930ms
A  backend/src/main/java/de/bestellsystem/merkliste/api/MerklisteController.java
A  backend/src/main/java/de/bestellsystem/merkliste/api/MerklisteneintragDto.java
A  backend/src/main/java/de/bestellsystem/merkliste/application/MerklisteService.java
A  backend/src/main/java/de/bestellsystem/merkliste/domain/Merklisteneintrag.java
A  backend/src/main/java/de/bestellsystem/merkliste/domain/MerklisteneintragId.java
A  backend/src/main/java/de/bestellsystem/merkliste/persistence/MerklisteneintragRepository.java
A  backend/src/main/resources/db/migration/V6__merklisteneintrag.sql
A  backend/src/test/java/de/bestellsystem/merkliste/api/MerklisteApiIT.java
A  backend/src/test/java/de/bestellsystem/merkliste/api/VerstellbareUhrKonfiguration.java
M  docs/01-entscheidungen.md
M  docs/ARCHITEKTUR.md
M  epics/E03-merkliste.md
M  frontend/openapi.json
M  frontend/src/app/api/.openapi-generator/FILES
M  frontend/src/app/api/api/api.ts
A  frontend/src/app/api/api/merkliste.service.ts
A  frontend/src/app/api/model/merklisteneintrag.ts
M  frontend/src/app/api/model/models.ts
M  frontend/src/app/app.routes.ts
M  frontend/src/app/kunde/kunde-detail/kunde-detail.html
M  frontend/src/app/kunde/kunde-detail/kunde-detail.spec.ts
A  frontend/src/app/merkliste/merkliste.css
A  frontend/src/app/merkliste/merkliste.html
A  frontend/src/app/merkliste/merkliste.spec.ts
A  frontend/src/app/merkliste/merkliste.ts
M  stories/US-03.1-produkt-merken.md
A  stories/US-03.2-produkt-entfernen.md
```

**⏺ Bash**

```bash
git commit -q -F - <<'EOF'
Add wishlist with save, remove and frontend page (E03)

US-03.1 and US-03.2 with D-21: saved timestamp, PUT 201/200 via
INSERT ON CONFLICT, DELETE always 204, page /kunden/:id/merkliste.

Open "Fertig wenn" item of step 7: saving and removing in the browser
not yet verified by hand (Chrome extension not connected); API checked
through the dev-server proxy instead.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1; git status --short
```

```
d928f94 Add wishlist with save, remove and frontend page (E03)
```

### ● Claude

Ich habe Schritt 7 (Merkliste) gebaut und als `d928f94` committet. Im Fahrplan ist er noch **nicht abgehakt**: Merken und Entfernen im Browser habe ich nicht von Hand geprüft. Die Chrome-Erweiterung war nicht verbunden.

**Was jetzt geht:**
- Ein Kunde kann Produkte auf seine Merkliste setzen, sie abrufen (neueste zuerst, mit Merkdatum, aktuellem Preis und Verfügbarkeit) und wieder entfernen.
- Doppeltes Merken ändert nichts, das erste Datum bleibt. Das gilt auch bei gleichzeitigem Merken.
- Entfernen eines nicht gemerkten Produkts gibt keinen Fehler. Unbekannter Kunde oder unbekanntes Produkt ergibt 404.
- Wird ein Produkt aus dem Katalog gelöscht, verschwindet es von allen Merklisten (D-07).
- Im Frontend gibt es die Seite `/kunden/:kundenId/merkliste`, verlinkt aus der Kundenansicht. Dort wählt man ein Produkt und merkt es oder entfernt Einträge. Laden, Fehler und leere Liste werden angezeigt.

**Deine Entscheidungen** stehen als neue D-21 in `docs/01-entscheidungen.md`: Zeitpunkt statt Datum speichern, Merken per `PUT` (201 neu / 200 schon da), Entfernen immer 204, eigene Seite im Frontend. US-03.2 liegt neu in `stories/`.

**Tests:**
- Backend: `./gradlew build` grün, mit 17 neuen Tests in `MerklisteApiIT`. Sie decken alle Akzeptanzkriterien von US-03.1 und US-03.2 ab. Dazu prüfen sie gleichzeitiges Merken und das Merkdatum in Berliner Zeit. Sie prüfen auch, dass der Abruf immer 2 Datenbankabfragen braucht, egal wie lang die Liste ist.
- Frontend: `npm test` grün (84 Tests), `npm run build` ok.

Ein Review-Agent hat nichts Ernstes gefunden. Seinen Hinweis auf einen fehlenden Test für den Merkliste-Link habe ich ergänzt.

**Offen:** Statt im Browser habe ich die API über den Proxy des Frontend-Servers geprüft (`:4200`), gegen die echte Datenbank. Merken 201, nochmal 200, Liste korrekt, Entfernen 204, Seite lädt. Dafür habe ich das laufende Backend neu gestartet; es lief noch mit altem Code ohne Merkliste. Für den Haken bitte selbst durchklicken: http://localhost:4200/kunden → Kunde öffnen → „Merkliste“ → Produkt merken, Verfügbarkeit ansehen, entfernen. Oder Chrome verbinden, dann prüfe ich es. Danach hake ich Schritt 7 im Fahrplan ab.

**Danach kommt** Schritt 8: Versand (E07).

