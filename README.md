# Bestellsystem (E-Commerce)

Fachliche Grundlage für die Neuentwicklung des Bestellsystems. Ausgangspunkt ist die Anforderungsbeschreibung in `docs/00-anforderung.md`.

## Inhalt

| Datei | Inhalt |
|---|---|
| `docs/00-anforderung.md` | Originaltext der Anforderung |
| `docs/01-entscheidungen.md` | Aufgelöste Lücken und Widersprüche (verbindlich) |
| `docs/02-domaenenmodell.md` | UML-Klassendiagramm (Mermaid), Invarianten, Formeln |
| `docs/fahrplan.md` | Umsetzungsschritte 0 bis 9 mit Prompt und "Fertig wenn" |
| `CLAUDE.md` | Regeln für Claude Code (Stack, Struktur, Tests, Hibernate) |
| `.claude/skills/naechster-schritt/` | `/naechster-schritt`: obersten offenen Schritt abarbeiten |
| `epics/` | Alle Epics (E00 bis E08) |
| `stories/` | Ausformulierte User Stories mit Akzeptanzkriterien |

## Akteure

- **Kunde**: registriert sich, pflegt Adressbuch und Merkliste, bestellt.
- **Shop-Mitarbeiter**: pflegt Katalog, Lagerbestand, Gutscheine, Versandpakete.
- **System**: Zahlungsanbieter (meldet Zahlungsstatus), zeitgesteuerter Mahnlauf, E-Mail-Versand.

## Epics

| ID | Epic | Priorität | Stories |
|---|---|---|---|
| E00 | [Domänenkern & Plattform](epics/E00-domaenenkern-plattform.md) | MVP | (technisch) |
| E01 | [Kundenkonto & Adressbuch](epics/E01-kundenkonto-adressbuch.md) | MVP | [US-01.1](stories/US-01.1-registrierung-mit-lieferadresse.md) |
| E02 | [Produktkatalog & Lagerbestand](epics/E02-produktkatalog-lagerbestand.md) | MVP | [US-02.1](stories/US-02.1-verfuegbarkeit-pruefen.md) |
| E03 | [Merkliste](epics/E03-merkliste.md) | MVP | [US-03.1](stories/US-03.1-produkt-merken.md) |
| E04 | [Bestellung & Checkout](epics/E04-bestellung-checkout.md) | MVP | [US-04.1](stories/US-04.1-bestellung-aufgeben.md) |
| E05 | [Zahlungen & Erstattungen](epics/E05-zahlungen-erstattungen.md) | MVP | [US-05.1](stories/US-05.1-zahlungsstatus-pruefen.md) |
| E06 | [Mahnwesen](epics/E06-mahnwesen.md) | MVP | [US-06.1](stories/US-06.1-automatische-mahnpruefung.md) |
| E07 | [Versand & Sendungsverfolgung](epics/E07-versand-sendungsverfolgung.md) | nach MVP | (Kandidaten im Epic) |
| E08 | [Gutscheine & Aktionen](epics/E08-gutscheine-aktionen.md) | MVP | [US-08.1](stories/US-08.1-gutschein-einloesen.md) |

## Empfohlene Umsetzungsreihenfolge

1. **E00**: Geld-Typ, Projektgerüst, Persistenz, Testinfrastruktur.
2. **E02, E01**: Produkte und Kunden, ohne sie geht nichts.
3. **E04 + E08**: Checkout inklusive Gutschein (Rabatt fließt in den Gesamtwert).
4. **E05**: Zahlungen, `istVollstaendigBezahlt()`.
5. **E06**: Mahnprüfung (baut auf E04 und E05 auf).
6. **E03**: Merkliste (unabhängig, kann parallel laufen).
7. **E07**: Versand.

Die Domänenlogik (`berechneZwischenbetrag`, `berechneGesamtwert`, `istVollstaendigBezahlt`, `istVerfuegbar`, Mahnprüfung) wird testgetrieben als reiner Code ohne UI und Datenbank gebaut.
