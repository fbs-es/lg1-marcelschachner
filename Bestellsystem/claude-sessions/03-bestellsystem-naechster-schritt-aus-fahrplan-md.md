## Session 3: /naechster-schritt aus fahrplan.md

*Projekt: bestellsystem · ID `3a3d0c07-fe15-4c29-88a4-6ef5c769ef4f` · 2026-11-12 08:54 – 2026-11-12 09:09 UTC*

### ❯ Nutzer

> /clear

### ❯ Nutzer

> /naechster-schritt

**⏺ Bash**

```bash
cat docs/fahrplan.md && ls docs && git status --short && head -50 2026-11-12-095422-command-messagenaechster-schrittcommand-messa.txt
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
… [gekürzt, 260 Zeilen gesamt]
````

**⏺ Bash**

```bash
cat epics/E02-*.md stories/US-02.1-*.md docs/ARCHITEKTUR.md && ls -R backend/src | head -80 && ls -a . .github/workflows && cat .github/workflows/*.yml && ls docs/adr
```

```
# E02 Produktkatalog & Lagerbestand

**Priorität:** MVP
**Akteure:** Shop-Mitarbeiter (Pflege), Kunde und System (Abfrage)
**Abhängig von:** E00

## Ziel

Produkte mit Titel, Listenpreis und Lagerbestand verwalten und jederzeit prüfen können, ob ein Produkt verfügbar ist.

## Umfang

- Produkte anlegen, Titel und Listenpreis ändern.
- Lagerbestand setzen (Wareneingang, Korrektur); nie negativ. → I-7
- Verfügbarkeitsprüfung `istVerfuegbar()` und `istVerfuegbar(menge)`. → D-09
- Preisänderungen wirken nicht auf bestehende Bestellungen. → D-10

## Nicht im Umfang

- Kategorien, Suche, Bilder, Varianten, Steuersätze.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-02.1](../stories/US-02.1-verfuegbarkeit-pruefen.md) | Verfügbarkeit eines Produkts prüfen | ausformuliert |
| US-02.2 | Produkt anlegen und Listenpreis pflegen | Kandidat |
| US-02.3 | Lagerbestand buchen | Kandidat |
# US-02.1 Verfügbarkeit eines Produkts prüfen

**Epic:** E02 Produktkatalog & Lagerbestand
**Entscheidungen:** D-09

## Story

**Als** Kunde
**möchte ich** sehen, ob ein Produkt aktuell verfügbar ist,
**damit** ich nur bestelle, was auch geliefert werden kann.

## Akzeptanzkriterien
… [gekürzt, 314 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/01-entscheidungen.md && grep -n -i -B2 -A25 "produkt" docs/02-domaenenmodell.md | head -120
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
… [gekürzt, 266 Zeilen gesamt]
````

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && cat build.gradle.kts src/main/resources/application.yml src/main/java/de/bestellsystem/gemeinsam/domain/Geld.java src/main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java src/main/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiKonfiguration.java; ls -R src/test
```

```
plugins {
    java
    id("org.springframework.boot") version "3.5.16"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.11.0"
}

group = "de.bestellsystem"
version = "0.1.0-SNAPSHOT"
description = "Bestellsystem: REST-Backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.10.0")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
… [gekürzt, 293 Zeilen gesamt]
```

**⏺ Skill**

```
{
 "skill": "feature-dev:feature-dev",
 "args": "Setze Schritt 2 aus docs/fahrplan.md um: Produktkatalog (E02) als erster Durchstich durch alle Schichten. Backend: Produkt-Entity, Migration, Repository, Service, REST-Endpunkte, US-02.1 vollständig. Frontend: Angular-Gerüst unter frontend/ nach CLAUDE.md, Routing, Layout, generierter API-Client, Produktliste mit Verfügbarkeit, Produkt anlegen."
}
```

```
Launching skill: feature-dev:feature-dev
```

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Scope: \"Produkt anlegen\" und \"Bestand setzen\" (US-02.1 AK5) gehören eigentlich zu den Kandidaten US-02.2 und US-02.3. Welche Endpunkte soll Schritt 2 bauen?",
   "header": "Endpunkte",
   "multiSelect": false,
   "options": [
    {
     "label": "Minimal + Bestand (Recommended)",
     "description": "POST /produkte, GET /produkte, GET /produkte/{id}, PUT /produkte/{id}/lagerbestand (AK5), GET /produkte/{id}/verfuegbarkeit?menge=n (AK3/4). Ändern von Titel/Preis kommt später mit US-02.2."
    },
    {
     "label": "Nur Domain für AK3-5",
     "description": "POST + GET. istVerfuegbar(menge) und Bestand setzen nur im Domain-Modell mit Unit-Tests, ohne eigene Endpunkte."
    },
    {
     "label": "US-02.2/02.3 mitmachen",
     "description": "Erst die Akzeptanzkriterien für US-02.2 und US-02.3 gemeinsam festlegen, dann zusätzlich PUT für Titel und Listenpreis."
    }
   ]
  },
  {
   "question": "Validierung beim Anlegen eines Produkts: Welche Regeln gelten für Titel und Listenpreis?",
   "header": "Regeln",
   "multiSelect": false,
   "options": [
    {
     "label": "Titel 1-200, Preis ≥ 0 (Recommended)",
     "description": "Titel wird getrimmt, darf nicht leer sein, max. 200 Zeichen, muss nicht eindeutig sein. Listenpreis ≥ 0 Cent (0 erlaubt, z. B. für Gratisartikel). Anfangsbestand ≥ 0."
    },
… [gekürzt, 68 Zeilen gesamt]
```

```
Your questions have been answered: "Scope: "Produkt anlegen" und "Bestand setzen" (US-02.1 AK5) gehören eigentlich zu den Kandidaten US-02.2 und US-02.3. Welche Endpunkte soll Schritt 2 bauen?"="Minimal + Bestand (Recommended)", "Validierung beim Anlegen eines Produkts: Welche Regeln gelten für Titel und Listenpreis?"="Titel 1-200, Preis ≥ 0 (Recommended)", "Woraus soll `npm run generate:api` den Client erzeugen? Laut CLAUDE.md aus der laufenden OpenAPI, das braucht aber ein gestartetes Backend."="Committete Spec-Datei (Recommended)", "Welches UI-Styling soll das Frontend nutzen?"="Eigenes schlichtes CSS (Recommended)". You can now continue with these answers in mind.
```

**⏺ Bash**

```bash
cd /tmp && npm view @angular/cli dist-tags --json 2>&1 | head -30; npm view @openapitools/openapi-generator-cli version; curl -s https://plugins.gradle.org/m2/org/springdoc/openapi-gradle-plugin/org.springdoc.openapi-gradle-plugin.gradle.plugin/maven-metadata.xml | grep -o '<latest>[^<]*'; node -v; which google-chrome chromium chromium-browser 2>/dev/null
```

```
Exit code 1
{
  "v6-lts": "6.2.9",
  "v7-lts": "7.3.10",
  "v8-lts": "8.3.29",
  "v9-lts": "9.1.15",
  "v10-lts": "10.2.4",
  "v11-lts": "11.2.19",
  "v12-lts": "12.2.18",
  "v13-lts": "13.3.11",
  "v14-lts": "14.2.13",
  "v15-lts": "15.2.11",
  "v16-lts": "16.2.16",
  "v17-lts": "17.3.17",
  "v18-lts": "18.2.21",
  "v19-lts": "19.2.27",
  "v20-lts": "20.3.42",
  "v21-lts": "21.2.29",
  "next": "22.3.0-next.5",
  "latest": "22.2.6"
}
2.42.2
<latest>2.1.0
v22.20.0
/usr/bin/google-chrome
chromium not found
chromium-browser not found
```

### ● Claude

Angular: höchste LTS ist **21** (`v21-lts` 21.2.29; 22 ist aktiv, aber noch keine LTS). Angular 21 testet mit Vitest/jsdom, also braucht CI keinen Browser.

## Entwurf Schritt 2

Die Struktur gibt CLAUDE.md vor, darum ein Entwurf statt drei Varianten.

**Backend `de.bestellsystem.katalog`**
```
domain/Produkt            @Entity "produkt": id UUID (im Konstruktor vergeben, final → equals/hashCode über id),
                          titel, listenpreis Geld (listenpreis_cent), lagerbestand int, @Version version
                          Konstruktor prüft D-15 + I-7 → IllegalArgumentException (400)
                          istVerfuegbar(), istVerfuegbar(menge) (menge < 1 → IAE, AK4), setzeLagerbestand(n) (n < 0 → IAE, AK5)
persistence/ProduktRepository   JpaRepository<Produkt, UUID>, findAllByOrderByTitelAsc
application/ProduktService      legeAn, alle, finde (→ NichtGefundenException 404), setzeLagerbestand, istVerfuegbar
api/ProduktController           /api/v1/produkte
  POST   /                         ProduktAnlegenDto → 201 + Location, ProduktDto
  GET    /                         List<ProduktDto> (nach Titel)
  GET    /{id}                     ProduktDto
  PUT    /{id}/lagerbestand        LagerbestandDto → ProduktDto
  GET    /{id}/verfuegbarkeit?menge=n   VerfuegbarkeitDto {produktId, menge, istVerfuegbar}
gemeinsam/api/GeldDto     {cent: long, waehrung: "EUR"} (Pflichtfeld waehrung = EUR)
ProduktDto                {id, titel, listenpreis: GeldDto, lagerbestand, istVerfuegbar}
V2__produkt.sql           Tabelle produkt, CHECK listenpreis_cent ≥ 0, lagerbestand ≥ 0
OpenApiKonfiguration      feste Server-URL, Schlüssel sortiert → deterministische Spec
```

**OpenAPI-Spec als Datei** (ADR 0002)
- `OpenApiSpezifikationIT` (Testcontainers) holt `/v3/api-docs` und vergleicht mit `frontend/openapi.json`. Weicht sie ab, wird `build` rot.
- `./gradlew aktualisiereOpenApi` führt denselben Test mit Schreib-Flag aus und schreibt die Datei neu.

**Frontend `frontend/` (Angular 21, Standalone, Signals, Vitest)**
```
proxy.conf.json           /api → :8080
openapitools.json         Generator-Version gepinnt; generate:api: openapi.json → src/app/api (committet)
app.config.ts             Router, HttpClient, LOCALE_ID de-DE, DEFAULT_CURRENCY_CODE EUR, BASE_PATH ""
app (Layout)              Kopfzeile + Navigation "Katalog", <router-outlet>
gemeinsam/geld.pipe.ts    GeldDto → CurrencyPipe (Cent / 100, de-DE)
gemeinsam/euro-eingabe.ts "12,99" → 1299 Cent, reine String-Arithmetik ohne float
gemeinsam/problem-meldung.ts   ProblemDetail → lesbarer Text inkl. Feldfehler
katalog/produkt-liste     Route /produkte: Laden / Fehler / Leer / Tabelle (Titel, Preis, Bestand, Verfügbar)
katalog/produkt-anlegen   Route /produkte/neu: Reactive Form, Serverfehler anzeigen, danach zur Liste
```
Tests: Komponenten mit `HttpTestingController`, Helfer als reine Unit-Tests.

**Doku/CI**
- D-15 (Titel 1–200, getrimmt, nicht eindeutig; Listenpreis ≥ 0; Bestand ≥ 0)
- ADR 0002, CLAUDE.md-Befehlszeile zu generate:api, ARCHITEKTUR.md
- CI-Job `frontend`: `npm ci`, `generate:api` + `git diff --exit-code`, `npm test`, `npm run build`

Abweichung vom Fahrplan: keine. Ändern von Titel und Preis kommt erst mit US-02.2.

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "Entwurf Schritt 2 (Produkt-Entity + 5 Endpunkte, OpenAPI-Spec als committete Datei mit Drift-Test, Angular 21 mit Liste und Anlegen-Formular): so umsetzen?",
   "header": "Freigabe",
   "multiSelect": false,
   "options": [
    {
     "label": "Freigeben",
     "description": "Implementierung wie im Entwurf beschrieben starten."
    },
    {
     "label": "Ändern",
     "description": "Vor dem Start etwas am Entwurf anpassen (bitte im Freitext angeben)."
    }
   ]
  }
 ]
}
```

```
Your questions have been answered: "Entwurf Schritt 2 (Produkt-Entity + 5 Endpunkte, OpenAPI-Spec als committete Datei mit Drift-Test, Angular 21 mit Liste und Anlegen-Formular): so umsetzen?"="Freigeben". You can now continue with these answers in mind.
```

### ● Claude

Freigegeben. Als Erstes D-15, danach Backend testgetrieben.

**⏺ Bash**

```bash
cat src/test/java/de/bestellsystem/BestellsystemApplicationIT.java src/test/java/de/bestellsystem/TestcontainersKonfiguration.java src/main/java/de/bestellsystem/gemeinsam/domain/Anschrift.java src/main/java/de/bestellsystem/gemeinsam/domain/NichtGefundenException.java; head -40 src/test/java/de/bestellsystem/gemeinsam/domain/GeldTest.java; cat src/main/resources/db/migration/V1*; cat ../docs/adr/0001*; cat ../.gitignore
```

```
package de.bestellsystem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.gemeinsam.application.FachlicheEinstellungen;
import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestcontainersKonfiguration.class, FesteUhrKonfiguration.class})
class BestellsystemApplicationIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private Clock uhr;
    @Autowired private FachlicheEinstellungen einstellungen;

    @Test
    @DisplayName("E00: Ein Test kann Datum und Uhrzeit festlegen")
    void uhrIstImTestFestgelegt() {
        assertThat(uhr.instant()).isEqualTo(FesteUhrKonfiguration.JETZT);
        assertThat(LocalDate.now(uhr)).isEqualTo(LocalDate.of(2026, 3, 15));
    }

    @Test
    @DisplayName("D-03: Zahlungsziel 14 Tage, Mahnintervall 7 Tage")
    void fachlicheStandardwerte() {
        assertThat(einstellungen.zahlungsziel()).isEqualTo(Period.ofDays(14));
… [gekürzt, 224 Zeilen gesamt]
```

### ● Claude

Jetzt Doku-Entscheidung D-15 und Domain-Tests (Test zuerst).

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat >> docs/01-entscheidungen.md <<'EOF'

---

## D-15 Produktdaten beim Anlegen

**Problem:** Das Domänenmodell nennt `titel`, `listenpreis` und `lagerbestand`, aber keine Regeln für gültige Werte.

**Entscheidung:**
- `titel` wird getrimmt, ist Pflicht und höchstens **200 Zeichen** lang. Er muss **nicht eindeutig** sein.
- `listenpreis` ≥ 0 Cent (0 € ist erlaubt, z. B. Gratisartikel).
- `lagerbestand` ≥ 0 bereits beim Anlegen (I-7).
- Verstöße sind ungültige Eingaben (HTTP 400).

**Nicht im Umfang (Schritt 2):** Titel und Listenpreis ändern (US-02.2), Bestandsbuchungen mit Historie (US-02.3).
EOF
mkdir -p backend/src/{main,test}/java/de/bestellsystem/katalog/{domain,application,api,persistence}
cat > backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java <<'EOF'
package de.bestellsystem.katalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

… [gekürzt, 144 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew test --tests '*ProduktTest' -q 2>&1 | grep -E "error|FAIL" | head -5
```

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:15: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:16: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:34: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:44: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:54: error: cannot find symbol
```

### ● Claude

Rot wie erwartet. Jetzt die Entity:

**⏺ Bash**

```bash
cat > src/main/java/de/bestellsystem/katalog/domain/Produkt.java <<'EOF'
package de.bestellsystem.katalog.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
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

    @Version private long version;

    private String titel;

    @Embedded
    @AttributeOverride(name = "cent", column = @Column(name = "listenpreis_cent"))
    private Geld listenpreis;
… [gekürzt, 113 Zeilen gesamt]
```

```
rc=0
```

**⏺ Bash**

```bash
sed -i 's/@Version private long version;/\/\/ Wrapper-Typ: null kennzeichnet für Spring Data ein neues Produkt trotz vergebener UUID.\n    @Version private Long version;/' src/main/java/de/bestellsystem/katalog/domain/Produkt.java && grep -n -A1 "Wrapper" src/main/java/de/bestellsystem/katalog/domain/Produkt.java
cat > src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java <<'EOF'
package de.bestellsystem.katalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@SpringBootTest
… [gekürzt, 217 Zeilen gesamt]
```

```
23:    // Wrapper-Typ: null kennzeichnet für Spring Data ein neues Produkt trotz vergebener UUID.
24-    @Version private Long version;
```

### ● Claude

Ein Matcher in `anlegenLiefert201MitLocation` ist falsch geschrieben. Der Test prüft jetzt über `legeAn` (das die Location kontrolliert) und das getrimmte Ergebnis:

**⏺ Bash**

```bash
python3 - <<'EOF'
p='src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java'
s=open(p).read()
start=s.index('    @Test\n    void anlegenLiefert201MitLocation')
end=s.index('    @Test\n    void listeIstNachTitelSortiert')
s=s[:start]+'''    @Test
    @DisplayName("D-15: Anlegen liefert 201 mit Location, Titel getrimmt")
    void anlegenLiefert201MitLocation() throws Exception {
        String id = legeAn("  Tischlampe ", 2500, 2);

        mockMvc.perform(get(PRODUKTE + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titel").value("Tischlampe"))
                .andExpect(jsonPath("$.listenpreis.cent").value(2500));
    }

'''+s[end:]
open(p,'w').write(s)
EOF
```

```
(Bash completed with no output)
```

### ● Claude

Jetzt Migration, Repository, Service, DTOs und Controller:

**⏺ Bash**

```bash
B=src/main/java/de/bestellsystem && cat > src/main/resources/db/migration/V2__produkt.sql <<'EOF'
-- E02: Produktkatalog (D-09, D-15, I-7)
CREATE TABLE produkt (
    id               UUID         PRIMARY KEY,
    version          BIGINT       NOT NULL,
    titel            VARCHAR(200) NOT NULL,
    listenpreis_cent BIGINT       NOT NULL CHECK (listenpreis_cent >= 0),
    lagerbestand     INTEGER      NOT NULL CHECK (lagerbestand >= 0)
);

CREATE INDEX produkt_titel_idx ON produkt (titel);
EOF
cat > $B/katalog/persistence/ProduktRepository.java <<'EOF'
package de.bestellsystem.katalog.persistence;

import de.bestellsystem.katalog.domain.Produkt;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProduktRepository extends JpaRepository<Produkt, UUID> {

    List<Produkt> findAllByOrderByTitelAsc();
}
EOF
cat > $B/katalog/application/ProduktService.java <<'EOF'
package de.bestellsystem.katalog.application;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
… [gekürzt, 285 Zeilen gesamt]
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T09:59:50.076+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T09:59:50.078+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T09:59:50.089+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
2026-11-12T09:59:50.370+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T09:59:50.371+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown initiated...
2026-11-12T09:59:50.372+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-2 - Shutdown completed.
2026-11-12T09:59:50.589+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T09:59:50.590+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Shutdown initiated...
2026-11-12T09:59:50.591+01:00  INFO 875573 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-3 - Shutdown completed.
rc=0
```

**⏺ Bash**

```bash
python3 - <<'EOF'
import glob,xml.etree.ElementTree as E
t=f=s=0
for p in glob.glob('build/test-results/test/*.xml'):
    r=E.parse(p).getroot(); t+=int(r.get('tests'));f+=int(r.get('failures'))+int(r.get('errors'));s+=int(r.get('skipped'))
    if 'Produkt' in p: print(p.split('/')[-1], r.get('tests'), r.get('failures'), r.get('errors'))
print('total',t,'fail',f,'skip',s)
EOF
```

```
TEST-de.bestellsystem.katalog.api.ProduktApiIT.xml 11 0 0
TEST-de.bestellsystem.katalog.domain.ProduktTest.xml 15 0 0
total 71 fail 0 skip 0
```

### ● Claude

71 Tests grün. Jetzt deterministische OpenAPI und Drift-Test:

**⏺ Bash**

```bash
python3 - <<'EOF'
p='src/main/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiKonfiguration.java'
s=open(p).read()
s=s.replace('import io.swagger.v3.oas.models.info.Info;\n','import io.swagger.v3.oas.models.info.Info;\nimport io.swagger.v3.oas.models.servers.Server;\nimport java.util.List;\n')
s=s.replace('''                                                + " (EUR)."));''','''                                                + " (EUR)."))
                // Feste relative Server-URL statt des Request-Hosts: die exportierte Spec
                // (frontend/openapi.json) bleibt dadurch unabhängig vom Port reproduzierbar.
                .servers(List.of(new Server().url("/").description("Aktueller Host")));''')
open(p,'w').write(s)
p='src/main/resources/application.yml'
s=open(p).read()
s=s.replace('springdoc:\n','springdoc:\n  writer-with-order-by-keys: true\n')
open(p,'w').write(s)
EOF
cat > src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java <<'EOF'
package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.bestellsystem.TestcontainersKonfiguration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
… [gekürzt, 89 Zeilen gesamt]
```

```
39:tasks.withType<Test> {
40-    useJUnitPlatform()
41-}
42-
```

### ● Claude

Für die Spec braucht es `frontend/`. Angular-21-Gerüst anlegen:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && npx -y @angular/cli@21 new frontend --routing --style=css --ssr=false --skip-git --zoneless --ai-config=none --defaults 2>&1 | tail -15; ls frontend; cat frontend/package.json
```

```
CREATE frontend/.vscode/launch.json (470 bytes)
CREATE frontend/.vscode/mcp.json (179 bytes)
CREATE frontend/.vscode/tasks.json (978 bytes)
CREATE frontend/src/main.ts (222 bytes)
CREATE frontend/src/index.html (294 bytes)
CREATE frontend/src/styles.css (80 bytes)
CREATE frontend/src/app/app.css (0 bytes)
CREATE frontend/src/app/app.spec.ts (675 bytes)
CREATE frontend/src/app/app.ts (290 bytes)
CREATE frontend/src/app/app.html (20144 bytes)
CREATE frontend/src/app/app.config.ts (313 bytes)
CREATE frontend/src/app/app.routes.ts (77 bytes)
CREATE frontend/public/favicon.ico (15086 bytes)
- Installing packages (npm)...
[32m✔[39m Packages installed successfully.
angular.json
node_modules
package.json
package-lock.json
public
README.md
src
tsconfig.app.json
tsconfig.json
tsconfig.spec.json
{
  "name": "frontend",
  "version": "0.0.0",
  "scripts": {
    "ng": "ng",
    "start": "ng serve",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test"
  },
  "private": true,
  "packageManager": "npm@11.6.3",
  "dependencies": {
    "@angular/common": "^21.2.0",
    "@angular/compiler": "^21.2.0",
… [gekürzt, 57 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend && rm -rf .vscode/mcp.json README.md && ls -a .vscode; cat angular.json | python3 -c "import json,sys;d=json.load(sys.stdin);print(json.dumps(d['projects']['frontend']['architect']['test'],indent=1));print(json.dumps(d['projects']['frontend']['architect']['serve'],indent=1))"; cat src/app/app.config.ts src/app/app.ts .prettierrc 2>/dev/null; ls -a
```

```
.
..
extensions.json
launch.json
tasks.json
{
 "builder": "@angular/build:unit-test"
}
{
 "builder": "@angular/build:dev-server",
 "configurations": {
  "production": {
   "buildTarget": "frontend:build:production"
  },
  "development": {
   "buildTarget": "frontend:build:development"
  }
 },
 "defaultConfiguration": "development"
}
import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes)
  ]
};
import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
… [gekürzt, 70 Zeilen gesamt]
```

**⏺ Bash**

```bash
rm .vscode/launch.json .vscode/tasks.json && cd ../backend && ./gradlew spotlessApply aktualisiereOpenApi -q 2>&1 | grep -iE "fail|error" | head; head -c 1500 ../frontend/openapi.json; grep -c . ../frontend/openapi.json
```

```
{
  "openapi" : "3.1.0",
  "info" : {
    "description" : "REST-API des Bestellsystems. Basis /api/v1. Fehler als ProblemDetail (RFC 9457), Geldbeträge in Cent (EUR).",
    "title" : "Bestellsystem API",
    "version" : "v1"
  },
  "servers" : [ {
    "url" : "/",
    "description" : "Aktueller Host"
  } ],
  "tags" : [ {
    "description" : "Produktkatalog und Lagerbestand (E02)",
    "name" : "Produkte"
  } ],
  "paths" : {
    "/api/v1/produkte" : {
      "get" : {
        "operationId" : "listeProdukte",
        "responses" : {
          "200" : {
            "content" : {
              "*/*" : {
                "schema" : {
                  "type" : "array",
                  "items" : {
                    "$ref" : "#/components/schemas/Produkt"
                  }
                }
              }
            },
            "description" : "OK"
          }
        },
        "summary" : "Alle Produkte, nach Titel sortiert",
        "tags" : [ "Produkte" ]
      },
      "post" : {
        "operationId" : "legeProduktAn",
        "requestBody" : {
… [gekürzt, 56 Zeilen gesamt]
```

### ● Claude

Spec gut, aber Inhaltstyp ist `*/*` und Pflichtfelder sind nicht markiert. Der generierte Client hätte damit nur optionale Felder. Korrektur:

**⏺ Bash**

```bash
B=src/main/java/de/bestellsystem && python3 - <<'EOF'
import re
B='src/main/java/de/bestellsystem/'
def sub(p,a,b):
    s=open(B+p).read(); assert a in s,(p,a); s=s.replace(a,b); open(B+p,'w').write(s)
sub('katalog/api/ProduktDto.java','@Schema(name = "Produkt")','@Schema(\n        name = "Produkt",\n        requiredProperties = {"id", "titel", "listenpreis", "lagerbestand", "istVerfuegbar"})')
sub('katalog/api/VerfuegbarkeitDto.java','@Schema(name = "Verfuegbarkeit", ','@Schema(\n        name = "Verfuegbarkeit",\n        requiredProperties = {"produktId", "menge", "istVerfuegbar"},\n        ')
# ProblemDetail media type
p='katalog/api/ProduktController.java'
s=open(B+p).read()
s=s.replace('content = @Content(schema = @Schema(implementation = ProblemDetail.class))','content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class))')
s=s.replace('    private final ProduktService service;','    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;\n\n    private final ProduktService service;')
s=s.replace('import org.springframework.http.ProblemDetail;','import org.springframework.http.MediaType;\nimport org.springframework.http.ProblemDetail;')
open(B+p,'w').write(s)
p='../resources/application.yml'
EOF
sed -i 's/  writer-with-order-by-keys: true/  writer-with-order-by-keys: true\n  default-produces-media-type: application\/json/' src/main/resources/application.yml
./gradlew spotlessApply aktualisiereOpenApi -q 2>&1 | grep -iE "fail|error" | head; python3 -c "
import json;d=json.load(open('../frontend/openapi.json'))
for k,v in d['components']['schemas'].items(): print(k, v.get('required'))
print(list(d['paths']['/api/v1/produkte']['post']['responses']['201']['content']), list(d['paths']['/api/v1/produkte']['post']['responses']['400']['content']))
print(json.dumps(d['paths']['/api/v1/produkte/{produktId}/verfuegbarkeit']['get']['parameters']))"
```

```
Eingabe ['name']
Geld ['cent', 'waehrung']
Lagerbestand ['lagerbestand']
ProblemDetail None
Produkt ['id', 'istVerfuegbar', 'lagerbestand', 'listenpreis', 'titel']
ProduktAnlegen ['lagerbestand', 'listenpreis', 'titel']
Verfuegbarkeit ['istVerfuegbar', 'menge', 'produktId']
['application/json'] ['application/problem+json']
[{"in": "path", "name": "produktId", "required": true, "schema": {"type": "string", "format": "uuid"}}, {"description": "Gew\u00fcnschte Menge, mindestens 1", "in": "query", "name": "menge", "required": true, "schema": {"type": "integer", "format": "int32"}}]
```

### ● Claude

Schema `Eingabe` kommt aus dem Test-Controller `FehlerAusloesenderController`, der im Test-Kontext mitgescannt wird. Er darf nicht in der exportierten Spec landen:

**⏺ Bash**

```bash
head -40 src/test/java/de/bestellsystem/gemeinsam/api/FehlerAusloesenderController.java; head -30 src/test/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlungTest.java; python3 -c "
import json;d=json.load(open('../frontend/openapi.json'));print(list(d['paths']))"
```

```
package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Nur für Tests: löst jede Fehlerart aus, die die zentrale Fehlerbehandlung übersetzt. */
@RestController
@RequestMapping("/test/fehler")
class FehlerAusloesenderController {

    record Eingabe(@NotBlank String name, @Positive int menge) {}

    @GetMapping("/nicht-gefunden")
    void nichtGefunden() {
        throw new NichtGefundenException("Kunde 42 existiert nicht.");
    }

    @GetMapping("/konflikt")
    void konflikt() {
        throw new KonfliktException("E-Mail-Adresse ist bereits vergeben.");
    }

    @GetMapping("/versionskonflikt")
    void versionskonflikt() {
        throw new ObjectOptimisticLockingFailureException(Object.class, "id");
    }

    @GetMapping("/abgelehnt")
    void abgelehnt() {
        throw new FachlicheAblehnungException("Gutschein ist abgelaufen.");
… [gekürzt, 71 Zeilen gesamt]
```

### ● Claude

`springdoc.paths-to-match: /api/**` begrenzt die Spec auf die echte API. Das gilt auch im Betrieb:

**⏺ Bash**

```bash
sed -i 's/  default-produces-media-type: application\/json/  default-produces-media-type: application\/json\n  paths-to-match: \/api\/**/' src/main/resources/application.yml && sed -n '/springdoc/,/^$/p' src/main/resources/application.yml && ./gradlew aktualisiereOpenApi -q 2>&1 | grep -iE "fail|error"; python3 -c "
import json;d=json.load(open('../frontend/openapi.json'));print(list(d['paths']), list(d['components']['schemas']))"
```

```
springdoc:
  writer-with-order-by-keys: true
  default-produces-media-type: application/json
  paths-to-match: /api/**
  swagger-ui:
    path: /swagger-ui.html

['/api/v1/produkte', '/api/v1/produkte/{produktId}', '/api/v1/produkte/{produktId}/lagerbestand', '/api/v1/produkte/{produktId}/verfuegbarkeit'] ['Geld', 'Lagerbestand', 'ProblemDetail', 'Produkt', 'ProduktAnlegen', 'Verfuegbarkeit']
```

### ● Claude

Spec sauber. Jetzt den Client-Generator einrichten:

**⏺ Bash**

```bash
curl -s https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/maven-metadata.xml | grep -oE '<(latest|release)>[^<]*'; cd /home/mschachner/Projects/bestellsystem/frontend && npm i -D @openapitools/openapi-generator-cli@2.42.2 2>&1 | tail -2
```

```
<latest>7.27.0
<release>7.27.0

Run `npm audit` for details.
```

**⏺ Bash**

```bash
cat > openapitools.json <<'EOF'
{
  "$schema": "./node_modules/@openapitools/openapi-generator-cli/config.schema.json",
  "spaces": 2,
  "generator-cli": {
    "version": "7.27.0",
    "generators": {
      "bestellsystem": {
        "generatorName": "typescript-angular",
        "inputSpec": "openapi.json",
        "output": "src/app/api",
        "additionalProperties": {
          "ngVersion": "21.2.0",
          "providedIn": "root",
          "fileNaming": "kebab-case",
          "enumPropertyNaming": "original",
          "stringEnums": true,
          "supportsES6": true
        },
        "globalProperty": {
          "skipFormModel": "false"
        }
      }
    }
  }
}
EOF
npm pkg set scripts.generate:api="openapi-generator-cli generate --generator-key bestellsystem && prettier --write src/app/api --log-level warn"
npm pkg set scripts.start="ng serve --proxy-config proxy.conf.json"
cat > proxy.conf.json <<'EOF'
… [gekürzt, 38 Zeilen gesamt]
```

```
[[bestellsystem] openapi.json] # We appreciate your support! Please consider donating to help us maintain this project.   #
[[bestellsystem] openapi.json] # https://opencollective.com/openapi_generator/donate                                      #
[[bestellsystem] openapi.json] ############################################################################################
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,enumPropertyNaming=original,stringEnums=true,supportsES6=true" --global-property="skipFormModel=false" exited with code 0
[bestellsystem] openapi.json
src/app/api/api/api.ts
src/app/api/api.base.service.ts
src/app/api/api.module.ts
src/app/api/api/produkte.service.ts
src/app/api/configuration.ts
src/app/api/encoder.ts
src/app/api/.gitignore
src/app/api/git_push.sh
src/app/api/index.ts
src/app/api/model/geld.ts
src/app/api/model/lagerbestand.ts
src/app/api/model/models.ts
src/app/api/model/problem-detail.ts
src/app/api/model/produkt-anlegen.ts
src/app/api/model/produkt.ts
src/app/api/model/verfuegbarkeit.ts
src/app/api/.openapi-generator/FILES
src/app/api/.openapi-generator-ignore
src/app/api/.openapi-generator/VERSION
src/app/api/param.ts
src/app/api/provide-api.ts
src/app/api/query.params.ts
src/app/api/README.md
src/app/api/variables.ts
/**
 * Bestellsystem API
 *
 *
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
/* tslint:disable:no-unused-variable member-ordering */

… [gekürzt, 109 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat src/app/api/.gitignore src/app/api/.openapi-generator-ignore | grep -v '^#' ; grep -n "basePath" src/app/api/api.base.service.ts | head; cat src/app/api/provide-api.ts; cat src/app/api/model/produkt.ts | tail -12
```

```
wwwroot/*.js
node_modules
typings
dist





16:  protected basePath = 'http://localhost';
21:  constructor(basePath?: string | string[], configuration?: Configuration) {
23:    if (typeof this.configuration.basePath !== 'string') {
24:      const firstBasePath = Array.isArray(basePath) ? basePath[0] : undefined;
26:        basePath = firstBasePath;
29:      if (typeof basePath !== 'string') {
30:        basePath = this.basePath;
32:      this.configuration.basePath = basePath;
import { EnvironmentProviders, makeEnvironmentProviders } from '@angular/core';
import { Configuration, ConfigurationParameters } from './configuration';
import { BASE_PATH } from './variables';

// Returns the service class providers, to be used in the [ApplicationConfig](https://angular.dev/api/core/ApplicationConfig).
export function provideApi(
  configOrBasePath: string | ConfigurationParameters,
): EnvironmentProviders {
  return makeEnvironmentProviders([
    typeof configOrBasePath === 'string'
      ? { provide: BASE_PATH, useValue: configOrBasePath }
      : {
          provide: Configuration,
          useValue: new Configuration({ ...configOrBasePath }),
        },
  ]);
}
import { Geld } from './geld';

export interface Produkt {
  id: string;
  /**
   * Lagerbestand > 0 (D-09)
… [gekürzt, 46 Zeilen gesamt]
```

**⏺ Bash**

```bash
python3 - <<'EOF'
import json
d=json.load(open('openapitools.json'))
g=d['generator-cli']['generators']['bestellsystem']
g.pop('globalProperty')
g['ignoreFileOverride']='openapi-generator-ignore'
json.dump(d,open('openapitools.json','w'),indent=2,ensure_ascii=False); open('openapitools.json','a').write('\n')
EOF
cat > openapi-generator-ignore <<'EOF'
# Nicht benötigte Dateien des typescript-angular-Generators (ADR 0002)
.gitignore
git_push.sh
README.md
api.module.ts
EOF
npm pkg set scripts.generate:api="rm -rf src/app/api && openapi-generator-cli generate --generator-key bestellsystem && prettier --write src/app/api --log-level warn"
npm run generate:api >/dev/null 2>&1; find src/app/api -type f | sort | tr '\n' ' '; grep -rn "api.module" src/app/api | head -3
```

```
src/app/api/api/api.ts src/app/api/api.base.service.ts src/app/api/api.module.ts src/app/api/api/produkte.service.ts src/app/api/configuration.ts src/app/api/encoder.ts src/app/api/.gitignore src/app/api/git_push.sh src/app/api/index.ts src/app/api/model/geld.ts src/app/api/model/lagerbestand.ts src/app/api/model/models.ts src/app/api/model/problem-detail.ts src/app/api/model/produkt-anlegen.ts src/app/api/model/produkt.ts src/app/api/model/verfuegbarkeit.ts src/app/api/.openapi-generator/FILES src/app/api/.openapi-generator-ignore src/app/api/.openapi-generator/VERSION src/app/api/param.ts src/app/api/provide-api.ts src/app/api/query.params.ts src/app/api/README.md src/app/api/variables.ts src/app/api/.openapi-generator/FILES:5:api.module.ts
src/app/api/index.ts:5:export * from './api.module';
```

**⏺ Bash**

```bash
python3 - <<'EOF'
import json
d=json.load(open('openapitools.json'))
d['generator-cli']['generators']['bestellsystem'].pop('ignoreFileOverride')
json.dump(d,open('openapitools.json','w'),indent=2,ensure_ascii=False); open('openapitools.json','a').write('\n')
EOF
sed -i '/api.module.ts/d' openapi-generator-ignore
npm pkg set scripts.generate:api="rm -rf src/app/api && openapi-generator-cli generate --generator-key bestellsystem --ignore-file-override=openapi-generator-ignore && prettier --write src/app/api --log-level warn"
npm run generate:api 2>&1 | grep -iE "error|exited" ; find src/app/api -type f | sort | tr '\n' ' '
```

```
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,enumPropertyNaming=original,stringEnums=true,supportsES6=true" exited with code 0
src/app/api/api/api.ts src/app/api/api.base.service.ts src/app/api/api.module.ts src/app/api/api/produkte.service.ts src/app/api/configuration.ts src/app/api/encoder.ts src/app/api/.gitignore src/app/api/git_push.sh src/app/api/index.ts src/app/api/model/geld.ts src/app/api/model/lagerbestand.ts src/app/api/model/models.ts src/app/api/model/problem-detail.ts src/app/api/model/produkt-anlegen.ts src/app/api/model/produkt.ts src/app/api/model/verfuegbarkeit.ts src/app/api/.openapi-generator/FILES src/app/api/.openapi-generator-ignore src/app/api/.openapi-generator/VERSION src/app/api/param.ts src/app/api/provide-api.ts src/app/api/query.params.ts src/app/api/README.md src/app/api/variables.ts
```

### ● Claude

Mit `--generator-key` reicht der Wrapper den Ignore-Parameter nicht durch. Den Aufwand lohnt das nicht. Ich übernehme die Generator-Ausgabe unverändert, sie ist harmlos:

**⏺ Bash**

```bash
rm openapi-generator-ignore && npm pkg set scripts.generate:api="rm -rf src/app/api && openapi-generator-cli generate --generator-key bestellsystem && prettier --write src/app/api --log-level warn" && npm run generate:api >/dev/null 2>&1 && md5sum src/app/api/api/produkte.service.ts && npm run generate:api >/dev/null 2>&1 && md5sum src/app/api/api/produkte.service.ts && cat openapitools.json && cat package.json | grep -A8 scripts
```

```
2268c12ad5479d010a95c1fe2c1c21e1  src/app/api/api/produkte.service.ts
2268c12ad5479d010a95c1fe2c1c21e1  src/app/api/api/produkte.service.ts
{
  "$schema": "./node_modules/@openapitools/openapi-generator-cli/config.schema.json",
  "spaces": 2,
  "generator-cli": {
    "version": "7.27.0",
    "generators": {
      "bestellsystem": {
        "generatorName": "typescript-angular",
        "inputSpec": "openapi.json",
        "output": "src/app/api",
        "additionalProperties": {
          "ngVersion": "21.2.0",
          "providedIn": "root",
          "fileNaming": "kebab-case",
          "enumPropertyNaming": "original",
          "stringEnums": true,
          "supportsES6": true
        }
      }
    }
  }
}
  "scripts": {
    "ng": "ng",
    "start": "ng serve --proxy-config proxy.conf.json",
    "build": "ng build",
    "watch": "ng build --watch --configuration development",
    "test": "ng test",
    "generate:api": "rm -rf src/app/api && openapi-generator-cli generate --generator-key bestellsystem && prettier --write src/app/api --log-level warn"
  },
  "private": true,
```

### ● Claude

Generator läuft reproduzierbar (zwei Läufe, identischer Hash). Jetzt der Frontend-Code mit Tests:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && mkdir -p gemeinsam katalog/produkt-liste katalog/produkt-anlegen && rm app.html
cat > app.config.ts <<'EOF'
import { DEFAULT_CURRENCY_CODE, LOCALE_ID, ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';

import { provideApi } from './api';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(),
    // Relativ zum Host: lokal leitet proxy.conf.json /api an das Backend weiter.
    provideApi(''),
    { provide: LOCALE_ID, useValue: 'de-DE' },
    { provide: DEFAULT_CURRENCY_CODE, useValue: 'EUR' },
  ],
};
EOF
cat > app.routes.ts <<'EOF'
import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'produkte' },
  {
    path: 'produkte',
    title: 'Produkte',
    loadComponent: () => import('./katalog/produkt-liste/produkt-liste').then((m) => m.ProduktListe),
… [gekürzt, 328 Zeilen gesamt]
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
sed -i 's/import { provideRouter, withComponentInputBinding } from/import { provideRouter } from/; s/provideRouter(routes, withComponentInputBinding())/provideRouter(routes)/' app.config.ts
cat > katalog/produkt-liste/produkt-liste.ts <<'EOF'
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Produkt, ProdukteService } from '../../api';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';

@Component({
  selector: 'app-produkt-liste',
  imports: [RouterLink, GeldPipe],
  templateUrl: './produkt-liste.html',
  styleUrl: './produkt-liste.css',
})
export class ProduktListe {
  private readonly produkteService = inject(ProdukteService);

  protected readonly zustand = signal<Zustand>('laedt');
  protected readonly produkte = signal<Produkt[]>([]);
  protected readonly fehlermeldung = signal('');

  constructor() {
    this.laden();
  }

  protected laden(): void {
    this.zustand.set('laedt');
… [gekürzt, 440 Zeilen gesamt]
```

```
    src/app/api/model/geld.ts:22:2:
[37m      22 │   [32mwaehrung[37m: GeldWaehrungEnum;
         ╵   [32m~~~~~~~~[0m

  The expected type comes from property 'waehrung' which is declared here on type 'Geld'

    src/app/api/model/geld.ts:22:2:
[37m      22 │   [32mwaehrung[37m: GeldWaehrungEnum;
         ╵   [32m~~~~~~~~[0m

  The expected type comes from property 'waehrung' which is declared here on type 'Geld'

    src/app/api/model/geld.ts:22:2:
[37m      22 │   [32mwaehrung[37m: GeldWaehrungEnum;
         ╵   [32m~~~~~~~~[0m


[31m✘ [41;31m[[41;97mERROR[41;31m][0m [1mTS2322: Type '"EUR"' is not assignable to type 'GeldWaehrungEnum'.[0m [1m[35m[plugin angular-compiler][0m

    src/app/katalog/produkt-liste/produkt-liste.spec.ts:12:29:
[37m      12 │   listenpreis: { cent: 4999, [32mwaehrung[37m: 'EUR' },
         ╵                              [32m~~~~~~~~[0m

  The expected type comes from property 'waehrung' which is declared here on type 'Geld'

    src/app/api/model/geld.ts:22:2:
[37m      22 │   [32mwaehrung[37m: GeldWaehrungEnum;
         ╵   [32m~~~~~~~~[0m
```

### ● Claude

`stringEnums` macht aus `waehrung` ein TS-`enum`. Ein String-Literal-Typ ist schlanker:

**⏺ Bash**

```bash
sed -i '/"stringEnums": true,/d; /"enumPropertyNaming": "original",/d' openapitools.json && npm run generate:api >/dev/null 2>&1 && cat src/app/api/model/geld.ts | tail -12 && npm test -- --watch=false 2>&1 | tail -30
```

```
  cent: number;
  /**
   * Währung, immer EUR
   */
  waehrung: Geld.WaehrungEnum;
}
export namespace Geld {
  export const WaehrungEnum = {
    Eur: 'EUR',
  } as const;
  export type WaehrungEnum = (typeof WaehrungEnum)[keyof typeof WaehrungEnum];
}
✔ Building...
Initial chunk files                                 | Names                                            |  Raw size
chunk-J3TUJYAW.js                                   | -                                                |  25.86 kB | 
spec-app-katalog-produkt-liste-produkt-liste.js     | spec-app-katalog-produkt-liste-produkt-liste     |  12.98 kB | 
spec-app-katalog-produkt-anlegen-produkt-anlegen.js | spec-app-katalog-produkt-anlegen-produkt-anlegen |  12.41 kB | 
chunk-RSTWSNOR.js                                   | -                                                |   4.75 kB | 
spec-app-app.js                                     | spec-app-app                                     |   3.67 kB | 
init-testbed.js                                     | init-testbed                                     |   1.21 kB | 
spec-app-gemeinsam-problem-meldung.js               | spec-app-gemeinsam-problem-meldung               |   1.10 kB | 
spec-app-gemeinsam-euro-eingabe.js                  | spec-app-gemeinsam-euro-eingabe                  |   1.02 kB | 
chunk-NWOHG2P4.js                                   | -                                                | 974 bytes | 
vitest-mock-patch.js                                | vitest-mock-patch                                | 974 bytes | 
styles.css                                          | styles                                           | 917 bytes | 
chunk-OZ3KKVEV.js                                   | -                                                | 815 bytes | 
spec-app-gemeinsam-geld.pipe.js                     | spec-app-gemeinsam-geld.pipe                     | 651 bytes | 
chunk-HZLG5MLY.js                                   | -                                                | 516 bytes | 

                                                    | Initial total                                    |  67.86 kB

Application bundle generation complete. [1.839 seconds] - 2026-11-12T09:05:18.662Z


[1m[30m[46m RUN [49m[39m[22m [36mv4.1.12 [39m[90m/home/mschachner/Projects/bestellsystem/frontend[39m


[2m Test Files [22m [1m[32m6 passed[39m[22m[90m (6)[39m
[2m      Tests [22m [1m[32m29 passed[39m[22m[90m (29)[39m
[2m   Start at [22m 10:05:18
… [gekürzt, 41 Zeilen gesamt]
```

### ● Claude

29 Frontend-Tests grün. Jetzt Prod-Build und kompletter Backend-Build (inkl. Drift-Check):

**⏺ Bash**

```bash
npm run build 2>&1 | tail -6; cd ../backend && ./gradlew build -q 2>&1 | grep -E "FAIL|error|violations" | head; echo rc=${PIPESTATUS[0]}; ls build/test-results/test | wc -l
```

```
chunk-JYVUELIV.js   | -               | 482 bytes |               482 bytes

Application bundle generation complete. [4.152 seconds] - 2026-11-12T09:05:31.850Z

Output location: /home/mschachner/Projects/bestellsystem/frontend/dist/frontend

rc=
12
```

**⏺ Bash**

```bash
./gradlew build > /tmp/build.log 2>&1; echo rc=$?; tail -3 /tmp/build.log; python3 - <<'EOF'
import glob,xml.etree.ElementTree as E
t=f=0
for p in glob.glob('build/test-results/test/*.xml'):
    r=E.parse(p).getroot(); t+=int(r.get('tests'));f+=int(r.get('failures'))+int(r.get('errors'))
print('total',t,'fail',f)
EOF
```

```
rc=0
BUILD SUCCESSFUL in 1s
9 actionable tasks: 9 up-to-date
Consider enabling configuration cache to speed up this build: https://docs.gradle.org/9.9.1/userguide/configuration_cache_enabling.html
total 72 fail 0
```

### ● Claude

Build grün, 72 Backend-Tests. Ändert sich `frontend/openapi.json`, muss `test` neu laufen. Die Datei trage ich deshalb als Eingabe ein, sonst meldet Gradle fälschlich "up-to-date":

**⏺ Bash**

```bash
python3 - <<'EOF'
p='build.gradle.kts'
s=open(p).read()
s=s.replace('''tasks.withType<Test> {
    useJUnitPlatform()
}''','''tasks.withType<Test> {
    useJUnitPlatform()
}

// OpenApiSpezifikationIT vergleicht gegen diese Datei; Änderungen daran sollen Tests neu auslösen.
tasks.test {
    inputs.file("../frontend/openapi.json").withPropertyName("openApiSpezifikation").optional()
}''')
open(p,'w').write(s)
EOF
cd .. && docker compose up -d 2>&1 | tail -2
```

```
 Container bestellsystem-postgres-1 Running 
 Container bestellsystem-mailpit-1 Running
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew bootRun > /tmp/bootrun.log 2>&1
```

```
Command running in background with ID: b0gwzwhje. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/1993b7ef-13c1-476c-8dcf-bed3151f182a/tasks/b0gwzwhje.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend && npm start > /tmp/ngserve.log 2>&1
```

```
Command running in background with ID: bbzmtd420. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/1993b7ef-13c1-476c-8dcf-bed3151f182a/tasks/bbzmtd420.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

### ● Claude

Während Server starten: Doku (ADR 0002, CLAUDE.md, CI).

**⏺ Bash**

```bash
cat > docs/adr/0002-openapi-spezifikation-als-datei.md <<'EOF'
# ADR 0002: OpenAPI-Spezifikation als versionierte Datei

**Status:** angenommen (2026-11-12)

## Kontext

Das Frontend ruft das Backend nur über einen aus der OpenAPI erzeugten Client auf (`typescript-angular`). Erzeugt man den Client aus der laufenden API (`/v3/api-docs`), braucht das ein gestartetes Backend mit Datenbank. Dann ist die Erzeugung weder in CI reproduzierbar noch offline möglich.

## Entscheidung

- Die Spezifikation liegt versioniert unter **`frontend/openapi.json`**.
- **`./gradlew aktualisiereOpenApi`** schreibt sie neu. Dafür startet der Task den Spring-Kontext mit Testcontainers und liest `/v3/api-docs`.
- **`OpenApiSpezifikationIT`** läuft in `./gradlew build` und vergleicht die Datei mit der aktuellen API. Weicht sie ab, ist der Build rot.
- Damit die Datei deterministisch ist:
  - Schlüssel werden sortiert (`springdoc.writer-with-order-by-keys`).
  - Die Server-URL ist fest `/`.
  - Nur Pfade unter `/api/**` kommen in die Spec, keine Test-Controller.
- **`npm run generate:api`** erzeugt `frontend/src/app/api/` aus der Datei:
  - Die Generator-Version ist in `openapitools.json` gepinnt.
  - Das Skript löscht das Ausgabeverzeichnis vorher und formatiert danach mit Prettier.
- Der generierte Client wird committet. CI erzeugt ihn neu und prüft mit `git diff --exit-code`, dass er unverändert ist.

## Konsequenzen

- Ablauf bei API-Änderungen: `./gradlew aktualisiereOpenApi`, dann `npm run generate:api`. Beides wird committet.
- `generate:api` braucht Java (der Generator ist ein JAR). Das ist lokal und in CI ohnehin vorhanden.
- Die exportierte Spec ist zugleich die API-Dokumentation. Schritt 9 kann sie zusätzlich als `docs/api/openapi.yaml` ablegen.
EOF
python3 - <<'EOF'
… [gekürzt, 70 Zeilen gesamt]
```

```
yaml-ok
  "tag_name": "v7.1.0",
```

**⏺ Bash**

```bash
sed -i 's/actions\/setup-node@v6/actions\/setup-node@v7/' .github/workflows/ci.yml; grep -m3 -E "Started|APPLICATION FAILED|Error" /tmp/bootrun.log; grep -m2 -E "Local:|error" /tmp/ngserve.log
```

```
2026-11-12T10:06:09.691+01:00  INFO 917268 --- [bestellsystem] [           main] d.b.BestellsystemApplication             : Started BestellsystemApplication in 3.31 seconds (process running for 3.598)
  ➜  Local:   http://localhost:4200/
```

### ● Claude

Beide Server laufen. Browser-Nachweis über Chrome:

**⏺ Skill**

```
{
 "skill": "claude-in-chrome"
}
```

```
Launching skill: claude-in-chrome
```

**⏺ ToolSearch**

```
{
 "query": "select:mcp__claude-in-chrome__tabs_context_mcp,mcp__claude-in-chrome__navigate,mcp__claude-in-chrome__computer,mcp__claude-in-chrome__read_page,mcp__claude-in-chrome__tabs_create_mcp,mcp__claude-in-chrome__tabs_close_mcp,mcp__claude-in-chrome__form_input,mcp__claude-in-chrome__find",
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

Chrome-Erweiterung ist nicht verbunden. Ersatz: Headless-Chrome per DevTools-Protokoll mit kleinem Node-Skript. Es füllt das Formular aus, sendet es ab, liest die Liste und macht Screenshots.

**⏺ Bash**

```bash
mkdir -p /tmp/e2e && cat > /tmp/e2e/durchklick.mjs <<'EOF'
import { spawn } from 'node:child_process';
import { writeFileSync } from 'node:fs';
const chrome = spawn('google-chrome', ['--headless=new', '--remote-debugging-port=9333', '--user-data-dir=/tmp/e2e/profil', '--window-size=1200,800', 'about:blank'], { stdio: 'ignore' });
const warte = (ms) => new Promise((r) => setTimeout(r, ms));
await warte(1500);
const ziele = await (await fetch('http://127.0.0.1:9333/json')).json();
const ws = new WebSocket(ziele.find((z) => z.type === 'page').webSocketDebuggerUrl);
await new Promise((r) => (ws.onopen = r));
let n = 0; const offen = new Map();
ws.onmessage = (e) => { const m = JSON.parse(e.data); if (offen.has(m.id)) { offen.get(m.id)(m.result); offen.delete(m.id); } };
const cdp = (method, params = {}) => new Promise((r) => { const id = ++n; offen.set(id, r); ws.send(JSON.stringify({ id, method, params })); });
const js = async (ausdruck) => (await cdp('Runtime.evaluate', { expression: ausdruck, awaitPromise: true, returnByValue: true })).result.value;
const bild = async (datei) => writeFileSync(datei, Buffer.from((await cdp('Page.captureScreenshot')).data, 'base64'));
const titel = 'Stehlampe E2E ' + Date.now();

await cdp('Page.navigate', { url: 'http://localhost:4200/produkte' }); await warte(2500);
console.log('Liste vorher:', await js(`document.querySelector('main').innerText`));
await js(`document.querySelector('a[href="/produkte/neu"]').click()`); await warte(1500);
console.log('URL Formular:', await js('location.pathname'));
await js(`(() => { const setze = (id, w) => { const f = document.getElementById(id); f.value = w; f.dispatchEvent(new Event('input')); f.dispatchEvent(new Event('blur')); };
  setze('titel', '${titel}'); setze('listenpreis', '49,99'); setze('lagerbestand', '3'); })()`);
await bild('/tmp/e2e/1-formular.png');
await js(`document.querySelector('button[type=submit]').click()`); await warte(2000);
console.log('URL nach Speichern:', await js('location.pathname'));
const zeile = await js(`[...document.querySelectorAll('tbody tr')].map(z => z.innerText).find(t => t.includes('${titel}'))`);
console.log('Neue Zeile:', JSON.stringify(zeile));
await bild('/tmp/e2e/2-liste.png');

// Fehlerfall: Backend-Ablehnung sichtbar? (leerer Titel nur aus Leerzeichen besteht Client-Validierung)
… [gekürzt, 39 Zeilen gesamt]
```

```
Liste vorher: Produkte
Produkt anlegen

Noch keine Produkte im Katalog.
URL Formular: /produkte/neu
URL nach Speichern: /produkte
Neue Zeile: "Stehlampe E2E 1794474420891\t49,99 €\t3\tverfügbar"
Fehleranzeige: "Ungültige Eingabe: Die Eingabe ist ungültig.\ntitel: must not be blank"
Shell cwd was reset to /home/mschachner/Projects/bestellsystem
```

**⏺ Read**

```
/tmp/e2e/2-liste.png
```

**⏺ Read**

```
/tmp/e2e/3-fehler.png
```

```
[image]
```

```
[image]
```

### ● Claude

Die Oberfläche passt. Für die deutschen Meldungen erst ein Test, der am jetzigen Code fehlschlagen muss:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
p='src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java'
s=open(p).read()
a='''                                .content(anlegenJson(" ", -1, -1)))'''
assert a in s
s=s.replace(a,'''                                .header("Accept-Language", "en-US")
                                .content(anlegenJson(" ", -1, -1)))''')
a='''                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("lagerbestand")));
    }'''
s=s.replace(a,'''                .andExpect(jsonPath("$.fehler[*].feld").value(hasItem("lagerbestand")))
                .andExpect(
                        jsonPath("$.fehler[?(@.feld == 'titel')].meldung")
                                .value(hasItem("darf nicht leer sein")));
    }''',1)
open(p,'w').write(s)
EOF
./gradlew test --tests '*ProduktApiIT' -q 2>&1 | grep -E "ProduktApiIT >|Expected|but" | head -4
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
./gradlew test --tests '*ProduktApiIT' > /tmp/t.log 2>&1; echo rc=$?; grep -E "FAILED|AssertionError" /tmp/t.log | head
```

```
rc=1
ProduktApiIT > D-15: leerer Titel, negativer Preis oder Bestand ergeben 400 mit Feldfehlern FAILED
    java.lang.AssertionError at ProduktApiIT.java:161
> Task :test FAILED
BUILD FAILED in 10s
```

### ● Claude

Rot wie erwartet. Fix: feste Locale `de`.

**⏺ Bash**

```bash
python3 - <<'EOF'
p='src/main/resources/application.yml'
s=open(p).read()
a='  mail:\n    host:'
s=s.replace(a,'''  web:
    # Deutscher Shop: Validierungsmeldungen immer deutsch, unabhängig von Accept-Language
    locale: de
    locale-resolver: fixed
'''+a,1)
open(p,'w').write(s)
EOF
./gradlew test --tests '*ProduktApiIT' > /tmp/t.log 2>&1; echo rc=$?
```

```
rc=0
```

### ● Claude

Grün. Jetzt ARCHITEKTUR.md aktualisieren:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat frontend/.gitignore | grep -v '^#' | grep . ; python3 - <<'EOF'
p='docs/ARCHITEKTUR.md'
s=open(p).read()
def r(a,b):
    global s
    assert a in s,a; s=s.replace(a,b,1)
r('Stand: Schritt 1 (Backend-Fundament, E00).','Stand: Schritt 2 (Produktkatalog E02, Frontend-Fundament).')
r('Browser (Angular, ab Schritt 2)','Browser (Angular 21, :4200, Proxy /api → :8080)')
r('- CI: `.github/workflows/ci.yml` führt `./gradlew build` auf `ubuntu-latest` aus (Docker für Testcontainers vorhanden).',
'''- CI: `.github/workflows/ci.yml` mit zwei Jobs auf `ubuntu-latest`:
  - `backend`: `./gradlew build` (Docker für Testcontainers vorhanden)
  - `frontend`: `npm ci`, `generate:api` plus `git diff --exit-code`, `npm test`, `npm run build`''')
r('''| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`) |''',
'''| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`; prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |

## REST-API

OpenAPI unter `/v3/api-docs`, Swagger UI unter `/swagger-ui.html`. Exportiert nach `frontend/openapi.json` ([ADR 0002](adr/0002-openapi-spezifikation-als-datei.md)).

| Methode | Pfad | Ergebnis |
|---|---|---|
| POST | `/api/v1/produkte` | 201 + `Location`, 400 (D-15) |
| GET | `/api/v1/produkte` | alle Produkte nach Titel |
| GET | `/api/v1/produkte/{id}` | Produkt mit `istVerfuegbar`, 404 |
| PUT | `/api/v1/produkte/{id}/lagerbestand` | Bestand absolut setzen, 400 bei < 0, 404, 409 Versionskonflikt |
| GET | `/api/v1/produkte/{id}/verfuegbarkeit?menge=n` | `{produktId, menge, istVerfuegbar}`, 400 bei menge < 1, 404 |
… [gekürzt, 73 Zeilen gesamt]
```

```
/dist
/tmp
/out-tsc
/bazel-out
/node_modules
npm-debug.log
yarn-error.log
.idea/
.project
.classpath
.c9/
*.launch
.settings/
*.sublime-workspace
.vscode/*
!.vscode/settings.json
!.vscode/tasks.json
!.vscode/launch.json
!.vscode/extensions.json
!.vscode/mcp.json
.history/*
/.angular/cache
.sass-cache/
/connect.lock
/coverage
/libpeerconnection.log
testem.log
/typings
__screenshots__/
.DS_Store
Thumbs.db
```

### ● Claude

Abschluss-Check: kompletter Backend-Build, Frontend-Tests und Build, Reproduzierbarkeit des Clients.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew build > /tmp/build.log 2>&1; echo backend rc=$?; python3 - <<'EOF'
import glob,xml.etree.ElementTree as E
t=f=s=0
for p in glob.glob('build/test-results/test/*.xml'):
    r=E.parse(p).getroot(); t+=int(r.get('tests'));f+=int(r.get('failures'))+int(r.get('errors'));s+=int(r.get('skipped'))
print('backend tests',t,'fail',f,'skip',s)
EOF
cd ../frontend && find src/app/api -type f -exec md5sum {} + | sort > /tmp/vor.md5 && npm run generate:api >/dev/null 2>&1 && find src/app/api -type f -exec md5sum {} + | sort | diff - /tmp/vor.md5 && echo client-reproduzierbar; npm test -- --watch=false 2>&1 | grep -E "Test Files|Tests "; npm run build >/dev/null 2>&1; echo frontend-build rc=$?
```

```
backend rc=0
backend tests 72 fail 0 skip 0
client-reproduzierbar
[2m Test Files [22m [1m[32m6 passed[39m[22m[90m (6)[39m
[2m      Tests [22m [1m[32m29 passed[39m[22m[90m (29)[39m
frontend-build rc=0
```

### ● Claude

Alles grün. Dev-Server stoppen, dann gezielt stagen:

**⏺ Bash**

```bash
pkill -f "ng serve" ; pkill -f "BestellsystemApplication" ; pkill -f "gradlew bootRun"; cd /home/mschachner/Projects/bestellsystem && git add CLAUDE.md .github/workflows/ci.yml docs/01-entscheidungen.md docs/ARCHITEKTUR.md docs/adr/0002-openapi-spezifikation-als-datei.md backend/build.gradle.kts backend/src frontend && git status --short | grep -v '^A  frontend/src/app/api/' ; git status --short | grep -c 'frontend/src/app/api/'; git diff --cached --stat | tail -1; git diff --cached --name-only | grep -E "node_modules|dist|\.env|\.angular|\.log" 
```

```
Exit code 144
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && git add CLAUDE.md .github/workflows/ci.yml docs/01-entscheidungen.md docs/ARCHITEKTUR.md docs/adr/0002-openapi-spezifikation-als-datei.md backend/build.gradle.kts backend/src frontend && git status --short | grep -v 'frontend/src/app/api/' ; git diff --cached --stat | tail -1; git diff --cached --name-only | grep -E "node_modules|dist/|\.env|\.angular|\.log"; pgrep -fa "ng serve|BestellsystemApplication" | head -3
```

```
M  .github/workflows/ci.yml
M  CLAUDE.md
M  backend/build.gradle.kts
A  backend/src/main/java/de/bestellsystem/gemeinsam/api/GeldDto.java
M  backend/src/main/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiKonfiguration.java
A  backend/src/main/java/de/bestellsystem/katalog/api/LagerbestandDto.java
A  backend/src/main/java/de/bestellsystem/katalog/api/ProduktAnlegenDto.java
A  backend/src/main/java/de/bestellsystem/katalog/api/ProduktController.java
A  backend/src/main/java/de/bestellsystem/katalog/api/ProduktDto.java
A  backend/src/main/java/de/bestellsystem/katalog/api/VerfuegbarkeitDto.java
A  backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
A  backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
A  backend/src/main/java/de/bestellsystem/katalog/persistence/ProduktRepository.java
M  backend/src/main/resources/application.yml
A  backend/src/main/resources/db/migration/V2__produkt.sql
A  backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java
A  backend/src/test/java/de/bestellsystem/katalog/api/ProduktApiIT.java
A  backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
M  docs/01-entscheidungen.md
M  docs/ARCHITEKTUR.md
A  docs/adr/0002-openapi-spezifikation-als-datei.md
A  frontend/.editorconfig
A  frontend/.gitignore
A  frontend/.prettierrc
A  frontend/.vscode/extensions.json
A  frontend/angular.json
A  frontend/openapi.json
A  frontend/openapitools.json
A  frontend/package-lock.json
A  frontend/package.json
A  frontend/proxy.conf.json
A  frontend/public/favicon.ico
A  frontend/src/app/app.config.ts
A  frontend/src/app/app.css
A  frontend/src/app/app.routes.ts
A  frontend/src/app/app.spec.ts
A  frontend/src/app/app.ts
A  frontend/src/app/gemeinsam/euro-eingabe.spec.ts
A  frontend/src/app/gemeinsam/euro-eingabe.ts
A  frontend/src/app/gemeinsam/geld.pipe.spec.ts
… [gekürzt, 61 Zeilen gesamt]
```

### ● Claude

ng serve beendet (Exit 143 = von mir gestoppt). Backend-Prozess läuft noch, stoppe ihn. Staging sauber: keine Artefakte, Session-Export bleibt ungestaged.

**⏺ Bash**

```bash
kill 917268; cd /home/mschachner/Projects/bestellsystem && git commit -q -F - <<'EOF'
Add product catalog with Angular frontend (E02)

Backend: Produkt entity with availability checks and stock invariant,
V2 migration, service, REST endpoints under /api/v1/produkte.
US-02.1 AK1-5 covered by domain unit tests and API integration tests.
D-15 fixes title and price rules; validation messages are German.

Frontend: Angular 21 scaffold with layout, routing, product list
(loading, error, empty state) and create form. API client is generated
from the committed frontend/openapi.json (ADR 0002); a backend test
fails when the spec drifts. CI builds and tests both parts.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1
```

```
e1d663f Add product catalog with Angular frontend (E02)
```

**⏺ Bash**

```bash
sed -i 's/^## \[ \] 2 Fundament Frontend + Katalog (E02)$/## [x] 2 Fundament Frontend + Katalog (E02) (2026-11-12, e1d663f)/' docs/fahrplan.md && grep -n "^## \[x\] 2" docs/fahrplan.md && git add docs/fahrplan.md && git commit -q -m "Hake Schritt 2 im Fahrplan ab" -m "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -2 && git status --short
```

```
46:## [x] 2 Fundament Frontend + Katalog (E02) (2026-11-12, e1d663f)
06ccf74 Hake Schritt 2 im Fahrplan ab
e1d663f Add product catalog with Angular frontend (E02)
?? 2026-11-12-095422-command-messagenaechster-schrittcommand-messa.txt
```

### ● Claude

Schritt 2 (Produktkatalog E02 plus Frontend-Fundament) ist fertig und abgehakt. Backend-Build mit 72 Tests grün, Frontend 29/29 Tests grün, Prod-Build ok.

**Backend**
- Produkt-Entity mit `istVerfuegbar()`, `istVerfuegbar(menge)` und `setzeLagerbestand`. Ungültige Werte werden im Domain-Objekt abgelehnt.
- Migration `V2__produkt.sql` mit CHECK-Constraints in der Datenbank.
- Fünf Endpunkte unter `/api/v1/produkte`: anlegen, Liste, einzeln abrufen, Lagerbestand setzen, Verfügbarkeit für eine Menge prüfen.
- Die fünf Akzeptanzkriterien aus US-02.1 sind als Unit-Tests und als API-Tests gegen echtes PostgreSQL abgedeckt.
- Validierungsmeldungen kommen jetzt immer deutsch. Beim Durchklicken kam vorher "must not be blank", weil der Browser Englisch angefragt hatte.

**OpenAPI und Client** (ADR 0002)
- Die Spezifikation liegt als Datei in `frontend/openapi.json`. `./gradlew aktualisiereOpenApi` schreibt sie neu.
- Ein Backend-Test lässt den Build fehlschlagen, wenn die Datei nicht mehr zum Code passt.
- `npm run generate:api` erzeugt den Client aus dieser Datei, ohne laufendes Backend. Zwei Läufe ergeben identische Dateien.
- Weil sich der Ablauf ändert, habe ich die Befehlszeile in `CLAUDE.md` angepasst.

**Frontend**
- Angular 21, die höchste LTS-Version (22 ist aktuell, aber noch keine LTS).
- Seite `/produkte`: Liste mit Preis und Verfügbarkeit, zeigt Laden, Fehler mit "Erneut versuchen" und Leerzustand.
- Seite `/produkte/neu`: Formular zum Anlegen. Euro-Eingaben wie "49,99" werden ohne Gleitkomma in Cent umgerechnet, Fehler des Backends erscheinen lesbar.

**Doku und CI**
- Neue Entscheidung D-15: Titel 1–200 Zeichen und nicht eindeutig, Listenpreis ≥ 0.
- `ARCHITEKTUR.md` ist nachgezogen.
- CI hat einen neuen Job `frontend`: Client neu erzeugen und auf Abweichungen prüfen, Tests, Build.

**Commits:** `e1d663f` (Arbeit), `06ccf74` (Fahrplan abgehakt)

**Offen:**
- Der CI-Workflow ist auf GitHub noch nicht gelaufen (kein Push). Lokal laufen alle seine Schritte grün.
- Den Browser-Test habe ich mit Headless-Chrome gemacht, weil die Chrome-Erweiterung nicht verbunden war. Produkt anlegen und in der Liste sehen hat funktioniert. Einmal selbst durchklicken mit `npm start` lohnt trotzdem.
- Die drei Code-Review-Agenten aus feature-dev habe ich nicht gestartet, nur selbst geprüft. Wenn du ein Review willst, `/code-review`.
- `2026-11-12-095422-command-messagenaechster-schrittcommand-messa.txt` (ein Sitzungs-Export) liegt weiter uncommittet im Repo-Root. Ich habe die Datei bewusst nicht angefasst.

**Nächster Schritt:** 3, Kundenkonto und Adressbuch (E01). Vorher `/clear`, dann `/naechster-schritt`. Für US-01.2 und US-01.3 legen wir zuerst die Akzeptanzkriterien zusammen fest.

