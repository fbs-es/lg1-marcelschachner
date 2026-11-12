# E04 Bestellung & Checkout

**Priorität:** MVP (Kern des Systems)
**Akteur:** Kunde
**Abhängig von:** E01, E02; arbeitet mit E05 (erste Zahlung) und E08 (Gutschein)

## Ziel

Kunden geben Bestellungen auf. Preise, Rabatt und Lieferadresse werden dabei eingefroren, der Lagerbestand sinkt, und der Gesamtwert lässt sich jederzeit berechnen.

## Umfang

- Bestellung mit ≥ 1 Position aus dem Katalog aufgeben. → I-5
- Lieferadresse aus dem Adressbuch wählen; Snapshot wird kopiert. → D-08
- Einzelpreis aus Listenpreis einfrieren, Zeilennummern vergeben, Duplikate zusammenfassen. → D-10
- Bestandsprüfung und Bestandsreduktion atomar. → D-09
- Optional Gutschein anwenden (Prüfung in E08). → D-04
- Erste Zahlung mit Status `AUSSTEHEND` anlegen. → D-02
- Fälligkeit setzen (`bestelldatum + 14 Tage`). → D-03
- `berechneZwischenbetrag()`, `berechneGesamtwert()`. → D-01
- Bestellhistorie des Kunden abrufen.

## Nicht im Umfang

- Warenkorb als eigene, persistente Entität, Storno, Versandkosten, Steuern.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-04.1](../stories/US-04.1-bestellung-aufgeben.md) | Bestellung aufgeben | ausformuliert |
| [US-04.2](../stories/US-04.2-bestellhistorie-einsehen.md) | Bestellhistorie einsehen | ausformuliert |
