# E01 Kundenkonto & Adressbuch

**Priorität:** MVP
**Akteur:** Kunde
**Abhängig von:** E00

## Ziel

Kunden können ein Konto anlegen und ein persönliches Adressbuch mit Liefer- und Rechnungsadressen pflegen, das beim Bestellen verwendet wird.

## Umfang

- Registrierung mit Vorname, Nachname, eindeutiger E-Mail und erster Lieferadresse. → D-06, D-13, D-16
- Weitere Adressen anlegen, ändern, löschen.
- Adressen als Liefer- und/oder Rechnungsadresse kennzeichnen. → D-06
- Schutz der Invariante "mindestens eine Lieferadresse". → I-1
- Änderungen an Adressen wirken nicht auf bestehende Bestellungen. → D-08

## Nicht im Umfang

- Login, Passwort, Konto löschen (DSGVO), Standardadresse.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-01.1](../stories/US-01.1-registrierung-mit-lieferadresse.md) | Registrierung mit Lieferadresse | ausformuliert |
| [US-01.2](../stories/US-01.2-weitere-adresse-hinzufuegen.md) | Weitere Liefer- oder Rechnungsadresse hinzufügen | ausformuliert |
| [US-01.3](../stories/US-01.3-adresse-aendern-loeschen.md) | Adresse ändern oder löschen (letzte Lieferadresse geschützt) | ausformuliert |
