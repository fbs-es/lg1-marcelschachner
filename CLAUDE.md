# Bestellsystem

> Wird in jede Sitzung geladen. Eine Zeile pro Regel. Bei Widerspruch zwischen Regel und Auftrag: nachfragen, nicht raten.

## Überblick

Full-Stack-Bestellsystem für einen Onlineshop: Kunden, Adressbuch, Katalog, Merkliste, Bestellung, Zahlung, Mahnwesen, Versand, Gutscheine.
Spring Boot (REST, JPA/Hibernate) + PostgreSQL + Angular.

## Quellen der Wahrheit

Rangfolge bei Widerspruch: `docs/01-entscheidungen.md` > `stories/` > `epics/` > `docs/02-domaenenmodell.md` > `docs/00-anforderung.md`.

- Fachliche Lücke oder Widerspruch entdeckt → stoppen, nachfragen, dann zuerst eine neue Entscheidung `D-xx` in `docs/01-entscheidungen.md`, danach Code.
- Nie stillschweigend eine fachliche Regel erfinden.
- Akzeptanzkriterien einer Story sind verbindlich und werden 1:1 zu Tests.

## Arbeitsweise

- Fahrplan: `docs/fahrplan.md`. Ein Schritt pro Sitzung, in der angegebenen Reihenfolge.
- "nächster Schritt" = `/naechster-schritt`: obersten offenen Schritt des Fahrplans abarbeiten.
- Nur den Umfang des aktuellen Schritts bauen. Was später kommt, wird nicht vorgebaut.
- Schritt fertig = Tests grün, `docs/ARCHITEKTUR.md` aktuell, Fahrplan abgehakt, committet.

## Stack

- Backend: Java 21, Spring Boot 3.x, Gradle 9 mit Kotlin DSL (Wrapper `./gradlew`), Spring Data JPA / Hibernate, Flyway, springdoc-openapi, Bean Validation.
- Datenbank: PostgreSQL 16. Lokal via Docker Compose; Tests via Testcontainers (kein H2).
- Tests Backend: JUnit 5, AssertJ, Spring Boot Test, Testcontainers, MockMvc.
- Frontend: Angular (aktuelle LTS), Standalone Components, Signals, Reactive Forms, Angular Router.
- API-Client: aus OpenAPI generiert (`openapi-generator`, `typescript-angular`).
- Lokale Infrastruktur: `docker-compose.yml` mit PostgreSQL und Mailpit (E-Mail-Test).

## Befehle

```bash
docker compose up -d                         # PostgreSQL + Mailpit
cd backend && ./gradlew build                # Build + alle Tests + Spotless-Check
cd backend && ./gradlew bootRun              # API auf :8080, Swagger UI unter /swagger-ui.html
cd frontend && npm ci && npm start           # Angular auf :4200, Proxy auf :8080
cd frontend && npm test                      # Frontend-Tests
cd backend && ./gradlew aktualisiereOpenApi  # frontend/openapi.json + docs/api/openapi.yaml exportieren (ADR 0002)
cd frontend && npm run generate:api          # Client aus frontend/openapi.json neu erzeugen
```

Befehle entstehen in Schritt 1 und 2; bis dahin existieren sie noch nicht.

- Lokal vorhanden: Java 21, Node 22, Docker 29 mit Compose. Angular CLI ist nicht global installiert: immer `npx @angular/cli` bzw. die Projekt-Skripte nutzen.
- Backend immer über den Gradle-Wrapper `./gradlew`, nie das System-`gradle` (Version 4.4 ist zu alt).
- Formatierung: `./gradlew spotlessApply` (google-java-format, AOSP); `build` prüft sie.

## Struktur

```
backend/   Spring Boot
frontend/  Angular
docs/      Anforderung, Entscheidungen, Domänenmodell, Architektur, Fahrplan
epics/     Epics
stories/   User Stories mit Akzeptanzkriterien
```

- Gradle: `group` `de.bestellsystem`, `rootProject.name` `bestellsystem-backend`; Basispaket `de.bestellsystem`.
- Backend nach Fachlichkeit geschnitten: `de.bestellsystem.{kunde, katalog, merkliste, bestellung, zahlung, gutschein, mahnwesen, versand, gemeinsam}`.
- Je Fachpaket: `domain` (Entities, Wertobjekte, Fachlogik), `application` (Services, Transaktionen), `api` (Controller, DTOs), `persistence` (Repositories).
- Zusätzlich `infrastruktur` für Adapter zu externen Systemen (z. B. Mail) und technische Konfiguration (Uhr, OpenAPI); implementiert Ports aus `application`.
- `gemeinsam`: `Geld`, `Anschrift`, Fehlerbehandlung, Uhr, Benachrichtigungs-Port, Bestellnummern-Generator.
- Abhängigkeiten nur in Richtung `api` → `application` → `domain`; `domain` kennt kein Spring außer JPA-Annotationen.
- Frontend spiegelt die Fachpakete: `src/app/<fachpaket>/`; `src/app/api/` ist generiert.

## Benennung

- Fachbegriffe deutsch wie im Domänenmodell (`Kunde`, `Bestellung`, `istVollstaendigBezahlt`), Technik englisch (`Repository`, `Controller`, `Service`, `Dto`).
- Keine Umlaute in Bezeichnern (`ae`, `oe`, `ue`, `ss`).
- Booleans als Aussage (`istVerfuegbar`, `istUeberfaellig`). Keine Abkürzungen.

## Domänenregeln

- Fachlogik gehört in Domain-Objekte (reichhaltiges Modell), Services orchestrieren nur.
- Abgeleitete Werte (`gesamtwert`, `bezahlteSumme`, `offenerBetrag`) werden berechnet, nie gespeichert (D-01).
- Geld nur als Wertobjekt `Geld` (Cent als `long`, EUR); nie `double`/`float` (D-11).
- Eingefrorene Werte (`einzelpreis`, `rabattBetrag`, `lieferanschrift`) haben keine Setter (I-8).
- Invarianten werden im Domain-Objekt durchgesetzt, nicht nur im Controller.
- Zeit nur über injizierte `java.time.Clock`; nie `LocalDate.now()` ohne Clock.

## JPA / Hibernate

- `spring.jpa.open-in-view=false`.
- Schema ausschließlich über Flyway (`ddl-auto=validate`); jede Änderung = neue Migration `V<n>__<beschreibung>.sql`, nie eine bestehende ändern.
- `Geld` und `Anschrift` als `@Embeddable`.
- `Merklisteneintrag` als eigene Entity mit `@EmbeddedId` (Kunde, Produkt), kein `@ManyToMany`.
- Assoziationen standardmäßig `LAZY`; benötigte Graphen per `JOIN FETCH` oder `@EntityGraph`. N+1 vermeiden.
- Bidirektional nur, wo die Fachlogik es braucht (z. B. Bestellung ↔ Positionen, Zahlungen).
- `Produkt` hat `@Version` (optimistisches Sperren des Lagerbestands, D-09).
- Kompositionen (Kunde → Adressen, Bestellung → Positionen/Zahlungen/Pakete) mit `cascade = ALL, orphanRemoval = true`.
- `equals`/`hashCode` nicht über generierte IDs auf Basis veränderlicher Felder; Lombok `@Data` auf Entities verboten.

## REST-API

- Basis `/api/v1`, Ressourcen im Plural (`/kunden`, `/produkte`, `/bestellungen`).
- Kein Login im MVP: kundenbezogene Pfade tragen die Kunden-ID (`/api/v1/kunden/{kundenId}/merkliste`).
- Nie Entities ausliefern oder annehmen; nur DTOs als Java `record`.
- Eingaben per Bean Validation (`@Valid`).
- Fehler als `ProblemDetail` (RFC 9457) über einen zentralen `@RestControllerAdvice`.
- Statuscodes: 201 + `Location` beim Anlegen, 400 ungültige Eingabe, 404 nicht gefunden, 409 Konflikt (E-Mail vergeben, Bestand reicht nicht, Versionskonflikt), 422 fachliche Ablehnung (Gutschein abgelaufen, letzte Lieferadresse).
- Geldbeträge in der API als Cent (`long`) plus Währung.
- Jeder Endpunkt hat OpenAPI-Beschreibung; die OpenAPI ist die API-Doku.

## Frontend

- API-Aufrufe nur über den generierten Client; generierten Code nie von Hand ändern.
- Keine Fachlogik duplizieren: Gesamtwert, Rabatt, Verfügbarkeit kommen vom Backend.
- Beträge mit `CurrencyPipe` in `de-DE` aus Cent anzeigen.
- Jeder Datenabruf zeigt Laden, Fehler und Leerzustand.
- Fehler aus `ProblemDetail` verständlich anzeigen.

## Tests (TDD)

- Test zuerst; ein Bugfix-Test muss am ungefixten Code fehlschlagen.
- Jedes Akzeptanzkriterium = mindestens ein Test, benannt nach Story: `@DisplayName("US-04.1 AK3: nicht genug Bestand lehnt ganze Bestellung ab")`.
- Domain-Logik: reine Unit-Tests ohne Spring.
- Persistenz und Transaktionen: Integrationstests mit Testcontainers.
- Controller: MockMvc gegen echten Service, Fehlerformat und Statuscodes prüfen.
- Mocks nur an Grenzen (E-Mail-Versand, Uhr, externe Dienste), nie Domain-Objekte.
- Nebenläufigkeit beim Lagerbestand wird getestet (zwei Checkouts, eine Lampe).
- Keine `@Disabled`-Tests, kein auskommentierter Code.

## Dokumentation

- `docs/ARCHITEKTUR.md` aktuell halten (Pakete, Datenfluss, Tabellen, Migrationen), im selben Commit wie die Änderung.
- Neue fachliche Entscheidung → `D-xx` in `docs/01-entscheidungen.md`, im selben Commit.
- Neue Technikentscheidung → ADR in `docs/adr/NNNN-titel.md`.
- Kommentare nur für ein nicht offensichtliches Warum.

## Git

- Ein Commit pro abgeschlossener logischer Einheit; nie rot committen.
- Betreff im Imperativ, ohne Punkt, ≤ 72 Zeichen.
- Keine Geheimnisse im Repo; Zugangsdaten über Umgebungsvariablen, `.env` steht in `.gitignore`.

## Nicht im Umfang (MVP)

Login/Spring Security, Storno, Versandkosten, Steuern, Rechnungsadresse in der Bestellung, Mahnstufen und Gebühren, Gutschein-Nutzungslimits, echte Zahlungsanbieter, Tracking-APIs.
