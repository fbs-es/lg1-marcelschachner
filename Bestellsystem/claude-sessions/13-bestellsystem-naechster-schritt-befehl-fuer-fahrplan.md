## Session 13: /naechster-schritt Befehl für Fahrplan

*Projekt: bestellsystem · ID `e59b39b8-fc87-411b-a7c6-f46d2e59bb39` · 2026-11-12 11:35 – 2026-11-12 11:59 UTC*

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

### ● Claude

Schritt 9 dran (Doku, normaler Auftrag). Erst Bestand sichten.

**⏺ Bash**

```bash
ls -la && ls docs docs/adr epics stories && wc -l docs/*.md docs/adr/*.md && cat README.md 2>/dev/null | head -50; ls frontend; cat frontend/package.json | head -30; cat .github/workflows/*.yml; cat docker-compose.yml
```

```
total 76
drwxrwxr-x 10 mschachner mschachner 4096 Nov 12 11:21 .
drwxrwxr-x 49 mschachner mschachner 4096 Nov 12 09:25 ..
drwxrwxr-x  8 mschachner mschachner 4096 Nov 12 12:15 backend
drwxrwxr-x  3 mschachner mschachner 4096 Nov 12 09:36 .claude
-rw-rw-r--  1 mschachner mschachner 8264 Nov 12 10:06 CLAUDE.md
-rw-rw-r--  1 mschachner mschachner  659 Nov 12 09:51 docker-compose.yml
drwxrwxr-x  3 mschachner mschachner 4096 Nov 12 12:16 docs
-rw-rw-r--  1 mschachner mschachner  330 Nov 12 09:51 .env
-rw-rw-r--  1 mschachner mschachner  311 Nov 12 09:51 .env.example
drwxrwxr-x  2 mschachner mschachner 4096 Nov 12 12:04 epics
drwxrwxr-x  8 mschachner mschachner 4096 Nov 12 10:05 frontend
drwxrwxr-x  9 mschachner mschachner 4096 Nov 12 12:16 .git
-rw-rw-r--  1 mschachner mschachner  111 Nov 12 09:38 .gitattributes
drwxrwxr-x  3 mschachner mschachner 4096 Nov 12 09:51 .github
-rw-rw-r--  1 mschachner mschachner  789 Nov 12 11:21 .gitignore
-rw-rw-r--  1 mschachner mschachner 2861 Nov 12 09:36 README.md
drwxrwxr-x  2 mschachner mschachner 4096 Nov 12 12:04 stories
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
0003-pessimistische-sperre-im-checkout.md

epics:
E00-domaenenkern-plattform.md
E01-kundenkonto-adressbuch.md
E02-produktkatalog-lagerbestand.md
E03-merkliste.md
E04-bestellung-checkout.md
E05-zahlungen-erstattungen.md
E06-mahnwesen.md
E07-versand-sendungsverfolgung.md
… [gekürzt, 239 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat docs/ARCHITEKTUR.md
```

````
<persisted-output>
Output too large (33.8KB). Full output saved to: /home/mschachner/.claude/projects/-home-mschachner-Projects-bestellsystem/e59b39b8-fc87-411b-a7c6-f46d2e59bb39/tool-results/bwv426ue6.txt

Preview (first 2KB):
# Architektur

Stand: Schritt 8 (Versand E07: US-07.1 bis US-07.3, D-22). Wird mit jedem Schritt im selben Commit nachgezogen.

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
...
</persisted-output>
````

**⏺ Read**

```
/home/mschachner/.claude/projects/-home-mschachner-Projects-bestellsystem/e59b39b8-fc87-411b-a7c6-f46d2e59bb39/tool-results/bwv426ue6.txt
```

````
1	# Architektur
2	
3	Stand: Schritt 8 (Versand E07: US-07.1 bis US-07.3, D-22). Wird mit jedem Schritt im selben Commit nachgezogen.
4	
5	## Überblick
6	
7	```
8	Browser (Angular 21, :4200, Proxy /api → :8080)
9	   │  HTTP/JSON, /api/v1
10	   ▼
11	Spring Boot 3.5 (Java 21, Gradle)  ──SMTP──▶  Mailpit (lokal) / Mailserver
12	   │  JPA/Hibernate, JDBC
13	   ▼
14	PostgreSQL 16 (Schema per Flyway)
15	```
16	
17	- Build: Gradle 9.9.1 über `./gradlew`, Kotlin DSL ([ADR 0001](adr/0001-gradle-als-build-werkzeug.md)).
18	- `./gradlew build` = kompilieren, alle Tests (Unit + Testcontainers), Spotless-Formatprüfung.
19	- CI: `.github/workflows/ci.yml` mit zwei Jobs auf `ubuntu-latest`:
20	  - `backend`: `./gradlew build` (Docker für Testcontainers vorhanden)
21	  - `frontend`: `npm ci`, `generate:api` plus `git diff --exit-code`, `npm test`, `npm run build`
22	
23	## Pakete
24	
25	Schichten je Fachpaket: `api` → `application` → `domain`; `persistence` und `infrastruktur` implementieren Ports aus `application`.
26	
27	| Paket | Inhalt |
28	|---|---|
29	| `de.bestellsystem` | `BestellsystemApplication` (inkl. `@ConfigurationPropertiesScan`) |
30	| `gemeinsam.domain` | `Geld` (Cent `long`, EUR, `@Embeddable` Record), `Anschrift` (`@Embeddable` Record, ISO-3166-Alpha-2, Längen nach D-16), fachliche Ausnahmen `NichtGefundenException` (404), `KonfliktException` (409), `FachlicheAblehnungException` (422) |
31	| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung` (Fehler des Ports: `BenachrichtigungNichtVersendetException`), `BestellnummernGenerator` + Port `BestellnummernZaehler`, `FachlicheEinstellungen` (`bestellsystem.*`) |
32	| `gemeinsam.persistence` | `SequenzBestellnummernZaehler` (`nextval('bestellnummer_seq')` über `JdbcClient`) |
33	| `gemeinsam.infrastruktur` | `UhrKonfiguration` (`Clock`-Bean in konfigurierter Zeitzone), `MailBenachrichtigungsversand` (`JavaMailSender`, übersetzt `MailException`), `ZeitplanKonfiguration` (`@EnableScheduling`), `MailEinstellungen` (`bestellsystem.mail.*`), `OpenApiKonfiguration` |
34	| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |
35	| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |
36	| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |
37	| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |
38	| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
39	| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen), `fuehreMahnpruefungDurch(stichtag, bestellungen, mahnintervall)` (US-06.1, D-20: mahnt fällige Bestellungen, liefert `Optional<Mahnbenachrichtigung>`); setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
40	| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`), `sperreZumMahnen` (`PESSIMISTIC_WRITE`, D-20) |
… [gekürzt, 373 Zeilen gesamt]
````

**⏺ Bash**

```bash
cat docs/01-entscheidungen.md docs/adr/*.md docs/00-anforderung.md
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
… [gekürzt, 411 Zeilen gesamt]
````

### ● Claude

Doku-Bestand klar. Jetzt OpenAPI-Export-Mechanik und Frontend-Routen prüfen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && cat build.gradle.kts && find . -name 'OpenApiSpezifikationIT.java' -o -name 'OpenApiExport*' | grep -v build/ | xargs cat; cat ../frontend/src/app/app.routes.ts; ls src/main/resources src/main/resources/db/migration; cat src/main/resources/application.yml; cat ../.env.example; grep -ril gutschein src/main/resources/db | head
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
… [gekürzt, 234 Zeilen gesamt]
```

### ● Claude

Plan: `OpenApiSpezifikationIT` um YAML-Export nach `docs/api/openapi.yaml` erweitern (reproduzierbar, drift-geprüft). Gutschein hat keine API → Handbuch braucht SQL. Schema und UI-Texte holen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && sed -n 1,30p backend/src/main/resources/db/migration/V4__gutschein_bestellung_zahlung.sql; cat frontend/src/app/app.html 2>/dev/null || grep -n template -A30 frontend/src/app/app.ts; grep -rhoE '<button[^>]*>[^<]+|<h[12][^>]*>[^<]+|<label[^>]*>[^<]+' frontend/src/app --include=*.html | grep -v '/api/' | sort -u | head -80; ls backend/gradle 2>/dev/null; cd backend && ./gradlew -q dependencies --configuration testRuntimeClasspath | grep -i 'dataformat-yaml' | head -3
```

```
-- E04 + E08: Gutschein, Bestellung, Positionen, Zahlungen (D-01 bis D-05, D-08 bis D-11, D-17)
CREATE TABLE gutschein (
    id              UUID         PRIMARY KEY,
    -- gespeichert getrimmt und großgeschrieben (D-17), daher genügt ein einfacher Unique-Constraint
    code            VARCHAR(50)  NOT NULL CONSTRAINT gutschein_code_eindeutig UNIQUE,
    rabatt_art      VARCHAR(20)  NOT NULL CHECK (rabatt_art IN ('PROZENT', 'FESTBETRAG')),
    prozent         NUMERIC(5,2),
    festbetrag_cent BIGINT,
    ablaufdatum     DATE         NOT NULL,
    CONSTRAINT gutschein_rabattwert CHECK (
        (rabatt_art = 'PROZENT' AND prozent > 0 AND prozent <= 100 AND festbetrag_cent IS NULL)
        OR (rabatt_art = 'FESTBETRAG' AND festbetrag_cent > 0 AND prozent IS NULL))
);

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
9:  template: `
10-    <header class="kopf">
11-      <a class="marke" routerLink="/">Bestellsystem</a>
12-      <nav aria-label="Hauptnavigation">
13-        <a routerLink="/produkte" routerLinkActive="aktiv">Katalog</a>
14-        <a routerLink="/kunden" routerLinkActive="aktiv">Kunden</a>
15-        <a routerLink="/checkout" routerLinkActive="aktiv"
16-          >Warenkorb ({{ warenkorb.stueckzahl() }})</a
17-        >
18-      </nav>
… [gekürzt, 54 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && grep -rhoE '(<button[^>]*>\s*[^<{]+|<h[123][^>]*>[^<]+|<legend>[^<]+|routerLink="[^"]+"[^>]*>[^<]+)' --include='*.ts' --exclude-dir=api . | sed 's/^ *//' | sort -u
```

```
<button type="button" (click)="laden()">Erneut versuchen
<button type="submit" [disabled]="sendet()">Adresse speichern
<button type="submit" [disabled]="sendet()">Registrieren
<h1>Kunden
<h1>Kunde registrieren
<h2>Bestellungen
<h2>Lieferadresse
routerLink="/">Bestellsystem
routerLink="/kunden">Abbrechen
routerLink="/kunden/neu">Kunde registrieren
routerLink="/kunden" routerLinkActive="aktiv">Kunden
routerLink="/produkte" routerLinkActive="aktiv">Katalog
```

**⏺ Bash**

```bash
grep -rhoE '(<button[^>]*>\s*[^<{]+|<h[123][^>]*>\s*[^<{]+|<legend>[^<]+|<label[^>]*>\s*[^<{]+|routerLink[^>]*>\s*[^<{]+)' --exclude-dir=api --exclude='*.spec.ts' . | tr -s ' \n' ' ' | sed 's/<button/\n<button/g; s/<h[123]/\n&/g; s/<legend/\n&/g; s/<label/\n&/g; s/routerLink/\nrouterLink/g' | sort -u
```

```
<button type="button" (click)="ladeKunden()">Erneut versuchen 
<button type="button" (click)="laden()">Erneut versuchen 
<button type="button" (click)="oeffne('neu')">Adresse hinzufügen 
<button type="submit" [disabled]="sendet()">Adresse speichern 
<button type="submit" [disabled]="sendet()">Merken 
<button type="submit" [disabled]="sendet()">Paket anlegen 
<button type="submit" [disabled]="sendet()">Registrieren 
<button type="submit" [disabled]="sendet()">Speichern 
<button type="submit" [disabled]="sendet()">Zahlungspflichtig bestellen 
<h1>Bestellung 
<h1>Kunden 
<h1>Kunde registrieren 
<h1>Merkliste 
<h1>Produkt anlegen 
<h1>Produkte 
<h1>Warenkorb 
<h2>Adressbuch 
<h2>Bestellungen 
<h2>Kasse 
<h2>Lieferadresse 
<h2>Versand 
<h2>Zahlung 
<h3>Paket anlegen 
<label for="dienstleister">Dienstleister 
<label for="email">E-Mail 
<label for="gutscheincode">Gutscheincode (optional) 
<label for="kundenId">Kunde 
<label for="lagerbestand">Lagerbestand 
<label for="listenpreis">Listenpreis (€) 
<label for="nachname">Nachname 
<label [for]="praefix() + 'land'">Land (ISO-Code, z. B. DE) 
<label [for]="praefix() + 'postleitzahl'">Postleitzahl 
<label [for]="praefix() + 'stadt'">Stadt 
<label [for]="praefix() + 'strasse'">Straße und Hausnummer 
<label for="produktId">Produkt 
<label for="sendungsnummer">Sendungsnummer 
<label for="titel">Titel 
<label for="vorname">Vorname 
<label for="zahlungsart">Zahlungsart 
<legend>Lieferadresse 
… [gekürzt, 54 Zeilen gesamt]
```

**⏺ Bash**

```bash
grep -rn -A3 '<button' --exclude-dir=api --exclude='*.spec.ts' . | grep -vE '<button|^--' | grep -vE '^\S+-\s*$' | sed 's/^[^-]*-[0-9]*-//' | sort -u | head -40; grep -rn 'zahlungsart\|Zahlungsart' bestellung/bezeichnungen.ts | head
```

```
      }
    }
  }
            <a class="knopf zweitrangig" routerLink="/produkte">Weiter einkaufen</a>
              [attr.aria-label]="eintrag.produkt.titel + ' entfernen'"
bestellung/bestellung-detail/bestellung-detail.html-11-    </div>
bestellung/bestellung-detail/bestellung-detail.html-12-  }
bestellung/bestellung-detail/bestellung-detail.html-13-  @default {
                  </button>
                </button>
              class="knopf zweitrangig"
                (click)="setzeStatus(paket, auswahl.value)"
      @default {
    @default {
  @default {
                [disabled]="sendet()"
                </div>
          </div>
        </div>
      </div>
    </div>
                  Entfernen
                    Erneut versuchen
        </form>
    </form>
</form>
@if (dienstleister.invalid && dienstleister.touched) {
    @if (produktId.invalid && produktId.touched) {
katalog/produkt-anlegen/produkt-anlegen.html-28-    <a class="knopf zweitrangig" routerLink="/produkte">Abbrechen</a>
katalog/produkt-anlegen/produkt-anlegen.html-29-  </div>
katalog/produkt-anlegen/produkt-anlegen.html-30-</form>
katalog/produkt-liste/produkt-liste.html-14-    </div>
katalog/produkt-liste/produkt-liste.html-15-  }
katalog/produkt-liste/produkt-liste.html-16-  @default {
katalog/produkt-liste/produkt-liste.html-46-                    type="button"
katalog/produkt-liste/produkt-liste.html-47-                    class="knopf zweitrangig"
katalog/produkt-liste/produkt-liste.html-48-                    [attr.aria-label]="produkt.titel + ' in den Warenkorb'"
kunde/adresse-formular/adresse-formular.ts-44-          Abbrechen
kunde/adresse-formular/adresse-formular.ts-45-        </button>
kunde/adresse-formular/adresse-formular.ts-46-      </div>
… [gekürzt, 48 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 40,60p katalog/produkt-liste/produkt-liste.html; grep -n -B2 -A6 'Bearbeiten\|Löschen\|loesche\|Status setzen' kunde/kunde-detail/*.html kunde/kunde-detail/*.ts versand/versandpakete/*.html 2>/dev/null | grep -vE 'spec' | head -50; ls kunde/kunde-detail versand/versandpakete
```

```
                  <span class="marke ausverkauft">nicht verfügbar</span>
                }
              </td>
              <td class="warenkorb">
                @if (produkt.istVerfuegbar) {
                  <button
                    type="button"
                    class="knopf zweitrangig"
                    [attr.aria-label]="produkt.titel + ' in den Warenkorb'"
                    (click)="warenkorb.lege(produkt)"
                  >
                    In den Warenkorb
                  </button>
                  @if (warenkorb.mengeVon(produkt.id); as menge) {
                    <span class="im-warenkorb">{{ menge }} im Warenkorb</span>
                  }
                }
              </td>
            </tr>
          }
        </tbody>
kunde/kunde-detail/kunde-detail.ts-71-  }
kunde/kunde-detail/kunde-detail.ts-72-
kunde/kunde-detail/kunde-detail.ts:73:  protected loeschen(adresse: Adresse): void {
kunde/kunde-detail/kunde-detail.ts-74-    this.bearbeitung.set(null);
kunde/kunde-detail/kunde-detail.ts-75-    this.fuehreAus(this.adressbuchService.entferneAdresse(this.kundenId(), adresse.id), () =>
kunde/kunde-detail/kunde-detail.ts-76-      this.aendereAdressen((adressen) => adressen.filter((a) => a.id !== adresse.id)),
kunde/kunde-detail/kunde-detail.ts-77-    );
kunde/kunde-detail/kunde-detail.ts-78-  }
kunde/kunde-detail/kunde-detail.ts-79-
--
--
versand/versandpakete/versandpakete.html-43-                (click)="setzeStatus(paket, auswahl.value)"
versand/versandpakete/versandpakete.html-44-              >
versand/versandpakete/versandpakete.html:45:                Status setzen
versand/versandpakete/versandpakete.html-46-              </button>
versand/versandpakete/versandpakete.html-47-            } @else {
versand/versandpakete/versandpakete.html-48-              <span class="endzustand">abgeschlossen</span>
versand/versandpakete/versandpakete.html-49-            }
versand/versandpakete/versandpakete.html-50-          </td>
… [gekürzt, 69 Zeilen gesamt]
```

### ● Claude

Genug Kontext. Zuerst YAML-Export in `OpenApiSpezifikationIT` (Test zuerst, dann Datei erzeugen).

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && cat > src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java <<'EOF'
package de.bestellsystem.gemeinsam.infrastruktur;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import de.bestellsystem.TestcontainersKonfiguration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Hält {@code frontend/openapi.json} und {@code docs/api/openapi.yaml} synchron zum Code (ADR
 * 0002). Neu schreiben mit {@code ./gradlew aktualisiereOpenApi}.
 */
@SpringBootTest
@AutoConfigureMockMvc
… [gekürzt, 98 Zeilen gesamt]
```

```
 backend/build.gradle.kts                           |  7 ++--
 .../infrastruktur/OpenApiSpezifikationIT.java      | 43 ++++++++++++++++------
 2 files changed, 35 insertions(+), 15 deletions(-)

* What went wrong:
A problem was found with the configuration of task ':test' (type 'Test').
Input file does not exist
  Type 'org.gradle.api.tasks.testing.Test' property 'openApiDokumentation' specifies file '/home/mschachner/Projects/bestellsystem/docs/api/openapi.yaml' which doesn't exist
    An input file was expected to be present but it doesn't exist
    For more information, please refer to https://docs.gradle.org/9.9.1/userguide/validation_problems.html#input_file_does_not_exist in the Gradle documentation.
    Possible solutions:
      1. Make sure the file exists before the task is called.
      2. Make sure that the task which produces the file is declared as an input.

* Try:
> Run with --scan to get full insights from a Build Scan (powered by Develocity).

BUILD FAILED in 1s
```

### ● Claude

Rot wie erwartet (Datei fehlt). Jetzt exportieren, dann Test.

**⏺ Bash**

```bash
./gradlew aktualisiereOpenApi -q 2>&1 | tail -5; git status --short ..; head -30 ../docs/api/openapi.yaml; wc -l ../docs/api/openapi.yaml; ./gradlew test --tests '*OpenApiSpezifikationIT' -q 2>&1 | tail -5 && echo TEST-OK
```

```
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T12:36:54.523+01:00  INFO 1780681 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T12:36:54.525+01:00  INFO 1780681 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-11-12T12:36:54.527+01:00  INFO 1780681 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
 M build.gradle.kts
 M src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java
?? ../docs/api/
openapi: 3.1.0
info:
  description: "REST-API des Bestellsystems. Basis /api/v1. Fehler als ProblemDetail\
    \ (RFC 9457), Geldbeträge in Cent (EUR)."
  title: Bestellsystem API
  version: v1
servers:
- url: /
  description: Aktueller Host
tags:
- description: "Zahlungsstatus und Erstattungen (E05, D-19)"
  name: Zahlungen
- description: Kundenkonto (E01)
  name: Kunden
- description: "Liefer- und Rechnungsadressen eines Kunden (E01, D-06)"
  name: Adressbuch
- description: Produktkatalog und Lagerbestand (E02)
  name: Produkte
- description: "Checkout und Bestellabruf (E04, E08)"
  name: Bestellungen
- description: Mahnprüfung und Mahnlauf (E06)
  name: Mahnwesen
- description: "Gemerkte Produkte eines Kunden (E03, D-07, D-21)"
  name: Merkliste
- description: "Versandpakete und Lieferstatus (E07, D-12, D-22)"
  name: Versand
paths:
  /api/v1/kunden:
    get:
      operationId: listeKunden
1690 ../docs/api/openapi.yaml
OpenJDK 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
2026-11-12T12:37:04.233+01:00  INFO 1781746 --- [bestellsystem] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
… [gekürzt, 43 Zeilen gesamt]
```

### ● Claude

Export grün, `frontend/openapi.json` unverändert. Jetzt Anker für D-xx-Links berechnen (GitHub-Slug).

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && python3 - <<'EOF'
import re
for l in open('docs/01-entscheidungen.md'):
    if l.startswith('## D-'):
        t=l[3:].strip()
        s=re.sub(r'[^\w\- ]','',t.lower()).replace(' ','-')
        print(t[:4], s)
EOF
```

```
D-01 d-01-gesamtwert-gespeichert-oder-berechnet
D-02 d-02-zahlung-mindestens-eine-vs-offene-beträge
D-03 d-03-zahlungsrückstand-braucht-eine-fälligkeit
D-04 d-04-gutschein-art-des-rabatts-und-wirkung
D-05 d-05-erstattungen-und-transaktionsstatus
D-06 d-06-liefer--und-rechnungsadressen
D-07 d-07-merkliste-als-assoziationsklasse
D-08 d-08-lieferadress-snapshot
D-09 d-09-lagerbestand-und-verfügbarkeit
D-10 d-10-positionen-preis-zeilennummer-duplikate
D-11 d-11-geldbeträge-und-währung
D-12 d-12-versandpakete
D-13 d-13-kennungen-und-eindeutigkeit
D-14 d-14-zähler-der-bestellnummer
D-15 d-15-produktdaten-beim-anlegen
D-16 d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit
D-17 d-17-checkout-ablehnungen-gutscheincode-erste-zahlung
D-18 d-18-checkout-im-frontend-und-bestellhistorie
D-19 d-19-zahlungsstatus-ändern-und-erstattung-buchen
D-20 d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf
D-21 d-21-merkliste-im-detail-zeitpunkt-merken-entfernen
D-22 d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte
```

### ● Claude

Jetzt ADRs 0004–0011 schreiben. D-Verweise als `{D-xx}`-Platzhalter, danach per Skript in Links umwandeln.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/docs/adr && cat > 0004-modularer-monolith-nach-fachlichkeit.md <<'EOF'
# ADR 0004: Modularer Monolith, nach Fachlichkeit geschnitten

**Status:** angenommen (2026-11-12)

## Kontext

Das Bestellsystem umfasst neun Fachgebiete (E00 bis E08), die eng zusammenhängen: Der Checkout braucht Kunde, Produkt und Gutschein in einer Transaktion ({D-09}), die Mahnprüfung liest Bestellungen und Zahlungen ({D-03}, {D-20}). Ein Team, ein Deployment, eine Datenbank.

## Entscheidung

- **Ein** Spring-Boot-Backend (Java 21, Spring Boot 3.5) mit **einer** PostgreSQL-Datenbank.
- Pakete nach Fachlichkeit: `de.bestellsystem.{kunde, katalog, merkliste, bestellung, zahlung, gutschein, mahnwesen, versand, gemeinsam}`.
- In jedem Fachpaket die Schichten `api` → `application` → `domain`, dazu `persistence` und `infrastruktur`. Abhängigkeiten nur in dieser Richtung.
- `domain` enthält die Fachlogik (reichhaltiges Modell) und kennt außer JPA-Annotationen kein Spring. Services in `application` orchestrieren und schneiden Transaktionen.
- Aggregate nach dem Domänenmodell: `Kunde` (mit Adressen), `Produkt`, `Gutschein`, `Bestellung` (mit Positionen, Zahlungen, Mahnungen, Versandpaketen), `Merklisteneintrag`. Teile eines Aggregats werden nur über die Wurzel geändert; darum nutzen `ZahlungController` und `VersandController` den `BestellungService` ({D-19}, {D-22}).
- Andere Aggregate werden über ID referenziert (`Bestellung.kundeId`) oder lesend über LAZY-Assoziationen (`Bestellposition.produkt`).
- Zugriff auf externe Systeme (E-Mail) nur über Ports in `application`, Adapter in `infrastruktur` (ADR 0011).

## Konsequenzen

- Eine Transaktion kann mehrere Aggregate umfassen (Checkout senkt Bestand und legt die Bestellung an). Das ist gewollt und mit Microservices nicht ohne Saga zu haben.
- Domain-Logik ist ohne Spring und Datenbank als Unit-Test prüfbar.
- Die Paketgrenzen sind Konvention, kein Compilerzwang. Eine spätere Prüfung (z. B. ArchUnit oder Spring Modulith) ist nachrüstbar.
EOF
cat > 0005-postgresql-flyway-testcontainers.md <<'EOF'
# ADR 0005: PostgreSQL mit Flyway, Tests mit Testcontainers

**Status:** angenommen (2026-11-12)

… [gekürzt, 202 Zeilen gesamt]
```

```
0001-gradle-als-build-werkzeug.md
0002-openapi-spezifikation-als-datei.md
0003-pessimistische-sperre-im-checkout.md
0004-modularer-monolith-nach-fachlichkeit.md
0005-postgresql-flyway-testcontainers.md
0006-geld-als-cent-wertobjekt.md
0007-fehler-als-problemdetail.md
0008-nebenlaeufigkeit-an-der-aggregatwurzel.md
0009-zeit-ueber-injizierte-uhr.md
0010-angular-frontend-mit-generiertem-client.md
0011-benachrichtigung-ueber-port.md
```

### ● Claude

Fakten in ADRs gegen Code prüfen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src && grep -rn 'BenachrichtigungNichtVersendet\|SERVICE_UNAVAILABLE' main/java/de/bestellsystem/gemeinsam/api/ | head -3; grep -rln 'class FesteUhrKonfiguration\|class VerstellbareUhrKonfiguration\|class BenachrichtigungsAttrappe' test | head; grep -rn 'Exact\|HALF_UP' main/java/de/bestellsystem/gemeinsam/domain/Geld.java | head -4; grep -rn 'PESSIMISTIC\|OPTIMISTIC_FORCE' main/java --include=*.java -l
```

```
main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java:3:import de.bestellsystem.gemeinsam.application.BenachrichtigungNichtVersendetException;
main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java:53:    @ExceptionHandler(BenachrichtigungNichtVersendetException.class)
main/java/de/bestellsystem/gemeinsam/api/ZentraleFehlerbehandlung.java:54:    ProblemDetail nichtVersendet(BenachrichtigungNichtVersendetException fehler) {
test/java/de/bestellsystem/FesteUhrKonfiguration.java
test/java/de/bestellsystem/VerstellbareUhrKonfiguration.java
test/java/de/bestellsystem/BenachrichtigungsAttrappe.java
main/java/de/bestellsystem/gemeinsam/domain/Geld.java:31:        return new Geld(Math.addExact(cent, andere.cent));
main/java/de/bestellsystem/gemeinsam/domain/Geld.java:35:        return new Geld(Math.subtractExact(cent, andere.cent));
main/java/de/bestellsystem/gemeinsam/domain/Geld.java:39:        return new Geld(Math.multiplyExact(cent, menge));
main/java/de/bestellsystem/gemeinsam/domain/Geld.java:46:                BigDecimal.valueOf(cent).multiply(prozent).divide(HUNDERT, 0, RoundingMode.HALF_UP);
(eval):1: no matches found: --include=*.java
```

### ● Claude

Stimmt. ADR 0003 bekommt Platzhalter, dann ADR-Index mit Zuordnung D-xx ↔ ADR (per Skript erzeugt).

**⏺ Bash**

```bash
sed -i 's/in einer Transaktion (D-09)/in einer Transaktion ({D-09})/; s/(409 mit Produktname, D-17)/(409 mit Produktname, {D-17})/' 0003-pessimistische-sperre-im-checkout.md && grep -n '{D-' 0003*.md && python3 - <<'EOF'
import re,glob,collections
rows=[];rev=collections.defaultdict(list)
for f in sorted(glob.glob('0*.md')):
    txt=open(f).read()
    title=txt.splitlines()[0].split(': ',1)[1]
    status=re.search(r'\*\*Status:\*\* (\S+)',txt).group(1)
    ds=sorted(set(re.findall(r'\{(D-\d\d)\}',txt)))
    nr=f[:4]
    for d in ds: rev[d].append(nr)
    rows.append((nr,f,title,status,ds))
out=["# Architekturentscheidungen (ADR)","",
"Technische Entscheidungen. Fachliche Entscheidungen stehen als `D-xx` in [01-entscheidungen.md](../01-entscheidungen.md); jede ADR verlinkt die D-xx, die sie umsetzt oder auf denen sie aufbaut.","",
"Neue ADR: nächste freie Nummer, Datei `NNNN-titel.md`, Abschnitte Kontext, Entscheidung, Konsequenzen; hier eintragen.","",
"| ADR | Titel | Status | Fachliche Entscheidungen |","|---|---|---|---|"]
for nr,f,t,s,ds in rows:
    out.append(f"| {nr} | [{t}]({f}) | {s} | {', '.join('{'+d+'}' for d in ds) or '–'} |")
out+=["","## Fachliche Entscheidungen und ihre technische Umsetzung","","| D-xx | ADR |","|---|---|"]
for i in range(1,23):
    d=f"D-{i:02d}"
    out.append(f"| {{{d}}} | {', '.join(f'[{n}]('+[r[1] for r in rows if r[0]==n][0]+')' for n in rev[d]) or 'rein fachlich, im Domänenmodell umgesetzt'} |")
open('README.md','w').write('\n'.join(out)+'\n')
EOF
cat README.md
```

```
7:Der Checkout prüft und senkt den Lagerbestand in einer Transaktion ({D-09}). Zwei gleichzeitige Checkouts dürfen den Bestand nicht gemeinsam unter 0 drücken. `Produkt` trägt `@Version`.
14:- Der zweite Checkout wartet, bis der erste festschreibt, und prüft dann den aktuellen Bestand. Reicht er nicht, folgt die fachliche Ablehnung (409 mit Produktname, {D-17}).
# Architekturentscheidungen (ADR)

Technische Entscheidungen. Fachliche Entscheidungen stehen als `D-xx` in [01-entscheidungen.md](../01-entscheidungen.md); jede ADR verlinkt die D-xx, die sie umsetzt oder auf denen sie aufbaut.

Neue ADR: nächste freie Nummer, Datei `NNNN-titel.md`, Abschnitte Kontext, Entscheidung, Konsequenzen; hier eintragen.

| ADR | Titel | Status | Fachliche Entscheidungen |
|---|---|---|---|
| 0001 | [Gradle als Build-Werkzeug](0001-gradle-als-build-werkzeug.md) | angenommen | – |
| 0002 | [OpenAPI-Spezifikation als versionierte Datei](0002-openapi-spezifikation-als-datei.md) | angenommen | – |
| 0003 | [Pessimistische Sperre auf Produkte im Checkout](0003-pessimistische-sperre-im-checkout.md) | angenommen | {D-09}, {D-17} |
| 0004 | [Modularer Monolith, nach Fachlichkeit geschnitten](0004-modularer-monolith-nach-fachlichkeit.md) | angenommen | {D-03}, {D-09}, {D-19}, {D-20}, {D-22} |
| 0005 | [PostgreSQL mit Flyway, Tests mit Testcontainers](0005-postgresql-flyway-testcontainers.md) | angenommen | {D-09}, {D-14}, {D-16}, {D-20}, {D-21}, {D-22} |
| 0006 | [Geld als Wertobjekt in ganzen Cent](0006-geld-als-cent-wertobjekt.md) | angenommen | {D-01}, {D-04}, {D-05}, {D-11}, {D-17} |
| 0007 | [Fehler als ProblemDetail mit fachlichen Ausnahmen](0007-fehler-als-problemdetail.md) | angenommen | {D-16}, {D-17}, {D-19}, {D-20}, {D-21}, {D-22} |
| 0008 | [Nebenläufigkeit über die Version der Aggregatwurzel](0008-nebenlaeufigkeit-an-der-aggregatwurzel.md) | angenommen | {D-09}, {D-16}, {D-19}, {D-20}, {D-21}, {D-22} |
| 0009 | [Zeit nur über eine injizierte Uhr](0009-zeit-ueber-injizierte-uhr.md) | angenommen | {D-03}, {D-04}, {D-19}, {D-20}, {D-21}, {D-22} |
| 0010 | [Angular-Frontend ohne eigene Fachlogik](0010-angular-frontend-mit-generiertem-client.md) | angenommen | {D-01}, {D-05}, {D-09}, {D-18}, {D-22} |
| 0011 | [Benachrichtigungen über einen Port, Versand in der Transaktion](0011-benachrichtigung-ueber-port.md) | angenommen | {D-03}, {D-20} |

## Fachliche Entscheidungen und ihre technische Umsetzung

| D-xx | ADR |
|---|---|
| {D-01} | [0006](0006-geld-als-cent-wertobjekt.md), [0010](0010-angular-frontend-mit-generiertem-client.md) |
| {D-02} | rein fachlich, im Domänenmodell umgesetzt |
| {D-03} | [0004](0004-modularer-monolith-nach-fachlichkeit.md), [0009](0009-zeit-ueber-injizierte-uhr.md), [0011](0011-benachrichtigung-ueber-port.md) |
| {D-04} | [0006](0006-geld-als-cent-wertobjekt.md), [0009](0009-zeit-ueber-injizierte-uhr.md) |
| {D-05} | [0006](0006-geld-als-cent-wertobjekt.md), [0010](0010-angular-frontend-mit-generiertem-client.md) |
| {D-06} | rein fachlich, im Domänenmodell umgesetzt |
| {D-07} | rein fachlich, im Domänenmodell umgesetzt |
| {D-08} | rein fachlich, im Domänenmodell umgesetzt |
| {D-09} | [0003](0003-pessimistische-sperre-im-checkout.md), [0004](0004-modularer-monolith-nach-fachlichkeit.md), [0005](0005-postgresql-flyway-testcontainers.md), [0008](0008-nebenlaeufigkeit-an-der-aggregatwurzel.md), [0010](0010-angular-frontend-mit-generiertem-client.md) |
| {D-10} | rein fachlich, im Domänenmodell umgesetzt |
| {D-11} | [0006](0006-geld-als-cent-wertobjekt.md) |
| {D-12} | rein fachlich, im Domänenmodell umgesetzt |
| {D-13} | rein fachlich, im Domänenmodell umgesetzt |
| {D-14} | [0005](0005-postgresql-flyway-testcontainers.md) |
… [gekürzt, 48 Zeilen gesamt]
```

### ● Claude

ADR-Index steht. Jetzt `docs/arc42.md`.

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/docs/arc42.md
# Architekturdokumentation nach arc42

Stand: Abschluss MVP (Schritt 9, 2026-11-12). Detailtabellen (alle Pakete, Endpunkte, Tabellen, Migrationen, Tests) stehen in [ARCHITEKTUR.md](ARCHITEKTUR.md); dieses Dokument gibt den Überblick und begründet ihn.

## 1 Einführung und Ziele

Ein Onlineshop stellt sein Bestellsystem neu auf ([Anforderung](00-anforderung.md)). Das MVP deckt ab:

| Fachgebiet | Epic | Kern |
|---|---|---|
| Kundenkonto, Adressbuch | E01 | Registrierung mit Lieferadresse, mindestens eine Lieferadresse (I-1) |
| Katalog, Lagerbestand | E02 | Produkt anlegen, Verfügbarkeit prüfen |
| Merkliste | E03 | Produkte merken und entfernen, Merkdatum |
| Bestellung, Checkout | E04 | Bestellung in einer Transaktion, Lieferadress-Snapshot, eingefrorene Preise |
| Zahlungen | E05 | Bezahlstatus, offener Betrag, Erstattungen |
| Mahnwesen | E06 | Mahnprüfung je Kunde, täglicher Mahnlauf, E-Mail |
| Versand | E07 | Pakete mit Sendungsnummer und Lieferstatus |
| Gutscheine | E08 | Prozent- oder Festbetragsrabatt, eingefroren in der Bestellung |

### Qualitätsziele

| Priorität | Ziel | Maßnahme |
|---|---|---|
| 1 | **Korrektheit von Geld und Bestand** | Cent als `long` (ADR 0006), Bestand nie negativ auch bei gleichzeitigen Checkouts (ADR 0003) |
| 2 | **Nachvollziehbarkeit** | Eingefrorene Preise, Rabatte, Lieferanschrift; abgeleitete Werte nur berechnet ({D-01}, {D-08}) |
| 3 | **Testbarkeit** | Fachlogik ohne Spring testbar, feste Uhr (ADR 0009), echtes PostgreSQL in Tests (ADR 0005) |
| 4 | **Änderbarkeit** | Pakete nach Fachlichkeit (ADR 0004), Frontend ohne Fachlogik (ADR 0010) |

### Stakeholder
… [gekürzt, 353 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/arc42.md (file state is current in your context — no need to Read it back)
```

### ● Claude

Erfundene Testnamen vermeiden: Nachweise prüfen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && find backend/src/test -name '*Mahn*' -o -name '*Geld*Test*' -o -name 'GutscheinTest*' | sed 's|.*/||'; grep -n 'DisplayName' backend/src/test/java/de/bestellsystem/mahnwesen/*.java | grep -iE 'idempot|selben Tag|AK7|Versand' | head; grep -n 'DisplayName' backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java backend/src/test/java/de/bestellsystem/gemeinsam/domain/GeldTest.java | grep -iE 'rund|prozent|ablauf' | head; grep -oE 'I-[0-9]+' docs/02-domaenenmodell.md | sort -uV | tr '\n' ' '
```

```
Mahnbestand.java
GeldTest.java
GutscheinTest.java
MahnpruefungTest.java
MahnwesenApiIT.java
MahnlaufZeitplanIT.java
MahnlaufIT.java
MahnServiceIT.java
backend/src/test/java/de/bestellsystem/gemeinsam/domain/GeldTest.java:49:    @DisplayName("D-11: Prozentanteil wird kaufmännisch auf ganze Cent gerundet")
backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java:53:    @DisplayName("US-08.1 AK5: Ablaufdatum gilt inklusive")
backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java:68:    @DisplayName("US-08.1 AK9: 10 % auf 0,25 € ergibt kaufmännisch gerundet 0,03 €")
backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java:74:    @DisplayName("D-04: Prozent muss größer 0 und höchstens 100 sein")
backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java:88:    @DisplayName("D-17: Prozent hat höchstens zwei Nachkommastellen")
backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java:103:    @DisplayName("D-04: Code und Ablaufdatum sind Pflicht")
I-1 I-2 I-3 I-4 I-5 I-6 I-7 I-8 I-9 I-10 I-11
```

**⏺ Bash**

```bash
grep -n 'DisplayName' backend/src/test/java/de/bestellsystem/mahnwesen/*.java | grep -iE 'idempot|selben Tag|wiederhol|AK7|scheitert|503' | head
```

```
(Bash completed with no output)
```

**⏺ Bash**

```bash
grep -rn 'DisplayName' backend/src/test/java/de/bestellsystem/mahnwesen/ | sed 's/.*mahnwesen\///' | cut -c1-150
```

```
application/MahnlaufZeitplanIT.java:6:import org.junit.jupiter.api.DisplayName;
application/MahnlaufZeitplanIT.java:23:    @DisplayName("US-06.2 AK4: Mahnlauf täglich um 06:00 eingeplant")
application/MahnServiceIT.java:20:import org.junit.jupiter.api.DisplayName;
application/MahnServiceIT.java:62:    @DisplayName("US-06.1 AK1-3: genau eine E-Mail mit B-1 und B-2, je eine Mahnung protokolliert")
application/MahnServiceIT.java:83:    @DisplayName("US-06.1 AK4: keine Nachricht am 05.11., erneute Mahnung am 08.11.")
application/MahnServiceIT.java:101:    @DisplayName("US-06.1 AK5: alle überfälligen bezahlt, nichts versendet, nichts protokolliert")
application/MahnServiceIT.java:112:    @DisplayName("US-06.1 AK6: Stichtag 15.10. mahnt B-1 nicht")
application/MahnServiceIT.java:119:    @DisplayName("US-06.1 AK7: Versandfehler protokolliert keine Mahnung, nächster Lauf mahnt")
application/MahnServiceIT.java:137:    @DisplayName("Unbekannter Kunde: 404")
api/MahnwesenApiIT.java:21:import org.junit.jupiter.api.DisplayName;
api/MahnwesenApiIT.java:69:    @DisplayName("US-06.2 AK5: Mahnprüfung liefert gemahnte Bestellungen mit offenem Betrag")
api/MahnwesenApiIT.java:89:    @DisplayName("US-06.2 AK5: unbekannter Kunde 404 als ProblemDetail")
api/MahnwesenApiIT.java:98:    @DisplayName("US-06.2 AK5: Versandfehler 503, keine Mahnung protokolliert")
api/MahnwesenApiIT.java:110:    @DisplayName("US-06.2 AK5: Mahnlauf liefert Stichtag und Zähler")
application/MahnlaufIT.java:16:import org.junit.jupiter.api.DisplayName;
application/MahnlaufIT.java:68:    @DisplayName("US-06.2 AK1: Anna und Ben je eine Nachricht, Clara keine")
application/MahnlaufIT.java:81:    @DisplayName("US-06.2 AK2: Versandfehler bei Anna hält den Lauf nicht auf")
application/MahnlaufIT.java:95:    @DisplayName("US-06.2 AK3: Wiederholung am selben Tag versendet und protokolliert nichts")
application/MahnlaufIT.java:107:    @DisplayName("US-06.2 AK4: mit cron \"-\" ist kein zeitgesteuerter Lauf eingeplant")
```

### ● Claude

Nachweise korrigieren.

**⏺ Bash**

```bash
python3 - <<'EOF'
p='docs/arc42.md'
s=open(p).read()
s=s.replace("| 10 % Rabatt auf 33,33 € | 3,33 € Rabatt, kein Rundungsfehler | `GeldTest`, `GutscheinTest` |",
"| 10 % Rabatt auf 0,25 € | 0,03 € Rabatt, kaufmännisch gerundet | `GutscheinTest` (US-08.1 AK9), `GeldTest` |")
s=s.replace("| Gutschein am Ablaufdatum | noch gültig | `GutscheinTest` (US-08.1) |","| Gutschein am Ablaufdatum | noch gültig | `GutscheinTest` (US-08.1 AK5) |")
s=s.replace("| Mahnlauf zweimal am selben Tag | zweiter Lauf verschickt nichts | `MahnServiceIT`/`MahnlaufIT` (US-06.2) |","| Mahnlauf zweimal am selben Tag | zweiter Lauf verschickt nichts | `MahnlaufIT` (US-06.2 AK3) |")
s=s.replace("| Mailserver fällt aus | keine Mahnung protokolliert, 503 | US-06.1 AK7 |","| Mailserver fällt aus | keine Mahnung protokolliert, 503 | `MahnServiceIT` (US-06.1 AK7), `MahnwesenApiIT` |")
open(p,'w').write(s)
EOF
grep -c 'AK9\|AK5)\|AK3)\|AK7)' docs/arc42.md
```

```
4
```

### ● Claude

Jetzt Benutzerhandbuch. Checkout-, Bestellansicht- und Merkliste-Templates kurz lesen für exakte Abläufe.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && sed -e 's/^\s*//' bestellung/checkout/checkout.html | grep -vE '^$|^\}|^<\/' | head -90
```

```
<h1>Warenkorb</h1>
@if (warenkorb.istLeer()) {
<div class="hinweis">
<p class="meldung">Der Warenkorb ist leer.</p>
<a routerLink="/produkte">Zum Katalog</a>
<table class="positionen">
<thead>
<tr>
<th scope="col">Produkt</th>
<th scope="col" class="zahl">Listenpreis</th>
<th scope="col" class="zahl">Menge</th>
<th scope="col"><span class="unsichtbar">Aktion</span></th>
<tbody>
@for (eintrag of warenkorb.eintraege(); track eintrag.produkt.id) {
<tr>
<td>{{ eintrag.produkt.titel }}</td>
<td class="zahl">{{ eintrag.produkt.listenpreis | geld }}</td>
<td class="zahl">
<input
class="menge"
type="number"
min="1"
step="1"
[value]="eintrag.menge"
[attr.aria-label]="'Menge ' + eintrag.produkt.titel"
(change)="aendereMenge(eintrag, $any($event.target))"
/>
<td>
<button
type="button"
class="knopf zweitrangig"
[attr.aria-label]="eintrag.produkt.titel + ' entfernen'"
(click)="warenkorb.entferne(eintrag.produkt.id)"
>
Entfernen
<p class="erklaerung">Zwischenbeträge, Rabatt und Gesamtwert stehen in der Bestellbestätigung.</p>
<h2>Kasse</h2>
@switch (kundenZustand()) {
@case ('laedt') {
<p class="hinweis" role="status">Kunden werden geladen …</p>
… [gekürzt, 90 Zeilen gesamt]
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/docs/benutzerhandbuch.md
# Benutzerhandbuch

Für Shop-Mitarbeiter und Tester des Bestellsystems (MVP). Start der Anwendung: siehe [README](../README.md#schnellstart). Die Oberfläche läuft unter http://localhost:4200.

Im MVP gibt es **kein Login**. Wer die Oberfläche bedient, wählt den Kunden aus einer Liste und handelt in seinem Namen. Fachliche Regeln stehen in [01-entscheidungen.md](01-entscheidungen.md) (`D-xx`).

## Überblick

Die Kopfzeile führt zu drei Bereichen:

| Menüpunkt | Inhalt |
|---|---|
| **Katalog** | Produkte mit Preis und Verfügbarkeit, Produkt anlegen, in den Warenkorb legen |
| **Kunden** | Kundenliste, Registrierung; je Kunde Adressbuch, Bestellungen, Merkliste |
| **Warenkorb (n)** | Inhalt des Warenkorbs und Kasse; `n` ist die Stückzahl |

Jede Liste zeigt beim Laden einen Hinweis, bei einem Fehler eine Meldung mit **Erneut versuchen** und bei leerem Ergebnis einen Leerhinweis. Abgelehnte Aktionen erscheinen als Meldung in Klartext, z. B. „Gutschein ist abgelaufen“.

Beträge werden in Euro angezeigt (`49,99 €`); intern rechnet das System in ganzen Cent.

## 1 Produkt anlegen

1. **Katalog** → **Produkt anlegen**.
2. **Titel** (Pflicht, höchstens 200 Zeichen), **Listenpreis (€)** (0 oder mehr, höchstens zwei Nachkommastellen), **Lagerbestand** (0 oder mehr) eingeben.
3. **Speichern**. Die Produktliste zeigt das neue Produkt.

In der Produktliste ist jedes Produkt als **verfügbar** (Bestand > 0) oder **nicht verfügbar** gekennzeichnet. Titel dürfen sich wiederholen ({D-15}).

Den Lagerbestand ändert man im MVP nur über die API: `PUT /api/v1/produkte/{id}/lagerbestand` (Swagger UI, siehe [Abschnitt 9](#9-funktionen-ohne-oberfläche)).
… [gekürzt, 179 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/benutzerhandbuch.md (file state is current in your context — no need to Read it back)
```

### ● Claude

Fakten im Handbuch gegen Templates prüfen.

**⏺ Bash**

```bash
grep -rhn -i 'rechnungsadresse\|lieferadresse' kunde/kunde-registrieren/*.html kunde/adresse-formular/*.ts | grep -iE 'label|<span|auch' | head; grep -n -iE 'faellig|warenwert|rabatt|gesamtwert|Lieferanschrift|bezahlt' bestellung/bestellung-detail/bestellung-detail.html | head -20; grep -n 'Speichern' -r katalog/produkt-anlegen/; grep -n 'Dienstleister\|HERMES' versand/bezeichnungen.ts | head
```

```
(eval):1: no matches found: kunde/kunde-registrieren/*.html
27:        <dd>{{ bestellung.faelligAm | date: 'dd.MM.yyyy' }}</dd>
28:        <dt>Lieferanschrift</dt>
31:            {{ bestellung.lieferanschrift.strasse }}<br />
32:            {{ bestellung.lieferanschrift.postleitzahl }} {{ bestellung.lieferanschrift.stadt
34:            {{ bestellung.lieferanschrift.land }}
62:            <th scope="row" colspan="4">Warenwert</th>
63:            <td class="zahl">{{ bestellung.warenwert | geld }}</td>
66:            <tr class="rabatt">
67:              <th scope="row" colspan="4">Rabatt (Gutschein {{ bestellung.gutscheincode }})</th>
68:              <td class="zahl">−{{ bestellung.rabattBetrag | geld }}</td>
72:            <th scope="row" colspan="4">Gesamtwert</th>
73:            <td class="zahl">{{ bestellung.gesamtwert | geld }}</td>
81:        <dd class="status" [class.bezahlt]="bestellung.istVollstaendigBezahlt">
82:          {{ bestellung.istVollstaendigBezahlt ? 'vollständig bezahlt' : 'offen' }}
84:        <dt>Bezahlt</dt>
85:        <dd class="bezahlt-summe">{{ bestellung.bezahlteSumme | geld }}</dd>
katalog/produkt-anlegen/produkt-anlegen.html:27:    <button type="submit" [disabled]="sendet()">Speichern</button>
4:type Dienstleister = Versandpaket.DienstleisterEnum;
16:export const DIENSTLEISTER: readonly { wert: Dienstleister; name: string }[] = [
17:  { wert: Versandpaket.DienstleisterEnum.Dhl, name: 'DHL' },
18:  { wert: Versandpaket.DienstleisterEnum.Dpd, name: 'DPD' },
19:  { wert: Versandpaket.DienstleisterEnum.Gls, name: 'GLS' },
20:  { wert: Versandpaket.DienstleisterEnum.Hermes, name: 'Hermes' },
21:  { wert: Versandpaket.DienstleisterEnum.Ups, name: 'UPS' },
28:export function dienstleisterName(dienstleister: Dienstleister): string {
```

**⏺ Bash**

```bash
ls kunde/kunde-registrieren kunde/adresse-formular; grep -rn -i -B1 -A2 'type="checkbox"' kunde/ --exclude='*.spec.ts' | grep -v '^\-\-' | head -30
```

```
kunde/adresse-formular:
adresse-formular.ts

kunde/kunde-registrieren:
kunde-registrieren.spec.ts
kunde-registrieren.ts
kunde/adresse-formular/adresse-formular.ts-25-      <label class="auswahl">
kunde/adresse-formular/adresse-formular.ts:26:        <input type="checkbox" formControlName="istLieferadresse" />
kunde/adresse-formular/adresse-formular.ts-27-        Lieferadresse
kunde/adresse-formular/adresse-formular.ts-28-      </label>
kunde/adresse-formular/adresse-formular.ts-29-      <label class="auswahl">
kunde/adresse-formular/adresse-formular.ts:30:        <input type="checkbox" formControlName="istRechnungsadresse" />
kunde/adresse-formular/adresse-formular.ts-31-        Rechnungsadresse
kunde/adresse-formular/adresse-formular.ts-32-      </label>
kunde/kunde-registrieren/kunde-registrieren.ts-39-          id="istAuchRechnungsadresse"
kunde/kunde-registrieren/kunde-registrieren.ts:40:          type="checkbox"
kunde/kunde-registrieren/kunde-registrieren.ts-41-          formControlName="istAuchRechnungsadresse"
kunde/kunde-registrieren/kunde-registrieren.ts-42-        />
```

**⏺ Bash**

```bash
sed -n 43,46p kunde/kunde-registrieren/kunde-registrieren.ts; grep -n 'bezahlt\|offen' bestellung/bestellhistorie/*.html | head -5
```

```
        Auch als Rechnungsadresse verwenden
      </label>

      @if (fehlermeldung()) {
(eval):1: no matches found: bestellung/bestellhistorie/*.html
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && python3 - <<'EOF'
p='docs/benutzerhandbuch.md'
s=open(p).read()
s=s.replace('Optional „auch Rechnungsadresse“ ankreuzen.','Optional **Auch als Rechnungsadresse verwenden** ankreuzen.')
s=s.replace('  - **Zahlung**: Bezahlstatus, bezahlte Summe,','  - **Zahlung**: Bezahlstatus („vollständig bezahlt“ oder „offen“), bezahlte Summe,')
s=s.replace('Zahlung per Swagger auf `ERFOLGREICH` setzen: Bestellung zeigt „bezahlt“.','Zahlung per Swagger auf `ERFOLGREICH` setzen: Bestellansicht zeigt „vollständig bezahlt“, die Historie „bezahlt“.')
s=s.replace('Anschrift eingeben, mindestens eines der Kennzeichen *Lieferadresse* oder *Rechnungsadresse* setzen','Anschrift eingeben, mindestens eines der Kennzeichen **Lieferadresse** oder **Rechnungsadresse** ankreuzen')
open(p,'w').write(s)
EOF
grep -c 'Auch als Rechnungsadresse\|vollständig bezahlt' docs/benutzerhandbuch.md
```

```
3
```

### ● Claude

Jetzt README neu.

**⏺ Write**

````
/home/mschachner/Projects/bestellsystem/README.md
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
… [gekürzt, 133 Zeilen gesamt]
````

```
The file /home/mschachner/Projects/bestellsystem/README.md has been updated successfully. (file state is current in your context — no need to Read it back)
```

### ● Claude

Platzhalter `{D-xx}` in Links umwandeln, Links prüfen.

**⏺ Bash**

```bash
python3 - <<'EOF'
import re,glob,os
anchors={}
heads=[]
for l in open('docs/01-entscheidungen.md'):
    if l.startswith('## D-'):
        t=l[3:].strip(); anchors[t[:4]]=re.sub(r'[^\w\- ]','',t.lower()).replace(' ','-')
files=glob.glob('docs/adr/*.md')+['docs/arc42.md','docs/benutzerhandbuch.md']
for f in files:
    s=open(f).read()
    rel=os.path.relpath('docs/01-entscheidungen.md',os.path.dirname(f))
    n=len(re.findall(r'\{D-\d\d\}',s))
    s=re.sub(r'\{(D-\d\d)\}',lambda m:f'[{m[1]}]({rel}#{anchors[m[1]]})',s)
    open(f,'w').write(s); print(f,n)
# verify all relative links resolve
bad=0
for f in files+['README.md']:
    for tgt in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)',open(f).read()):
        if tgt.startswith('http'): continue
        if not os.path.exists(os.path.normpath(os.path.join(os.path.dirname(f),tgt))):
            print('BROKEN',f,tgt); bad+=1
print('broken',bad)
EOF
grep -rn '{D-' docs README.md | head
```

```
docs/adr/0005-postgresql-flyway-testcontainers.md 6
docs/adr/0001-gradle-als-build-werkzeug.md 0
docs/adr/0008-nebenlaeufigkeit-an-der-aggregatwurzel.md 11
docs/adr/0006-geld-als-cent-wertobjekt.md 6
docs/adr/0003-pessimistische-sperre-im-checkout.md 2
docs/adr/0007-fehler-als-problemdetail.md 6
docs/adr/0002-openapi-spezifikation-als-datei.md 0
docs/adr/0010-angular-frontend-mit-generiertem-client.md 6
docs/adr/README.md 65
docs/adr/0004-modularer-monolith-nach-fachlichkeit.md 5
docs/adr/0009-zeit-ueber-injizierte-uhr.md 7
docs/adr/0011-benachrichtigung-ueber-port.md 4
docs/arc42.md 22
docs/benutzerhandbuch.md 15
broken 0
```

### ● Claude

Alle Links gültig. ADR 0002 und ARCHITEKTUR.md an YAML-Export anpassen.

**⏺ Bash**

```bash
python3 - <<'EOF'
p='docs/adr/0002-openapi-spezifikation-als-datei.md'
s=open(p).read()
s=s.replace("- **`./gradlew aktualisiereOpenApi`** schreibt sie neu.","- **`./gradlew aktualisiereOpenApi`** schreibt sie neu, zusammen mit der Dokumentationsfassung **`docs/api/openapi.yaml`** (gleicher Inhalt als YAML).")
s=s.replace("- **`OpenApiSpezifikationIT`** läuft in `./gradlew build` und vergleicht die Datei mit der aktuellen API. Weicht sie ab, ist der Build rot.","- **`OpenApiSpezifikationIT`** läuft in `./gradlew build` und vergleicht beide Dateien mit der aktuellen API. Weicht eine ab, ist der Build rot.")
s=s.replace("- Ablauf bei API-Änderungen: `./gradlew aktualisiereOpenApi`, dann `npm run generate:api`. Beides wird committet.","- Ablauf bei API-Änderungen: `./gradlew aktualisiereOpenApi`, dann `npm run generate:api`. Alles wird committet.")
s=s.replace("- Die exportierte Spec ist zugleich die API-Dokumentation. Schritt 9 kann sie zusätzlich als `docs/api/openapi.yaml` ablegen.","- Die exportierte Spec ist zugleich die API-Dokumentation; `docs/api/openapi.yaml` ist ihre lesbare Fassung (seit Schritt 9).")
open(p,'w').write(s)
p='docs/ARCHITEKTUR.md'
s=open(p).read()
s=s.replace("Stand: Schritt 8 (Versand E07: US-07.1 bis US-07.3, D-22). Wird mit jedem Schritt im selben Commit nachgezogen.",
"Stand: Schritt 9 (Abschlussdokumentation). Wird mit jedem Schritt im selben Commit nachgezogen.\n\nÜberblick und Begründungen: [arc42.md](arc42.md); technische Entscheidungen: [adr/](adr/README.md); Bedienung: [benutzerhandbuch.md](benutzerhandbuch.md). Dieses Dokument ist die Detailreferenz.")
s=s.replace("Exportiert nach `frontend/openapi.json` ([ADR 0002](adr/0002-openapi-spezifikation-als-datei.md)).","Exportiert nach `frontend/openapi.json` (Quelle des Clients) und `docs/api/openapi.yaml` (Dokumentation), beide per `./gradlew aktualisiereOpenApi` ([ADR 0002](adr/0002-openapi-spezifikation-als-datei.md)).")
s=s.replace("| Spec-Drift | `OpenApiSpezifikationIT`; `./gradlew aktualisiereOpenApi` schreibt die Datei neu | ADR 0002 |","| Spec-Drift | `OpenApiSpezifikationIT` (prüft `frontend/openapi.json` und `docs/api/openapi.yaml`); `./gradlew aktualisiereOpenApi` schreibt beide neu | ADR 0002 |")
open(p,'w').write(s)
EOF
git diff --stat
```

```
 README.md                                          | 156 ++++++++++++++++-----
 backend/build.gradle.kts                           |   7 +-
 .../infrastruktur/OpenApiSpezifikationIT.java      |  43 ++++--
 docs/ARCHITEKTUR.md                                |   8 +-
 docs/adr/0002-openapi-spezifikation-als-datei.md   |   8 +-
 docs/adr/0003-pessimistische-sperre-im-checkout.md |   4 +-
 6 files changed, 166 insertions(+), 60 deletions(-)
```

### ● Claude

Jetzt Gesamtbuild und Frontend-Tests.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew build -q 2>&1 | grep -vE 'INFO|WARN|Sharing is only' | tail -15; echo "BACKEND EXIT ${pipestatus[1]}"; cd ../frontend && npm test -- --watch=false 2>&1 | tail -6
```

```
BACKEND EXIT 0

[2m Test Files [22m [1m[32m15 passed[39m[22m[90m (15)[39m
[2m      Tests [22m [1m[32m94 passed[39m[22m[90m (94)[39m
[2m   Start at [22m 12:42:44
[2m   Duration [22m 3.27s[2m (transform 1.30s, setup 6.39s, import 4.17s, tests 4.01s, environment 18.41s)[22m
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && python3 - <<'EOF'
import glob,re
t=f=s=0
for x in glob.glob('build/test-results/test/*.xml'):
    h=open(x).read(500)
    m=re.search(r'tests="(\d+)" skipped="(\d+)" failures="(\d+)" errors="(\d+)"',h)
    t+=int(m[1]);s+=int(m[2]);f+=int(m[3])+int(m[4])
print('tests',t,'skipped',s,'failed',f)
EOF
grep -l 'dokumentierteSpezifikationIstAktuell' build/test-results/test/*.xml
```

```
tests 334 skipped 0 failed 0
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ls build/test-results/test/ | grep -i openapi; grep -o 'testcase name="[^"]*"' build/test-results/test/*OpenApi*.xml
```

```
TEST-de.bestellsystem.gemeinsam.infrastruktur.OpenApiSpezifikationIT.xml
testcase name="ADR 0002: frontend/openapi.json entspricht der aktuellen API"
testcase name="ADR 0002: docs/api/openapi.yaml entspricht der aktuellen API"
```

### ● Claude

Beide Drift-Tests liefen grün. Gegenprobe: veränderte YAML muss rot werden.

**⏺ Bash**

```bash
cp ../docs/api/openapi.yaml /tmp/oa.bak && echo "x: 1" >> ../docs/api/openapi.yaml && ./gradlew test --tests '*OpenApiSpezifikationIT' -q 2>&1 | grep -E 'FAILED|veraltet' | head -3; cp /tmp/oa.bak ../docs/api/openapi.yaml && cmp /tmp/oa.bak ../docs/api/openapi.yaml && echo restored
```

```
BUILD FAILED in 10s
restored
```

### ● Claude

Rot wie erwartet, wiederhergestellt. CLAUDE.md-Befehlskommentar nachziehen, dann committen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && sed -i 's|# frontend/openapi.json aus dem Code exportieren (ADR 0002)|# frontend/openapi.json + docs/api/openapi.yaml exportieren (ADR 0002)|' CLAUDE.md && grep -n aktualisiereOpenApi CLAUDE.md && git add CLAUDE.md README.md backend/build.gradle.kts backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java docs/ARCHITEKTUR.md docs/arc42.md docs/benutzerhandbuch.md docs/api/openapi.yaml docs/adr/ && git status --short
```

```
42:cd backend && ./gradlew aktualisiereOpenApi  # frontend/openapi.json + docs/api/openapi.yaml exportieren (ADR 0002)
M  CLAUDE.md
M  README.md
M  backend/build.gradle.kts
M  backend/src/test/java/de/bestellsystem/gemeinsam/infrastruktur/OpenApiSpezifikationIT.java
M  docs/ARCHITEKTUR.md
M  docs/adr/0002-openapi-spezifikation-als-datei.md
M  docs/adr/0003-pessimistische-sperre-im-checkout.md
A  docs/adr/0004-modularer-monolith-nach-fachlichkeit.md
A  docs/adr/0005-postgresql-flyway-testcontainers.md
A  docs/adr/0006-geld-als-cent-wertobjekt.md
A  docs/adr/0007-fehler-als-problemdetail.md
A  docs/adr/0008-nebenlaeufigkeit-an-der-aggregatwurzel.md
A  docs/adr/0009-zeit-ueber-injizierte-uhr.md
A  docs/adr/0010-angular-frontend-mit-generiertem-client.md
A  docs/adr/0011-benachrichtigung-ueber-port.md
A  docs/adr/README.md
A  docs/api/openapi.yaml
A  docs/arc42.md
A  docs/benutzerhandbuch.md
```

**⏺ Bash**

```bash
git commit -q -F - <<'EOF'
Add final documentation: README, arc42, ADRs, user guide, OpenAPI

- README with purpose, prerequisites, Docker Compose quickstart, tests
- docs/arc42.md: context, building blocks, runtime (checkout, dunning
  run), deployment, cross-cutting concepts, risks
- ADR 0004 to 0011 plus ADR index linking every D-xx decision
- docs/benutzerhandbuch.md with the main user workflows
- docs/api/openapi.yaml exported by aktualisiereOpenApi and checked
  for drift by OpenApiSpezifikationIT

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1
```

```
b8ff055 Add final documentation: README, arc42, ADRs, user guide, OpenAPI
```

**⏺ Bash**

```bash
sed -i 's/^## \[ \] 9 Dokumentation$/## [x] 9 Dokumentation (2026-11-12, b8ff055)/' docs/fahrplan.md && grep -n '^## \[x\] 9' docs/fahrplan.md && git add docs/fahrplan.md && git commit -q -m "Hake Schritt 9 im Fahrplan ab" -m "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -2 && git status --short
```

```
188:## [x] 9 Dokumentation (2026-11-12, b8ff055)
373242e Hake Schritt 9 im Fahrplan ab
b8ff055 Add final documentation: README, arc42, ADRs, user guide, OpenAPI
[This command modified 1 file you've previously read: docs/fahrplan.md. Call Read before editing.]
```

### ● Claude

Schritt 9 (Dokumentation) ist fertig und abgehakt. Das war der letzte Schritt im Fahrplan, es ist nichts mehr offen.

**Fertig wenn, Punkt für Punkt:**
- **`README.md`**: neu geschrieben mit Zweck, Voraussetzungen, Schnellstart über Docker Compose, Konfiguration, Tests und dem Ablauf bei API-Änderungen. Dazu Projektstruktur, eine Tabelle aller Doku-Dateien und Epics mit allen umgesetzten Stories.
- **`docs/arc42.md`**: Kontext, Bausteine (Ebene 1 und 2), Laufzeit als Sequenzdiagramme für Checkout und Mahnlauf, Verteilung, Querschnitt (Fehler, Transaktionen und Nebenläufigkeit, Geld mit Formeln, Zeit, Tests), Qualitätsszenarien, Risiken, Glossar. Die zitierten Testnamen habe ich gegen den Code geprüft.
- **`docs/adr/`**: acht neue ADRs (0004 bis 0011): modularer Monolith, PostgreSQL mit Flyway und Testcontainers, Geld in Cent, ProblemDetail, Nebenläufigkeit über die Aggregatwurzel, Uhr, Angular ohne Fachlogik, Benachrichtigungs-Port. Neu ist auch `docs/adr/README.md`: ein Index mit einer Zuordnung in beide Richtungen (D-xx zu ADR und zurück). Alle D-xx-Verweise sind Links mit Anker; ein Skript hat geprüft, dass keiner ins Leere zeigt.
- **`docs/benutzerhandbuch.md`**: Produkt anlegen, Registrierung, Adressbuch, Checkout mit Ablehnungsfällen, Bestellansicht, Versand, Merkliste, Mahnwesen und Mailpit, Funktionen nur über Swagger, Gutschein per SQL und ein Durchlauf zum Ausprobieren.
- **`docs/api/openapi.yaml`**: wird reproduzierbar erzeugt. `./gradlew aktualisiereOpenApi` schreibt jetzt neben `frontend/openapi.json` auch diese Datei, und `OpenApiSpezifikationIT` prüft beide auf Abweichung. Mit einer absichtlich veränderten YAML schlug der Build fehl, danach habe ich die Datei wiederhergestellt.

Außerdem habe ich ADR 0002, `ARCHITEKTUR.md` und den Befehlskommentar in `CLAUDE.md` an den YAML-Export angepasst.

**Tests:** `./gradlew build` ist grün (334 Tests, 0 übersprungen, 0 fehlgeschlagen), `npm test` ebenfalls (94 Tests in 15 Dateien).

**Commits:**
- `b8ff055` Add final documentation: README, arc42, ADRs, user guide, OpenAPI
- `373242e` Hake Schritt 9 im Fahrplan ab

**Lücken der Oberfläche, die das Handbuch umgeht:** Gutscheine lassen sich nicht über Oberfläche oder API anlegen (US-08.2 fehlt), das Handbuch nutzt dafür SQL. Zahlungsstatus und Lagerbestand lassen sich nur über Swagger ändern. Beides steht auch in arc42 unter Risiken.

### ❯ Nutzer

> Ist nun alles abgeschlossen und fertig?

**⏺ Bash**

```bash
grep -c '^## \[ \]' docs/fahrplan.md; grep -c '^## \[x\]' docs/fahrplan.md; git status --short | head; git remote -v; git status -sb | head -1
```

```
0
11
## main
```

### ● Claude

Fahrplan ja, ein produktiver Shop nein.

**Abgeschlossen:**
- Alle 11 Schritte im Fahrplan (0 bis 9, mit 4a und 4b) sind abgehakt, keiner ist mehr offen.
- Der Arbeitsstand ist komplett committet, es liegt nichts Ungespeichertes herum.
- Backend: 334 Tests grün. Frontend: 94 Tests grün.

**Was noch offen ist:**
- **Kein Remote.** Das Repo hat kein `git remote`. Es ist also nirgends gepusht, und der CI-Workflow ist noch nie gelaufen. Bisher ist alles nur lokal geprüft.
- **Grenzen des MVP**, so geplant:
  - Kein Login und keine Berechtigungen: Jeder kann jede Kunden-ID aufrufen. Vor einem echten Betrieb ist das Pflicht.
  - Gutscheine lassen sich nur per SQL anlegen. Zahlungsstatus und Lagerbestand lassen sich nur über Swagger ändern.
  - Bewusst ausgelassen: Storno, Versandkosten, Steuern, Mahnstufen, echte Zahlungsanbieter.
- **Für mehrere Instanzen oder Last fehlt noch:**
  - Der Mahnlauf hat keine Sperre gegen parallele Läufe.
  - Mahn-E-Mails werden „mindestens einmal“ verschickt, können also doppelt kommen. Ein Outbox-Muster würde das verhindern.
  - Für eine Produktionsumgebung gibt es noch keine Festlegung.

Alle Punkte stehen in `docs/arc42.md` unter „11 Risiken“.

Sinnvolle nächste Schritte wären ein Remote anlegen und pushen (dann läuft die CI zum ersten Mal), danach Login sowie Pflegeoberflächen für Gutscheine und Bestand (US-08.2, US-02.2) als neue Schritte im Fahrplan. Soll ich das Remote einrichten?

### ❯ Nutzer

> Geht das mit dem naechster-schritt skill?

### ● Claude

Nicht direkt. Der Skill nimmt immer den obersten Schritt mit `## [ ]` aus `docs/fahrplan.md`, und alle Schritte sind `[x]`. Ein Aufruf jetzt würde nur melden, dass nichts offen ist.

Damit `/naechster-schritt` wieder etwas zu tun hat, müssen neue Schritte in den Fahrplan, im selben Format wie bisher (Werkzeug, Eingaben, Prompt, Fertig wenn). Ein Vorschlag:

| Schritt | Inhalt | Werkzeug |
|---|---|---|
| 10 Remote + CI | Repo auf GitHub anlegen, pushen, CI grün | von Hand |
| 11 Pflege Gutschein + Bestand | US-08.2 und US-02.2 klären, Backend und Frontend | feature-dev |
| 12 Login | Spring Security, Rollen Kunde und Mitarbeiter; braucht vorher eine Änderung in `CLAUDE.md`, dort steht Login als „nicht im Umfang“ | feature-dev |

Zu Schritt 10: Ein GitHub-Repo anlegen und pushen veröffentlicht den Code. Das macht der Skill nicht von sich aus. Dafür muss ich von dir wissen:
- Konto oder Organisation und Name des Repos,
- privat oder öffentlich.

Soll ich die Schritte so in den Fahrplan eintragen? Dann läuft der nächste über `/clear` und `/naechster-schritt` wie gewohnt.

### ❯ Nutzer

> Fix dann alles in dieser Session!

**⏺ Bash**

```bash
gh auth status 2>&1 | head -6; gh repo view jellybrigade/bestellsystem --json name,visibility 2>&1 | head -2
```

```
github.com
  ✓ Logged in to github.com account jellybrigade (keyring)
  - Active account: true
  - Git operations protocol: ssh
  - Token: gho_************************************
  - Token scopes: 'admin:public_key', 'delete_repo', 'gist', 'read:org', 'repo'
GraphQL: Could not resolve to a Repository with the name 'jellybrigade/bestellsystem'. (repository)
```

**⏺ AskUserQuestion**

```
{
 "questions": [
  {
   "question": "GitHub-Repo für das Bestellsystem: gh ist als jellybrigade angemeldet, und jellybrigade/bestellsystem gibt es noch nicht. Wie soll ich es anlegen und pushen?",
   "header": "Remote",
   "multiSelect": false,
   "options": [
    {
     "label": "Privat (Recommended)",
     "description": "gh repo create jellybrigade/bestellsystem --private, main pushen, CI-Lauf abwarten und prüfen"
    },
    {
     "label": "Öffentlich",
     "description": "Gleich, aber öffentlich sichtbar: Code wird veröffentlicht und ggf. indexiert"
    },
    {
     "label": "Kein Remote",
     "description": "Schritt 10 überspringen, alles bleibt lokal"
    }
   ]
  },
  {
   "question": "Login steht in CLAUDE.md unter „Nicht im Umfang (MVP)“. Soll er trotzdem jetzt gebaut werden?",
   "header": "Login",
   "multiSelect": false,
   "options": [
    {
     "label": "Weglassen (Recommended)",
     "description": "Login bleibt außerhalb des MVP; nur als Risiko dokumentiert. Spart den größten und riskantesten Umbau (alle Pfade, Tests, Frontend)"
    },
… [gekürzt, 68 Zeilen gesamt]
```

```
Your questions have been answered: "GitHub-Repo für das Bestellsystem: gh ist als jellybrigade angemeldet, und jellybrigade/bestellsystem gibt es noch nicht. Wie soll ich es anlegen und pushen?"="Privat (Recommended)", "Login steht in CLAUDE.md unter „Nicht im Umfang (MVP)“. Soll er trotzdem jetzt gebaut werden?"="Weglassen (Recommended)", "CLAUDE.md verlangt einen Fahrplan-Schritt pro Sitzung. Für deinen Auftrag „alles in dieser Session“ wäre das eine Ausnahme. Ist die okay?"="Ausnahme okay (Recommended)", "Gutschein anlegen (US-08.2) und Bestand/Produkt pflegen (US-02.2) haben noch keine Akzeptanzkriterien. Wie sollen die entstehen?"="Vorschlag + Freigabe (Recommended)". You can now continue with these answers in mind.
```

### ● Claude

Entscheidungen klar: Repo privat, kein Login, Ausnahme okay, Kriterien zur Freigabe. Fahrplan erweitern, dazu Epic-Kandidaten lesen.

**⏺ Bash**

```bash
cat epics/E02-produktkatalog-lagerbestand.md epics/E08-gutscheine-aktionen.md; cat stories/US-02.1-verfuegbarkeit-pruefen.md; tail -25 docs/fahrplan.md
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
# E08 Gutscheine & Aktionen

**Priorität:** MVP (gemeinsam mit E04, da der Rabatt in den Gesamtwert eingeht)
**Akteure:** Shop-Mitarbeiter (Pflege), Kunde (Einlösung)
**Abhängig von:** E04

## Ziel

Aktionscodes gewähren einen prozentualen oder festen Rabatt auf eine Bestellung, solange sie gültig sind.

## Umfang

… [gekürzt, 117 Zeilen gesamt]
```

### ❯ Nutzer

> [Request interrupted by user]

### ❯ Nutzer

> Ich erstelle die Repo.

### ● Claude

Gut, das Repo legst du an. Ich lege kein Remote an und pushe nichts. Wenn es steht: entweder du pushst selbst, oder du gibst mir die URL, dann setze ich `origin`, pushe `main` und prüfe den ersten CI-Lauf. Schritt 10 im Fahrplan hängt daran.

Inzwischen der Entwurf für **Schritt 11 (Pflege von Produkten und Gutscheinen)** zur Freigabe. US-02.3 („Lagerbestand buchen“ mit Historie) bleibt draußen.

## US-02.2 Produkt pflegen (Titel, Listenpreis, Bestand)

**Neue API:** `PUT /api/v1/produkte/{id}` mit `{titel, listenpreis}`. Den Bestand setzt weiter der bestehende Endpunkt `PUT …/lagerbestand`.

1. **Ändern:** Titel und Listenpreis werden ersetzt, Antwort 200 mit dem Produkt.
2. **Gleiche Regeln wie beim Anlegen (D-15):** Titel getrimmt und 1 bis 200 Zeichen, Preis ≥ 0 Cent. Sonst 400.
3. **Unbekanntes Produkt:** 404.
4. **Bestehende Bestellungen bleiben unverändert (D-10):** Ein Preis von 10 € auf 12 € ändert den Einzelpreis einer alten Bestellung nicht.
5. **Merkliste zeigt den neuen Preis:** Sie liest den aktuellen Preis (D-21).
6. **Gleichzeitige Änderung:** Der Verlierer bekommt 409 Versionskonflikt, wie beim Bestand.
7. **Frontend:** In der Produktliste je Produkt **Bearbeiten**. Das führt auf `/produkte/:id` mit zwei Formularen: „Stammdaten“ (Titel, Preis) und „Lagerbestand“. Erfolg und Fehler (ProblemDetail) werden angezeigt.

## US-08.2 Gutschein anlegen

**Neue API:** `POST /api/v1/gutscheine` und `GET /api/v1/gutscheine`

1. **Prozentgutschein:** `{code, rabattArt: PROZENT, prozent, ablaufdatum}` ergibt 201 mit `Location`.
2. **Festbetrag:** `{code, rabattArt: FESTBETRAG, festbetrag: {cent, waehrung}, ablaufdatum}` ergibt 201.
3. **Rabattwert ungültig (D-04, D-17):** Prozent ≤ 0, > 100 oder mehr als zwei Nachkommastellen, Festbetrag ≤ 0, oder Wert passt nicht zur Rabattart. Jeweils 400.
4. **Code:** wird getrimmt und großgeschrieben gespeichert. Bereits vergeben, auch in anderer Schreibweise: 409.
5. **Liste:** alle Gutscheine mit Code, Rabatt, Ablaufdatum und `istGueltig` (heute ≤ Ablaufdatum, laut Uhr).
6. **Wirkung:** Ein frisch angelegter Gutschein ist im Checkout sofort einlösbar.
7. **Frontend:** neuer Menüpunkt **Gutscheine** mit Liste (Laden, Fehler, Leerzustand) und Formular „Gutschein anlegen“.

**Neue Entscheidung D-23 mit drei offenen Punkten. Mein Vorschlag:**
- **a)** Code nur Buchstaben, Ziffern und `-`, 1 bis 50 Zeichen (bisher war jeder Text bis 50 Zeichen erlaubt).
- **b)** Ein Ablaufdatum vor heute wird mit 422 abgelehnt. Ein abgelaufener Gutschein wäre sofort nutzlos.
- **c)** Gutscheine ändern oder löschen ist nicht im Umfang.

Gibst du das frei, oder willst du bei a bis c oder bei den Kriterien etwas ändern? Danach trage ich die Schritte 10 und 11 in den Fahrplan ein, lege D-23 und die Stories ab und baue sie wie gewohnt: Test zuerst, eigene Commits, Abhaken.

### ❯ Nutzer

> Ok go.

### ● Claude

Los. Erst bestehenden Code für Produkt und Gutschein lesen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && cat katalog/domain/Produkt.java katalog/application/ProduktService.java katalog/api/ProduktController.java katalog/api/ProduktAnlegenDto.java katalog/api/ProduktDto.java katalog/api/LagerbestandDto.java
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
… [gekürzt, 387 Zeilen gesamt]
```

**⏺ Bash**

```bash
ls gutschein/*/; cat gutschein/domain/Gutschein.java gutschein/application/GutscheinService.java gutschein/persistence/GutscheinRepository.java gemeinsam/api/GeldDto.java; ls gemeinsam/infrastruktur; sed -n 1,80p gemeinsam/infrastruktur/OpenApiKonfiguration.java
```

```
gutschein/application/:
GutscheinService.java

gutschein/domain/:
Gutschein.java
RabattArt.java

gutschein/persistence/:
GutscheinRepository.java
package de.bestellsystem.gutschein.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** Aktionscode mit prozentualem oder festem Rabatt auf den Warenwert (D-04, D-17). */
@Entity
@Table(name = "gutschein")
public class Gutschein {

    public static final int CODE_MAXIMALE_LAENGE = 50;

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    @Id private UUID id;

    private String code;

    @Enumerated(EnumType.STRING)
    private RabattArt rabattArt;

… [gekürzt, 239 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src && sed -n 1,200p test/java/de/bestellsystem/katalog/api/ProduktApiIT.java; grep -n -A25 'public Kunde registriere' main/java/de/bestellsystem/kunde/application/KundeService.java
```

```
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
@AutoConfigureMockMvc
@Import(TestcontainersKonfiguration.class)
class ProduktApiIT {

    private static final String PRODUKTE = "/api/v1/produkte";

    @Autowired private MockMvc mockMvc;
    @Autowired private ProduktRepository repository;

    @BeforeEach
    void leereKatalog() {
        repository.deleteAll();
… [gekürzt, 226 Zeilen gesamt]
```

### ● Claude

Doku zuerst: D-23, zwei Stories, Fahrplan-Schritte 10 und 11, Epic-Status.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && cat >> docs/01-entscheidungen.md <<'EOF'

---

## D-23 Produkt und Gutschein pflegen

**Problem:** E02 und E08 nennen „Titel und Listenpreis ändern“ und „Gutschein anlegen“, die Stories US-02.2 und US-08.2 waren aber nur Kandidaten. Offen waren erlaubte Zeichen im Gutscheincode, der Umgang mit einem Ablaufdatum in der Vergangenheit und Statuscodes.

**Entscheidung:**
- Produkt ändern: `PUT /api/v1/produkte/{id}` ersetzt Titel und Listenpreis mit denselben Regeln wie beim Anlegen (D-15). Der Lagerbestand bleibt beim eigenen Endpunkt `PUT …/lagerbestand`. Bestehende Bestellungen behalten ihren Einzelpreis (D-10); die Merkliste zeigt den neuen Preis. Gleichzeitige Änderungen: 409 Versionskonflikt.
- Gutschein anlegen: `POST /api/v1/gutscheine` (201 + `Location`), Liste `GET /api/v1/gutscheine` nach Code sortiert, je Gutschein `istGueltig` (heute laut Uhr ≤ Ablaufdatum, D-04).
- Neue Codes bestehen aus 1–50 Buchstaben, Ziffern und `-`; gespeichert getrimmt und großgeschrieben (D-17). Sonst 400. Beim Einlösen bleibt die Suche unverändert: ein Code mit anderen Zeichen ist dort einfach unbekannt (422, US-08.1 AK6).
- Prozent: größer 0, höchstens 100, höchstens zwei Nachkommastellen; Festbetrag größer 0 Cent. Der Rabattwert muss zur Rabattart passen (genau einer von `prozent` und `festbetrag`). Verstöße: 400.
- Code bereits vergeben, auch in anderer Schreibweise oder gleichzeitig angelegt: **409**.
- Ablaufdatum vor heute (laut Uhr): **422**. Heute ist erlaubt.
- Frontend: Seite `/produkte/:id` mit den Formularen „Stammdaten“ und „Lagerbestand“, erreichbar über „Bearbeiten“ in der Produktliste; Menüpunkt „Gutscheine“ mit Liste und Formular „Gutschein anlegen“.

**Nicht im Umfang (Schritt 11):** Gutschein ändern oder löschen, Produkt löschen, Bestandsbuchungen mit Historie (US-02.3).
EOF
cat > stories/US-02.2-produkt-pflegen.md <<'EOF'
# US-02.2 Produkt pflegen

**Epic:** E02 Produktkatalog & Lagerbestand
**Entscheidungen:** D-10, D-15, D-23

## Story

**Als** Shop-Mitarbeiter
**möchte ich** Titel, Listenpreis und Lagerbestand eines Produkts ändern,
**damit** der Katalog aktuell bleibt.
… [gekürzt, 146 Zeilen gesamt]
```

```
epics/E02-produktkatalog-lagerbestand.md:27:| [US-02.2](../stories/US-02.2-produkt-pflegen.md) | Produkt pflegen (Titel, Listenpreis, Bestand) | ausformuliert |
epics/E08-gutscheine-aktionen.md:27:| [US-08.2](../stories/US-08.2-gutschein-anlegen.md) | Gutschein anlegen | ausformuliert |
[This command modified 1 file you've previously read: docs/fahrplan.md. Call Read before editing.]
```

**⏺ Bash**

```bash
git add docs/01-entscheidungen.md docs/fahrplan.md stories/US-02.2-produkt-pflegen.md stories/US-08.2-gutschein-anlegen.md epics/E02-produktkatalog-lagerbestand.md epics/E08-gutscheine-aktionen.md && git commit -q -m "Plan steps 10 and 11, add D-23 and stories US-02.2, US-08.2" -m "Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline -1
```

```
1e1de25 Plan steps 10 and 11, add D-23 and stories US-02.2, US-08.2
```

### ● Claude

Backend, Test zuerst. Stil der Domain-Tests ansehen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && sed -n 1,40p katalog/domain/ProduktTest.java; sed -n 1,50p gutschein/domain/GutscheinTest.java; tail -5 gutschein/domain/GutscheinTest.java; ls gutschein gutschein/*; ls; grep -rn 'leere' Datenbestand.java | head -3
```

```
package de.bestellsystem.katalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
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
    void verfuegbarBeiBestand() {
        assertThat(produktMitBestand(3).istVerfuegbar()).isTrue();
    }

    @Test
    @DisplayName("US-02.1 AK2: Produkt mit Lagerbestand 0 ist nicht verfügbar")
    void nichtVerfuegbarOhneBestand() {
        assertThat(produktMitBestand(0).istVerfuegbar()).isFalse();
    }

    @Test
    @DisplayName("US-02.1 AK3: Bestand 3 reicht für Menge 3, nicht für Menge 4")
    void verfuegbarkeitFuerMenge() {
        Produkt produkt = produktMitBestand(3);

        assertThat(produkt.istVerfuegbar(3)).isTrue();
        assertThat(produkt.istVerfuegbar(4)).isFalse();
    }

… [gekürzt, 116 Zeilen gesamt]
```

**⏺ Bash**

```bash
cat Datenbestand.java FesteUhrKonfiguration.java; sed -n 1,80p bestellung/api/BestellungApiIT.java
```

```
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
package de.bestellsystem.bestellung.api;

… [gekürzt, 118 Zeilen gesamt]
```

### ● Claude

Domain-Tests für US-02.2 und US-08.2 schreiben (sollen erst nicht kompilieren).

**⏺ Bash**

```bash
python3 - <<'EOF'
p='katalog/domain/ProduktTest.java'
s=open(p).read()
i=s.rstrip().rfind('}')
add='''
    @Test
    @DisplayName("US-02.2 AK1: Titel und Listenpreis werden ersetzt, Titel getrimmt")
    void stammdatenAendern() {
        Produkt produkt = produktMitBestand(3);

        produkt.aendereStammdaten("  Stehlampe Nova ", Geld.ausCent(5999));

        assertThat(produkt.getTitel()).isEqualTo("Stehlampe Nova");
        assertThat(produkt.getListenpreis()).isEqualTo(Geld.ausCent(5999));
        assertThat(produkt.getLagerbestand()).isEqualTo(3);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("US-02.2 AK2: leerer Titel wird abgelehnt, Produkt bleibt unverändert")
    void leererTitelBeimAendern(String titel) {
        Produkt produkt = produktMitBestand(3);

        assertThatThrownBy(() -> produkt.aendereStammdaten(titel, Geld.ausCent(5999)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(produkt.getTitel()).isEqualTo("Stehlampe");
        assertThat(produkt.getListenpreis()).isEqualTo(Geld.ausCent(4999));
    }

… [gekürzt, 212 Zeilen gesamt]
```

```
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:160: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:174: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:185: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:187: error: cannot find symbol
/home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java:189: error: cannot find symbol
```

### ● Claude

Rot wie erwartet. Domain umsetzen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && python3 - <<'EOF'
p='katalog/domain/Produkt.java'
s=open(p).read()
s=s.replace('''    public void setzeLagerbestand(int lagerbestand) {''','''    /** Neue Preise gelten nur für künftige Bestellungen (D-10, D-23). */
    public void aendereStammdaten(String titel, Geld listenpreis) {
        String neuerTitel = pruefeTitel(titel);
        Geld neuerListenpreis = pruefeListenpreis(listenpreis);
        this.titel = neuerTitel;
        this.listenpreis = neuerListenpreis;
    }

    public void setzeLagerbestand(int lagerbestand) {''')
open(p,'w').write(s)

p='gutschein/domain/Gutschein.java'
s=open(p).read()
s=s.replace('import de.bestellsystem.gemeinsam.domain.Geld;','import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;\nimport de.bestellsystem.gemeinsam.domain.Geld;')
s=s.replace('import java.util.UUID;','import java.util.UUID;\nimport java.util.regex.Pattern;')
s=s.replace('''    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);
''','''    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    /** Erlaubte Zeichen neuer Codes nach dem Normalisieren (D-23). */
    public static final String CODE_MUSTER = "[A-Z0-9-]{1," + CODE_MAXIMALE_LAENGE + "}";

    private static final Pattern NEUER_CODE = Pattern.compile(CODE_MUSTER);
''')
s=s.replace('''    /** Codes werden ohne Beachtung''','''    /**
     * Legt einen Gutschein über die Pflege an (US-08.2, D-23). Genau einer von {@code prozent} und
     * {@code festbetrag} passt zur Rabattart; ein Ablaufdatum vor {@code heute} ist eine fachliche
     * Ablehnung.
… [gekürzt, 85 Zeilen gesamt]
```

```
OK
```

### ● Claude

Domain grün. Jetzt API-Integrationstests. Checkout-JSON-Format nachsehen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && grep -n -A25 'private .*bestellJson\|private String .*Json\|private RequestBuilder' bestellung/api/BestellungApiIT.java | head -50; grep -rn 'TransactionTemplate' kunde/*/KundeNebenlaeufigkeitIT.java 2>/dev/null | head -3; find . -name KundeNebenlaeufigkeitIT.java
```

```
kunde/application/KundeNebenlaeufigkeitIT.java:26:import org.springframework.transaction.support.TransactionTemplate;
kunde/application/KundeNebenlaeufigkeitIT.java:38:    @Autowired private TransactionTemplate transaktion;
./kunde/application/KundeNebenlaeufigkeitIT.java
```

**⏺ Bash**

```bash
grep -n 'gutscheincode\|zahlungsart\|positionen' bestellung/api/BestellungApiIT.java | head -8; grep -n -B3 -A12 'US-08.1 AK1' bestellung/api/BestellungApiIT.java | head -40
```

```
120:                    .andExpect(jsonPath("$.positionen.length()").value(2))
121:                    .andExpect(jsonPath("$.positionen[0].zeilennummer").value(1))
122:                    .andExpect(jsonPath("$.positionen[0].titel").value("Buch"))
123:                    .andExpect(jsonPath("$.positionen[0].menge").value(2))
124:                    .andExpect(jsonPath("$.positionen[0].einzelpreis.cent").value(1250))
125:                    .andExpect(jsonPath("$.positionen[0].zwischenbetrag.cent").value(2500))
126:                    .andExpect(jsonPath("$.positionen[1].zeilennummer").value(2))
127:                    .andExpect(jsonPath("$.positionen[1].titel").value("Lampe"))
313-
314-        @Test
315-        @DisplayName(
316:                "US-08.1 AK1: HERBST10 auf 55,00 € ergibt Rabatt 5,50 € und Gesamtwert 49,50 €")
317-        void prozentrabatt() throws Exception {
318-            gibAuf(zweiBuecherEineLampe(), "HERBST10")
319-                    .andExpect(status().isCreated())
320-                    .andExpect(jsonPath("$.gutscheincode").value("HERBST10"))
321-                    .andExpect(jsonPath("$.warenwert.cent").value(5500))
322-                    .andExpect(jsonPath("$.rabattBetrag.cent").value(550))
323-                    .andExpect(jsonPath("$.gesamtwert.cent").value(4950))
324-                    .andExpect(jsonPath("$.zahlungen[0].betrag.cent").value(4950));
325-        }
326-
327-        @Test
328-        @DisplayName("US-08.1 AK2: MINUS15 auf 55,00 € ergibt Gesamtwert 40,00 €")
```

**⏺ Bash**

```bash
grep -n -A30 'ResultActions gibAuf(' bestellung/api/BestellungApiIT.java | head -45; grep -n -A3 'zweiBuecherEineLampe()' bestellung/api/BestellungApiIT.java | tail -4
```

```
547:    private ResultActions gibAuf(String positionen, String gutscheincode) throws Exception {
548-        return gibAuf(kundenId, positionen, lieferadresse, gutscheincode);
549-    }
550-
551:    private ResultActions gibAuf(UUID kunde, String positionen, UUID adresse, String gutscheincode)
552-            throws Exception {
553-        String code = gutscheincode == null ? "null" : "\"" + gutscheincode + "\"";
554-        return mockMvc.perform(
555-                post(BESTELLUNGEN, kunde)
556-                        .contentType(MediaType.APPLICATION_JSON)
557-                        .content(
558-                                """
559-                                {"positionen": [%s], "lieferadresseId": "%s",
560-                                 "zahlungsart": "RECHNUNG", "gutscheincode": %s}
561-                                """
562-                                        .formatted(positionen, adresse, code)));
563-    }
564-
565-    private static String bestellnummer(ResultActions antwort) throws Exception {
566-        return antwort.andExpect(status().isCreated())
567-                .andReturn()
568-                .getResponse()
569-                .getHeader("Location")
570-                .replaceAll(".*/", "");
571-    }
572-
573-    private String zweiBuecherEineLampe() {
574-        return position(buch, 2) + "," + position(lampe, 1);
575-    }
576-
577-    private String einBuch() {
578-        return position(buch, 1);
579-    }
580-
581-    private static String position(Produkt produkt, int menge) {
573:    private String zweiBuecherEineLampe() {
574-        return position(buch, 2) + "," + position(lampe, 1);
575-    }
576-
```

**⏺ Bash**

```bash
mkdir -p /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/gutschein/api && cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem && cat > katalog/api/ProduktPflegeApiIT.java <<'EOF'
package de.bestellsystem.katalog.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import de.bestellsystem.Datenbestand;
import de.bestellsystem.FesteUhrKonfiguration;
import de.bestellsystem.TestcontainersKonfiguration;
import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.katalog.persistence.ProduktRepository;
import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
… [gekürzt, 428 Zeilen gesamt]
```

```
(Bash completed with no output)
```

### ● Claude

Location-Matcher im ersten Test ist Unsinn geworden, korrigieren.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/test/java/de/bestellsystem/gutschein/api && python3 - <<'EOF'
p='GutscheinApiIT.java'
s=open(p).read()
s=s.replace('''                        .andExpect(header().string("Location", endsWith(GUTSCHEINE + "/")
                                .getClass() == null ? "" : ""))''','''                        .andExpect(
                                header().string(
                                        "Location", matchesPattern(".*" + GUTSCHEINE + "/[0-9a-f-]{36}")))''')
s=s.replace('import static org.hamcrest.Matchers.endsWith;\n','')
s=s.replace('import static org.hamcrest.Matchers.hasItem;','import static org.hamcrest.Matchers.hasItem;\nimport static org.hamcrest.Matchers.matchesPattern;')
s=s.replace('java.util.UUID.randomUUID()','UUID.randomUUID()').replace('import java.time.LocalDate;','import java.time.LocalDate;\nimport java.util.UUID;')
open(p,'w').write(s)
EOF
grep -n 'matchesPattern\|UUID' GutscheinApiIT.java | head
```

```
4:import static org.hamcrest.Matchers.matchesPattern;
24:import java.util.UUID;
66:                                        "Location", matchesPattern(".*" + GUTSCHEINE + "/[0-9a-f-]{36}")))
176:        mockMvc.perform(get(GUTSCHEINE + "/{id}", UUID.randomUUID()))
```

### ● Claude

Backend-Code umsetzen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend/src/main/java/de/bestellsystem && python3 - <<'EOF'
p='katalog/application/ProduktService.java'
s=open(p).read()
s=s.replace('''    @Transactional
    public Produkt setzeLagerbestand(''','''    @Transactional
    public Produkt aendere(UUID produktId, String titel, Geld listenpreis) {
        Produkt produkt = finde(produktId);
        produkt.aendereStammdaten(titel, listenpreis);
        return produkt;
    }

    @Transactional
    public Produkt setzeLagerbestand(''')
open(p,'w').write(s)

p='katalog/api/ProduktController.java'
s=open(p).read()
s=s.replace('''    @PutMapping("/{produktId}/lagerbestand")''','''    @PutMapping("/{produktId}")
    @Operation(
            operationId = "aendereProdukt",
            summary = "Titel und Listenpreis ändern (US-02.2, D-23)",
            description = "Bestehende Bestellungen behalten ihren Einzelpreis (D-10).")
    @ApiResponse(responseCode = "200", description = "Geändert")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe (D-15)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
… [gekürzt, 338 Zeilen gesamt]
```

```
done
```

**⏺ Bash**

```bash
for f in build/test-results/test/*{ProduktPflegeApiIT,GutscheinApiIT,ProduktTest,GutscheinTest}.xml; do grep -o 'testsuite name="[^"]*" tests="[0-9]*" skipped="[0-9]*" failures="[0-9]*" errors="[0-9]*"' $f; done
```

```
testsuite name="de.bestellsystem.katalog.api.ProduktPflegeApiIT" tests="6" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.gutschein.api.GutscheinApiIT" tests="9" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.katalog.domain.ProduktTest" tests="23" skipped="0" failures="0" errors="0"
testsuite name="de.bestellsystem.gutschein.domain.GutscheinTest" tests="24" skipped="0" failures="0" errors="0"
```

### ● Claude

Backend grün (6 + 9 neue IT-Fälle). OpenAPI exportieren, Client erzeugen.

**⏺ Bash**

```bash
./gradlew aktualisiereOpenApi -q 2>&1 | grep -iE 'fail|error' | head; cd ../frontend && npm run generate:api 2>&1 | tail -3; git -C .. status --short; ls src/app/api/api; grep -n 'prozent\|festbetrag' src/app/api/model/gutschein*.ts | head
```

```
[[bestellsystem] openapi.json] ############################################################################################
[[bestellsystem] openapi.json] java -jar "/home/mschachner/Projects/bestellsystem/frontend/node_modules/@openapitools/openapi-generator-cli/versions/7.27.0.jar" generate --input-spec="openapi.json" --generator-name="typescript-angular" --output="src/app/api" --additional-properties="ngVersion=21.2.0,providedIn=root,fileNaming=kebab-case,supportsES6=true" exited with code 0
[bestellsystem] openapi.json
 M backend/src/main/java/de/bestellsystem/gutschein/application/GutscheinService.java
 M backend/src/main/java/de/bestellsystem/gutschein/domain/Gutschein.java
 M backend/src/main/java/de/bestellsystem/gutschein/persistence/GutscheinRepository.java
 M backend/src/main/java/de/bestellsystem/katalog/api/ProduktController.java
 M backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
 M backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
 M backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java
 M backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
 M docs/api/openapi.yaml
 M frontend/openapi.json
 M frontend/src/app/api/.openapi-generator/FILES
 M frontend/src/app/api/api/api.ts
 M frontend/src/app/api/api/produkte.service.ts
 M frontend/src/app/api/model/models.ts
?? backend/src/main/java/de/bestellsystem/gutschein/api/
?? backend/src/main/java/de/bestellsystem/katalog/api/ProduktAendernDto.java
?? backend/src/test/java/de/bestellsystem/gutschein/api/
?? backend/src/test/java/de/bestellsystem/katalog/api/ProduktPflegeApiIT.java
?? frontend/src/app/api/api/gutscheine.service.ts
?? frontend/src/app/api/model/gutschein-anlegen.ts
?? frontend/src/app/api/model/gutschein.ts
?? frontend/src/app/api/model/produkt-aendern.ts
adressbuch.service.ts
api.ts
bestellungen.service.ts
gutscheine.service.ts
kunden.service.ts
mahnwesen.service.ts
merkliste.service.ts
produkte.service.ts
versand.service.ts
zahlungen.service.ts
src/app/api/model/gutschein.ts:18:  festbetrag?: Geld;
src/app/api/model/gutschein.ts:27:  prozent?: number;
src/app/api/model/gutschein-anlegen.ts:13: * Neuer Gutschein (D-04, D-23). Genau einer von prozent und festbetrag, passend zur Rabattart.
src/app/api/model/gutschein-anlegen.ts:27:  festbetrag?: Geld;
src/app/api/model/gutschein-anlegen.ts:31:  prozent?: number;
```

### ● Claude

Backend-Gesamtbuild im Hintergrund, parallel Frontend lesen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew build -q > /tmp/build11.log 2>&1; echo "EXIT $?" >> /tmp/build11.log
```

```
Command running in background with ID: bo8nr008y. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/bo8nr008y.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem/frontend; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && ls katalog/produkt-anlegen katalog/produkt-liste gemeinsam; cat katalog/produkt-anlegen/produkt-anlegen.ts katalog/produkt-anlegen/produkt-anlegen.html; cat gemeinsam/euro-eingabe.ts 2>/dev/null | head -60
```

```
gemeinsam:
euro-eingabe.spec.ts
euro-eingabe.ts
geld.pipe.spec.ts
geld.pipe.ts
problem-meldung.spec.ts
problem-meldung.ts

katalog/produkt-anlegen:
produkt-anlegen.html
produkt-anlegen.spec.ts
produkt-anlegen.ts

katalog/produkt-liste:
produkt-liste.css
produkt-liste.html
produkt-liste.spec.ts
produkt-liste.ts
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProdukteService } from '../../api';
import { euroBetrag, euroInCent } from '../../gemeinsam/euro-eingabe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

@Component({
  selector: 'app-produkt-anlegen',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './produkt-anlegen.html',
})
export class ProduktAnlegen {
  private readonly produkteService = inject(ProdukteService);
  private readonly router = inject(Router);

  protected readonly formular = inject(NonNullableFormBuilder).group({
    titel: ['', [Validators.required, Validators.maxLength(200)]],
    listenpreis: ['', [Validators.required, euroBetrag]],
    lagerbestand: [0, [Validators.required, Validators.min(0), Validators.pattern(/^\d+$/)]],
  });
… [gekürzt, 122 Zeilen gesamt]
```

### ● Claude

IDE-Diagnosen betreffen bestehende Specs (Vitest-Globals), Tests liefen grün; ignoriere sie. Produktliste und Spec-Stil lesen.

**⏺ Bash**

```bash
cat katalog/produkt-liste/produkt-liste.html katalog/produkt-liste/produkt-liste.ts; cat katalog/produkt-anlegen/produkt-anlegen.spec.ts | head -80
```

```
<div class="titelzeile">
  <h1>Produkte</h1>
  <a class="knopf" routerLink="/produkte/neu">Produkt anlegen</a>
</div>

@switch (zustand()) {
  @case ('laedt') {
    <p class="hinweis" role="status">Produkte werden geladen …</p>
  }
  @case ('fehler') {
    <div class="hinweis fehler" role="alert">
      <p class="meldung">{{ fehlermeldung() }}</p>
      <button type="button" (click)="laden()">Erneut versuchen</button>
    </div>
  }
  @default {
    @if (produkte().length === 0) {
      <p class="hinweis">Noch keine Produkte im Katalog.</p>
    } @else {
      <table>
        <thead>
          <tr>
            <th scope="col">Titel</th>
            <th scope="col" class="zahl">Listenpreis</th>
            <th scope="col" class="zahl">Lagerbestand</th>
            <th scope="col">Verfügbarkeit</th>
            <th scope="col"><span class="unsichtbar">Warenkorb</span></th>
          </tr>
        </thead>
        <tbody>
          @for (produkt of produkte(); track produkt.id) {
            <tr>
              <td>{{ produkt.titel }}</td>
              <td class="zahl">{{ produkt.listenpreis | geld }}</td>
              <td class="zahl">{{ produkt.lagerbestand }}</td>
              <td>
                @if (produkt.istVerfuegbar) {
                  <span class="marke verfuegbar">verfügbar</span>
                } @else {
                  <span class="marke ausverkauft">nicht verfügbar</span>
… [gekürzt, 186 Zeilen gesamt]
```

**⏺ Bash**

```bash
sed -n 1,70p kunde/kunde-detail/kunde-detail.ts; grep -n 'setInput\|componentRef' kunde/kunde-detail/kunde-detail.spec.ts | head -3; cat app.css | head -40; grep -n 'centInEuro\|euroAusCent' -r gemeinsam | head
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
… [gekürzt, 106 Zeilen gesamt]
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app/api && grep -nE 'public (aendereProdukt|setzeLagerbestand|legeGutscheinAn|listeGutscheine)\(' -A3 api/*.ts | grep -v observe | head -20; sed -n 1,60p model/gutschein-anlegen.ts | grep -vE '^\s*\*|^/\*\*' 
```

```
api/gutscheine.service.ts:147:  public legeGutscheinAn(
api/gutscheine.service.ts-148-    gutscheinAnlegen: GutscheinAnlegen,
api/gutscheine.service.ts-150-    reportProgress?: boolean,
--
api/gutscheine.service.ts:157:  public legeGutscheinAn(
api/gutscheine.service.ts-158-    gutscheinAnlegen: GutscheinAnlegen,
api/gutscheine.service.ts-160-    reportProgress?: boolean,
--
api/gutscheine.service.ts:167:  public legeGutscheinAn(
api/gutscheine.service.ts-168-    gutscheinAnlegen: GutscheinAnlegen,
api/gutscheine.service.ts-170-    reportProgress?: boolean,
--
api/gutscheine.service.ts:177:  public legeGutscheinAn(
api/gutscheine.service.ts-178-    gutscheinAnlegen: GutscheinAnlegen,
api/gutscheine.service.ts-180-    reportProgress: boolean = false,
--
api/gutscheine.service.ts:246:  public listeGutscheine(
api/gutscheine.service.ts-248-    reportProgress?: boolean,
api/gutscheine.service.ts-249-    options?: {
--
import { Geld } from './geld';

export interface GutscheinAnlegen {
  /**
  ablaufdatum: string;
  /**
  code: string;
  /**
  festbetrag?: Geld;
  /**
  prozent?: number;
  rabattArt: GutscheinAnlegen.RabattArtEnum;
}
export namespace GutscheinAnlegen {
  export const RabattArtEnum = {
    Prozent: 'PROZENT',
    Festbetrag: 'FESTBETRAG',
  } as const;
  export type RabattArtEnum = (typeof RabattArtEnum)[keyof typeof RabattArtEnum];
}
```

**⏺ Bash**

```bash
grep -nE 'public (aendereProdukt|setzeLagerbestand)\(' -A2 api/produkte.service.ts | head -6; grep -n 'Gutschein\b\|namespace Gutschein' model/gutschein.ts | head -3
```

```
64:  public aendereProdukt(
65-    produktId: string,
66-    produktAendern: ProduktAendern,
--
75:  public aendereProdukt(
76-    produktId: string,
12:export interface Gutschein {
28:  rabattArt: Gutschein.RabattArtEnum;
30:export namespace Gutschein {
```

### ● Claude

Frontend schreiben: Hilfsfunktion, Produkt-Bearbeiten, Gutscheine, Routen, Navigation, Specs.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && cat >> gemeinsam/euro-eingabe.ts <<'EOF'

/** Cent als Eingabetext im deutschen Format (4999 → "49,99"), zum Vorbelegen von Formularen. */
export function centAlsEuroEingabe(cent: number): string {
  const euro = Math.trunc(cent / 100);
  const rest = String(cent % 100).padStart(2, '0');
  return `${euro},${rest}`;
}
EOF
python3 - <<'EOF'
p='gemeinsam/euro-eingabe.spec.ts'
s=open(p).read()
s=s.replace("from './euro-eingabe';","from './euro-eingabe';",1)
import re
m=re.search(r"import \{([^}]*)\} from './euro-eingabe';",s)
names=[n.strip() for n in m[1].split(',') if n.strip()]
if 'centAlsEuroEingabe' not in names: names.insert(0,'centAlsEuroEingabe')
s=s.replace(m[0],"import { "+", ".join(names)+" } from './euro-eingabe';")
s=s.rstrip()+'''

describe('centAlsEuroEingabe', () => {
  it('formatiert Cent als deutsche Euro-Eingabe', () => {
    expect(centAlsEuroEingabe(4999)).toBe('49,99');
    expect(centAlsEuroEingabe(500)).toBe('5,00');
    expect(centAlsEuroEingabe(7)).toBe('0,07');
    expect(centAlsEuroEingabe(0)).toBe('0,00');
  });

  it('ergibt mit euroInCent wieder denselben Betrag', () => {
    expect(euroInCent(centAlsEuroEingabe(123456))).toBe(123456);
… [gekürzt, 289 Zeilen gesamt]
```

```
import { FormControl } from '@angular/forms';
import { centAlsEuroEingabe, euroBetrag, euroInCent } from './euro-eingabe';

describe('euroInCent', () => {
  it.each([
28:            <th scope="col"><span class="unsichtbar">Pflege</span></th>
63:                  >Bearbeiten</a
app.ts:15:        <a routerLink="/gutscheine" routerLinkActive="aktiv">Gutscheine</a>
app.routes.ts:24:    path: 'gutscheine',
app.routes.ts:26:    loadComponent: () => import('./gutschein/gutscheine/gutscheine').then((m) => m.Gutscheine),
```

**⏺ Bash**

```bash
tail -3 /tmp/build11.log
```

```
2026-11-12T12:54:44.801+01:00  INFO 1879982 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-8 - Shutdown initiated...
2026-11-12T12:54:44.803+01:00  INFO 1879982 --- [bestellsystem] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-8 - Shutdown completed.
EXIT 0
```

### ● Claude

Backend-Build grün. Jetzt Gutscheine-Seite.

**⏺ Bash**

```bash
cat > gutschein/gutscheine/gutscheine.ts <<'EOF'
import { DatePipe, DecimalPipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';

import { Gutschein, GutscheinAnlegen, GutscheineService } from '../../api';
import { euroInCent } from '../../gemeinsam/euro-eingabe';
import { GeldPipe } from '../../gemeinsam/geld.pipe';
import { problemMeldung } from '../../gemeinsam/problem-meldung';

type Zustand = 'laedt' | 'fehler' | 'geladen';
type RabattArt = GutscheinAnlegen.RabattArtEnum;

const RABATTART = GutscheinAnlegen.RabattArtEnum;
const PROZENT_EINGABE = /^\d{1,3}(?:[.,]\d{1,2})?$/;

/** Prüft nur das Eingabeformat; Grenzen wie „höchstens 100 %“ prüft das Backend (D-23). */
function rabattwertFormat(gruppe: AbstractControl): ValidationErrors | null {
  const rabattArt = gruppe.get('rabattArt')!.value as RabattArt;
  const wert = String(gruppe.get('rabattwert')!.value ?? '').trim();
  if (!wert) {
    return null;
  }
  const gueltig =
… [gekürzt, 218 Zeilen gesamt]
```

```
Application bundle generation complete. [3.436 seconds] - 2026-11-12T11:55:32.325Z
```

### ● Claude

Kompiliert. Specs schreiben.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && cat > katalog/produkt-bearbeiten/produkt-bearbeiten.spec.ts <<'EOF'
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { LOCALE_ID } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';

import { Produkt, provideApi } from '../../api';
import { ProduktBearbeiten } from './produkt-bearbeiten';

registerLocaleData(localeDe);

const LAMPE: Produkt = {
  id: 'p1',
  titel: 'Stehlampe',
  listenpreis: { cent: 4999, waehrung: 'EUR' },
  lagerbestand: 3,
  istVerfuegbar: true,
};

describe('ProduktBearbeiten (US-02.2)', () => {
  let fixture: ComponentFixture<ProduktBearbeiten>;
  let http: HttpTestingController;
  let element: HTMLElement;

  function feld(id: string): HTMLInputElement {
    return element.querySelector<HTMLInputElement>(`#${id}`)!;
  }
… [gekürzt, 363 Zeilen gesamt]
```

```
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Produkt, provideApi } from '../../api';
import { Warenkorb } from '../../bestellung/warenkorb';
import { ProduktListe } from './produkt-liste';

const LAMPE: Produkt = {
  id: '1',
  titel: 'Stehlampe',
  listenpreis: { cent: 4999, waehrung: 'EUR' },
  lagerbestand: 3,
  istVerfuegbar: true,
};

describe('ProduktListe', () => {
  let fixture: ComponentFixture<ProduktListe>;
  let http: HttpTestingController;

  const text = () => (fixture.nativeElement as HTMLElement).textContent ?? '';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ProduktListe],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        provideApi(''),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(ProduktListe);
  });

  afterEach(() => http.verify());

  it('zeigt den Ladezustand', async () => {
… [gekürzt, 50 Zeilen gesamt]
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='katalog/produkt-liste/produkt-liste.spec.ts'
s=open(p).read()
i=s.rstrip().rfind('});')
add='''
  it('US-02.2 AK7: verlinkt je Produkt auf die Bearbeitung', async () => {
    http.expectOne('/api/v1/produkte').flush([LAMPE]);
    await fixture.whenStable();

    const link = (fixture.nativeElement as HTMLElement).querySelector<HTMLAnchorElement>(
      'a[aria-label="Stehlampe bearbeiten"]',
    )!;
    expect(link.textContent).toContain('Bearbeiten');
    expect(link.getAttribute('href')).toBe('/produkte/1');
  });
'''
s=s[:i]+add+'});\n'
open(p,'w').write(s)
EOF
cd /home/mschachner/Projects/bestellsystem/frontend && npx prettier --write src/app/katalog src/app/gutschein src/app/gemeinsam src/app/app.ts src/app/app.routes.ts --log-level warn && npm test -- --watch=false 2>&1 | grep -E '✓|✗|×|FAIL|Test Files|Tests |Error|expected' | grep -vE '✓' | head -30
```

```
[31m     [31m×[31m zeigt die Stückzahl im Warenkorb in der Navigation[39m[32m 30[2mms[22m[39m
[31m     [31m×[31m AK6: listet Code, Rabatt, Ablaufdatum und Gültigkeit[39m[32m 50[2mms[22m[39m
[31m⎯⎯⎯⎯⎯⎯⎯[39m[1m[41m Failed Tests 2 [49m[22m[31m⎯⎯⎯⎯⎯⎯⎯[39m
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/app.spec.ts[2m > [22mApp[2m > [22mzeigt die Stückzahl im Warenkorb in der Navigation
[31m[1mAssertionError[22m: expected 'Gutscheine' to contain 'Warenkorb (3)'[39m
[41m[1m FAIL [22m[49m [30m[43m frontend [49m[39m src/app/gutschein/gutscheine/gutscheine.spec.ts[2m > [22mGutscheine (US-08.2)[2m > [22mAK6: listet Code, Rabatt, Ablaufdatum und Gültigkeit
[31m[1mAssertionError[22m: expected 'MINUS5 5,00 € 31.08.2026abgelaufen' to contain '5,00 €'[39m
[2m Test Files [22m [1m[31m2 failed[39m[22m[2m | [22m[1m[32m15 passed[39m[22m[90m (17)[39m
[2m      Tests [22m [1m[31m2 failed[39m[22m[2m | [22m[1m[32m109 passed[39m[22m[90m (111)[39m
```

### ● Claude

Zwei Fehler: App-Spec greift Warenkorb-Link per Position, Geldtext enthält geschütztes Leerzeichen. Ansehen.

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend/src/app && grep -n 'Warenkorb (3)' -B6 app.spec.ts
```

```
33-        istVerfuegbar: true,
34-      },
35-      3,
36-    );
37-    await fixture.whenStable();
38-    const link = (fixture.nativeElement as HTMLElement).querySelectorAll('nav a')[2];
39:    expect(link.textContent).toContain('Warenkorb (3)');
```

**⏺ Bash**

```bash
python3 - <<'EOF'
p='app.spec.ts'
s=open(p).read()
s=s.replace("""    const link = (fixture.nativeElement as HTMLElement).querySelectorAll('nav a')[2];""","""    const link = (fixture.nativeElement as HTMLElement).querySelector('nav a[href="/checkout"]')!;""")
open(p,'w').write(s)
p='gutschein/gutscheine/gutscheine.spec.ts'
s=open(p).read()
s=s.replace("expect(zeilen[1].textContent).toContain('5,00 €');","expect(zeilen[1].textContent).toMatch(/5,00\\s€/);")
open(p,'w').write(s)
EOF
cd .. && cd .. && npx prettier --write src/app/app.spec.ts src/app/gutschein --log-level warn && npm test -- --watch=false 2>&1 | grep -E 'FAIL|Test Files|Tests |AssertionError' | head
```

```
[2m Test Files [22m [1m[32m17 passed[39m[22m[90m (17)[39m
[2m      Tests [22m [1m[32m111 passed[39m[22m[90m (111)[39m
```

### ● Claude

Frontend: 111 Tests grün. Produktionsbuild prüfen, dann Stack für Browser-Check starten.

**⏺ Bash**

```bash
npm run build 2>&1 | grep -E 'ERROR|WARN|Application bundle|budget' | head; cd .. && docker compose up -d 2>&1 | tail -2; docker compose ps --format '{{.Service}} {{.Status}}'
```

```
Application bundle generation complete. [2.983 seconds] - 2026-11-12T11:56:46.645Z
 Container bestellsystem-mailpit-1 Running 
 Container bestellsystem-postgres-1 Running 
mailpit Up 3 hours (healthy)
postgres Up 3 hours (healthy)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && (ss -ltn | grep -E ':8080|:4200') ; ./gradlew bootRun > /tmp/bootrun.log 2>&1
```

```
Command running in background with ID: byz0g9eut. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/byz0g9eut.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend && npm start > /tmp/ngserve.log 2>&1
```

```
Command running in background with ID: bww7hpcor. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/bww7hpcor.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ ToolSearch**

```
{
 "query": "select:Monitor",
 "max_results": 1
}
```

```
[tool_reference]
```

**⏺ Bash**

```bash
tail -5 /tmp/ngserve.log; tail -3 /tmp/bootrun.log
```

```
> frontend@0.0.0 start
> ng serve --proxy-config proxy.conf.json

An unhandled exception occurred: Port 4200 is already in use. Use '--port' to specify a different port.
See "/tmp/ng-YeQZdk/angular-errors.log" for further details.
2026-11-12T12:56:52.461+01:00  INFO 1898586 --- [bestellsystem] [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000489: No JTA platform available (set 'hibernate.transaction.jta.platform' to enable JTA platform integration)
2026-11-12T12:56:52.485+01:00  INFO 1898586 --- [bestellsystem] [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2026-11-12T12:56:52.608+01:00  INFO 1898586 --- [bestellsystem] [           main] o.s.d.j.r.query.QueryEnhancerFactory     : Hibernate is in classpath; If applicable, HQL parser will be used.
```

### ● Claude

Port 4200 schon belegt: laufender Dev-Server (lädt Änderungen selbst neu). Warten bis Backend bereit.

**⏺ Bash**

```bash
for i in $(seq 1 60); do grep -qE 'Started BestellsystemApplication|APPLICATION FAILED|Port 8080 was already' /tmp/bootrun.log && break; sleep 1; done; grep -E 'Started Bestell|FAILED|already in use' /tmp/bootrun.log | head -3; curl -s -o /dev/null -w '%{http_code}\n' localhost:4200/api/v1/gutscheine
```

```
APPLICATION FAILED TO START
Web server failed to start. Port 8080 was already in use.
> Task :bootRun FAILED
404
```

### ● Claude

Alter Backend-Prozess auf 8080 (ohne neue Endpunkte, daher 404). Prüfen, wem er gehört.

**⏺ Bash**

```bash
for p in 8080 4200; do pid=$(ss -ltnp "sport = :$p" | grep -oP 'pid=\K[0-9]+' | head -1); echo "$p pid=$pid"; ps -o pid,ppid,lstart,cmd -p $pid | cut -c1-200; done
```

```
8080 pid=1570313
    PID    PPID                  STARTED CMD
1570313  924471  Do Nov 12 11:58:46 2026 /usr/lib/jvm/java-21-openjdk-amd64/bin/java -XX:TieredStopAtLevel=1 -Dfile.encoding=UTF-8 -Duser.country=US -Duser.language=en -Duser.variant -cp /home/mschach
4200 pid=1170633
    PID    PPID                  STARTED CMD
1170633 1170632  Do Nov 12 10:50:31 2026 ng serve --proxy-config proxy.conf.json (frontend)
```

### ● Claude

Laufende Server stammen nicht aus dieser Sitzung. Nicht beenden; eigene Instanzen auf 8081/4201 starten.

**⏺ Bash**

```bash
cat /home/mschachner/Projects/bestellsystem/frontend/proxy.conf.json; mkdir -p /tmp/bs-check && sed 's/8080/8081/' /home/mschachner/Projects/bestellsystem/frontend/proxy.conf.json > /tmp/bs-check/proxy.json; cat /tmp/bs-check/proxy.json
```

```
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
{
  "/api": {
    "target": "http://localhost:8081",
    "secure": false
  }
}
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/backend && ./gradlew bootRun --args='--server.port=8081' > /tmp/bootrun.log 2>&1
```

```
Command running in background with ID: bj1gy28m1. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/bj1gy28m1.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem/frontend && npx ng serve --port 4201 --proxy-config /tmp/bs-check/proxy.json > /tmp/ngserve.log 2>&1
```

```
Command running in background with ID: bb1nfxgcj. Output is being written to: /tmp/claude-1000/-home-mschachner-Projects-bestellsystem/3764a946-0feb-4646-aea0-ec59345d60bf/tasks/bb1nfxgcj.output. You will be notified when it completes — do not poll that file or sleep while waiting. Read the file only if the user asks for progress, or if you need a line it prints before you can go on, such as a server's ready line or port.
Session cwd remains /home/mschachner/Projects/bestellsystem; directory changes made by the backgrounded command do not apply to subsequent commands.
```

**⏺ Bash**

```bash
for i in $(seq 1 90); do grep -qE 'Started BestellsystemApplication|APPLICATION FAILED' /tmp/bootrun.log && grep -qE 'Local:|error|Error' /tmp/ngserve.log && break; sleep 1; done; grep -E 'Started Bestell|FAILED' /tmp/bootrun.log | head -2; grep -E 'Local:|rror' /tmp/ngserve.log | head -2; curl -s -o /dev/null -w '%{http_code}\n' localhost:4201/api/v1/gutscheine
```

```
2026-11-12T12:57:12.411+01:00  INFO 1902318 --- [bestellsystem] [           main] d.b.BestellsystemApplication             : Started BestellsystemApplication in 4.45 seconds (process running for 4.69)
  ➜  Local:   http://localhost:4201/
200
```

### ● Claude

Beide bereit (4201 → 8081). Browser-Check vorbereiten.

**⏺ Skill**

```
{
 "skill": "anthropic-skills:chrome-browser"
}
```

```
Launching skill: anthropic-skills:chrome-browser
```

**⏺ ToolSearch**

```
{
 "query": "select:mcp__claude-in-chrome__tabs_context_mcp,mcp__claude-in-chrome__navigate,mcp__claude-in-chrome__computer,mcp__claude-in-chrome__read_page,mcp__claude-in-chrome__tabs_create_mcp,mcp__claude-in-chrome__tabs_close_mcp,mcp__claude-in-chrome__find,mcp__claude-in-chrome__form_input,mcp__claude-in-chrome__get_page_text",
 "max_results": 9
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
[tool_reference]
```

**⏺ mcp__claude-in-chrome__tabs_context_mcp**

```
{}
```

```
Browser extension is not connected. Please ensure the Claude browser extension is installed and running (https://claude.ai/chrome), and that you are logged into claude.ai with the same account as Claude Code. If this is your first time connecting to Chrome, you may need to restart Chrome for the installation to take effect. If you continue to experience issues, please report a bug: https://github.com/anthropics/claude-code/issues/new?labels=bug,claude-in-chrome
```

### ● Claude

Chrome-Erweiterung nicht verbunden. Kein Retry. Stattdessen Ablauf über den Dev-Proxy (4201, gleicher Weg wie der Browser) prüfen.

**⏺ Bash**

```bash
B=http://localhost:4201/api/v1; J='Content-Type: application/json'; S=$(date +%H%M%S)
P=$(curl -s -X POST $B/produkte -H "$J" -d '{"titel":"Pruef-Lampe","listenpreis":{"cent":4999,"waehrung":"EUR"},"lagerbestand":2}' | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])')
echo "aendern:"; curl -s -X PUT $B/produkte/$P -H "$J" -d '{"titel":"Pruef-Lampe Nova","listenpreis":{"cent":5999,"waehrung":"EUR"}}'; echo
echo "gutschein:"; curl -s -X POST $B/gutscheine -H "$J" -d "{\"code\":\"test-$S\",\"rabattArt\":\"PROZENT\",\"prozent\":10,\"ablaufdatum\":\"2026-12-31\"}"; echo
echo "gestern:"; curl -s -o /dev/null -w '%{http_code}\n' -X POST $B/gutscheine -H "$J" -d '{"code":"ALT1","rabattArt":"PROZENT","prozent":10,"ablaufdatum":"2020-01-01"}'
K=$(curl -s -X POST $B/kunden -H "$J" -d "{\"vorname\":\"Pruef\",\"nachname\":\"Kunde\",\"email\":\"pruef-$S@example.test\",\"lieferadresse\":{\"strasse\":\"Hauptstr. 1\",\"stadt\":\"Berlin\",\"postleitzahl\":\"10115\",\"land\":\"DE\"},\"istAuchRechnungsadresse\":false}")
KID=$(echo "$K" | python3 -c 'import sys,json;print(json.load(sys.stdin)["id"])'); AID=$(echo "$K" | python3 -c 'import sys,json;print(json.load(sys.stdin)["adressen"][0]["id"])')
echo "checkout:"; curl -s -X POST $B/kunden/$KID/bestellungen -H "$J" -d "{\"positionen\":[{\"produktId\":\"$P\",\"menge\":1}],\"lieferadresseId\":\"$AID\",\"zahlungsart\":\"RECHNUNG\",\"gutscheincode\":\"TEST-$S\"}" | python3 -c 'import sys,json;d=json.load(sys.stdin);print(d.get("gutscheincode"),d["positionen"][0]["einzelpreis"],d["rabattBetrag"],d["gesamtwert"])'
curl -s -o /dev/null -w 'gutscheine-seite %{http_code}\n' localhost:4201/gutscheine; curl -s -o /dev/null -w 'produkt-seite %{http_code}\n' localhost:4201/produkte/$P
```

```
aendern:
{"id":"cef63c17-e8ef-447a-8ae3-c94272eb1963","titel":"Pruef-Lampe Nova","listenpreis":{"cent":5999,"waehrung":"EUR"},"lagerbestand":2,"istVerfuegbar":true}
gutschein:
{"id":"886678cc-8bf6-41ed-ba18-964ed2f4fb13","code":"TEST-125736","rabattArt":"PROZENT","prozent":10,"festbetrag":null,"ablaufdatum":"2026-12-31","istGueltig":true}
gestern:
422
checkout:
TEST-125736 {'cent': 5999, 'waehrung': 'EUR'} {'cent': 600, 'waehrung': 'EUR'} {'cent': 5399, 'waehrung': 'EUR'}
gutscheine-seite 200
produkt-seite 200
```

### ● Claude

Ablauf durchgängig korrekt (10 % auf 59,99 € → 6,00 € Rabatt). Doku nachziehen: Handbuch, ARCHITEKTUR, arc42, README.

**⏺ Bash**

````bash
python3 - <<'EOF'
import re
def edit(p, pairs):
    s=open(p).read()
    for a,b in pairs:
        if a not in s: raise SystemExit(f'NOT FOUND in {p}: {a[:60]}')
        s=s.replace(a,b)
    open(p,'w').write(s)

E='01-entscheidungen.md#d-23-produkt-und-gutschein-pflegen'
edit('docs/benutzerhandbuch.md',[
("| **Katalog** | Produkte mit Preis und Verfügbarkeit, Produkt anlegen, in den Warenkorb legen |",
 "| **Katalog** | Produkte mit Preis und Verfügbarkeit, Produkt anlegen und bearbeiten, in den Warenkorb legen |"),
("| **Kunden** | Kundenliste, Registrierung; je Kunde Adressbuch, Bestellungen, Merkliste |",
 "| **Kunden** | Kundenliste, Registrierung; je Kunde Adressbuch, Bestellungen, Merkliste |\n| **Gutscheine** | Gutscheine mit Rabatt, Ablaufdatum und Gültigkeit; Gutschein anlegen |"),
("""Den Lagerbestand ändert man im MVP nur über die API: `PUT /api/v1/produkte/{id}/lagerbestand` (Swagger UI, siehe [Abschnitt 9](#9-funktionen-ohne-oberfläche)).""",
f"""### Produkt bearbeiten

1. **Katalog** → beim Produkt **Bearbeiten**.
2. Unter **Stammdaten** Titel und Listenpreis ändern, **Stammdaten speichern**.
3. Unter **Lagerbestand** den neuen Bestand eintragen (absolut, z. B. nach Wareneingang), **Lagerbestand speichern**.

Ein neuer Preis gilt nur für künftige Bestellungen; bestehende Bestellungen behalten ihren Einzelpreis. Die Merkliste zeigt sofort den neuen Preis ([D-23]({E})). Hat jemand das Produkt gleichzeitig geändert, erscheint „Versionskonflikt“: Seite neu laden und erneut speichern."""),
("""| Lagerbestand setzen | `PUT /api/v1/produkte/{id}/lagerbestand` | Bestand absolut, ≥ 0 |
""",""),
("""### Gutschein anlegen

Gutscheine haben im MVP weder Oberfläche noch Endpunkt (US-08.2 ist nicht umgesetzt). Zum Testen direkt in der Datenbank anlegen; der Code muss **großgeschrieben** gespeichert werden ([D-17](01-entscheidungen.md#d-17-checkout-ablehnungen-gutscheincode-erste-zahlung)):

```bash
… [gekürzt, 80 Zeilen gesamt]
````

```
25:| 2 | **Nachvollziehbarkeit** | Eingefrorene Preise, Rabatte, Lieferanschrift; abgeleitete Werte nur berechnet ([D-01](01-entscheidungen.md#d-01-gesamtwert-gespeichert-oder-berechnet), [D-08](01-entscheidungen.md#d-08-lieferadress-snapshot)) |
79:| Korrekte Beträge | Wertobjekt `Geld` in Cent; abgeleitete Werte nie speichern (ADR 0006, [D-01](01-entscheidungen.md#d-01-gesamtwert-gespeichert-oder-berechnet)) |
```

### ● Claude

Jetzt ARCHITEKTUR.md und ADR-Index.

**⏺ Bash**

```bash
python3 - <<'EOF'
def edit(p, pairs):
    s=open(p).read()
    for a,b in pairs:
        if a not in s: raise SystemExit(f'NOT FOUND in {p}: {a[:70]}')
        s=s.replace(a,b,1)
    open(p,'w').write(s)
edit('docs/adr/README.md',[("| [D-22](../01-entscheidungen.md#d-22","| [D-23](../01-entscheidungen.md#d-23-produkt-und-gutschein-pflegen) | rein fachlich, im Domänenmodell umgesetzt |\n| [D-22](../01-entscheidungen.md#d-22")])
s=open('docs/adr/README.md').read()
# move D-23 row after D-22 row
lines=s.split('\n'); i=[k for k,l in enumerate(lines) if l.startswith('| [D-23]')][0]; r=lines.pop(i)
j=[k for k,l in enumerate(lines) if l.startswith('| [D-22]')][0]; lines.insert(j+1,r)
open('docs/adr/README.md','w').write('\n'.join(lines))

edit('docs/ARCHITEKTUR.md',[
("Stand: Schritt 9 (Abschlussdokumentation).","Stand: Schritt 11 (Pflege von Produkt und Gutschein: US-02.2, US-08.2, D-23)."),
("`setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |","`aendereStammdaten(titel, listenpreis)` (US-02.2, gleiche Regeln wie beim Anlegen), `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |"),
("| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID),","| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `aendere` (Titel und Preis),"),
("| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`,","| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktAendernDto`,"),
("`normalisiere(code)` (getrimmt, groß),","`legeAn(code, rabattArt, prozent, festbetrag, ablaufdatum, heute)` (US-08.2, D-23: Code `[A-Z0-9-]{1,50}`, Rabattwert passend zur Art, sonst 400; Ablaufdatum vor heute 422), `normalisiere(code)` (getrimmt, groß),"),
("| `gutschein.persistence` | `GutscheinRepository.findByCode` |","| `gutschein.persistence` | `GutscheinRepository`: `findByCode`, `existsByCode`, `findAllByOrderByCodeAsc` |"),
("| `gutschein.application` | `GutscheinService.zumEinloesen(code)` (422 bei unbekanntem Code) |","| `gutschein.application` | `GutscheinService`: `legeAn` (heute aus `Clock`; 409 bei vergebenem Code, vorab per `exists` und über den Unique-Constraint beim `saveAndFlush`), `alle`, `finde` (404), `heute`, `zumEinloesen(code)` (422 bei unbekanntem Code) |\n| `gutschein.api` | `GutscheinController`, DTOs `GutscheinAnlegenDto`, `GutscheinDto` (mit `istGueltig` heute) |"),
("| GET | `/api/v1/produkte/{id}` | Produkt mit `istVerfuegbar`, 404 |","| GET | `/api/v1/produkte/{id}` | Produkt mit `istVerfuegbar`, 404 |\n| PUT | `/api/v1/produkte/{id}` | `{titel, listenpreis}` ersetzen: 200, 400 (D-15), 404, 409 Versionskonflikt (US-02.2, D-23) |"),
("| POST | `/api/v1/kunden` | Registrierung","| POST | `/api/v1/gutscheine` | `{code, rabattArt, prozent \\| festbetrag, ablaufdatum}`: 201 + `Location`, 400, 409 Code vergeben, 422 Ablaufdatum vor heute (US-08.2, D-23) |\n| GET | `/api/v1/gutscheine` | alle Gutscheine nach Code, mit `istGueltig` |\n| GET | `/api/v1/gutscheine/{id}` | ein Gutschein, 404 |\n| POST | `/api/v1/kunden` | Registrierung"),
("| `src/app/katalog/produkt-anlegen` |","| `src/app/katalog/produkt-bearbeiten` | `/produkte/:produktId` (Link „Bearbeiten“ in der Produktliste): Laden, Fehler mit Wiederholen; Formulare „Stammdaten“ (`aendereProdukt`) und „Lagerbestand“ (`setzeLagerbestand`), je Erfolgs- oder ProblemDetail-Meldung (US-02.2) |\n| `src/app/gutschein/gutscheine` | `/gutscheine` (Menüpunkt): Liste mit Rabatt, Ablaufdatum, gültig/abgelaufen; Laden, Fehler, Leerzustand; Formular „Gutschein anlegen“ (Formatprüfung nur der Eingabe, Grenzen prüft das Backend), danach Liste neu laden (US-08.2) |\n| `src/app/katalog/produkt-anlegen` |"),
("`euroInCent` + Validator `euroBetrag` (ohne Gleitkomma),","`euroInCent` + Validator `euroBetrag` (ohne Gleitkomma), `centAlsEuroEingabe` (Vorbelegung),"),
("| Domain-Unit Katalog | JUnit 5, AssertJ | `ProduktTest` (US-02.1 AK1–5, D-15) |","| Domain-Unit Katalog 
… [gekürzt, 32 Zeilen gesamt]
```

```
| [D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf) | [0004](0004-modularer-monolith-nach-fachlichkeit.md), [0005](0005-postgresql-flyway-testcontainers.md), [0007](0007-fehler-als-problemdetail.md), [0008](0008-nebenlaeufigkeit-an-der-aggregatwurzel.md), [0009](0009-zeit-ueber-injizierte-uhr.md), [0011](0011-benachrichtigung-ueber-port.md) |
| [D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen) | [0005](0005-postgresql-flyway-testcontainers.md), [0007](0007-fehler-als-problemdetail.md), [0008](0008-nebenlaeufigkeit-an-der-aggregatwurzel.md), [0009](0009-zeit-ueber-injizierte-uhr.md) |
| [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte) | [0004](0004-modularer-monolith-nach-fachlichkeit.md), [0005](0005-postgresql-flyway-testcontainers.md), [0007](0007-fehler-als-problemdetail.md), [0008](0008-nebenlaeufigkeit-an-der-aggregatwurzel.md), [0009](0009-zeit-ueber-injizierte-uhr.md), [0010](0010-angular-frontend-mit-generiertem-client.md) |
| [D-23](../01-entscheidungen.md#d-23-produkt-und-gutschein-pflegen) | rein fachlich, im Domänenmodell umgesetzt |
```

### ● Claude

Links prüfen, dann committen (Backend-Build war nach letzter Code-Änderung grün, Frontend 111 grün).

**⏺ Bash**

```bash
python3 - <<'EOF'
import re,os,glob
bad=0
for f in glob.glob('docs/**/*.md',recursive=True)+['README.md']:
    for tgt in re.findall(r'\]\(([^)#\s]+)(?:#[^)]*)?\)',open(f).read()):
        if tgt.startswith('http'): continue
        if not os.path.exists(os.path.normpath(os.path.join(os.path.dirname(f),tgt))): print('BROKEN',f,tgt); bad+=1
print('broken',bad)
EOF
cd frontend && git diff --quiet -- src/app/api && echo "api unchanged since generate" ; npm test -- --watch=false 2>&1 | grep -E 'Test Files|Tests ' ; cd .. && git status --short
```

```
broken 0
[2m Test Files [22m [1m[32m17 passed[39m[22m[90m (17)[39m
[2m      Tests [22m [1m[32m111 passed[39m[22m[90m (111)[39m
 M README.md
 M backend/src/main/java/de/bestellsystem/gutschein/application/GutscheinService.java
 M backend/src/main/java/de/bestellsystem/gutschein/domain/Gutschein.java
 M backend/src/main/java/de/bestellsystem/gutschein/persistence/GutscheinRepository.java
 M backend/src/main/java/de/bestellsystem/katalog/api/ProduktController.java
 M backend/src/main/java/de/bestellsystem/katalog/application/ProduktService.java
 M backend/src/main/java/de/bestellsystem/katalog/domain/Produkt.java
 M backend/src/test/java/de/bestellsystem/gutschein/domain/GutscheinTest.java
 M backend/src/test/java/de/bestellsystem/katalog/domain/ProduktTest.java
 M docs/ARCHITEKTUR.md
 M docs/adr/README.md
 M docs/api/openapi.yaml
 M docs/arc42.md
 M docs/benutzerhandbuch.md
 M frontend/openapi.json
 M frontend/src/app/api/.openapi-generator/FILES
 M frontend/src/app/api/api/api.ts
 M frontend/src/app/api/api/produkte.service.ts
 M frontend/src/app/api/model/models.ts
 M frontend/src/app/app.routes.ts
 M frontend/src/app/app.spec.ts
 M frontend/src/app/app.ts
 M frontend/src/app/gemeinsam/euro-eingabe.spec.ts
 M frontend/src/app/gemeinsam/euro-eingabe.ts
 M frontend/src/app/katalog/produkt-liste/produkt-liste.html
 M frontend/src/app/katalog/produkt-liste/produkt-liste.spec.ts
?? backend/src/main/java/de/bestellsystem/gutschein/api/
?? backend/src/main/java/de/bestellsystem/katalog/api/ProduktAendernDto.java
?? backend/src/test/java/de/bestellsystem/gutschein/api/
?? backend/src/test/java/de/bestellsystem/katalog/api/ProduktPflegeApiIT.java
?? frontend/src/app/api/api/gutscheine.service.ts
?? frontend/src/app/api/model/gutschein-anlegen.ts
?? frontend/src/app/api/model/gutschein.ts
?? frontend/src/app/api/model/produkt-aendern.ts
?? frontend/src/app/gutschein/
?? frontend/src/app/katalog/produkt-bearbeiten/
```

### ● Claude

Committen. Browser-Punkt nicht im echten Browser nachgewiesen → Schritt 11 noch nicht abhaken, offenen Punkt in Commit-Body.

**⏺ Bash**

```bash
git add README.md docs/ARCHITEKTUR.md docs/adr/README.md docs/api/openapi.yaml docs/arc42.md docs/benutzerhandbuch.md \
 backend/src/main/java/de/bestellsystem/gutschein backend/src/main/java/de/bestellsystem/katalog backend/src/test/java/de/bestellsystem/gutschein backend/src/test/java/de/bestellsystem/katalog \
 frontend/openapi.json frontend/src/app/api frontend/src/app/app.routes.ts frontend/src/app/app.spec.ts frontend/src/app/app.ts frontend/src/app/gemeinsam frontend/src/app/katalog frontend/src/app/gutschein && git status --short | grep -v '^[AM] ' ; git commit -q -F - <<'EOF'
Add product editing and coupon creation (US-02.2, US-08.2)

Backend: PUT /produkte/{id} for title and list price, POST/GET
/gutscheine with code rules, 409 on duplicate code and 422 for an
expiry date in the past (D-23). Frontend: product edit page with
master data and stock forms, coupons page with list and create form.

Open "Fertig wenn" item: browser walkthrough (edit product, create
coupon, redeem in checkout) not yet confirmed in a real browser; the
same flow passed through the dev proxy via HTTP.

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>
EOF
git log --oneline -1; git status --short
```

```
e2afd2a Add product editing and coupon creation (US-02.2, US-08.2)
```

### ● Claude

Arbeit committet. Prüfen, ob das Repo inzwischen existiert.

**⏺ Bash**

```bash
gh repo view jellybrigade/bestellsystem --json name,visibility,sshUrl 2>&1 | head -3; git -C /home/mschachner/Projects/bestellsystem remote -v
```

```
GraphQL: Could not resolve to a Repository with the name 'jellybrigade/bestellsystem'. (repository)
```

### ● Claude

Schritt 11 ist gebaut, getestet und committet (`e2afd2a`). Ich habe ihn noch nicht abgehakt: Der Durchlauf im echten Browser fehlt. Schritt 10 wartet auf dein Repo.

**Schritt 11: Produkt bearbeiten und Gutschein anlegen**
- **Backend:** `PUT /api/v1/produkte/{id}` ändert Titel und Preis. Neu sind `POST /api/v1/gutscheine` sowie `GET /api/v1/gutscheine` und `GET /api/v1/gutscheine/{id}`. Ein vergebener Code gibt 409, ein Ablaufdatum vor heute 422. Die Regeln stehen in D-23.
- **Frontend:** In der Produktliste führt **Bearbeiten** auf eine Seite mit den Formularen „Stammdaten“ und „Lagerbestand“. Neuer Menüpunkt **Gutscheine** mit Liste und Formular „Gutschein anlegen“.
- **Doku:** D-23, die Stories US-02.2 und US-08.2, Fahrplan, ARCHITEKTUR, arc42, README und Handbuch sind nachgezogen. Der SQL-Umweg für Gutscheine ist aus dem Handbuch raus.

**Tests:** `./gradlew build` grün, darin neu `ProduktPflegeApiIT` (6 Fälle) und `GutscheinApiIT` (9 Fälle) plus Domain-Tests. Frontend: 111 Tests grün, Produktionsbuild ohne Fehler.

**Commits:**
- `1e1de25` Fahrplan-Schritte 10 und 11, D-23 und die zwei Stories
- `e2afd2a` Umsetzung; der offene Browser-Punkt steht im Commit-Text

**Warum nicht abgehakt:** Die Chrome-Erweiterung war nicht verbunden. Den gleichen Ablauf habe ich stattdessen per HTTP über den Dev-Proxy durchgespielt: Produkt ändern, Gutschein anlegen, im Checkout einlösen. Das ergab 10 % auf 59,99 €, also 6,00 € Rabatt. Ein Ablaufdatum in der Vergangenheit gab 422.

Zum Prüfen im Browser laufen meine Instanzen noch:
- **http://localhost:4201** (Backend auf 8081)
- Produkt bearbeiten, Gutschein anlegen, im Checkout einlösen.

Passt das, hake ich Schritt 11 ab.

**Achtung bei deinen eigenen Servern:** Auf 8080 und 4200 laufen Server, die vor dieser Sitzung gestartet wurden. Die habe ich nicht angefasst. Das Backend auf 8080 ist alter Stand, und die neue Seite **Gutscheine** zeigt dort einen Fehler, bis du es neu startest.

**Schritt 10:** `jellybrigade/bestellsystem` gibt es noch nicht. Sobald es angelegt ist, setze ich `origin`, pushe `main` und prüfe den ersten CI-Lauf.

