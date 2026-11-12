# E08 Gutscheine & Aktionen

**Priorität:** MVP (gemeinsam mit E04, da der Rabatt in den Gesamtwert eingeht)
**Akteure:** Shop-Mitarbeiter (Pflege), Kunde (Einlösung)
**Abhängig von:** E04

## Ziel

Aktionscodes gewähren einen prozentualen oder festen Rabatt auf eine Bestellung, solange sie gültig sind.

## Umfang

- Gutschein anlegen (Code, Rabattart, Rabattwert, Ablaufdatum). → D-04
- Gutschein im Checkout einlösen, höchstens einer je Bestellung.
- Gültigkeitsprüfung, Rabattberechnung, Einfrieren des Rabattbetrags. → D-04, I-8, I-9
- Gesamtwert wird nie negativ. → D-01

## Nicht im Umfang

- Nutzungslimits, kundengebundene Codes, Mindestbestellwert, Kombination mehrerer Gutscheine.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-08.1](../stories/US-08.1-gutschein-einloesen.md) | Gutschein bei der Bestellung einlösen | ausformuliert |
| [US-08.2](../stories/US-08.2-gutschein-anlegen.md) | Gutschein anlegen | ausformuliert |
