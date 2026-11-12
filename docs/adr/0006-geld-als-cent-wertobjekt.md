# ADR 0006: Geld als Wertobjekt in ganzen Cent

**Status:** angenommen (2026-11-12)

## Kontext

Gesamtwert, Rabatt, bezahlte Summe und offener Betrag werden laufend berechnet ([D-01](../01-entscheidungen.md#d-01-gesamtwert-gespeichert-oder-berechnet), [D-05](../01-entscheidungen.md#d-05-erstattungen-und-transaktionsstatus)). Gleitkomma-Arithmetik erzeugt Rundungsfehler (`0.1 + 0.2 ≠ 0.3`), die beim Abgleich `bezahlteSumme ≥ gesamtwert` falsche Ergebnisse liefern. Prozentrabatte müssen kaufmännisch gerundet werden ([D-04](../01-entscheidungen.md#d-04-gutschein-art-des-rabatts-und-wirkung), [D-11](../01-entscheidungen.md#d-11-geldbeträge-und-währung), [D-17](../01-entscheidungen.md#d-17-checkout-ablehnungen-gutscheincode-erste-zahlung)).

## Entscheidung

- Wertobjekt `Geld` in `gemeinsam.domain`: Java-`record` mit Betrag in **ganzen Cent als `long`**, Währung fest **EUR**.
- Operationen `addiere`, `subtrahiere`, `multipliziere(menge)`, `prozentsatz(BigDecimal)`; Prozent rundet `HALF_UP` auf ganze Cent.
- Überlauf wirft `ArithmeticException` (`Math.*Exact`) statt still falsch zu rechnen.
- `Geld` darf als Zwischenergebnis negativ sein; Nichtnegativität prüfen die Entities (I-7).
- Persistenz als `@Embeddable` mit `@AttributeOverride` auf sprechende Spalten (`listenpreis_cent`, `rabatt_betrag_cent`, …).
- In der API als `{cent, waehrung}` (`GeldDto`). Das Frontend rechnet Euro-Eingaben ohne Gleitkomma in Cent um und zeigt Beträge mit der `CurrencyPipe` in `de-DE`.
- Abgeleitete Beträge werden nie gespeichert, nur aus eingefrorenen Eingaben berechnet ([D-01](../01-entscheidungen.md#d-01-gesamtwert-gespeichert-oder-berechnet)).

## Konsequenzen

- Keine Rundungsdifferenzen zwischen Backend, Datenbank und Frontend.
- Mehrere Währungen bräuchten eine Erweiterung (Umrechnung, Rundung je Währung); im MVP nicht vorgesehen.
