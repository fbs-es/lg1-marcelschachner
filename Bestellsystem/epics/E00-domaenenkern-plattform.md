# E00 Domänenkern & Plattform

**Priorität:** MVP (Voraussetzung für alle anderen Epics)

## Ziel

Ein lauffähiges Grundgerüst, auf dem alle fachlichen Epics aufsetzen: Projektstruktur, Persistenz, Testinfrastruktur und gemeinsame Bausteine.

## Umfang

- Projektgerüst inklusive Build, Linting und automatisierten Tests (CI).
- Wertobjekt **Geld** (Cent-Ganzzahl, EUR, Addition, Multiplikation mit Menge, kaufmännisches Runden). → D-11
- Wertobjekt **Anschrift**. → D-08
- Persistenzschicht mit Migrationen; Transaktionen für den Checkout. → D-09
- Schnittstelle (Port) **Benachrichtigungsversand** (E-Mail), im Test durch eine Attrappe ersetzbar. → D-03
- Konfiguration: Zahlungsziel (14 Tage), Mahnintervall (7 Tage). → D-03
- Generator für fachliche Bestellnummern. → D-13
- Uhr als injizierbare Abhängigkeit (Stichtage testbar).

## Nicht im Umfang

- Benutzeroberfläche, Authentifizierung (eigene Entscheidung zum Tech-Stack nötig).

## Akzeptanz auf Epic-Ebene

- `Geld` rechnet ohne Gleitkommafehler (z. B. 3 × 0,10 € = 0,30 €).
- Tests laufen lokal und in der CI grün.
- Ein Test kann Datum und Uhrzeit festlegen.
