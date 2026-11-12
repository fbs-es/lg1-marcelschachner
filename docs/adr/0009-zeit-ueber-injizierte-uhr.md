# ADR 0009: Zeit nur über eine injizierte Uhr

**Status:** angenommen (2026-11-12)

## Kontext

Viele Regeln hängen vom Datum ab: Bestelldatum und Fälligkeit ([D-03](../01-entscheidungen.md#d-03-zahlungsrückstand-braucht-eine-fälligkeit)), Ablaufdatum des Gutscheins ([D-04](../01-entscheidungen.md#d-04-gutschein-art-des-rabatts-und-wirkung)), Mahnintervall ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)), Merkdatum ([D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen)), Zeitpunkte von Zahlungen und Paketen ([D-19](../01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)). Tests müssen diese Regeln an festen Stichtagen prüfen, etwa „Gutschein gilt am Ablaufdatum noch“.

## Entscheidung

- Einzige Zeitquelle ist eine `java.time.Clock`-Bean (`UhrKonfiguration`) in der Zeitzone `bestellsystem.zeitzone` (Standard `Europe/Berlin`).
- Kein `LocalDate.now()`, `Instant.now()` o. ä. ohne `Clock`. Domain-Methoden bekommen Stichtag oder Zeitpunkt als Parameter; Services lesen ihn aus der Uhr.
- Zeitpunkte werden als `Instant`/`TIMESTAMPTZ` gespeichert und für die Anzeige in `bestellsystem.zeitzone` umgerechnet ([D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen)).
- Tests ersetzen die Uhr: `FesteUhrKonfiguration` (12.11.2026 wie in den Stories) oder `VerstellbareUhrKonfiguration` (je Test verstellbar).

## Konsequenzen

- Zeitabhängige Regeln sind deterministisch testbar, auch über Tagesgrenzen.
- Der Mahnlauf ist an jedem Stichtag reproduzierbar.
