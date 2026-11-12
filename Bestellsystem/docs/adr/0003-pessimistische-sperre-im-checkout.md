# ADR 0003: Pessimistische Sperre auf Produkte im Checkout

**Status:** angenommen (2026-11-12)

## Kontext

Der Checkout prüft und senkt den Lagerbestand in einer Transaktion ([D-09](../01-entscheidungen.md#d-09-lagerbestand-und-verfügbarkeit)). Zwei gleichzeitige Checkouts dürfen den Bestand nicht gemeinsam unter 0 drücken. `Produkt` trägt `@Version`.

Rein optimistisch scheitert der zweite Checkout mit einem Versionskonflikt, auch wenn der Bestand für beide gereicht hätte (zwei Kunden kaufen je 1 von 10 Büchern). Der Kunde sähe "Versionskonflikt" statt eines fachlichen Hinweises.

## Entscheidung

- Der Checkout lädt alle bestellten Produkte mit `PESSIMISTIC_WRITE` (`SELECT … FOR UPDATE`), sortiert nach `id`. Die feste Reihenfolge verhindert Deadlocks zwischen Checkouts mit überlappenden Produkten.
- Der zweite Checkout wartet, bis der erste festschreibt, und prüft dann den aktuellen Bestand. Reicht er nicht, folgt die fachliche Ablehnung (409 mit Produktname, [D-17](../01-entscheidungen.md#d-17-checkout-ablehnungen-gutscheincode-erste-zahlung)).
- `@Version` bleibt: Der Bestand ändert sich weiterhin auch über `PUT /produkte/{id}/lagerbestand`, das optimistisch arbeitet. Die Version steigt auch beim Checkout.

## Konsequenzen

- Gleichzeitige Checkouts auf dasselbe Produkt laufen nacheinander. Bei einem Shop dieser Größe unkritisch.
- Ein gleichzeitiges `PUT lagerbestand` kann am Versionskonflikt scheitern (409), nie ein Checkout.
