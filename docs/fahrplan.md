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
- `docs/ARCHITEKTUR.md` angelegt
- CI-Workflow führt `./gradlew build` aus

---

## [x] 2 Fundament Frontend + Katalog (E02) (2026-11-12, e1d663f)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E02-*.md`, `stories/US-02.1-*.md`

```
/feature-dev Setze Schritt 2 aus docs/fahrplan.md um: Produktkatalog (E02) als erster Durchstich
durch alle Schichten. Backend: Produkt-Entity, Migration, Repository, Service, REST-Endpunkte,
US-02.1 vollständig. Frontend: Angular-Gerüst unter frontend/ nach CLAUDE.md, Routing, Layout,
generierter API-Client, Produktliste mit Verfügbarkeit, Produkt anlegen.
```

**Fertig wenn:**
- Alle Akzeptanzkriterien US-02.1 als Tests grün
- Produkt im Browser anlegen und in der Liste sehen
- `npm run generate:api` erzeugt den Client reproduzierbar
- Frontend-Tests grün, CI baut beide Teile

---

## [x] 3 Kundenkonto & Adressbuch (E01) (2026-11-12, b969a0d)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E01-*.md`, `stories/US-01.1-*.md`, D-06, D-13

```
/feature-dev Setze Schritt 3 aus docs/fahrplan.md um: Kundenkonto und Adressbuch (E01),
Backend und Frontend. US-01.1 vollständig, dazu die Kandidaten US-01.2 und US-01.3
(vorher Akzeptanzkriterien mit mir klären und in stories/ ablegen).
```

**Fertig wenn:**
- US-01.1 bis US-01.3 als Tests grün
- Letzte Lieferadresse nicht löschbar (422)
- Registrierung und Adressbuch im Browser bedienbar

---

## [x] 4a Checkout Backend (E04 + E08) (2026-11-12, 9cfb2a2)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E04-*.md`, `epics/E08-*.md`, `stories/US-04.1-*.md`, `stories/US-08.1-*.md`, D-01 bis D-04, D-08 bis D-11

```
/feature-dev Setze Schritt 4a aus docs/fahrplan.md um: Checkout im Backend (E04 + E08).
Bestellung, Positionen, Lieferadress-Snapshot, Bestandsreduktion in einer Transaktion,
Gutschein mit eingefrorenem Rabatt, erste Zahlung AUSSTEHEND, Fälligkeit.
US-04.1 und US-08.1 vollständig. Kein Frontend.
```

**Fertig wenn:**
- Alle Akzeptanzkriterien US-04.1 und US-08.1 als Tests grün
- Nebenläufigkeitstest: zwei Checkouts auf die letzte Lampe, genau einer erfolgreich
- Bestellabruf ohne N+1 (Anzahl SQL-Abfragen im Test geprüft)

---

## [x] 4b Checkout Frontend (2026-11-12, 70eb08e)

**Werkzeug:** feature-dev
**Eingaben:** Ergebnis 4a, `stories/US-04.1-*.md`, `stories/US-08.1-*.md`

```
/feature-dev Setze Schritt 4b aus docs/fahrplan.md um: Checkout im Frontend.
Warenkorb (nur clientseitig), Lieferadresse wählen, Zahlungsart, Gutscheinfeld mit
Fehleranzeige, Bestellbestätigung, Bestellhistorie (US-04.2, Kriterien vorher mit mir klären).
```

**Fertig wenn:**
- Bestellung im Browser von Produktliste bis Bestätigung durchspielbar
- Abgelaufener Gutschein und fehlender Bestand werden verständlich angezeigt
- Frontend-Tests grün

---

## [x] 5 Zahlungen (E05) (2026-11-12, 826fd9a)

**Werkzeug:** goal (Logik) + feature-dev (Endpunkte, falls Design offen)
**Eingaben:** `epics/E05-*.md`, `stories/US-05.1-*.md`, D-05

```
/goal Alle Akzeptanzkriterien aus stories/US-05.1-zahlungsstatus-pruefen.md sind als Tests
umgesetzt und grün, dazu Endpunkte für Zahlungsstatus-Änderung und Erstattung, ./gradlew build grün,
docs/ARCHITEKTUR.md aktualisiert, Schritt 5 in docs/fahrplan.md abgehakt.
```

**Fertig wenn:**
- US-05.1 als Tests grün
- Bezahlstatus und offener Betrag in der Bestellansicht sichtbar

---

## [x] 6 Mahnwesen (E06) (2026-11-12, 874b481)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E06-*.md`, `stories/US-06.1-*.md`, D-03

```
/feature-dev Setze Schritt 6 aus docs/fahrplan.md um: Mahnwesen (E06).
Kunde.fuehreMahnpruefungDurch, Mahnung-Entity, täglicher @Scheduled-Lauf (US-06.2,
Kriterien vorher mit mir klären), E-Mail über den Benachrichtigungs-Port.
```

**Fertig wenn:**
- Alle Akzeptanzkriterien US-06.1 als Tests grün (feste Uhr)
- Mahn-E-Mail lokal in Mailpit sichtbar
- Lauf ist idempotent bei Wiederholung am selben Tag

---

## [ ] 7 Merkliste (E03)

**Werkzeug:** goal oder feature-dev
**Eingaben:** `epics/E03-*.md`, `stories/US-03.1-*.md`, D-07

```
/feature-dev Setze Schritt 7 aus docs/fahrplan.md um: Merkliste (E03), Backend und Frontend.
US-03.1 und US-03.2 (Kriterien vorher mit mir klären).
```

**Fertig wenn:**
- US-03.1 und US-03.2 als Tests grün
- Merken und Entfernen im Browser, Verfügbarkeit sichtbar

---

## [ ] 8 Versand (E07)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E07-*.md`, D-12

```
/feature-dev Setze Schritt 8 aus docs/fahrplan.md um: Versand (E07), Backend und Frontend.
Zuerst US-07.1 bis US-07.3 mit mir ausformulieren und in stories/ ablegen, dann umsetzen.
```

**Fertig wenn:**
- US-07.1 bis US-07.3 als Tests grün
- Ungültige Statusübergänge werden abgelehnt (422)

---

## [ ] 9 Dokumentation

**Werkzeug:** normaler Auftrag (kein feature-dev)
**Eingaben:** gesamter Code, `docs/`

```
Erstelle die Abschlussdokumentation nach Schritt 9 in docs/fahrplan.md.
```

**Fertig wenn:**
- `README.md`: Zweck, Voraussetzungen, Start mit Docker Compose, Tests ausführen
- `docs/arc42.md`: Kontext, Bausteinsicht, Laufzeitsicht (Checkout, Mahnlauf), Verteilungssicht, Querschnitt (Fehler, Transaktionen, Geld), Risiken
- `docs/adr/` vollständig, Entscheidungen D-xx verlinkt
- `docs/benutzerhandbuch.md` mit den wichtigsten Abläufen
- OpenAPI-Spezifikation exportiert unter `docs/api/openapi.yaml`
