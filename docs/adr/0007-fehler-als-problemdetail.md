# ADR 0007: Fehler als ProblemDetail mit fachlichen Ausnahmen

**Status:** angenommen (2026-11-12)

## Kontext

Das Frontend muss Ablehnungen verständlich anzeigen (Gutschein abgelaufen, Bestand reicht nicht, letzte Lieferadresse). Die Entscheidungen legen je Fall einen Statuscode fest ([D-16](../01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit), [D-17](../01-entscheidungen.md#d-17-checkout-ablehnungen-gutscheincode-erste-zahlung), [D-19](../01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen), [D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf), [D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)). Die Domain soll dafür kein HTTP kennen.

## Entscheidung

- Fehlerantworten folgen **RFC 9457** (`application/problem+json`, Springs `ProblemDetail`).
- Die Domain wirft drei fachliche Ausnahmen aus `gemeinsam.domain`:
  - `NichtGefundenException` → 404
  - `KonfliktException` → 409 (E-Mail vergeben, Bestand reicht nicht, Sendungsnummer vergeben)
  - `FachlicheAblehnungException` → 422 (Regel verletzt, z. B. letzte Lieferadresse, Gutschein abgelaufen, unerlaubter Statusübergang)
- Eine zentrale `@RestControllerAdvice` (`ZentraleFehlerbehandlung`) übersetzt außerdem:
  - Bean Validation → 400 mit `fehler: [{feld, meldung}]`
  - `IllegalArgumentException` aus Wertobjekten → 400
  - `ObjectOptimisticLockingFailureException` → 409 „Versionskonflikt“ (ADR 0008)
  - `BenachrichtigungNichtVersendetException` → 503
  - alles andere → 500 ohne interne Details, geloggt
- Validierungsmeldungen sind immer deutsch (`spring.web.locale=de`, fester `LocaleResolver`).
- Das Frontend wandelt jede Fehlerantwort mit `problemMeldung` in einen Text um.

## Konsequenzen

- Statuscodes stehen an einer Stelle und sind über MockMvc-Tests abgesichert.
- Die Domain bleibt frei von Spring-Web; der Controller kennt keine Fehlerfälle.
