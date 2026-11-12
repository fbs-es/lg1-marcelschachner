# ADR 0008: Nebenläufigkeit über die Version der Aggregatwurzel

**Status:** angenommen (2026-11-12)

## Kontext

Mehrere Invarianten betreffen ein ganzes Aggregat und können durch zwei gleichzeitige Änderungen an verschiedenen Teilen verletzt werden:

- Zwei Anfragen löschen gleichzeitig je eine von zwei Lieferadressen; danach hätte der Kunde keine (I-1, [D-16](../01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit)).
- Zwei Erstattungen über je 60 € auf 100 € bezahlt; zusammen über dem erstattbaren Betrag ([D-19](../01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen)).
- Zwei Mahnprüfungen desselben Kunden mahnen dieselbe Bestellung doppelt ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)).

Eine Änderung an einer `Adresse` oder `Zahlung` erhöht die Version der Wurzel nicht von selbst.

## Entscheidung

- `Kunde`, `Bestellung` und `Produkt` tragen `@Version`.
- Jede Änderung an Teilen des Aggregats sperrt die Wurzel mit **`OPTIMISTIC_FORCE_INCREMENT`**: Adressbuch ([D-16](../01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit)), Zahlungsstatus und Erstattung ([D-19](../01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen)), Versandpakete ([D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)). Die zweite gleichzeitige Änderung scheitert beim Commit mit **409 Versionskonflikt** (ADR 0007).
- Ausnahmen, in denen Warten besser ist als Scheitern, sperren **pessimistisch**:
  - Checkout sperrt Produkte (ADR 0003, [D-09](../01-entscheidungen.md#d-09-lagerbestand-und-verfügbarkeit)).
  - Mahnprüfung sperrt den Kunden (`SELECT … FOR UPDATE`), damit gleichzeitige Prüfungen nacheinander laufen und die zweite die Mahnungen der ersten sieht ([D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)).
- Doppelte Werte über Aggregate hinweg (E-Mail, Sendungsnummer, Merklisteneintrag) sichern Unique-Constraints ab; die Verletzung wird in 409 übersetzt bzw. beim Merken per `ON CONFLICT DO NOTHING` geschluckt ([D-16](../01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit), [D-21](../01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)).
- Jeder dieser Fälle hat einen Nebenläufigkeitstest mit zwei echten Transaktionen gegen PostgreSQL.

## Konsequenzen

- Invarianten halten auch unter Last, ohne lange Sperren.
- Der Verlierer eines Konflikts muss die Anfrage wiederholen; das Frontend zeigt die 409-Meldung an.
