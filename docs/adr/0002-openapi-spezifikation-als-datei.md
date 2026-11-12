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
