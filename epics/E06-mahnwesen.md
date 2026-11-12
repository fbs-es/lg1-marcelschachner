# E06 Mahnwesen

**Priorität:** MVP
**Akteur:** System (zeitgesteuerter Mahnlauf), Kunde (Empfänger)
**Abhängig von:** E04, E05, E00 (Benachrichtigungsversand)

## Ziel

Für ein Kundenkonto wird automatisch geprüft, ob Bestellungen überfällig und nicht vollständig bezahlt sind. Der Kunde erhält dann eine Mahnung mit dem jeweils offenen Betrag.

## Umfang

- `Kunde.fuehreMahnpruefungDurch(stichtag)`. → D-03
- Erkennung von Rückständen über `istUeberfaellig(stichtag)`. → D-03, D-05
- Eine E-Mail pro Kunde und Prüfung, mit jeder überfälligen Bestellung und ihrem offenen Betrag. → D-03
- Mahnung je Bestellung protokollieren; höchstens einmal pro Mahnintervall. → D-03
- Täglicher Mahnlauf über alle Kunden (ruft die Prüfung je Kunde auf).

## Nicht im Umfang

- Mahnstufen, Gebühren, Zinsen, Inkasso, Mahnung per Post.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-06.1](../stories/US-06.1-automatische-mahnpruefung.md) | Automatische Mahnprüfung für ein Kundenkonto | ausformuliert |
| US-06.2 | Täglicher Mahnlauf über alle Kunden | Kandidat |
