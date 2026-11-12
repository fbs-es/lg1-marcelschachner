# E02 Produktkatalog & Lagerbestand

**Priorität:** MVP
**Akteure:** Shop-Mitarbeiter (Pflege), Kunde und System (Abfrage)
**Abhängig von:** E00

## Ziel

Produkte mit Titel, Listenpreis und Lagerbestand verwalten und jederzeit prüfen können, ob ein Produkt verfügbar ist.

## Umfang

- Produkte anlegen, Titel und Listenpreis ändern.
- Lagerbestand setzen (Wareneingang, Korrektur); nie negativ. → I-7
- Verfügbarkeitsprüfung `istVerfuegbar()` und `istVerfuegbar(menge)`. → D-09
- Preisänderungen wirken nicht auf bestehende Bestellungen. → D-10

## Nicht im Umfang

- Kategorien, Suche, Bilder, Varianten, Steuersätze.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-02.1](../stories/US-02.1-verfuegbarkeit-pruefen.md) | Verfügbarkeit eines Produkts prüfen | ausformuliert |
| [US-02.2](../stories/US-02.2-produkt-pflegen.md) | Produkt pflegen (Titel, Listenpreis, Bestand) | ausformuliert |
| US-02.3 | Lagerbestand buchen | Kandidat |
