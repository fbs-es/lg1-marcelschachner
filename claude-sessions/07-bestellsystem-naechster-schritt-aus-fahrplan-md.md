## Session 7: /naechster-schritt aus fahrplan.md

*Projekt: bestellsystem · ID `6b1d85f1-85b1-47f8-8e18-b97d9de00df8` · 2026-11-12 10:19 – 2026-11-12 10:19 UTC*

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
ls stories epics && git status --short
```

```
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
?? backend/bin/
```

### ● Claude

Schritt 5 ist **Zahlungen (E05)**. Schritte 0 bis 4b sind abgehakt, die Story `stories/US-05.1-zahlungsstatus-pruefen.md` liegt vor.

Als Werkzeug ist **goal** vorgesehen. Den Befehl startest du selbst:

```
/goal Alle Akzeptanzkriterien aus stories/US-05.1-zahlungsstatus-pruefen.md sind als Tests
umgesetzt und grün, dazu Endpunkte für Zahlungsstatus-Änderung und Erstattung, ./gradlew build grün,
docs/ARCHITEKTUR.md aktualisiert, Schritt 5 in docs/fahrplan.md abgehakt.
```

Zwei Punkte vorher:

- **Lücke im goal:** Der Fertig-wenn-Punkt „Bezahlstatus und offener Betrag in der Bestellansicht sichtbar“ ist Frontend-Arbeit, das goal deckt nur das Backend ab. Das Frontend braucht danach eine eigene Runde mit feature-dev, oder du ergänzt das goal um „Frontend: Bezahlstatus und offener Betrag in Bestellansicht, `npm test` grün“. Sonst darf der Schritt nicht abgehakt werden, obwohl das goal das Abhaken verlangt.
- **`backend/bin/` ist untracked:** Vermutlich Build-Ausgabe der IDE. Gehört in `.gitignore`, nicht ins Repo.

Diese Sitzung hat nichts geändert, deshalb gibt es auch keinen Commit.

