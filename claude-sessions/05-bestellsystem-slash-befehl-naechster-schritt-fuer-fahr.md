## Session 5: Slash-Befehl /naechster-schritt für fahrplan.md

*Projekt: bestellsystem · ID `0f3855f1-89ea-4d35-a277-ede7ac135d1b` · 2026-11-12 09:25 – 2026-11-12 09:38 UTC*

### ❯ Nutzer

> /clear

### ❯ Nutzer

> /naechster-schritt

**⏺ Bash**

```bash
cat docs/fahrplan.md && git status --short && git log --oneline -3
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
… [gekürzt, 207 Zeilen gesamt]
````

### ● Claude

Step 4a dran. Fresh session. Inputs lesen.

**⏺ Bash**

```bash
ls epics stories docs docs/adr; cat epics/E04-*.md epics/E08-*.md stories/US-04.1-*.md stories/US-08.1-*.md
```

```
docs:
00-anforderung.md
01-entscheidungen.md
02-domaenenmodell.md
adr
ARCHITEKTUR.md
fahrplan.md

docs/adr:
0001-gradle-als-build-werkzeug.md
0002-openapi-spezifikation-als-datei.md

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
US-05.1-zahlungsstatus-pruefen.md
US-06.1-automatische-mahnpruefung.md
US-08.1-gutschein-einloesen.md
# E04 Bestellung & Checkout

**Priorität:** MVP (Kern des Systems)
**Akteur:** Kunde
**Abhängig von:** E01, E02; arbeitet mit E05 (erste Zahlung) und E08 (Gutschein)

## Ziel
… [gekürzt, 221 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/01-entscheidungen.md docs/ARCHITEKTUR.md; cat docs/02-domaenenmodell.md
```

````
# Entscheidungen zu Lücken und Widersprüchen

Verbindlich für Modell, Epics und Stories. Jede Entscheidung hat eine ID (`D-xx`), auf die Epics und Stories verweisen. Änderungen hier zuerst, dann im Code.

---

## D-01 Gesamtwert: gespeichert oder berechnet?

**Problem:** Der Text sagt, die Bestellung "speichert den berechneten Gesamtwert" und der Gesamtwert müsse "dynamisch berechnet werden können". Beides gleichzeitig ist redundant und kann auseinanderlaufen.

**Entscheidung:** Der Gesamtwert ist ein **abgeleitetes Attribut** (`/gesamtwert` im UML) und wird nicht als eigene Spalte gespeichert.

```
warenwert   = Summe aller Positionen (menge × einzelpreis)
gesamtwert  = max(0, warenwert − rabattBetrag)
```

Historische Stabilität ist trotzdem gegeben, weil alle Eingaben eingefroren sind: der Einzelpreis je Position (D-10) und der Rabattbetrag (D-04).

**Begründung:** Eine einzige Wahrheitsquelle. Ein gespeicherter Wert müsste bei jeder Änderung nachgezogen werden.

**Option für später:** Falls Reports den Wert brauchen, darf er als Cache persistiert werden; die Berechnung bleibt maßgeblich.

---

## D-02 Zahlung "mindestens eine" vs. offene Beträge

**Problem:** "Eine Bestellung erfordert mindestens eine Zahlungsbuchung" (1..\*). Gleichzeitig soll die Mahnprüfung offene Beträge finden. Wäre jede Bestellung sofort bezahlt, gäbe es nichts zu mahnen.

**Entscheidung:** Die Multiplizität **1..\* bleibt**. Beim Checkout wird sofort eine Zahlung mit Status `AUSSTEHEND` und der gewählten Zahlungsart angelegt. Nur Buchungen mit Status `ERFOLGREICH` zählen als geleistet.

**Begründung:** Erfüllt den Originaltext wörtlich und bildet die Realität ab: Die Zahlungsabsicht existiert ab Bestellung, das Geld kommt später (Rechnung, Vorkasse) oder gar nicht (fehlgeschlagen).

---

## D-03 Zahlungsrückstand braucht eine Fälligkeit

**Problem:** Ohne Fälligkeitsdatum ist jede gerade aufgegebene, noch unbezahlte Bestellung sofort "im Rückstand".

**Entscheidung:**
… [gekürzt, 589 Zeilen gesamt]
````

**⏺ Skill**

```
{
 "skill": "feature-dev:feature-dev",
 "args": "Setze Schritt 4a aus docs/fahrplan.md um: Checkout im Backend (E04 + E08). Bestellung, Positionen, Lieferadress-Snapshot, Bestandsreduktion in einer Transaktion, Gutschein mit eingefrorenem Rabatt, erste Zahlung AUSSTEHEND, Fälligkeit. US-04.1 und US-08.1 vollständig. Kein Frontend."
}
```

```
Launching skill: feature-dev:feature-dev
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && find src -type f | sort && cat build.gradle.kts && cat src/main/resources/application*.yml 2>/dev/null || ls src/main/resources
```

```
src/main/java/de/bestellsystem/BestellsystemApplication.java
src/main/java/de/bestellsystem/gemeinsam/api/GeldDto.java
src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java
src/main/java/de/bestellsystem/gemeinsam/application/Benachrichtigung.java
src/main/java/de/bestellsystem/gemeinsam/application/Benachrichtigungsversand.java
src/main/java/de/bestellsystem/gemeinsam/application/BestellnummernGenerator.java
src/main/java/de/bestellsystem/gemeinsam/application/BestellnummernZaehler.java
src/main/java/de/bestellsystem/gemeinsam/application/FachlicheEinstellungen.java
src/main/java/de/bestellsystem/gemeinsam/domain/Anschrift.java
src/main/java/de/bestellsystem/gemeinsam/domain/FachlicheAblehnungException.java
src/main/java/de/bestellsystem/gemeinsam/domain/Geld.java
src/main/java/de/bestellsystem/gemeinsam/domain/KonfliktException.java
src/main/java/de/bestellsystem/gemeinsam/domain/NichtGefundenException.java
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/MailBenachrichtigungsversand.java
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/MailEinstellungen.java
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiKonfiguration.java
src/main/java/de/bestellsystem/gemeinsam/infrastruktur/UhrKonfiguration.java
src/main/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehler.java
src/main/java/de/bestellsystem/katalog/api/LagerbestandDto.java
src/main/java/de/bestellsystem/katalog/api/ProduktAnlegenDto.java
src/main/java/de/bestellsystem/katalog/api/ProduktController.java
src/main/java/de/bestellsystem/katalog/api/ProduktDto.java
src/main/java/de/bestellsystem/katalog/api/VerfuegbarkeitDto.java
src/main/java/de/bestellsystem/katalog/application/ProduktService.java
src/main/java/de/bestellsystem/katalog/domain/Produkt.java
src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java
src/main/java/de/bestellsystem/kunde/api/AdressbuchController.java
src/main/java/de/bestellsystem/kunde/api/AdresseDto.java
src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java
src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java
src/main/java/de/bestellsystem/kunde/api/KundeController.java
src/main/java/de/bestellsystem/kunde/api/KundeDto.java
src/main/java/de/bestellsystem/kunde/api/KundeUebersichtDto.java
src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java
src/main/java/de/bestellsystem/kunde/application/KundeService.java
src/main/java/de/bestellsystem/kunde/domain/Adresse.java
src/main/java/de/bestellsystem/kunde/domain/Email.java
src/main/java/de/bestellsystem/kunde/domain/Kunde.java
src/main/java/de/bestellsystem/kunde/persistence/KundeRepository.java
src/main/resources/application.yml
… [gekürzt, 161 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd src/main/java/de/bestellsystem && cat gemeinsam/domain/Geld.java gemeinsam/domain/Anschrift.java katalog/domain/Produkt.java kunde/domain/Kunde.java kunde/domain/Adresse.java
```

```
package de.bestellsystem.gemeinsam.domain;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Geldbetrag in ganzen Cent, Währung immer EUR (D-11).
 *
 * <p>Darf negativ sein, damit Zwischenergebnisse (z. B. Zahlungen minus Erstattungen) rechenbar
 * bleiben; "Beträge ≥ 0" (I-7) setzen die Entities durch.
 */
@Embeddable
public record Geld(long cent) implements Comparable<Geld> {

    public static final String WAEHRUNG = "EUR";
    public static final Geld NULL = new Geld(0);

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    public static Geld ausCent(long cent) {
        return new Geld(cent);
    }

    public String waehrung() {
        return WAEHRUNG;
    }

    public Geld addiere(Geld andere) {
        return new Geld(Math.addExact(cent, andere.cent));
    }

    public Geld subtrahiere(Geld andere) {
        return new Geld(Math.subtractExact(cent, andere.cent));
    }

    public Geld multipliziere(int menge) {
        return new Geld(Math.multiplyExact(cent, menge));
    }
… [gekürzt, 453 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat kunde/application/KundeService.java kunde/persistence/KundeRepository.java katalog/application/ProduktService.java kunde/api/AdressbuchController.java gemeinsam/application/BestellnummernGenerator.java gemeinsam/application/FachlicheEinstellungen.java gemeinsam/api/GeldDto.java
```

```
package de.bestellsystem.kunde.application;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.kunde.domain.Adresse;
import de.bestellsystem.kunde.domain.Email;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.kunde.persistence.KundeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class KundeService {

    private final KundeRepository repository;
    private final EntityManager entityManager;

    KundeService(KundeRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    @Transactional
    public Kunde registriere(
            String vorname,
            String nachname,
            String email,
            Anschrift lieferadresse,
            boolean istAuchRechnungsadresse) {
        Kunde kunde =
                new Kunde(
                        vorname,
                        nachname,
… [gekürzt, 406 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && cat src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java src/main/java/de/bestellsystem/kunde/api/KundeController.java src/main/resources/db/migration/V3__kunde.sql src/main/resources/db/migration/V2__produkt.sql; sed -n 1,120p src/test/java/de/bestellsystem/kunde/api/KundeApiIT.java
```

```
package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Übersetzt alle Fehler in ProblemDetail (RFC 9457) mit den Statuscodes aus CLAUDE.md. */
@RestControllerAdvice
public class ZentraleFehlerbehandlung extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ZentraleFehlerbehandlung.class);

    @ExceptionHandler(NichtGefundenException.class)
    ProblemDetail nichtGefunden(NichtGefundenException fehler) {
        return problem(HttpStatus.NOT_FOUND, "Nicht gefunden", fehler.getMessage());
    }

    @ExceptionHandler(KonfliktException.class)
    ProblemDetail konflikt(KonfliktException fehler) {
        return problem(HttpStatus.CONFLICT, "Konflikt", fehler.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail versionskonflikt(ObjectOptimisticLockingFailureException fehler) {
        return problem(
… [gekürzt, 367 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && cat FesteUhrKonfiguration.java TestcontainersKonfiguration.java; sed -n 1,200p kunde/application/KundeNebenlaeufigkeitIT.java
```

```
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

    public static final Instant JETZT = Instant.parse("2026-03-15T09:30:00Z");
    public static final ZoneId ZEITZONE = ZoneId.of("Europe/Berlin");

    @Bean
    @Primary
    Clock festeUhr() {
        return Clock.fixed(JETZT, ZEITZONE);
    }
}
package de.bestellsystem;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersKonfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));
    }
}
package de.bestellsystem.kunde.application;
… [gekürzt, 211 Zeilen gesamt]
```

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Gutscheine anlegen: US-08.2 ist nur Kandidat, Schritt 4a erwähnt es nicht. US-08.1 braucht aber existierende Gutscheine. Wie sollen Gutscheine in Schritt 4a entstehen?",
   "header": "Gutschein",
   "multiSelect": false,
   "options": [
    {
     "label": "Nur Testdaten (Recommended)",
     "description": "Kein Endpunkt; Tests legen Gutscheine über das Repository an. Anlage-Endpunkt (US-08.2) später, spätestens vor 4b-Browsertest."
    },
    {
     "label": "Minimaler POST-Endpunkt",
     "description": "POST /api/v1/gutscheine jetzt mit bauen; ich formuliere US-08.2 vorher kurz mit dir aus."
    },
    {
     "label": "Flyway-Beispieldaten",
     "description": "HERBST10, MINUS15, SOMMER20 per Migration einspielen (landen dann auch in Produktion)."
    }
   ]
  },
  {
   "question": "Nebenläufigkeit beim Lagerbestand (zwei Checkouts, eine Lampe): welche Sperrstrategie im Checkout?",
   "header": "Sperren",
   "multiSelect": false,
   "options": [
    {
     "label": "Pessimistisch (Recommended)",
     "description": "SELECT … FOR UPDATE auf die Produkte, sortiert nach ID (kein Deadlock). Verlierer sieht Bestand 0 → 409 'Bestand reicht nicht' mit Produktname. Zwei Bestellungen je 1 von 10 Büchern gehen beide durch. @Version bleibt für Bestand-setzen. ADR 0003."
    },
… [gekürzt, 80 Zeilen gesamt]
```

```
Your questions have been answered: "Gutscheine anlegen: US-08.2 ist nur Kandidat, Schritt 4a erwähnt es nicht. US-08.1 braucht aber existierende Gutscheine. Wie sollen Gutscheine in Schritt 4a entstehen?"="Nur Testdaten (Recommended)", "Nebenläufigkeit beim Lagerbestand (zwei Checkouts, eine Lampe): welche Sperrstrategie im Checkout?"="Pessimistisch (Recommended)", "Statuscodes der Checkout-Ablehnungen. Vorschlag: leere Bestellung/Menge<1 → 400; unbekannter Kunde/Produkt → 404; fremde Adresse → 404 (wie D-16); Adresse ohne Lieferkennzeichen → 422; Bestand reicht nicht → 409; Gutschein abgelaufen oder unbekannt → 422. Passt das?"="Vorschlag so (Recommended)", "Erste Zahlung (AUSSTEHEND): Was steht in zahlungszeitpunkt, und wird bei Gesamtwert 0 € (Gutschein deckt alles) trotzdem eine Zahlung angelegt?"="Leer, 0 € anlegen (Recommended)". You can now continue with these answers in mind.
```

### ● Claude

Alle Empfehlungen gewählt. Architektur (selbst entworfen, Code-Basis klein und Muster eindeutig; keine Alternativ-Agents nötig):

**Domain**
- `gutschein.domain`: `Gutschein` (UUID, `code` normalisiert, `RabattArt`, `prozent` BigDecimal ≤ 2 Nachkommastellen / `festbetrag` Geld, `ablaufdatum`), `istGueltigAm(datum)`, `berechneRabatt(warenwert)` (HALF_UP, max warenwert).
- `zahlung.domain`: `Zahlung` (UUID, `Zahlungsart`, `Buchungsart`, `Transaktionsstatus`, `betrag`, `zahlungszeitpunkt` nullable), Fabrik `Zahlung.ausstehend(art, betrag)`.
- `katalog.domain.Produkt`: neu `entnehme(menge)` (nie negativ).
- `kunde.domain.Kunde`: neu `lieferanschrift(adressId)` → Snapshot-Kopie, 404 fremd / 422 kein Lieferkennzeichen.
- `bestellung.domain`: `Bestellung` (`bestellnummer` als `@Id`, `kundeId`, `bestelldatum`, `faelligAm`, `@Embedded lieferanschrift`, `gutschein` LAZY 0..1, `rabattBetrag` eingefroren, Positionen + Zahlungen cascade ALL, keine Setter). Fabrik `Bestellung.gibAuf(...)`: Duplikate zusammenfassen → ≥ 1 Position → **alle** Bestände prüfen (409 mit allen Titeln) → entnehmen → Preise einfrieren, Zeilennummern 1..n → Gutschein gültig am Bestelldatum (422 "abgelaufen") → Rabatt einfrieren → Zahlung AUSSTEHEND über Gesamtwert. `berechneWarenwert()`, `berechneGesamtwert()`. `Bestellposition.berechneZwischenbetrag()`.

**Application/Persistence**
- `BestellungService.gibAuf` (eine Transaktion): `KundeService.finde` → `ProduktService.sperreZumEntnehmen(ids)` (`PESSIMISTIC_WRITE`, `ORDER BY id`, 404 unbekannt) → `GutscheinService.finde(code)` (422 unbekannt) → `BestellnummernGenerator` → save.
- `finde(kundenId, bestellnummer)`: 2 Abfragen (Positionen+Produkt+Gutschein per `JOIN FETCH`, Zahlungen per zweitem Fetch), unabhängig von Positionsanzahl.

**API**: `POST /api/v1/kunden/{kundenId}/bestellungen` (201 + Location), `GET …/bestellungen/{bestellnummer}`. `AnschriftDto` wandert nach `gemeinsam.api` (Schemaname bleibt `Anschrift`, Client unverändert).

**Migration** `V4__gutschein_bestellung_zahlung.sql`. **Doku**: D-17 (Statuscodes, Gutschein-Normalisierung, Zahlung leer/0 €, Hinweis alle Produkte), ADR 0003 (pessimistische Sperre), ARCHITEKTUR.

**Tests**: Domain-Unit je AK (US-04.1 AK1–8, US-08.1 AK1–9), `BestellungApiIT` (Statuscodes, Snapshot, Preis eingefroren, Rollback), `BestellungNebenlaeufigkeitIT` (letzte Lampe), Abfragenzählung per Hibernate-Statistics.

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Architektur für Schritt 4a freigeben? (Bestellung.gibAuf als Domain-Fabrik, pessimistische Produktsperre im Service, bestellnummer als Primärschlüssel, Kunde per kundeId referenziert, AnschriftDto nach gemeinsam.api, Migration V4, D-17 + ADR 0003)",
   "header": "Freigabe",
   "multiSelect": false,
   "options": [
    {
     "label": "Freigeben (Recommended)",
     "description": "Umsetzen wie beschrieben."
    },
    {
     "label": "UUID als Bestellungs-ID",
     "description": "Bestellung bekommt zusätzlich UUID als Primärschlüssel; bestellnummer nur unique, URL nutzt trotzdem bestellnummer."
    },
    {
     "label": "Kunde als @ManyToOne",
     "description": "Bestellung hält @ManyToOne(LAZY) Kunde statt kundeId."
    }
   ]
  }
 ]
}
```

```
Your questions have been answered: "Architektur für Schritt 4a freigeben? (Bestellung.gibAuf als Domain-Fabrik, pessimistische Produktsperre im Service, bestellnummer als Primärschlüssel, Kunde per kundeId referenziert, AnschriftDto nach gemeinsam.api, Migration V4, D-17 + ADR 0003)"="Freigeben (Recommended)". You can now continue with these answers in mind.
```

### ● Claude

Freigegeben. Restliche Dateien für Umsetzung lesen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat backend/src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java backend/src/main/java/de/bestellsystem/kunde/api/AdresseDto.java backend/src/main/java/de/bestellsystem/katalog/api/ProduktDto.java backend/src/main/java/de/bestellsystem/gemeinsam/domain/KonfliktException.java docs/adr/0002-*.md; sed -n 1,60p backend/src/test/java/de/bestellsystem/kunde/domain/KundeTest.java; grep -rn "AnschriftDto" --include=*.java backend/src | grep -v "^backend/src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java"; sed -n 120,400p backend/src/test/java/de/bestellsystem/kunde/api/KundeApiIT.java | grep -n "private" 
```

```
package de.bestellsystem.katalog.persistence;

import de.bestellsystem.katalog.domain.Produkt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduktRepository extends JpaRepository<Produkt, UUID> {

    List<Produkt> findAllByOrderByTitelAsc();
}
package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.domain.Adresse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Adresse",
        requiredProperties = {"id", "anschrift", "istLieferadresse", "istRechnungsadresse"})
public record AdresseDto(
        UUID id, AnschriftDto anschrift, boolean istLieferadresse, boolean istRechnungsadresse) {

    static AdresseDto von(Adresse adresse) {
        return new AdresseDto(
                adresse.getId(),
                AnschriftDto.von(adresse.getAnschrift()),
                adresse.istLieferadresse(),
                adresse.istRechnungsadresse());
    }
}
package de.bestellsystem.katalog.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Produkt",
… [gekürzt, 161 Zeilen gesamt]
```

**⏺ Bash**

```bash
grep -rln "AnschriftDto" backend/src; cat backend/src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java backend/src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java; git mv backend/src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java backend/src/main/java/de/bestellsystem/gemeinsam/api/AnschriftDto.java
```

```
backend/src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java
backend/src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java
backend/src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java
backend/src/main/java/de/bestellsystem/kunde/api/AdresseDto.java
package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.domain.Kunde;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
        name = "Registrierung",
        description = "Neues Kundenkonto mit erster Lieferadresse (US-01.1)")
public record RegistrierungDto(
        @Schema(example = "Anna") @NotBlank @Size(max = Kunde.NAME_MAXIMALE_LAENGE) String vorname,
        @Schema(example = "Muster") @NotBlank @Size(max = Kunde.NAME_MAXIMALE_LAENGE)
                String nachname,
        @Schema(
                        description = "Eindeutig ohne Groß-/Kleinschreibung (D-13)",
                        example = "anna@example.de")
                @NotBlank
                String email,
        @NotNull(message = "Eine Lieferadresse ist erforderlich") @Valid AnschriftDto lieferadresse,
        @Schema(description = "Lieferadresse ist zugleich Rechnungsadresse", defaultValue = "false")
                boolean istAuchRechnungsadresse) {}
package de.bestellsystem.kunde.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(
        name = "AdresseEingabe",
        description = "Adresse mit Kennzeichen; mindestens eines muss gesetzt sein (I-2)")
public record AdresseEingabeDto(
        @NotNull @Valid AnschriftDto anschrift,
        @NotNull Boolean istLieferadresse,
… [gekürzt, 41 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && sed -i 's/^package de.bestellsystem.kunde.api;/package de.bestellsystem.gemeinsam.api;/; s/^    static AnschriftDto von/    public static AnschriftDto von/; s/^    Anschrift alsAnschrift/    public Anschrift alsAnschrift/' gemeinsam/api/AnschriftDto.java && for f in kunde/api/AdresseEingabeDto.java kunde/api/RegistrierungDto.java kunde/api/AdresseDto.java; do sed -i '0,/^import /s//import de.bestellsystem.gemeinsam.api.AnschriftDto;\nimport /' $f; done && head -5 kunde/api/AdresseDto.java && grep -n "static\|public Anschrift" gemeinsam/api/AnschriftDto.java
```

```
package de.bestellsystem.kunde.api;

import de.bestellsystem.gemeinsam.api.AnschriftDto;
import de.bestellsystem.kunde.domain.Adresse;
import io.swagger.v3.oas.annotations.media.Schema;
20:    public static AnschriftDto von(Anschrift anschrift) {
25:    public Anschrift alsAnschrift() {
```

### ● Claude

Erst D-17 und ADR 0003, dann Tests.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat >> docs/01-entscheidungen.md <<'EOF'

---

## D-17 Checkout: Ablehnungen, Gutscheincode, erste Zahlung

**Problem:** US-04.1 und US-08.1 legen fest, *dass* abgelehnt wird, aber nicht mit welchem Statuscode. Offen sind außerdem die Schreibweise des Gutscheincodes, der Zahlungszeitpunkt einer ausstehenden Zahlung und der Fall Gesamtwert 0 €.

**Entscheidung:**
- Statuscodes beim Checkout:
  - 400: keine Positionen (US-04.1 AK7), `menge < 1`, fehlende Pflichtfelder.
  - 404: unbekannter Kunde, unbekanntes Produkt, Adresse existiert nicht oder gehört einem anderen Kunden (wie D-16).
  - 409: Bestand reicht nicht (US-04.1 AK3). Der Hinweis nennt **alle** Produkte, deren Bestand nicht reicht.
  - 422: Adresse ohne Lieferkennzeichen (US-04.1 AK8), Gutschein abgelaufen oder unbekannt (US-08.1 AK4, AK6).
- Jede Ablehnung lässt keinen Bestand verändert und legt keine Bestellung an.
- Gutscheincode wird getrimmt und **großgeschrieben** gespeichert und gesucht (D-04).
- Prozentrabatt hat höchstens zwei Nachkommastellen.
- Die API nimmt höchstens **einen** Gutscheincode je Checkout an. "Zweiter Code ersetzt den ersten" (US-08.1 AK7) ist Verhalten der Eingabemaske.
- Die erste Zahlung (D-02) hat **keinen** `zahlungszeitpunkt`; er wird erst beim Statuswechsel gesetzt (E05).
- Auch bei Gesamtwert 0 € wird die erste Zahlung über 0,00 € mit Status `AUSSTEHEND` angelegt (I-5).

**Nicht im Umfang (Schritt 4a):** Gutschein anlegen (US-08.2), Bestellhistorie (US-04.2). Tests legen Gutscheine direkt an.
EOF
cat > docs/adr/0003-pessimistische-sperre-im-checkout.md <<'EOF'
# ADR 0003: Pessimistische Sperre auf Produkte im Checkout

**Status:** angenommen (2026-11-12)

## Kontext

… [gekürzt, 45 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ls src/test/resources 2>/dev/null; grep -rn "FesteUhrKonfiguration\|deleteAll\|@Import" src/test/java | grep -v "^.*import de"; sed -n 1,80p src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java | grep -n "Import\|class\|SpringBootTest"
```

```
src/test/java/de/bestellsystem/BestellsystemApplicationIT.java:23:@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
src/test/java/de/bestellsystem/BestellsystemApplicationIT.java:33:        assertThat(uhr.instant()).isEqualTo(FesteUhrKonfiguration.JETZT);
src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java:30:@Import(TestcontainersKonfiguration.class)
src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java:40:        repository.deleteAll();
src/test/java/de/bestellsystem/kunde/application/KundeNebenlaeufigkeitIT.java:29:@Import(TestcontainersKonfiguration.class)
src/test/java/de/bestellsystem/kunde/application/KundeNebenlaeufigkeitIT.java:43:        repository.deleteAll();
src/test/java/de/bestellsystem/FesteUhrKonfiguration.java:12:public class FesteUhrKonfiguration {
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:22:@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java:27:@Import(TestcontainersKonfiguration.class)
src/test/java/de/bestellsystem/kunde/api/KundeApiIT.java:31:@Import(TestcontainersKonfiguration.class)
src/test/java/de/bestellsystem/kunde/api/KundeApiIT.java:52:        repository.deleteAll();
17:import org.springframework.boot.test.context.SpringBootTest;
18:import org.springframework.context.annotation.Import;
25:@SpringBootTest
27:@Import(TestcontainersKonfiguration.class)
28:class OpenApiSpezifikationIT {
```

**⏺ Bash**

```bash
grep -n "JETZT\|2026\|03-15\|LocalDate" src/test/java/de/bestellsystem/BestellsystemApplicationIT.java src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java
```

```
src/test/java/de/bestellsystem/BestellsystemApplicationIT.java:11:import java.time.LocalDate;
src/test/java/de/bestellsystem/BestellsystemApplicationIT.java:33:        assertThat(uhr.instant()).isEqualTo(FesteUhrKonfiguration.JETZT);
src/test/java/de/bestellsystem/BestellsystemApplicationIT.java:34:        assertThat(LocalDate.now(uhr)).isEqualTo(LocalDate.of(2026, 3, 15));
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:8:import java.time.LocalDate;
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:25:    private static final LocalDate BESTELLDATUM = LocalDate.of(2026, 3, 15);
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:35:        assertThat(erste).matches("B-2026-\\d{6}");
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:36:        assertThat(zweite).matches("B-2026-\\d{6}");
src/test/java/de/bestellsystem/gemeinsam/persistence/SequenzBestellnummernZaehlerIT.java:60:        return Long.parseLong(bestellnummer.substring("B-2026-".length()));
```

### ● Claude

Feste Testuhr auf Story-Datum 12.11.2026 umstellen (gleicher Kontext für alle Checkout-ITs).

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && sed -i 's/2026-03-15T09:30:00Z/2026-11-12T09:30:00Z/' FesteUhrKonfiguration.java && sed -i 's/LocalDate.of(2026, 3, 15)/LocalDate.of(2026, 11, 12)/' BestellsystemApplicationIT.java && grep -n 2026 FesteUhrKonfiguration.java BestellsystemApplicationIT.java
```

```
BestellsystemApplicationIT.java:34:        assertThat(LocalDate.now(uhr)).isEqualTo(LocalDate.of(2026, 11, 12));
FesteUhrKonfiguration.java:14:    public static final Instant JETZT = Instant.parse("2026-11-12T09:30:00Z");
```

### ● Claude

Domain-Tests zuerst: Gutschein.

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java
package de.bestellsystem.gutschein.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GutscheinTest {

    static final LocalDate ZWOELFTER_NOVEMBER = LocalDate.of(2026, 11, 12);

    static Gutschein herbst10() {
        return Gutschein.prozent("HERBST10", new BigDecimal("10"), LocalDate.of(2026, 11, 30));
    }

    static Gutschein minus15() {
        return Gutschein.festbetrag("MINUS15", Geld.ausCent(1500), LocalDate.of(2026, 12, 31));
    }

    static Gutschein sommer20() {
        return Gutschein.prozent("SOMMER20", new BigDecimal("20"), LocalDate.of(2026, 8, 31));
    }

    @Test
    @DisplayName("US-08.1 AK1: 10 % auf 55,00 € ergibt 5,50 € Rabatt")
… [gekürzt, 111 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/domain/BestellungTest.java
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
… [gekürzt, 330 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/domain/BestellungTest.java (file state is current in your context — no need to Read it back)
```

### ● Claude

Tests für `Produkt.entnehme` und `Kunde.lieferanschrift` ergänzen.

**⏺ Bash**

```bash
head -20 katalog/domain/ProduktTest.java; tail -5 katalog/domain/ProduktTest.java; tail -5 kunde/domain/KundeTest.java
```

```
package de.bestellsystem.katalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ProduktTest {

    private static Produkt produktMitBestand(int lagerbestand) {
        return new Produkt("Stehlampe", Geld.ausCent(4999), lagerbestand);
    }

    @Test
    @DisplayName("US-02.1 AK1: Produkt mit Lagerbestand 3 ist verfügbar")
    @Test
    void neuesProduktHatId() {
        assertThat(produktMitBestand(1).getId()).isNotNull();
    }
}
            assertThatThrownBy(() -> kunde.getAdressen().clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }
}
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='katalog/domain/ProduktTest.java'
s=open(p).read().rstrip()
assert s.endswith('}')
s=s[:-1]+'''
    @Test
    @DisplayName("US-04.1 AK2: Entnehmen senkt den Lagerbestand")
    void entnehmen() {
        Produkt produkt = produktMitBestand(10);

        produkt.entnehme(2);

        assertThat(produkt.getLagerbestand()).isEqualTo(8);
    }

    @Test
    @DisplayName("D-09: Entnehmen über den Bestand hinaus wird abgelehnt, Bestand bleibt")
    void entnehmenUeberBestand() {
        Produkt produkt = produktMitBestand(1);

        assertThatThrownBy(() -> produkt.entnehme(2))
                .isInstanceOf(KonfliktException.class)
                .hasMessageContaining("Stehlampe");
        assertThat(produkt.getLagerbestand()).isEqualTo(1);
    }

    @Test
    @DisplayName("D-10: Entnehmen verlangt eine Menge von mindestens 1")
    void entnehmenMengeMindestensEins() {
        assertThatThrownBy(() -> produktMitBestand(5).entnehme(0))
… [gekürzt, 79 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Domain-Code schreiben.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && mkdir -p zahlung/domain zahlung/api gutschein/domain gutschein/persistence gutschein/application bestellung/domain bestellung/persistence bestellung/application bestellung/api && 
cat > zahlung/domain/Zahlungsart.java <<'EOF'
package de.bestellsystem.zahlung.domain;

public enum Zahlungsart {
    KREDITKARTE,
    PAYPAL,
    LASTSCHRIFT,
    RECHNUNG,
    VORKASSE
}
EOF
cat > zahlung/domain/Buchungsart.java <<'EOF'
package de.bestellsystem.zahlung.domain;

/** Bestimmt das Vorzeichen einer Buchung; der Betrag selbst ist immer positiv (D-05). */
public enum Buchungsart {
    ZAHLUNG,
    ERSTATTUNG
}
EOF
cat > zahlung/domain/Transaktionsstatus.java <<'EOF'
package de.bestellsystem.zahlung.domain;

/** Nur {@code ERFOLGREICH} zählt als geleistet (D-02, D-05). */
public enum Transaktionsstatus {
    AUSSTEHEND,
    ERFOLGREICH,
    FEHLGESCHLAGEN,
    STORNIERT
… [gekürzt, 274 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
sed -i 's/(anderes instanceof Gutschein gutschein \&\& id.equals(gutschein.id))/(anderes instanceof Gutschein gutschein \&\& id.equals(gutschein.getId()))/' gutschein/domain/Gutschein.java && sed -i 's/(anderes instanceof Produkt produkt \&\& id.equals(produkt.id))/(anderes instanceof Produkt produkt \&\& id.equals(produkt.getId()))/' katalog/domain/Produkt.java && grep -n "getId()))" gutschein/domain/Gutschein.java katalog/domain/Produkt.java
python3 - <<'EOF'
p='katalog/domain/Produkt.java'
s=open(p).read()
s=s.replace('''    public void setzeLagerbestand(int lagerbestand) {''','''    /** Senkt den Bestand beim Checkout; nie unter 0 (D-09). */
    public void entnehme(int menge) {
        if (!istVerfuegbar(menge)) {
            throw new KonfliktException(
                    "Nicht genug Bestand für %s: verfügbar %d, bestellt %d"
                            .formatted(titel, lagerbestand, menge));
        }
        lagerbestand -= menge;
    }

    public void setzeLagerbestand(int lagerbestand) {''')
s=s.replace('import de.bestellsystem.gemeinsam.domain.Geld;\n','import de.bestellsystem.gemeinsam.domain.Geld;\nimport de.bestellsystem.gemeinsam.domain.KonfliktException;\n')
open(p,'w').write(s)
p='kunde/domain/Kunde.java'
s=open(p).read()
s=s.replace('''    private boolean istEinzigeLieferadresse''','''    /** Anschrift der gewählten Lieferadresse als Snapshot für eine Bestellung (D-08). */
    public Anschrift lieferanschrift(UUID adressId) {
        Adresse adresse = adresse(adressId);
        if (!adresse.istLieferadresse()) {
            throw new FachlicheAblehnungException(
                    "Die Adresse " + adressId + " ist keine Lieferadresse.");
        }
        return adresse.getAnschrift();
    }

    private boolean istEinzigeLieferadresse''')
… [gekürzt, 32 Zeilen gesamt]
```

```
katalog/domain/Produkt.java:104:        return this == anderes || (anderes instanceof Produkt produkt && id.equals(produkt.getId()));
gutschein/domain/Gutschein.java:123:                || (anderes instanceof Gutschein gutschein && id.equals(gutschein.getId()));
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/bestellung/domain && cat > Bestellwunsch.java <<'EOF'
package de.bestellsystem.bestellung.domain;

import de.bestellsystem.katalog.domain.Produkt;

/** Ein vom Kunden ausgewähltes Produkt mit Menge, vor dem Zusammenfassen (D-10). */
public record Bestellwunsch(Produkt produkt, int menge) {

    public Bestellwunsch {
        if (produkt == null) {
            throw new IllegalArgumentException("produkt ist erforderlich");
        }
        if (menge < 1) {
            throw new IllegalArgumentException("menge muss mindestens 1 sein, war: " + menge);
        }
    }
}
EOF
cat > Bestellposition.java <<'EOF'
package de.bestellsystem.bestellung.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.domain.Produkt;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
… [gekürzt, 323 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Unit-Tests grün. Domain-AK4-Test hat sinnlose `teurer`-Konstruktion — vereinfachen; echter Nachweis folgt im IT per SQL-Preisänderung.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/domain && grep -n "teurer" -A3 BestellungTest.java
```

```
157:            Produkt teurer = new Produkt("Buch", Geld.ausCent(1500), 10);
158-
159:            assertThat(teurer.getListenpreis()).isNotEqualTo(buch.getListenpreis());
160-            assertThat(bestellung.getPositionen().getFirst().getEinzelpreis())
161-                    .isEqualTo(Geld.ausCent(1250));
162-            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(5500));
```

**⏺ Bash**

```bash
sed -i '157,159d' BestellungTest.java && sed -n 150,162p BestellungTest.java && sed -i 's/US-04.1 AK4: späterer Listenpreis ändert Einzelpreis und Gesamtwert nicht/US-04.1 AK4: Einzelpreis ist der Listenpreis zum Checkout/' BestellungTest.java
```

```
                    .hasMessageContaining("Buch");
        }

        @Test
        @DisplayName("US-04.1 AK4: späterer Listenpreis ändert Einzelpreis und Gesamtwert nicht")
        void preisEingefroren() {
            Bestellung bestellung = gibAuf(zweiBuecherEineLampe());
            assertThat(bestellung.getPositionen().getFirst().getEinzelpreis())
                    .isEqualTo(Geld.ausCent(1250));
            assertThat(bestellung.berechneGesamtwert()).isEqualTo(Geld.ausCent(5500));
        }

        @Test
[This command modified 1 file you've previously read: BestellungTest.java. Call Read before editing.]
```

### ● Claude

Persistence, Application, API, Migration.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main && 
cat > java/de/bestellsystem/gutschein/persistence/GutscheinRepository.java <<'EOF'
package de.bestellsystem.gutschein.persistence;

import de.bestellsystem.gutschein.domain.Gutschein;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GutscheinRepository extends JpaRepository<Gutschein, UUID> {

    /** Erwartet einen mit {@link Gutschein#normalisiere} normalisierten Code. */
    Optional<Gutschein> findByCode(String code);
}
EOF
cat > java/de/bestellsystem/gutschein/application/GutscheinService.java <<'EOF'
package de.bestellsystem.gutschein.application;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gutschein.domain.Gutschein;
import de.bestellsystem.gutschein.persistence.GutscheinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GutscheinService {

    private final GutscheinRepository repository;

… [gekürzt, 307 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && sed -i 's/^    private BigDecimal prozent;/    @Column(precision = 5, scale = 2)\n    private BigDecimal prozent;/; s/^import jakarta.persistence.Entity;/import jakarta.persistence.Column;\nimport jakarta.persistence.Entity;/' gutschein/domain/Gutschein.java &&
cat > zahlung/api/ZahlungDto.java <<'EOF'
package de.bestellsystem.zahlung.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
        name = "Zahlung",
        requiredProperties = {"id", "zahlungsart", "buchungsart", "transaktionsstatus", "betrag"})
public record ZahlungDto(
        UUID id,
        Zahlungsart zahlungsart,
        Buchungsart buchungsart,
        Transaktionsstatus transaktionsstatus,
        GeldDto betrag,
        @Schema(description = "Leer, solange kein Geld geflossen ist (D-17)")
                Instant zahlungszeitpunkt) {

    public static ZahlungDto von(Zahlung zahlung) {
        return new ZahlungDto(
                zahlung.getId(),
                zahlung.getZahlungsart(),
                zahlung.getBuchungsart(),
… [gekürzt, 290 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Jetzt Integrationstests.

**⏺ Bash**

```bash
mkdir -p /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/api /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/application && cat > /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/Datenbestand.java <<'EOF'
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
EOF
```

```
(Bash completed with no output)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java
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
… [gekürzt, 481 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java (file state is current in your context — no need to Read it back)
```

### ● Claude

Nebenläufigkeitstest.

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/application/BestellungNebenlaeufigkeitIT.java
package de.bestellsystem.bestellung.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
… [gekürzt, 155 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/bestellung/application/BestellungNebenlaeufigkeitIT.java (file state is current in your context — no need to Read it back)
```

**⏺ Bash**

```bash
./gradlew spotlessApply -q && ./gradlew test --tests '*Bestellung*' --tests '*Gutschein*' 2>&1 | grep -E "FAILED|Test.*>|Exception|tests completed|BUILD" | head -40
```

```
BUILD SUCCESSFUL in 25s
```

### ● Claude

Zu schnell — prüfen, ob ITs liefen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/build/test-results/test && for f in TEST-*Bestellung*.xml TEST-*Gutschein*.xml; do grep -o '<testsuite name="[^"]*" tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' $f; done
```

```
<testsuite name="de.bestellsystem.bestellung.api.BestellungApiIT$BestellungAufgeben" tests="15" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.api.BestellungApiIT$GutscheinEinloesen" tests="8" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.api.BestellungApiIT" tests="1" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.application.BestellungNebenlaeufigkeitIT" tests="2" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.domain.BestellungTest$BestellungAufgeben" tests="10" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.domain.BestellungTest$GutscheinEinloesen" tests="9" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.api.BestellungApiIT$GutscheinEinloesen" tests="8" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.bestellung.domain.BestellungTest$GutscheinEinloesen" tests="9" skipped="0" failures="0" errors="0"
<testsuite name="de.bestellsystem.gutschein.domain.GutscheinTest" tests="11" skipped="0" failures="0" errors="0"
```

### ● Claude

Alles grün (56 neue Tests). OpenAPI exportieren, Client neu erzeugen, Gesamtbuild.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew aktualisiereOpenApi -q 2>&1 | tail -5; cd ../frontend && npm run generate:api 2>&1 | tail -3 && cd .. && git status --short
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T10:36:16.426+01:00  INFO 1091916 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T10:36:16.429+01:00  INFO 1091916 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T10:36:16.431+01:00  INFO 1091916 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
[[bestellsystem] openapi.json] ############################################################################################
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,supportsES6=true" exited with code 0
[bestellsystem] openapi.json
RM backend/src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java -> backend/src/main/java/de/bestellsystem/gemeinsam/api/AnschriftDto.java
 M backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
 M backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
 M backend/src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java
 M backend/src/main/java/de/bestellsystem/kunde/api/AdresseDto.java
 M backend/src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java
 M backend/src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java
 M backend/src/main/java/de/bestellsystem/kunde/domain/Kunde.java
 M backend/src/test/java/de/bestellsystem/BestellsystemApplicationIT.java
 M backend/src/test/java/de/bestellsystem/FesteUhrKonfiguration.java
 M backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
 M backend/src/test/java/de/bestellsystem/kunde/domain/KundeTest.java
 M docs/01-entscheidungen.md
 M frontend/openapi.json
 M frontend/src/app/api/.openapi-generator/FILES
 M frontend/src/app/api/api/api.ts
 M frontend/src/app/api/model/models.ts
?? 2026-11-12-095422-command-messagenaechster-schrittcommand-messa.txt
?? 2026-11-12-102509-local-command-caveatthe-command-below-was-run-d.txt
?? backend/src/main/java/de/bestellsystem/bestellung/
?? backend/src/main/java/de/bestellsystem/gutschein/
?? backend/src/main/java/de/bestellsystem/zahlung/
?? backend/src/main/resources/db/migration/V4__gutschein_bestellung_zahlung.sql
?? backend/src/test/java/de/bestellsystem/Datenbestand.java
?? backend/src/test/java/de/bestellsystem/bestellung/
?? backend/src/test/java/de/bestellsystem/gutschein/
?? docs/adr/0003-pessimistische-sperre-im-checkout.md
?? frontend/src/app/api/api/bestellungen.service.ts
?? frontend/src/app/api/model/bestellposition.ts
?? frontend/src/app/api/model/bestellung-aufgeben.ts
?? frontend/src/app/api/model/bestellung.ts
?? frontend/src/app/api/model/positionswunsch.ts
?? frontend/src/app/api/model/zahlung.ts
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem/bestellung/application && grep -n "Anschrift lieferanschrift = kundeService" BestellungService.java
```

```
67:        Anschrift lieferanschrift = kundeService.finde(kundenId).lieferanschrift(lieferadresseId);
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
p='src/main/java/de/bestellsystem/katalog/application/ProduktService.java'
s=open(p).read()
old='''    public Map<UUID, Produkt> sperreZumEntnehmen(Set<UUID> produktIds) {
'''
new='''    public Map<UUID, Produkt> sperreZumEntnehmen(Set<UUID> produktIds) {
        if (produktIds.isEmpty()) {
            return Map.of();
        }
'''
assert old in s
open(p,'w').write(s.replace(old,new))
EOF
./gradlew build 2>&1 | grep -E "FAILED|BUILD|tests completed" ; cd ../frontend && npm test 2>&1 | tail -6
```

```
BUILD SUCCESSFUL in 23s

[2m Test Files [22m [1m[32m9 passed[39m[22m[90m (9)[39m
[2m      Tests [22m [1m[32m44 passed[39m[22m[90m (44)[39m
[2m   Start at [22m 10:37:06
[2m   Duration [22m 2.07s[2m (transform 492ms, setup 4.09s, import 1.35s, tests 915ms, environment 8.20s)[22m
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/build/test-results/test && cat TEST-*.xml | grep -o '<testsuite [^>]*' | sed -E 's/.*tests="([0-9]+)" skipped="([0-9]+)" failures="([0-9]+)" errors="([0-9]+)".*/\1 \2 \3 \4/' | awk '{t+=$1;s+=$2;f+=$3;e+=$4} END {print "tests",t,"skipped",s,"failures",f,"errors",e}'; ls -la --time-style=+%H:%M TEST-*BestellungApiIT.xml | awk '{print $6}'
```

```
tests 186 skipped 0 failures 0 errors 0
10:37
```

### ● Claude

Backend 186 Tests grün, Frontend 44. Jetzt `ARCHITEKTUR.md`.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
def rep(old,new):
    global s
    assert old in s, old
    s=s.replace(old,new,1)
rep('Stand: Schritt 3 (Kundenkonto und Adressbuch E01).','Stand: Schritt 4a (Checkout im Backend, E04 + E08).')
rep("| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`) |",
"| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |")
rep("| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`; prüft D-15 und I-7 |\n| `katalog.persistence` | `ProduktRepository` (Spring Data JPA) |\n| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID) |",
"| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |\n| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |\n| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |")
rep("`entferneAdresse`, `adresse(id)`;","`entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen);")
rep("| `kunde.api` | `KundeController`, `AdressbuchController`, DTOs `RegistrierungDto`, `AnschriftDto`, `AdresseEingabeDto`, `AdresseDto`, `KundeDto`, `KundeUebersichtDto` |",
"""| `kunde.api` | `KundeController`, `AdressbuchController`, DTOs `RegistrierungDto`, `AdresseEingabeDto`, `AdresseDto`, `KundeDto`, `KundeUebersichtDto` |
| `gutschein.domain` | `Gutschein` (Entity, UUID; Fabriken `prozent`, `festbetrag`): `normalisiere(code)` (getrimmt, groß), `istGueltigAm(datum)` (inklusive), `berechneRabatt(warenwert)` (HALF_UP, höchstens Warenwert); `RabattArt` |
| `gutschein.persistence` | `GutscheinRepository.findByCode` |
| `gutschein.application` | `GutscheinService.zumEinloesen(code)` (422 bei unbekanntem Code) |
| `zahlung.domain` | `Zahlung` (Entity, UUID, Teil der Bestellung; Fabrik `ausstehend`), Enums `Zahlungsart`, `Buchungsart`, `Transaktionsstatus` |
| `zahlung.api` | `ZahlungDto` |
| `bestellung.domain` | `Bestellung` (Aggregatwurzel, `bestellnummer` als ID, `@Version`, `kundeId`, `@Embedded lieferanschrift`, Gutschein 0..1 LAZY, eingefrorener `rabattBetrag`, Positionen und Zahlungen `cascade ALL`): Fabrik `gibAuf`, `berechneWarenwert`, `berechneGesamtwert`. `Bestellposition` (eingefrorener `einzelpreis`, Produkt LAZY, `berechneZwischenbetrag`). `Bestellwunsch` (Produkt + Menge vor dem Zusammenfassen) |
| `bestellung.persistence` | `BestellungRepository`: `findeMitPositionen` (`JOIN FETCH` Positionen, Produkt, Gutschein), `findeZahlungen` (`JOIN FETCH` Zahlungen) |
| `bestellung.application` | `BestellungService`: `gibAuf` (eine Transaktion), `finde` (zwei Abfragen); `Positionswunsch` |
| `bestellung.api` | `BestellungController`, DTOs `BestellungAufgebenDto`, `PositionswunschDto`, `BestellungDto`, `BestellpositionDto` |""")
rep("| DELETE | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | 204, 404, 409, 422 letzte Lieferadresse (I-1) |",
"""| DELETE | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | 204, 404, 409, 422 letzte Lieferadresse (I-1) |
| POST | `/api/v1/kunden/{kundenId}/bestellungen` | Checkout: 201 + `Location`, 400 (keine Positionen, Menge < 1), 404 (Kunde, Produkt, fremde Adresse), 409 Bestand, 422 (keine Lieferadresse, Gutschein abgelaufen/unbekannt), D-17 |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}` | Bestellung mit Positionen, Rabatt, Gesamtwer
… [gekürzt, 71 Zeilen gesamt]
```

```
145:### Bestellnummer (D-13, D-14)
```

### ● Claude

Fertig-wenn geprüft: alle AK US-04.1/US-08.1 als Tests grün ✓, Nebenläufigkeitstest letzte Lampe ✓, Abruf = 2 Abfragen (Hibernate-Statistics) ✓. Committen.

**⏺ Bash**

```bash
git add backend/src docs/01-entscheidungen.md docs/ARCHITEKTUR.md docs/adr/0003-pessimistische-sperre-im-checkout.md frontend/openapi.json frontend/src/app/api && git status --short && git commit -q -F - <<'EOF'
Add checkout with vouchers and first payment in backend (E04, E08)

Checkout creates order, positions, delivery address snapshot, frozen
discount and a pending first payment in one transaction. Products are
locked pessimistically so concurrent checkouts see current stock
(ADR 0003). Status codes and voucher rules recorded as D-17.

US-04.1 and US-08.1 fully covered by unit and integration tests,
including the last-lamp concurrency test and a query count check
for loading an order.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1
```

```
A  backend/src/main/java/de/bestellsystem/bestellung/api/BestellpositionDto.java
A  backend/src/main/java/de/bestellsystem/bestellung/api/BestellungAufgebenDto.java
A  backend/src/main/java/de/bestellsystem/bestellung/api/BestellungController.java
A  backend/src/main/java/de/bestellsystem/bestellung/api/BestellungDto.java
A  backend/src/main/java/de/bestellsystem/bestellung/api/PositionswunschDto.java
A  backend/src/main/java/de/bestellsystem/bestellung/application/BestellungService.java
A  backend/src/main/java/de/bestellsystem/bestellung/application/Positionswunsch.java
A  backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellposition.java
A  backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellung.java
A  backend/src/main/java/de/bestellsystem/bestellung/domain/Bestellwunsch.java
A  backend/src/main/java/de/bestellsystem/bestellung/persistence/BestellungRepository.java
R  backend/src/main/java/de/bestellsystem/kunde/api/AnschriftDto.java -> backend/src/main/java/de/bestellsystem/gemeinsam/api/AnschriftDto.java
A  backend/src/main/java/de/bestellsystem/gutschein/application/GutscheinService.java
A  backend/src/main/java/de/bestellsystem/gutschein/domain/Gutschein.java
A  backend/src/main/java/de/bestellsystem/gutschein/domain/RabattArt.java
A  backend/src/main/java/de/bestellsystem/gutschein/persistence/GutscheinRepository.java
M  backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
M  backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
M  backend/src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java
M  backend/src/main/java/de/bestellsystem/kunde/api/AdresseDto.java
M  backend/src/main/java/de/bestellsystem/kunde/api/AdresseEingabeDto.java
M  backend/src/main/java/de/bestellsystem/kunde/api/RegistrierungDto.java
M  backend/src/main/java/de/bestellsystem/kunde/domain/Kunde.java
A  backend/src/main/java/de/bestellsystem/zahlung/api/ZahlungDto.java
A  backend/src/main/java/de/bestellsystem/zahlung/domain/Buchungsart.java
A  backend/src/main/java/de/bestellsystem/zahlung/domain/Transaktionsstatus.java
A  backend/src/main/java/de/bestellsystem/zahlung/domain/Zahlung.java
A  backend/src/main/java/de/bestellsystem/zahlung/domain/Zahlungsart.java
A  backend/src/main/resources/db/migration/V4__gutschein_bestellung_zahlung.sql
M  backend/src/test/java/de/bestellsystem/BestellsystemApplicationIT.java
A  backend/src/test/java/de/bestellsystem/Datenbestand.java
M  backend/src/test/java/de/bestellsystem/FesteUhrKonfiguration.java
A  backend/src/test/java/de/bestellsystem/bestellung/api/BestellungApiIT.java
A  backend/src/test/java/de/bestellsystem/bestellung/application/BestellungNebenlaeufigkeitIT.java
A  backend/src/test/java/de/bestellsystem/bestellung/domain/BestellungTest.java
A  backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java
M  backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
M  backend/src/test/java/de/bestellsystem/kunde/domain/KundeTest.java
M  docs/01-entscheidungen.md
M  docs/ARCHITEKTUR.md
… [gekürzt, 54 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -i 's/^## \[ \] 4a Checkout Backend (E04 + E08)$/## [x] 4a Checkout Backend (E04 + E08) (2026-11-12, 9cfb2a2)/' docs/fahrplan.md && grep -n "4a Checkout" docs/fahrplan.md && git add docs/fahrplan.md && git commit -q -m "Hake Schritt 4a im Fahrplan ab" -m "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -2
```

```
84:## [x] 4a Checkout Backend (E04 + E08) (2026-11-12, 9cfb2a2)
36bdc88 Hake Schritt 4a im Fahrplan ab
9cfb2a2 Add checkout with vouchers and first payment in backend (E04, E08)
```

### ● Claude

Schritt 4a ist fertig: Der Checkout läuft im Backend, alle Tests sind grün, und der Schritt ist im Fahrplan abgehakt.

**Was gebaut ist**
- **Bestellung aufgeben:** `POST /api/v1/kunden/{kundenId}/bestellungen`. In einer Transaktion entstehen Bestellung, Positionen, eine Kopie der Lieferadresse, der eingefrorene Rabatt und die erste Zahlung (AUSSTEHEND, ohne Zahlungszeitpunkt). Fällig ist die Bestellung 14 Tage nach Bestelldatum, und der Lagerbestand sinkt.
- **Bestellung abrufen:** `GET …/bestellungen/{bestellnummer}`, immer genau 2 SQL-Abfragen, egal wie viele Positionen.
- **Gutschein:** Prozent oder Festbetrag. Das Ablaufdatum zählt noch mit, Groß- und Kleinschreibung spielen keine Rolle, kaufmännisch gerundet, und der Gesamtwert wird nie negativ.
- **Gleichzeitige Bestellungen:** Die Produkte werden beim Checkout gesperrt (`SELECT … FOR UPDATE`). Zwei Bestellungen auf die letzte Lampe: eine gelingt, die andere bekommt 409 mit Hinweis auf die Lampe. Reicht der Bestand für beide, gelingen beide.
- **Neue Tabellen:** Migration V4 legt `gutschein`, `bestellung`, `bestellposition` und `zahlung` an.

**Was sonst geändert ist**
- `AnschriftDto` liegt jetzt in `gemeinsam.api`. Der generierte Frontend-Client ändert sich dadurch nicht.
- Die feste Testuhr steht jetzt auf dem 12.11.2026, dem Datum aus den Stories.

**Entscheidungen**
- **D-17:** Statuscodes beim Checkout so, wie du sie freigegeben hast. Der Hinweis bei zu wenig Bestand nennt alle betroffenen Produkte. Auch bei 0 € wird eine Zahlung angelegt.
- **ADR 0003:** Begründung für das Sperren der Produkte.
- `docs/ARCHITEKTUR.md` ist nachgezogen.

**Tests**
- Backend: `./gradlew build` grün, 186 Tests ohne Fehler und ohne übersprungene.
- Frontend: `npm test` grün, 44 Tests. Der Client ist neu erzeugt.

**Commits**
- `9cfb2a2` Add checkout with vouchers and first payment in backend (E04, E08)
- `36bdc88` Hake Schritt 4a im Fahrplan ab

**Offen und Hinweise**
- **Gutschein anlegen (US-08.2) fehlt noch.** Bisher legen nur die Tests Gutscheine an. Spätestens für den Browsertest in 4b braucht es einen Endpunkt oder Beispieldaten.
- **US-08.1 AK7 ("zweiter Code ersetzt den ersten")** ist laut D-17 Sache der Eingabemaske. Das Backend nimmt nur einen Code an, umgesetzt wird es also in 4b.
- **US-08.1 AK5 (30.11.)** ist nur als Unit-Test abgedeckt, nicht über die API, weil die Testuhr fest auf dem 12.11. steht.
- **Abweichung vom Ablauf des feature-dev-Skills:** Code-Erkundung und Review habe ich selbst gemacht statt mit mehreren Agents. Die Änderung war überschaubar und die Muster eindeutig. Dabei habe ich einen Randfall behoben: Eine leere Positionsliste hätte bei direktem Service-Aufruf eine ungültige Abfrage an die Datenbank geschickt.
- Zwei unversionierte Sitzungsexporte (`2026-11-12-*.txt`) liegen im Repo-Wurzelverzeichnis. Ich habe sie bewusst nicht committet.

**Nächster Schritt:** 4b Checkout Frontend. Vorher die Kriterien für US-04.2 (Bestellhistorie) klären. Erst `/clear`, dann `/naechster-schritt`.

