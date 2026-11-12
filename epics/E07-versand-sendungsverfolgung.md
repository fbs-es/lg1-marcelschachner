# E07 Versand & Sendungsverfolgung

**Priorität:** nach MVP
**Akteure:** Shop-Mitarbeiter (Logistik), Kunde (Abfrage)
**Abhängig von:** E04

## Ziel

Bestellungen werden in ein oder mehrere Pakete aufgeteilt verschickt, und Kunden wie Mitarbeiter sehen den Lieferstatus.

## Umfang

- Versandpaket zu einer Bestellung anlegen (Sendungsnummer, Dienstleister). → D-12
- Mehrere Pakete je Bestellung; Bestellung ohne Paket ist gültig.
- Lieferstatus fortschreiben, nur erlaubte Übergänge. → D-12
- Pakete und Status einer Bestellung abrufen.

## Nicht im Umfang

- Zuordnung von Positionen zu Paketen, Tracking-API der Dienstleister, Versandetiketten.

## Story-Kandidaten

| ID | Titel |
|---|---|
| US-07.1 | Versandpaket für eine Bestellung anlegen |
| US-07.2 | Lieferstatus aktualisieren (ungültige Übergänge abgelehnt) |
| US-07.3 | Kunde sieht Pakete und Status seiner Bestellung |
