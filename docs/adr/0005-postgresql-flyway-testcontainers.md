# ADR 0005: PostgreSQL mit Flyway, Tests mit Testcontainers

**Status:** angenommen (2026-11-12)

## Kontext

Mehrere fachliche Regeln stützen sich auf Datenbankeigenschaften: eine Sequenz für die Bestellnummer ([D-14](../01-entscheidungen.md#d-14-zähler-der-bestellnummer)), Zeilensperren im Checkout ([D-09](../01-entscheidungen.md#d-09-lagerbestand-und-verfügbarkeit), ADR 0003) und bei der Mahnprüfung ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)), Unique-Constraints gegen gleichzeitige Duplikate ([D-16](../01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)), `INSERT … ON CONFLICT DO NOTHING` beim Merken ([D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen)). Eine In-Memory-Datenbank wie H2 bildet diese Semantik nicht verlässlich nach.

## Entscheidung

- **PostgreSQL 16** in allen Umgebungen; lokal per `docker-compose.yml`.
- Das Schema entsteht **nur über Flyway** (`backend/src/main/resources/db/migration/V<n>__<beschreibung>.sql`). Hibernate prüft es nur (`ddl-auto=validate`). Bestehende Migrationen werden nie geändert.
- Invarianten werden zusätzlich als `CHECK`-, `UNIQUE`- und Fremdschlüssel-Constraints abgesichert, wo die Datenbank sie ausdrücken kann.
- `spring.jpa.open-in-view=false`; benötigte Graphen werden per `JOIN FETCH` geladen. Die Zahl der SQL-Abfragen wird in Tests geprüft (kein N+1).
- Integrationstests laufen mit **Testcontainers** gegen echtes PostgreSQL (`TestcontainersKonfiguration`, `@ServiceConnection`), auch die Nebenläufigkeitstests.
- Zugangsdaten nur über Umgebungsvariablen; lokal aus `.env` (Vorlage `.env.example`).

## Konsequenzen

- `./gradlew build` braucht einen laufenden Docker-Daemon, lokal wie in CI.
- Tests sehen dieselben Sperren, Constraints und Fehlercodes wie der Betrieb.
- Die Bindung an PostgreSQL ist bewusst; ein Wechsel der Datenbank hätte Arbeit an Migrationen und nativen Abfragen zur Folge.
