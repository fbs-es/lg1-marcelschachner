# Test-Claude-Radiobeitrag – Web-Applikation

Web-Oberfläche und REST-API für die Sendeplanung aus `Test-Claude-Radiobeitrag`.

- **Backend:** Spring Boot 4.1 (Java 21), Spring Data JPA, Flyway, MariaDB, springdoc-openapi
- **Frontend:** Angular 21 (Standalone Components, Signals, Vitest)
- **Domäne:** `Test-Claude-Radiobeitrag` wird über `implementation project(':Test-Claude-Radiobeitrag')` eingebunden. Alle Business-Regeln laufen dort.

## Voraussetzungen

- Java 21 (Gradle-Wrapper im Root)
- Docker für MariaDB
- Node.js ≥ 22.12 (Angular 21). Für Angular 22 wäre Node ≥ 22.22.3 nötig.

## 1. Datenbank starten

Im Root des Repositories:

```bash
docker compose up -d mariadb
```

MariaDB 11 läuft auf `localhost:3307`, Datenbank, User und Passwort `lg1` / `lg1` / `lg1`.
Flyway legt die Tabellen beim ersten Backend-Start an (`db/migration/V1__init.sql`).
Bestehende fremde Tabellen in `lg1` bleiben unberührt (`baseline-on-migrate`).

## 2. Backend starten

Im Root des Repositories:

```bash
./gradlew :Test-Claude-RadiobeitragWeb:backend:bootRun
```

- REST-API: <http://localhost:8080/api>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI-JSON: <http://localhost:8080/v3/api-docs>

## 3. Frontend starten

```bash
cd Test-Claude-RadiobeitragWeb/frontend
npm install
npm start
```

- App: <http://localhost:4200>
- Der Dev-Proxy (`proxy.conf.json`) leitet `/api` an `http://localhost:8080` weiter.

## Tests

```bash
./gradlew checkAllTasks                       # alle Gradle-Tests inkl. Backend (H2, keine DB nötig)
./gradlew :Test-Claude-RadiobeitragWeb:backend:test        # nur Backend
cd Test-Claude-RadiobeitragWeb/frontend && npx ng test --watch=false   # Frontend (Vitest)
```

## REST-Endpunkte

| Methode | Pfad | Zweck |
|---|---|---|
| POST | `/api/sender` | Sender mit Programm anlegen |
| GET | `/api/sender`, `/api/sender/{id}` | Sender lesen |
| GET | `/api/programme/{id}` | Programm mit Sendungen |
| GET / POST | `/api/programme/{id}/sendungen` | Sendungen auflisten / anlegen (Überschneidung → 409) |
| GET | `/api/sendungen/{id}` | Sendeplan mit Startzeitpunkten, Gesamtdauer, Restzeit, `zeitlimitUeberschritten` |
| POST | `/api/sendungen/{id}/beitraege` | Beitrag anhängen, `"typ": "MUSIK" \| "WERBUNG" \| "MODERATION" \| "EILMELDUNG"` |
| POST | `/api/sendungen/{id}/beitraege/{beitragId}/kuerzen` | `{ "neueDauer": 180 }` |
| POST | `/api/sendungen/{id}/beitraege/{beitragId}/verschieben` | `{ "neuerIndex": 0 }` |
| DELETE | `/api/sendungen/{id}/beitraege/{beitragId}` | Beitrag löschen |
| POST | `/api/sendungen/{id}/eilmeldung` | Eilmeldung an `laufzeit` einfügen, Antwort mit `warnung` |

Fehler: `IllegalArgumentException` → 400, `IllegalStateException` → 409, nicht gefunden → 404.
Der Body ist immer `{ "status": ..., "message": "..." }` mit der deutschen Meldung aus der Domäne.

## Architektur

- **Entities** (`entity/`): `RadiosenderEntity`, `RadioprogrammEntity`, `RadiosendungEntity`, `RadiobeitragEntity` mit Unterklassen.
  Vererbung mit `SINGLE_TABLE`: wenige Zusatzfelder pro Typ, der Sendeplan wird immer komplett und polymorph geladen, eine Abfrage ohne Joins.
  Die Reihenfolge steht in `position_nr` (`@OrderColumn`). Der Startzeitpunkt wird nicht gespeichert, die Domäne berechnet ihn.
- **Mapper** (`DomainMapper`): baut Domänenobjekte aus Entities auf und schreibt Änderungen zurück.
  Über eine `IdentityHashMap` werden bekannte Beiträge aktualisiert und neue (z. B. Fortsetzungen nach einem Split) als neue Entities angelegt.
- **Services**: Entity laden → Domäne aufbauen → Domänenmethode aufrufen → zurückschreiben → speichern.
- **Limit-Fallstrick**: Ein gespeicherter Sendeplan darf nach einer Eilmeldung über der Maximaldauer liegen.
  Der Wiederaufbau nutzt deshalb `Radiosendung.beitragWiederherstellen()` (ohne Limitprüfung) statt `beitragHinzufuegen()`.
