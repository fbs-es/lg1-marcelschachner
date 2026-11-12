# E05 Zahlungen & Erstattungen

**Priorität:** MVP
**Akteure:** System (Zahlungsanbieter), Shop-Mitarbeiter (Erstattung)
**Abhängig von:** E04

## Ziel

Alle Geldflüsse einer Bestellung werden lückenlos gebucht, und für jede Bestellung ist jederzeit feststellbar, ob sie vollständig bezahlt ist.

## Umfang

- Zahlungsstatus aktualisieren (`AUSSTEHEND` → `ERFOLGREICH` / `FEHLGESCHLAGEN` / `STORNIERT`). → D-05
- Teilzahlungen als weitere Buchungen.
- Erstattungen als Buchung mit `buchungsart = ERSTATTUNG`. → D-05
- `bezahlteSumme()`, `offenerBetrag()`, `istVollstaendigBezahlt()`. → D-05

## Nicht im Umfang

- Echte Anbindung an Zahlungsanbieter (Webhooks), Rechnungs-PDF, Buchhaltungsexport.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-05.1](../stories/US-05.1-zahlungsstatus-pruefen.md) | Prüfen, ob eine Bestellung vollständig bezahlt ist | ausformuliert |
| US-05.2 | Zahlungsstatus vom Anbieter übernehmen | Kandidat |
| US-05.3 | Erstattung buchen | Kandidat |
