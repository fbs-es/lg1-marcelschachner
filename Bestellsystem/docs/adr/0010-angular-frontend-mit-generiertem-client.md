# ADR 0010: Angular-Frontend ohne eigene Fachlogik

**Status:** angenommen (2026-11-12)

## Kontext

Gesamtwert, Rabatt, Bezahlstatus, Verfügbarkeit und erlaubte Lieferstatus sind Fachregeln des Backends ([D-01](../01-entscheidungen.md#d-01-gesamtwert-gespeichert-oder-berechnet), [D-05](../01-entscheidungen.md#d-05-erstattungen-und-transaktionsstatus), [D-09](../01-entscheidungen.md#d-09-lagerbestand-und-verfügbarkeit), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)). Rechnet das Frontend sie nach, laufen zwei Implementierungen auseinander.

## Entscheidung

- Frontend mit **Angular 21**: Standalone Components, Signals, Reactive Forms, Router, zoneless. Tests mit Vitest und jsdom.
- Ordner spiegeln die Fachpakete (`src/app/katalog`, `kunde`, `bestellung`, `merkliste`, `versand`, `gemeinsam`).
- Backend-Aufrufe **nur** über den aus der OpenAPI generierten Client in `src/app/api/` (ADR 0002); generierter Code wird nie von Hand geändert.
- **Keine Fachlogik im Frontend.** Werte wie Gesamtwert, offener Betrag, `istVerfuegbar` und `erlaubteFolgestatus` kommen vom Backend. Der Warenkorb zeigt deshalb keine Summen ([D-18](../01-entscheidungen.md#d-18-checkout-im-frontend-und-bestellhistorie)).
- Der Warenkorb lebt nur im Browser-Speicher ([D-18](../01-entscheidungen.md#d-18-checkout-im-frontend-und-bestellhistorie)).
- Jeder Datenabruf zeigt Laden, Fehler (mit „Erneut versuchen“) und Leerzustand; Fehler aus `ProblemDetail` werden als Text angezeigt (ADR 0007).
- Beträge kommen als Cent und werden mit der `CurrencyPipe` in `de-DE` angezeigt (ADR 0006).
- Kein Login im MVP: Kunden werden aus einer Liste gewählt, kundenbezogene Pfade tragen die Kunden-ID.

## Konsequenzen

- Regeländerungen im Backend wirken ohne Frontend-Änderung.
- Eine Vorschau des Gesamtwerts vor dem Bestellen bräuchte einen eigenen Backend-Endpunkt.
