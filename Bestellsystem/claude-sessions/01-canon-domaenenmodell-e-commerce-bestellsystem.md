## Session 1: Domänenmodell E-Commerce-Bestellsystem

*Projekt: Canon · ID `7334ca7c-f7d6-49b8-8992-0a546c1dcf45` · 2026-11-12 08:07 – 2026-11-12 08:46 UTC*

### ❯ Nutzer

> /clear

### ❯ Nutzer

> Ein führendes E-Commerce-Unternehmen stellt sein Softwaresystem für Bestellungen um.
> Im System wird jeder Kunde mit einer eindeutigen Kundenkennung, seinem Vor- und Nachnamen sowie einer eindeutigen E-Mail-Adresse erfasst. Ein Kunde muss in der Lage sein, seine aktuell beobachteten Artikel abzurufen. Zudem muss für ein Kundenkonto eine automatische Mahnprüfung durchgeführt werden können, die alle Bestellungen des Kunden auf offene Beträge untersucht und im Fall von Zahlungsrückständen eine Mahn-Benachrichtigung mit dem jeweils offenen Betrag an den Kunden versendet.
> Ein Kunde muss mindestens eine Lieferadresse für spätere Einkäufe in seinem persönlichen Adressbuch hinterlegen. Bei Bedarf können weitere Liefer- und Rechnungsadressen hinzugefügt werden. Eine Adresse setzt sich aus einer Adresskennung, der Straße, der Stadt, der Postleitzahl und dem Land zusammen. Jede gespeicherte Adresse ist dabei stets genau einem Kunden zugeordnet.
> Jedes Produkt speichert seine Identifikationsnummer, den Produkttitel, den aktuellen Lagerbestand sowie den aktuellen Listenpreis. Das System muss jederzeit prüfen können, ob ein Produkt aktuell verfügbar ist.
> Kunden können Produkte auf eine persönliche Merkliste setzen. Ein Kunde kann beliebig viele Produkte auf seine Liste setzen. Umgekehrt kann ein Produkt auf der Merkliste mehrerer Kunden stehen oder von niemandem beobachtet werden. Für jedem Eintrag auf der Merkliste muss zwingend festgehalten werden, an welchem Datum das Produkt hinzugefügt wurde.
> Eine getätigte Bestellung wird zentral im System abgewickelt. Sie speichert die Bestellnummer, das Bestelldatum, den berechneten Gesamtwert sowie einen historischen Snapshot der Lieferadresse (bestehend aus Straße, Stadt, Postleitzahl und Land). Der Gesamtwert muss dynamisch berechnet werden können. Zudem muss für jede Bestellung jederzeit geprüft werden können, ob sie bereits vollständig bezahlt wurde (durch Abgleich des berechneten Gesamtwerts mit der Summe aller geleisteten Zahlungen). Ein Kunde kann im Laufe der Zeit beliebig viele Bestellungen aufgeben oder noch keine getätigt haben. Jede Bestellung gehört jedoch zu genau einem Kunden.
> Eine Bestellung setzt sich aus einzelnen Positionen zusammen. Jede Position speichert ihre Zeilennummer, die gewünschte Bestellmenge und den zum Kaufzeitpunkt eingefrorenen Einzelpreis. Für jede Position muss ein Zwischenbetrag berechnet werden können. Eine Bestellung besitzt mindestens eine bis beliebig viele Positionen, und jede Position gehört zu genau einer Bestellung.
> Jede Bestellposition verweist zudem über eine einfache Beziehung auf genau ein Produkt aus dem Katalog. Ein Produkt kann in beliebig vielen Bestellpositionen verbucht sein (oder in gar keiner), falls es noch nie gekauft wurde.
> Zur Erfassung von Zahlungsströmen werden Zahlungen mit einer Zahlungs-ID, der Zahlungsart, dem Transaktionsstatus, dem gezahlten Betrag und dem Zahlungszeitpunkt erfasst. Eine Bestellung erfordert mindestens eine bis beliebig viele Zahlungsbuchungen (z. B. bei Teilzahlungen oder Erstattungen), während jede Zahlung genau einer Bestellung zugewiesen ist.
> Für die Logistik existieren Versandpakete, die eine Sendungsverfolgungsnummer, den Logistikdienstleister und den Lieferstatus erfassen. Eine Bestellung kann in mehrere Versandpakete aufgeteilt werden oder noch keines aufweisen. Jedes Paket gehört zu genau einer Bestellung.
> Aktionscodes werden als Gutscheine mit einem Gutscheincode, einem Rabattwert und einem Ablaufdatum hinterlegt. Ein Gutschein kann bei mehreren Bestellungen angewendet werden oder noch ungenutzt im System verbleiben. Eine Bestellung kann von maximal einem Gutschein profitieren oder ohne Gutschein getätigt werden.
>
>
> Oben ist eine Definition, welches man in ein UML übertragen kann. Es ist die Basis meiner Applikation. Sollte man nun Funktionalität definieren (Epics/User-Stories) oder loslegen mit dem Code?

### ● Claude

**Empfehlung: Weder komplett vorab spezifizieren noch blind losprogrammieren.** Erst ein paar Epics und Stories schreiben, dann die Kernlogik codieren, dann den Rest Stück für Stück.

## Warum nicht sofort coden

Text beschreibt fast nur **Struktur**, also Entitäten, Attribute und Multiplizitäten. Es fehlt das **Verhalten**:
- **Akteure:** Wer macht was? Kunde, Admin, ein Batch-Job für Mahnungen?
- **Abläufe:** Wie läuft der Checkout? Wann wird der Lagerbestand reduziert? Wann wird der Adress-Snapshot gezogen?
- **Zustände:** Status von Zahlung und Paket. Welche Werte und welche Übergänge?
- **Nicht-funktionale Punkte:** Tech-Stack, Schnittstellen (API oder UI?), Persistenz.

Wer jetzt schon codet, baut ein Datenmodell, findet aber im ersten echten Ablauf die Lücken.

## Lücken und Widersprüche im Text (vorher klären)

1. **Gesamtwert wird "gespeichert" und "dynamisch berechnet".** Beides zugleich ist redundant. Im UML als abgeleitetes Attribut `/gesamtwert`, nicht als Spalte speichern.
2. **Bestellung "erfordert mindestens eine Zahlung" (1..\*).** Das widerspricht der Mahnprüfung: Eine unbezahlte Bestellung könnte so nie existieren. Entweder 0..\* oder eine Zahlung mit Status "ausstehend".
3. **Mahnung ohne Fälligkeit.** Es gibt kein Fälligkeitsdatum und keine Zahlungsfrist, also lässt sich "Rückstand" nicht von "gerade bestellt" unterscheiden.
4. **Gutschein.** Ist der Rabattwert absolut oder in Prozent? Wird er in `berechneGesamtwert()` eingerechnet? Wird das Ablaufdatum beim Anwenden geprüft?
5. **Erstattungen.** Sind das negative Beträge? Zählen nur Zahlungen mit Status "erfolgreich" für `istBezahlt()`?
6. **Liefer- vs. Rechnungsadresse.** Typ-Attribut oder Unterklassen? Die Bedingung "mindestens eine Lieferadresse" muss sich durchsetzen lassen.
7. **Merkliste.** Als Assoziationsklasse mit `hinzugefuegtAm` modellieren (Kunde \*–\* Produkt).
8. **Adress-Snapshot.** Ein eingebettetes Wertobjekt, keine Referenz auf `Adresse`.

## Reihenfolge

1. **Fragen oben klären** (eine Stunde, spart Tage).
2. **UML-Klassendiagramm** zeichnen, der Text ist ja schon fast fertig dafür.
3. **Epics festlegen:** Kundenkonto & Adressbuch · Katalog & Verfügbarkeit · Merkliste · Bestellung/Checkout · Zahlung · Mahnwesen · Versand · Gutscheine.
4. **Nur für den MVP Stories schreiben**, mit Akzeptanzkriterien. Beispiel: *"Als Kunde will ich eine Bestellung mit Gutschein aufgeben, damit der Rabatt abgezogen wird."* Kriterium: Abgelaufener Gutschein wird abgelehnt.
5. **Coden, Domain-Modell zuerst, testgetrieben.** `istVerfuegbar()`, `berechneZwischenbetrag()`, `berechneGesamtwert()`, `istBezahlt()` und die Mahnprüfung sind reine Logik und gut testbar. UI und Persistenz kommen danach.
6. **Weitere Epics in vertikalen Schritten** nachziehen.

Kurz gesagt: Die Stories liefern das Verhalten, das der Text nicht beschreibt. Für ein reines Lern- oder Übungsprojekt reicht das UML plus die geklärten Fragen, und du kannst direkt mit dem Domain-Modell anfangen.

Soll ich das Klassendiagramm (PlantUML oder Mermaid) oder die Epics und Stories ausformulieren?

### ❯ Nutzer

> Löse die Lücken und Widersprüche mit deinen Empfehlungen auf, und erstelle mir alle erforderlichen Epics, und für bestimmte erstelle uns eine User-Story. Erstelle diese in einem eigenen Unterordner in @../../Projects/

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/README.md
# Bestellsystem (E-Commerce)

Fachliche Grundlage für die Neuentwicklung des Bestellsystems. Ausgangspunkt ist die Anforderungsbeschreibung in `docs/00-anforderung.md`.

## Inhalt

| Datei | Inhalt |
|---|---|
| `docs/00-anforderung.md` | Originaltext der Anforderung |
| `docs/01-entscheidungen.md` | Aufgelöste Lücken und Widersprüche (verbindlich) |
| `docs/02-domaenenmodell.md` | UML-Klassendiagramm (Mermaid), Invarianten, Formeln |
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
… [gekürzt, 46 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/README.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/docs/00-anforderung.md
# Anforderung (Originaltext)

Ein führendes E-Commerce-Unternehmen stellt sein Softwaresystem für Bestellungen um.

Im System wird jeder Kunde mit einer eindeutigen Kundenkennung, seinem Vor- und Nachnamen sowie einer eindeutigen E-Mail-Adresse erfasst. Ein Kunde muss in der Lage sein, seine aktuell beobachteten Artikel abzurufen. Zudem muss für ein Kundenkonto eine automatische Mahnprüfung durchgeführt werden können, die alle Bestellungen des Kunden auf offene Beträge untersucht und im Fall von Zahlungsrückständen eine Mahn-Benachrichtigung mit dem jeweils offenen Betrag an den Kunden versendet.

Ein Kunde muss mindestens eine Lieferadresse für spätere Einkäufe in seinem persönlichen Adressbuch hinterlegen. Bei Bedarf können weitere Liefer- und Rechnungsadressen hinzugefügt werden. Eine Adresse setzt sich aus einer Adresskennung, der Straße, der Stadt, der Postleitzahl und dem Land zusammen. Jede gespeicherte Adresse ist dabei stets genau einem Kunden zugeordnet.

Jedes Produkt speichert seine Identifikationsnummer, den Produkttitel, den aktuellen Lagerbestand sowie den aktuellen Listenpreis. Das System muss jederzeit prüfen können, ob ein Produkt aktuell verfügbar ist.

Kunden können Produkte auf eine persönliche Merkliste setzen. Ein Kunde kann beliebig viele Produkte auf seine Liste setzen. Umgekehrt kann ein Produkt auf der Merkliste mehrerer Kunden stehen oder von niemandem beobachtet werden. Für jedem Eintrag auf der Merkliste muss zwingend festgehalten werden, an welchem Datum das Produkt hinzugefügt wurde.

Eine getätigte Bestellung wird zentral im System abgewickelt. Sie speichert die Bestellnummer, das Bestelldatum, den berechneten Gesamtwert sowie einen historischen Snapshot der Lieferadresse (bestehend aus Straße, Stadt, Postleitzahl und Land). Der Gesamtwert muss dynamisch berechnet werden können. Zudem muss für jede Bestellung jederzeit geprüft werden können, ob sie bereits vollständig bezahlt wurde (durch Abgleich des berechneten Gesamtwerts mit der Summe aller geleisteten Zahlungen). Ein Kunde kann im Laufe der Zeit beliebig viele Bestellungen aufgeben oder noch keine getätigt haben. Jede Bestellung gehört jedoch zu genau einem Kunden.

Eine Bestellung setzt sich aus einzelnen Positionen zusammen. Jede Position speichert ihre Zeilennummer, die gewünschte Bestellmenge und den zum Kaufzeitpunkt eingefrorenen Einzelpreis. Für jede Position muss ein Zwischenbetrag berechnet werden können. Eine Bestellung besitzt mindestens eine bis beliebig viele Positionen, und jede Position gehört zu genau einer Bestellung.

Jede Bestellposition verweist zudem über eine einfache Beziehung auf genau ein Produkt aus dem Katalog. Ein Produkt kann in beliebig vielen Bestellpositionen verbucht sein (oder in gar keiner), falls es noch nie gekauft wurde.

Zur Erfassung von Zahlungsströmen werden Zahlungen mit einer Zahlungs-ID, der Zahlungsart, dem Transaktionsstatus, dem gezahlten Betrag und dem Zahlungszeitpunkt erfasst. Eine Bestellung erfordert mindestens eine bis beliebig viele Zahlungsbuchungen (z. B. bei Teilzahlungen oder Erstattungen), während jede Zahlung genau einer Bestellung zugewiesen ist.

Für die Logistik existieren Versandpakete, die eine Sendungsverfolgungsnummer, den Logistikdienstleister und den Lieferstatus erfassen. Eine Bestellung kann in mehrere Versandpakete aufgeteilt werden oder noch keines aufweisen. Jedes Paket gehört zu genau einer Bestellung.

Aktionscodes werden als Gutscheine mit einem Gutscheincode, einem Rabattwert und einem Ablaufdatum hinterlegt. Ein Gutschein kann bei mehreren Bestellungen angewendet werden oder noch ungenutzt im System verbleiben. Eine Bestellung kann von maximal einem Gutschein profitieren oder ohne Gutschein getätigt werden.
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/00-anforderung.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

````
/home/mschachner/Projects/bestellsystem/docs/01-entscheidungen.md
# Entscheidungen zu Lücken und Widersprüchen

Verbindlich für Modell, Epics und Stories. Jede Entscheidung hat eine ID (`D-xx`), auf die Epics und Stories verweisen. Änderungen hier zuerst, dann im Code.

---

## D-01 Gesamtwert: gespeichert oder berechnet?

**Problem:** Der Text sagt, die Bestellung "speichert den berechneten Gesamtwert" und der Gesamtwert müsse "dynamisch berechnet werden können". Beides gleichzeitig ist redundant und kann auseinanderlaufen.

**Entscheidung:** Der Gesamtwert ist ein **abgeleitetes Attribut** (`/gesamtwert` im UML) und wird nicht als eigene Spalte gespeichert.

```
warenwert   = Summe aller Positionen (menge × einzelpreis)
gesamtwert  = max(0, warenwert − rabattBetrag)
```

Historische Stabilität ist trotzdem gegeben, weil alle Eingaben eingefroren sind: der Einzelpreis je Position (D-10) und der Rabattbetrag (D-04).

**Begründung:** Eine einzige Wahrheitsquelle. Ein gespeicherter Wert müsste bei jeder Änderung nachgezogen werden.

**Option für später:** Falls Reports den Wert brauchen, darf er als Cache persistiert werden; die Berechnung bleibt maßgeblich.

---

## D-02 Zahlung "mindestens eine" vs. offene Beträge

**Problem:** "Eine Bestellung erfordert mindestens eine Zahlungsbuchung" (1..\*). Gleichzeitig soll die Mahnprüfung offene Beträge finden. Wäre jede Bestellung sofort bezahlt, gäbe es nichts zu mahnen.

… [gekürzt, 164 Zeilen gesamt]
````

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/01-entscheidungen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

````
/home/mschachner/Projects/bestellsystem/docs/02-domaenenmodell.md
# Domänenmodell

Enthält alle Entscheidungen aus `01-entscheidungen.md`. Abgeleitete Attribute sind mit `/` markiert.

## Klassendiagramm

```mermaid
classDiagram
  direction LR

  class Kunde {
    kundenId: UUID
    vorname: String
    nachname: String
    email: Email «eindeutig»
    getBeobachteteArtikel() List~Produkt~
    fuehreMahnpruefungDurch(stichtag: Datum) Mahnbenachrichtigung?
  }

  class Adresse {
    adressId: UUID
    strasse: String
    stadt: String
    postleitzahl: String
    land: Laendercode
    istLieferadresse: Boolean
    istRechnungsadresse: Boolean
  }

… [gekürzt, 183 Zeilen gesamt]
````

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/02-domaenenmodell.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E00-domaenenkern-plattform.md
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
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E00-domaenenkern-plattform.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E01-kundenkonto-adressbuch.md
# E01 Kundenkonto & Adressbuch

**Priorität:** MVP
**Akteur:** Kunde
**Abhängig von:** E00

## Ziel

Kunden können ein Konto anlegen und ein persönliches Adressbuch mit Liefer- und Rechnungsadressen pflegen, das beim Bestellen verwendet wird.

## Umfang

- Registrierung mit Vorname, Nachname, eindeutiger E-Mail und erster Lieferadresse. → D-06, D-13
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
| US-01.2 | Weitere Liefer- oder Rechnungsadresse hinzufügen | Kandidat |
| US-01.3 | Adresse ändern oder löschen (letzte Lieferadresse geschützt) | Kandidat |
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E01-kundenkonto-adressbuch.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E02-produktkatalog-lagerbestand.md
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
| US-02.2 | Produkt anlegen und Listenpreis pflegen | Kandidat |
| US-02.3 | Lagerbestand buchen | Kandidat |
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E02-produktkatalog-lagerbestand.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E03-merkliste.md
# E03 Merkliste

**Priorität:** MVP (unabhängig, parallel umsetzbar)
**Akteur:** Kunde
**Abhängig von:** E01, E02

## Ziel

Kunden merken sich Produkte für später und sehen jederzeit ihre aktuell beobachteten Artikel.

## Umfang

- Produkt auf die Merkliste setzen; Datum wird festgehalten. → D-07
- Produkt von der Merkliste entfernen.
- Beobachtete Artikel abrufen, neueste zuerst, mit aktueller Verfügbarkeit und aktuellem Preis. → D-07, D-09
- Doppeltes Merken ist ein No-op. → I-4

## Nicht im Umfang

- Benachrichtigung bei Preissenkung oder Wiederverfügbarkeit, mehrere Listen, Teilen.

## Stories

| ID | Titel | Status |
|---|---|---|
| [US-03.1](../stories/US-03.1-produkt-merken.md) | Produkt merken und beobachtete Artikel abrufen | ausformuliert |
| US-03.2 | Produkt von der Merkliste entfernen | Kandidat |
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E03-merkliste.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E04-bestellung-checkout.md
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
… [gekürzt, 33 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E04-bestellung-checkout.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E05-zahlungen-erstattungen.md
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
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E05-zahlungen-erstattungen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E06-mahnwesen.md
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
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E06-mahnwesen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E07-versand-sendungsverfolgung.md
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
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E07-versand-sendungsverfolgung.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/epics/E08-gutscheine-aktionen.md
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
| US-08.2 | Gutschein anlegen | Kandidat |
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/epics/E08-gutscheine-aktionen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-01.1-registrierung-mit-lieferadresse.md
# US-01.1 Registrierung mit Lieferadresse

**Epic:** E01 Kundenkonto & Adressbuch
**Entscheidungen:** D-06, D-13

## Story

**Als** neuer Kunde
**möchte ich** mich mit meinem Namen, meiner E-Mail-Adresse und einer Lieferadresse registrieren,
**damit** ich später ohne erneute Adresseingabe bestellen kann.

## Akzeptanzkriterien

1. **Erfolgreiche Registrierung**
   Gegeben keine Registrierung mit `anna@example.de`,
   wenn ich mich mit Vorname, Nachname, `anna@example.de` und einer vollständigen Adresse registriere,
   dann existiert ein Kunde mit neuer Kundenkennung, und sein Adressbuch enthält genau diese Adresse als Lieferadresse.

2. **Lieferadresse ist Pflicht**
   Wenn ich mich ohne Adresse registriere,
   dann wird die Registrierung abgelehnt mit dem Hinweis, dass eine Lieferadresse erforderlich ist.

3. **E-Mail eindeutig, unabhängig von Groß-/Kleinschreibung**
   Gegeben ein Kunde mit `anna@example.de`,
   wenn sich jemand mit ` Anna@Example.DE ` registriert,
   dann wird die Registrierung abgelehnt, weil die E-Mail bereits vergeben ist.

4. **Pflichtfelder der Adresse**
   Wenn Straße, Stadt, Postleitzahl oder Land fehlen,
… [gekürzt, 40 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-01.1-registrierung-mit-lieferadresse.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-02.1-verfuegbarkeit-pruefen.md
# US-02.1 Verfügbarkeit eines Produkts prüfen

**Epic:** E02 Produktkatalog & Lagerbestand
**Entscheidungen:** D-09

## Story

**Als** Kunde
**möchte ich** sehen, ob ein Produkt aktuell verfügbar ist,
**damit** ich nur bestelle, was auch geliefert werden kann.

## Akzeptanzkriterien

1. **Verfügbar bei Bestand**
   Gegeben ein Produkt mit Lagerbestand 3,
   dann ist `istVerfuegbar()` wahr.

2. **Nicht verfügbar ohne Bestand**
   Gegeben ein Produkt mit Lagerbestand 0,
   dann ist `istVerfuegbar()` falsch.

3. **Verfügbarkeit für eine Menge**
   Gegeben ein Produkt mit Lagerbestand 3,
   dann ist `istVerfuegbar(3)` wahr und `istVerfuegbar(4)` falsch.

4. **Ungültige Menge**
   Wenn `istVerfuegbar(0)` oder eine negative Menge abgefragt wird,
   dann wird die Anfrage als ungültig abgelehnt.

… [gekürzt, 38 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-02.1-verfuegbarkeit-pruefen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-03.1-produkt-merken.md
# US-03.1 Produkt merken und beobachtete Artikel abrufen

**Epic:** E03 Merkliste
**Entscheidungen:** D-07, D-09

## Story

**Als** Kunde
**möchte ich** Produkte auf meine Merkliste setzen und diese jederzeit abrufen,
**damit** ich interessante Artikel später wiederfinde und kaufen kann.

## Akzeptanzkriterien

1. **Produkt merken**
   Gegeben meine leere Merkliste,
   wenn ich am 12.11.2026 das Produkt "Kopfhörer" merke,
   dann enthält meine Merkliste einen Eintrag "Kopfhörer" mit Datum 12.11.2026.

2. **Doppeltes Merken ändert nichts**
   Gegeben "Kopfhörer" steht seit 12.11.2026 auf meiner Merkliste,
   wenn ich es am 20.11.2026 erneut merke,
   dann steht es weiterhin genau einmal auf der Liste, mit Datum 12.11.2026.

3. **Beobachtete Artikel abrufen**
   Gegeben ich habe "Kopfhörer" (12.11.) und "Kabel" (15.11.) gemerkt,
   wenn ich meine beobachteten Artikel abrufe,
   dann sehe ich "Kabel" vor "Kopfhörer", jeweils mit Merkdatum, aktuellem Listenpreis und Verfügbarkeit.

4. **Listen sind persönlich**
… [gekürzt, 41 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-03.1-produkt-merken.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-04.1-bestellung-aufgeben.md
# US-04.1 Bestellung aufgeben

**Epic:** E04 Bestellung & Checkout
**Entscheidungen:** D-01, D-02, D-03, D-08, D-09, D-10, D-11

## Story

**Als** Kunde
**möchte ich** ausgewählte Produkte mit einer meiner Lieferadressen und einer Zahlungsart bestellen,
**damit** die Ware zu mir geliefert wird.

## Beispieldaten

| Produkt | Listenpreis | Lagerbestand |
|---|---|---|
| Buch | 12,50 € | 10 |
| Lampe | 30,00 € | 1 |

## Akzeptanzkriterien

1. **Bestellung wird angelegt**
   Gegeben ich bestelle 2 × Buch und 1 × Lampe an meine Lieferadresse "Hauptstr. 1, 10115 Berlin, DE" mit Zahlungsart RECHNUNG am 12.11.2026,
   dann existiert eine Bestellung mit eindeutiger Bestellnummer, Bestelldatum 12.11.2026 und
   - Position 1: Buch, Menge 2, Einzelpreis 12,50 €, Zwischenbetrag 25,00 €
   - Position 2: Lampe, Menge 1, Einzelpreis 30,00 €, Zwischenbetrag 30,00 €
   - Gesamtwert 55,00 €
   - Fälligkeit 26.11.2026
   - einer Zahlung über 55,00 €, Art RECHNUNG, Status AUSSTEHEND.

… [gekürzt, 64 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-04.1-bestellung-aufgeben.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-05.1-zahlungsstatus-pruefen.md
# US-05.1 Prüfen, ob eine Bestellung vollständig bezahlt ist

**Epic:** E05 Zahlungen & Erstattungen
**Entscheidungen:** D-02, D-05, D-11

## Story

**Als** Shop-Mitarbeiter
**möchte ich** für jede Bestellung sehen, ob sie vollständig bezahlt ist und wie viel noch offen ist,
**damit** ich nur bezahlte Ware versende und offene Posten erkenne.

## Akzeptanzkriterien

Gegeben jeweils eine Bestellung mit Gesamtwert **100,00 €**.

1. **Nur ausstehende Zahlung**
   Gegeben eine Zahlung 100,00 € mit Status AUSSTEHEND,
   dann ist die Bestellung nicht vollständig bezahlt, offener Betrag 100,00 €.

2. **Vollständig bezahlt**
   Gegeben eine Zahlung 100,00 € mit Status ERFOLGREICH,
   dann ist die Bestellung vollständig bezahlt, offener Betrag 0,00 €.

3. **Teilzahlungen**
   Gegeben Zahlungen 40,00 € und 60,00 €, beide ERFOLGREICH,
   dann ist die Bestellung vollständig bezahlt.
   Gegeben nur die Zahlung über 40,00 € ist ERFOLGREICH,
   dann ist der offene Betrag 60,00 €.

… [gekürzt, 50 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-05.1-zahlungsstatus-pruefen.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-06.1-automatische-mahnpruefung.md
# US-06.1 Automatische Mahnprüfung für ein Kundenkonto

**Epic:** E06 Mahnwesen
**Entscheidungen:** D-03, D-05

## Story

**Als** Shop-Betreiber
**möchte ich**, dass für ein Kundenkonto automatisch alle Bestellungen auf überfällige offene Beträge geprüft werden und der Kunde gemahnt wird,
**damit** Zahlungsrückstände ohne manuelle Arbeit eingefordert werden.

## Beispieldaten

Zahlungsziel 14 Tage, Mahnintervall 7 Tage. Kunde Anna, `anna@example.de`.

| Bestellung | Bestelldatum | Fällig am | Gesamtwert | erfolgreich bezahlt |
|---|---|---|---|---|
| B-1 | 01.10.2026 | 15.10.2026 | 50,00 € | 0,00 € |
| B-2 | 05.10.2026 | 19.10.2026 | 80,00 € | 30,00 € |
| B-3 | 10.10.2026 | 24.10.2026 | 20,00 € | 20,00 € |
| B-4 | 10.11.2026 | 24.11.2026 | 40,00 € | 0,00 € |

## Akzeptanzkriterien

1. **Eine Mahnung mit jedem offenen Betrag**
   Wenn die Mahnprüfung für Anna mit Stichtag 01.11.2026 läuft,
   dann erhält `anna@example.de` **genau eine** Benachrichtigung, die auflistet:
   - B-1: offen 50,00 €
   - B-2: offen 50,00 €
… [gekürzt, 61 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-06.1-automatische-mahnpruefung.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/stories/US-08.1-gutschein-einloesen.md
# US-08.1 Gutschein bei der Bestellung einlösen

**Epic:** E08 Gutscheine & Aktionen
**Entscheidungen:** D-01, D-04, D-11

## Story

**Als** Kunde
**möchte ich** beim Bestellen einen Gutscheincode eingeben,
**damit** ich den Rabatt der Aktion erhalte.

## Beispieldaten

| Code | Rabattart | Rabattwert | Ablaufdatum |
|---|---|---|---|
| HERBST10 | PROZENT | 10 | 30.11.2026 |
| MINUS15 | FESTBETRAG | 15,00 € | 31.12.2026 |
| SOMMER20 | PROZENT | 20 | 31.08.2026 |

## Akzeptanzkriterien

1. **Prozentrabatt**
   Gegeben ein Warenwert von 55,00 €,
   wenn ich am 12.11.2026 `HERBST10` einlöse,
   dann beträgt der Rabatt 5,50 € und der Gesamtwert 49,50 €.

2. **Festbetrag**
   Gegeben ein Warenwert von 55,00 €,
   wenn ich `MINUS15` einlöse,
… [gekürzt, 67 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/stories/US-08.1-gutschein-einloesen.md (file state is current in your context — no need to Read it back)
```

### ● Claude

Ordner `/home/mschachner/Projects/bestellsystem/` ist angelegt. Darin: alle Lücken aufgelöst, 9 Epics und 7 ausformulierte User Stories. Kein Git-Repo, nichts committet.

```
bestellsystem/
├── README.md                  Überblick, Akteure, Epic-Tabelle, Reihenfolge
├── docs/
│   ├── 00-anforderung.md      dein Originaltext
│   ├── 01-entscheidungen.md   D-01 bis D-13 (verbindlich)
│   └── 02-domaenenmodell.md   Mermaid-Klassendiagramm, Formeln, Invarianten
├── epics/                     E00 bis E08
└── stories/                   US-01.1 … US-08.1
```

## Wichtigste Entscheidungen

| # | Lücke | Lösung |
|---|---|---|
| D-01 | Gesamtwert speichern oder berechnen? | Wird berechnet: `max(0, Warenwert − Rabatt)`. Stabil, weil Einzelpreis und Rabatt beim Bestellen eingefroren werden. |
| D-02 | "Mind. 1 Zahlung", trotzdem offene Beträge | Mindestens eine Zahlung bleibt Pflicht. Beim Checkout wird eine Zahlung `AUSSTEHEND` angelegt; nur `ERFOLGREICH` zählt. |
| D-03 | Ab wann ist man im Rückstand? | Fällig 14 Tage nach Bestellung. Neue Entität **Mahnung**, damit niemand doppelt gemahnt wird (höchstens alle 7 Tage). Eine E-Mail pro Kunde listet jede überfällige Bestellung auf. |
| D-04 | Rabattwert unklar | Gutschein ist `PROZENT` oder `FESTBETRAG`. Ablaufdatum wird beim Checkout geprüft und zählt inklusive. Der Rabattbetrag wird eingefroren. |
| D-05 | Erstattungen | Buchungsart `ZAHLUNG` oder `ERSTATTUNG`, Betrag immer positiv. |
| D-06 | Liefer- vs. Rechnungsadresse | Zwei Kennzeichen je Adresse, eine Adresse darf beides sein. Die letzte Lieferadresse lässt sich nicht löschen. |
| D-08 | Adress-Snapshot | Die Adresse wird in die Bestellung kopiert, keine Referenz aufs Adressbuch. |
| D-09 | Lagerbestand | Sinkt beim Checkout in derselben Transaktion. Reicht der Bestand für eine Position nicht, wird die ganze Bestellung abgelehnt. |
| D-11 | Geld | Ganzzahlige Cent, nur EUR, nie Gleitkomma. |

Die übrigen Entscheidungen (D-07, D-10, D-12, D-13) betreffen Merkliste, Positionen, Versandstatus und Kennungen und stehen in `docs/01-entscheidungen.md`.

## Epics und Stories

| Epic | Ausformulierte Story |
|---|---|
| E00 Domänenkern & Plattform | keine (technisch) |
| E01 Kundenkonto & Adressbuch | US-01.1 Registrierung mit Lieferadresse |
| E02 Katalog & Lagerbestand | US-02.1 Verfügbarkeit prüfen |
| E03 Merkliste | US-03.1 Produkt merken und abrufen |
| E04 Bestellung & Checkout | US-04.1 Bestellung aufgeben |
| E05 Zahlungen & Erstattungen | US-05.1 Vollständig bezahlt? |
| E06 Mahnwesen | US-06.1 Automatische Mahnprüfung |
| E07 Versand | nur Kandidaten (nach dem MVP) |
| E08 Gutscheine | US-08.1 Gutschein einlösen |

Ausformuliert habe ich die Stories mit der eigentlichen Logik. Alle haben Akzeptanzkriterien im Format Gegeben/Wenn/Dann mit konkreten Zahlen, du kannst sie also direkt als Testfälle übernehmen. Die übrigen Stories stehen als Kandidaten in den Epic-Dateien.

**Noch offen, bevor du codest:** Tech-Stack und Oberfläche. Außerdem habe ich vorläufig festgelegt, dass Bestellungen keine Rechnungsadresse und kein Storno haben. Falls du beides brauchst, ändere zuerst die Entscheidungen.

### ❯ Nutzer

> Würdest du das selbst mit dem goal-skill oder dem feature-dev skill lösen?

### ● Claude

Keinen von beiden für das ganze Projekt. Einen goal-Skill sehe ich in dieser Sitzung übrigens nicht; verfügbar sind `feature-dev`, `ralph-loop` und `pseudocode`. Falls du einen goal-Skill woanders installiert hast, sag mir, was er macht.

## Warum nicht `feature-dev` für alles

`feature-dev` ist für Features in einer **bestehenden** Codebasis gebaut. Es durchsucht zuerst den vorhandenen Code nach Mustern, entwirft dann eine Architektur passend zum Bestand und prüft am Ende das Ergebnis.

Hier gibt es noch keinen Code, die Suche läuft also ins Leere. Den Entwurf haben wir mit den Entscheidungen und dem Domänenmodell schon gemacht. Du würdest Rechenzeit für Arbeit bezahlen, die erledigt ist.

## Was ich machen würde

1. **Tech-Stack festlegen** (Sprache, Framework, Datenbank). Das bleibt deine Entscheidung, kein Skill nimmt sie dir ab.
2. **E00 direkt in einer normalen Sitzung** aufsetzen: Projektgerüst, Geld-Typ, Tests, CI. Das ist klar umrissen und braucht keinen Skill.
3. **Domänenkern Story für Story testgetrieben** bauen, ebenfalls direkt: US-02.1, US-05.1, US-08.1, US-04.1, US-06.1. Die Akzeptanzkriterien sind schon fertige Testfälle, da ist der Ablauf jedes Mal gleich: Test schreiben, rot, Code, grün.
4. **Ab E04/E06 kann sich `feature-dev` lohnen.** Dann gibt es Code, an dem sich die Architektur orientieren kann, und die Features greifen über mehrere Komponenten (Checkout-Transaktion, Mahnlauf mit E-Mail-Versand). Die Prüfung am Ende fängt dort echte Fehler.
5. **`ralph-loop` nur für die mechanische Fleißarbeit**, zum Beispiel "setz alle Kandidaten-Stories aus E01 um, bis die Tests grün sind". Das funktioniert nur, wenn eindeutig feststeht, wann es fertig ist. Für Entwurfsfragen taugt es nicht.

Soll ich mit E00 anfangen? Dann sag mir zuerst Sprache und Framework.

### ❯ Nutzer

> Claude hat zwei features die beim codieren hilfreich wären - goal und feature-dev. Welches davon würdest du hier benutzen?

### ● Claude

**Jetzt `feature-dev`, später `goal`.** Muss es eins sein, dann `feature-dev`.

## Warum jetzt feature-dev

`feature-dev` arbeitet in Phasen: Anforderung verstehen, Code erkunden, Rückfragen stellen, Architektur entwerfen, umsetzen, prüfen. Das passt, weil noch Entscheidungen offen sind:
- **Tech-Stack und Oberfläche** sind nicht festgelegt. Genau dafür sind die Phasen Rückfragen und Architektur da.
- **E00 (Projektgerüst, Geld-Typ, Persistenz, Uhr, E-Mail-Schnittstelle)** ist Architekturarbeit, kein Abarbeiten.
- Die Prüfphase am Ende hält das Projekt von Anfang an sauber.

Ein Haken: Die Phase "Code erkunden" bringt wenig, weil noch kein Code existiert. Sie geht schnell durch, das ist kein Problem.

## Wann goal

Soweit ich `goal` kenne, setzt du ein Abbruchkriterium und Claude arbeitet selbstständig weiter, bis es erfüllt ist. Das lohnt sich erst, wenn das Ziel **eindeutig prüfbar** ist und keine Designfragen mehr offen sind. Bei dir trifft das auf die Stories zu, denn die Akzeptanzkriterien sind schon konkrete Testfälle:

```
/goal Alle Akzeptanzkriterien aus stories/US-05.1 sind als Tests umgesetzt und grün
```

Ohne Architektur würde `goal` einfach drauflos bauen und dabei selbst den Stack wählen. Das wäre eine Entscheidung, die du treffen solltest.

## Ablauf

1. `feature-dev` für **E00 plus Stack-Entscheidung**, dann den ersten Durchstich: **US-02.1** (Verfügbarkeit, reine Logik) und **US-04.1** (Checkout, berührt Transaktion und Persistenz).
2. Danach `goal` pro Story für die reine Logik: **US-05.1** (bezahlt?), **US-08.1** (Gutschein), **US-06.1** (Mahnprüfung).
3. Zurück zu `feature-dev` bei jedem Epic, das wieder Designfragen aufwirft, zum Beispiel **E07** Versand oder Storno.

Faustregel: Ist das Ziel offen, nimm `feature-dev`. Ist es klar und prüfbar, nimm `goal`.

### ❯ Nutzer

> Wir wollen daraus eine Full-Stack Applikation bauen mit Springboot, Datenbank, Hibernate, REST-API und Frontend/Angular. Anschließend eine Dokumentation. Kann Feature-Dev das in einem Schwung, oder sollte man es in Schritten ausführen - und wenn ja, in welchen Schritten?

### ● Claude

**In Schritten.** `feature-dev` kann das nicht sinnvoll in einem Durchgang.

## Warum nicht in einem Schwung

- **Kontext:** Mehrere Epics über zwei Schichten (Backend, Frontend), dazu Tests und Doku, sind zu viel für eine Sitzung. Gegen Ende wird die Arbeit flacher, frühe Entscheidungen gehen verloren.
- **Prüfpunkte:** `feature-dev` legt dir die Architektur zur Freigabe vor. Bei einem Riesenpaket kannst du sie kaum sinnvoll prüfen.
- **Fehler pflanzen sich fort:** Ist das Fundament schief (Geld-Typ, Entities, API-Stil), baut jedes Epic darauf weiter.
- **Grüner Zwischenstand:** Nach jedem Schritt sollen die Tests laufen und committet sein, damit es einen sauberen Punkt zum Zurückgehen gibt.

## Schritt 0: vorher festlegen (ohne feature-dev, ca. 30 Min.)

| Thema | Empfehlung |
|---|---|
| Versionen | Java 21, Spring Boot 3.x, Angular (aktuelle LTS) |
| Datenbank | PostgreSQL; Flyway für Migrationen; Testcontainers für Tests |
| Struktur | Ein Repo mit `backend/`, `frontend/`, `docs/` |
| API | REST plus OpenAPI (springdoc), daraus den Angular-Client generieren (openapi-generator) |
| Fehlerformat | `ProblemDetail` (RFC 9457) |
| **Login** | Steht nicht in der Anforderung, aber "meine Merkliste" braucht einen eingeloggten Kunden. Für den MVP reicht die Kunden-ID im Pfad; Spring Security als eigenes Epic später. |
| Regeln | `CLAUDE.md` im Repo: Konventionen, Verweis auf `docs/01-entscheidungen.md`, Testpflicht |

Die `CLAUDE.md` ist entscheidend. Jeder spätere Schritt startet in einer frischen Sitzung und liest von dort die Regeln.

## Die Schritte (je ein feature-dev-Lauf, vorher `/clear`)

| # | Schritt | Inhalt | Warum hier |
|---|---|---|---|
| 1 | **Fundament Backend** (E00) | Spring-Boot-Gerüst, `Geld` und `Anschrift` als `@Embeddable`, Flyway, Testcontainers, Fehlerbehandlung, OpenAPI, CI | Alles baut darauf auf |
| 2 | **Fundament Frontend + Katalog** (E02) | Angular-Gerüst, Routing, Layout, generierter API-Client; Produkte anlegen und anzeigen samt Verfügbarkeit | Erster Durchstich durch alle Schichten, beweist die Kette DB → REST → Angular |
| 3 | **Kunde & Adressbuch** (E01) | Registrierung, Adressen, Invariante "≥ 1 Lieferadresse" | Voraussetzung für Bestellungen |
| 4a | **Checkout Backend** (E04 + E08) | Bestellung, Positionen, Snapshot, Bestandsreduktion, Gutschein, erste Zahlung | Größter und riskantester Teil, daher Backend und Frontend getrennt |
| 4b | **Checkout Frontend** | Warenkorb in der Oberfläche, Adresswahl, Gutscheinfeld, Bestellübersicht | |
| 5 | **Zahlungen** (E05) | Statuswechsel, Erstattung, `istVollstaendigBezahlt` | Hier passt `/goal` gut: US-05.1 ist reine Logik |
| 6 | **Mahnwesen** (E06) | `@Scheduled`-Lauf, Mail-Schnittstelle, Mailpit lokal | Baut auf 4 und 5 auf |
| 7 | **Merkliste** (E03) | Assoziations-Entity, Ansicht | Unabhängig, kann auch früher |
| 8 | **Versand** (E07) | Pakete, Statusübergänge | Nach dem MVP |
| 9 | **Dokumentation** | siehe unten | |

Prompt-Muster pro Schritt:

```
/feature-dev Setze Epic E04 (Backend) um. Grundlage: epics/E04-*.md,
stories/US-04.1-*.md, stories/US-08.1-*.md, docs/01-entscheidungen.md.
Akzeptanzkriterien = Integrationstests. Kein Frontend in diesem Schritt.
```

Nach jedem Schritt: Tests grün, `docs/ARCHITEKTUR.md` aktualisiert, committen.

## Hibernate-Fallen, die du in die CLAUDE.md schreiben solltest

- **Keine Entities in der REST-API**, nur DTOs.
- **`spring.jpa.open-in-view=false`**, sonst bleiben Lazy-Loading-Fehler versteckt.
- **N+1-Abfragen** bei Bestellung → Positionen → Produkt per `JOIN FETCH` oder Entity-Graph lösen.
- **Lagerbestand:** `@Version` (optimistisches Sperren). Sonst verkaufen zwei gleichzeitige Checkouts dieselbe letzte Lampe.
- **Merklisteneintrag** als eigene Entity mit zusammengesetztem Schlüssel (Kunde, Produkt), kein `@ManyToMany`, da sonst das Merkdatum keinen Platz hat.
- **Geld** nie als `double`, sondern `long` Cent oder `BigDecimal`.

## Dokumentation

Ein Teil entsteht **laufend**: Architekturdoku und Entscheidungen werden in jedem Schritt mitgepflegt, die API-Doku erzeugt OpenAPI automatisch. Am Ende ist `feature-dev` das falsche Werkzeug, denn es baut Features und schreibt keine Doku. Dafür besser ein eigener Auftrag, etwa: *"Erstelle eine arc42-Dokumentation aus dem Code und docs/"*. Dazu kommen README mit Startanleitung (Docker Compose), Entscheidungen als ADRs und ein kurzes Benutzerhandbuch.

Soll ich Schritt 0 vorbereiten, also die `CLAUDE.md` und einen Fahrplan mit den Schritten in `bestellsystem/` anlegen?

### ❯ Nutzer

> Erstelle die CLAUDE.md nach deinen Empfehlungen. Wie würdest du diese Schritte am besten hinterlegen, dass diese abgearbeitet werden können.

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/CLAUDE.md
# Bestellsystem

> Wird in jede Sitzung geladen. Eine Zeile pro Regel. Bei Widerspruch zwischen Regel und Auftrag: nachfragen, nicht raten.

## Überblick

Full-Stack-Bestellsystem für einen Onlineshop: Kunden, Adressbuch, Katalog, Merkliste, Bestellung, Zahlung, Mahnwesen, Versand, Gutscheine.
Spring Boot (REST, JPA/Hibernate) + PostgreSQL + Angular.

## Quellen der Wahrheit

Rangfolge bei Widerspruch: `docs/01-entscheidungen.md` > `stories/` > `epics/` > `docs/02-domaenenmodell.md` > `docs/00-anforderung.md`.

- Fachliche Lücke oder Widerspruch entdeckt → stoppen, nachfragen, dann zuerst eine neue Entscheidung `D-xx` in `docs/01-entscheidungen.md`, danach Code.
- Nie stillschweigend eine fachliche Regel erfinden.
- Akzeptanzkriterien einer Story sind verbindlich und werden 1:1 zu Tests.

## Arbeitsweise

- Fahrplan: `docs/fahrplan.md`. Ein Schritt pro Sitzung, in der angegebenen Reihenfolge.
- "nächster Schritt" = `/naechster-schritt`: obersten offenen Schritt des Fahrplans abarbeiten.
- Nur den Umfang des aktuellen Schritts bauen. Was später kommt, wird nicht vorgebaut.
- Schritt fertig = Tests grün, `docs/ARCHITEKTUR.md` aktuell, Fahrplan abgehakt, committet.

## Stack

- Backend: Java 21, Spring Boot 3.x, Maven (Wrapper `./mvnw`), Spring Data JPA / Hibernate, Flyway, springdoc-openapi, Bean Validation.
- Datenbank: PostgreSQL 16. Lokal via Docker Compose; Tests via Testcontainers (kein H2).
- Tests Backend: JUnit 5, AssertJ, Spring Boot Test, Testcontainers, MockMvc.
… [gekürzt, 136 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/CLAUDE.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

````
/home/mschachner/Projects/bestellsystem/docs/fahrplan.md
# Fahrplan

Ein Schritt pro Sitzung, von oben nach unten. Start: neue Sitzung (oder `/clear`), dann `/naechster-schritt`.
Abgehakt wird erst, wenn **alle** "Fertig wenn"-Punkte erfüllt sind. Hinter dem Haken stehen Datum und Commit.

Werkzeug: **feature-dev** = Design offen, Architektur-Freigabe nötig · **goal** = Ziel eindeutig prüfbar, reine Logik.

---

## [ ] 0 Vorbereitung

**Werkzeug:** von Hand
**Ziel:** Repo und Rahmen stehen.

- [x] `CLAUDE.md` angelegt
- [ ] `git init`, `.gitignore` (Java, Node, `.env`, IDE)
- [ ] Paketname bestätigen (`de.bestellsystem` oder eigener)
- [ ] Docker und Java 21 und Node LTS lokal vorhanden

**Fertig wenn:** erster Commit mit Doku und `CLAUDE.md` existiert.

---

## [ ] 1 Fundament Backend (E00)

**Werkzeug:** feature-dev
**Eingaben:** `epics/E00-*.md`, `docs/01-entscheidungen.md` (D-08, D-11, D-13), `CLAUDE.md`

```
… [gekürzt, 203 Zeilen gesamt]
````

```
File created successfully at: /home/mschachner/Projects/bestellsystem/docs/fahrplan.md (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/.claude/skills/naechster-schritt/SKILL.md
---
name: naechster-schritt
description: Arbeitet den obersten offenen Schritt aus docs/fahrplan.md ab (mit feature-dev oder als goal-Vorschlag), prüft "Fertig wenn", hakt ab und committet. Verwenden bei "nächster Schritt", "weiter im Fahrplan", "naechster-schritt" oder /naechster-schritt.
---

# Nächster Schritt

1. **Schritt finden.** `docs/fahrplan.md` lesen. Oberster Schritt mit `## [ ]` ist dran. Alle früheren Schritte müssen `[x]` sein; sonst stoppen und melden.

2. **Kontext prüfen.** Läuft diese Sitzung schon länger (früherer Schritt bearbeitet), dem Nutzer `/clear` empfehlen und stoppen.

3. **Eingaben lesen.** Alle unter "Eingaben" genannten Dateien plus `CLAUDE.md` und `docs/ARCHITEKTUR.md` (falls vorhanden) lesen.

4. **Werkzeug wählen** nach der Zeile "Werkzeug":
   - `feature-dev` → Skill `feature-dev:feature-dev` mit dem Prompt aus dem Codeblock des Schritts aufrufen. Architektur-Phase vom Nutzer freigeben lassen, bevor implementiert wird.
   - `goal` → Den `/goal`-Befehl aus dem Codeblock dem Nutzer zum Ausführen anzeigen und stoppen (goal startet der Nutzer selbst).
   - `von Hand` / normaler Auftrag → Punkte direkt abarbeiten; destruktive oder unklare Punkte vorher fragen.

5. **Fertig-wenn prüfen.** Jeden Punkt unter "Fertig wenn" einzeln nachweisen (Befehl ausführen, Ausgabe nennen). Ein Punkt nicht erfüllt → nicht abhaken, offenen Rest melden.

6. **Abschließen.**
   - `docs/ARCHITEKTUR.md` aktualisieren.
   - Im Fahrplan `## [ ]` → `## [x]` und hinter den Titel `(JJJJ-MM-TT, <kurzer Commit-Hash>)` setzen.
   - Geänderte Dateien gezielt stagen (nie `git add -A`), committen nach `CLAUDE.md` → Git.

7. **Melden.** Was gebaut wurde, Testergebnis, offene Punkte, nächster Schritt.
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/.claude/skills/naechster-schritt/SKILL.md (file state is current in your context — no need to Read it back)
```

**⏺ Edit**

```
/home/mschachner/Projects/bestellsystem/README.md
--- alt
| `docs/02-domaenenmodell.md` | UML-Klassendiagramm (Mermaid), Invarianten, Formeln |
+++ neu
| `docs/02-domaenenmodell.md` | UML-Klassendiagramm (Mermaid), Invarianten, Formeln |
| `docs/fahrplan.md` | Umsetzungsschritte 0 bis 9 mit Prompt und "Fertig wenn" |
| `CLAUDE.md` | Regeln für Claude Code (Stack, Struktur, Tests, Hibernate) |
| `.claude/skills/naechster-schritt/` | `/naechster-schritt`: obersten offenen Schritt abarbeiten |
```

```
The file /home/mschachner/Projects/bestellsystem/README.md has been updated successfully. (file state is current in your context — no need to Read it back)
```

### ● Claude

Die `CLAUDE.md` ist angelegt. Für die Schritte habe ich zusätzlich einen Fahrplan und einen eigenen Befehl angelegt, der ihn abarbeitet. Noch ist nichts committet, der Ordner ist auch noch kein Git-Repo.

## Was angelegt ist

| Datei | Inhalt |
|---|---|
| `CLAUDE.md` | Rangfolge der Fachdokumente, Stack, Befehle, Paketstruktur, Benennung, Domänenregeln, Hibernate-Regeln, REST-Regeln mit Statuscodes, Frontend, Testregeln, Doku, Git, was nicht dazugehört |
| `docs/fahrplan.md` | Schritte 0 bis 9 mit Checkbox, Werkzeug, Eingaben, fertigem Prompt und "Fertig wenn" |
| `.claude/skills/naechster-schritt/SKILL.md` | Der Befehl `/naechster-schritt` |
| `README.md` | Um die drei Einträge ergänzt |

## So werden die Schritte hinterlegt

Ich empfehle drei Ebenen:

1. **Fahrplan als einzige Quelle für den Fortschritt.** Jeder Schritt ist eine Überschrift mit `[ ]` oder `[x]`. Ein Schritt gilt erst als erledigt, wenn jeder Punkt unter "Fertig wenn" nachgewiesen ist. Dann kommen Datum und Commit-Hash dahinter, damit man jeden Stand wiederfindet.
2. **Fertiger Prompt pro Schritt.** Jeder Lauf startet in einer frischen Sitzung und braucht keine Erinnerung an frühere. Der Prompt nennt die Story-Dateien, die Entscheidungen liefert die `CLAUDE.md`.
3. **Befehl `/naechster-schritt`.** Er nimmt den obersten offenen Schritt und prüft, ob alle früheren erledigt sind. Dann ruft er `feature-dev` mit dem hinterlegten Prompt auf. Bei Schritten für `goal` zeigt er dir nur den Befehl, den startest du selbst. Am Ende prüft er "Fertig wenn", hakt ab und committet.

## Ablauf

```
neue Sitzung → /naechster-schritt → Architektur freigeben → Umsetzung → Prüfung → Haken + Commit → /clear → …
```

## Bevor es losgeht (Schritt 0, von dir)

- `git init` und eine `.gitignore` anlegen.
- Paketname bestätigen. Im Moment steht dort der Platzhalter `de.bestellsystem`.
- Prüfen, ob Docker, Java 21 und Node in der LTS-Version installiert sind.

## Gut zu wissen

Einige Kandidaten-Stories (US-01.2/3, US-04.2, US-06.2, US-03.2, US-07.x) sind bewusst noch nicht ausformuliert. Der jeweilige Prompt lässt Claude deshalb erst die Akzeptanzkriterien mit dir klären und unter `stories/` ablegen, bevor gebaut wird.

Soll ich Schritt 0 erledigen, also `git init`, die `.gitignore` und den ersten Commit?

### ❯ Nutzer

> Du kannst das git init und .gitignore anlegen

### ❯ Nutzer

> Du kannst das git init und .gitignore anlegen und den Paketnamen bestätigen und alles prüfen

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && git rev-parse --is-inside-work-tree 2>&1; echo "---java"; java -version 2>&1 | head -3; echo "---javac"; javac -version 2>&1; echo "---mvn"; mvn -v 2>&1 | head -1; echo "---node"; node -v 2>&1; npm -v 2>&1; echo "---ng"; ng version 2>&1 | head -3; echo "---docker"; docker --version 2>&1; docker compose version 2>&1; docker info --format '{{.ServerVersion}}' 2>&1 | head -2; echo "---git"; git --version; git config --global user.name; echo "---sdkman/jenv"; ls ~/.sdkman/candidates/java 2>&1; ls /usr/lib/jvm 2>&1
```

```
fatal: not a git repository (or any parent up to mount point /)
Stopping at filesystem boundary (GIT_DISCOVERY_ACROSS_FILESYSTEM not set).
---java
openjdk version "21.0.13.0.3" 2026-10-24
OpenJDK Runtime Environment (build 21.0.13.0.3+1-1-24.04.1-Ubuntu)
OpenJDK 64-Bit Server VM (build 21.0.13.0.3+1-1-24.04.1-Ubuntu, mixed mode, sharing)
---javac
javac 21.0.13.0.3
---mvn
[1mApache Maven 3.8.7[m
---node
v22.20.0
11.6.3
---ng
zsh: command not found: ng
---docker
Docker version 29.9.3, build 5457199
Docker Compose version v5.7.0
29.9.3
---git
git version 2.43.0
jellybrigade
---sdkman/jenv
ls: cannot access '/home/mschachner/.sdkman/candidates/java': No such file or directory
default-java
java-1.11.0-openjdk-amd64
java-11-openjdk-amd64
java-1.21.0-openjdk-amd64
java-21-openjdk-amd64
openjdk-21
Shell cwd was reset to /home/mschachner/Projects/Canon
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/.gitignore
# Umgebung und Geheimnisse
.env
.env.*
!.env.example
*.pem
*.key

# Betriebssystem
.DS_Store
Thumbs.db

# IDEs
.idea/
*.iml
.vscode/*
!.vscode/extensions.json
.project
.classpath
.settings/
.factorypath
*.swp

# Claude Code (lokale Einstellungen; Skills und CLAUDE.md werden versioniert)
.claude/settings.local.json

# Backend (Maven / Java)
backend/target/
backend/.mvn/wrapper/maven-wrapper.jar
*.class
… [gekürzt, 48 Zeilen gesamt]
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/.gitignore (file state is current in your context — no need to Read it back)
```

**⏺ Write**

```
/home/mschachner/Projects/bestellsystem/.gitattributes
* text=auto eol=lf
*.cmd text eol=crlf
*.bat text eol=crlf
*.jar binary
*.png binary
*.jpg binary
*.ico binary
```

```
File created successfully at: /home/mschachner/Projects/bestellsystem/.gitattributes (file state is current in your context — no need to Read it back)
```

### ● Claude

Paketname festhalten, Fahrplan und Befehle aktualisieren:

**⏺ Edit**

```
/home/mschachner/Projects/bestellsystem/CLAUDE.md
--- alt
- Backend nach Fachlichkeit geschnitten:
+++ neu
- Maven: `groupId` `de.bestellsystem`, `artifactId` `bestellsystem-backend`; Basispaket `de.bestellsystem`.
- Backend nach Fachlichkeit geschnitten:
```

```
The file /home/mschachner/Projects/bestellsystem/CLAUDE.md has been updated successfully. (file state is current in your context — no need to Read it back)
```

**⏺ Edit**

```
/home/mschachner/Projects/bestellsystem/CLAUDE.md
--- alt
Befehle entstehen in Schritt 1 und 2; bis dahin existieren sie noch nicht.
+++ neu
Befehle entstehen in Schritt 1 und 2; bis dahin existieren sie noch nicht.

- Lokal vorhanden: Java 21, Maven 3.8, Node 22, Docker 29 mit Compose. Angular CLI ist nicht global installiert: immer `npx @angular/cli` bzw. die Projekt-Skripte nutzen.
- Backend immer über den Maven-Wrapper `./mvnw`, nie das System-`mvn` (Version 3.8 ist zu alt für aktuelle Plugins).
```

```
The file /home/mschachner/Projects/bestellsystem/CLAUDE.md has been updated successfully. (file state is current in your context — no need to Read it back)
```

**⏺ Edit**

```
/home/mschachner/Projects/bestellsystem/docs/fahrplan.md
--- alt
## [ ] 0 Vorbereitung

**Werkzeug:** von Hand
**Ziel:** Repo und Rahmen stehen.

- [x] `CLAUDE.md` angelegt
- [ ] `git init`, `.gitignore` (Java, Node, `.env`, IDE)
- [ ] Paketname bestätigen (`de.bestellsystem` oder eigener)
- [ ] Docker und Java 21 und Node LTS lokal vorhanden
+++ neu
## [x] 0 Vorbereitung (2026-11-12)

**Werkzeug:** von Hand
**Ziel:** Repo und Rahmen stehen.

- [x] `CLAUDE.md` angelegt
- [x] `git init`, `.gitignore` (Java, Node, `.env`, IDE), `.gitattributes` (LF)
- [x] Paketname bestätigt: `de.bestellsystem` (groupId), `bestellsystem-backend` (artifactId)
- [x] Geprüft: Java 21.0.13, Maven 3.8.7 (nur Wrapper nutzen), Node 22.20 / npm 11.6, Docker 29.9 + Compose 5.7, Daemon läuft; Angular CLI über `npx`
```

```
The file /home/mschachner/Projects/bestellsystem/docs/fahrplan.md has been updated successfully. (file state is current in your context — no need to Read it back)
```

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && git init -q -b main && echo "--- Links prüfen" && grep -rhoE '\]\(([^)#]+\.md)\)' --include='*.md' . | sed -E 's/\]\((.*)\)/\1/' | sort -u > /tmp/links.txt; for f in $(grep -rlE '\]\([^)#]+\.md\)' --include='*.md' .); do d=$(dirname "$f"); grep -oE '\]\([^)#]+\.md\)' "$f" | sed -E 's/\]\((.*)\)/\1/' | while read l; do [ -f "$d/$l" ] || echo "KAPUTT: $f -> $l"; done; done; echo "--- Story/Epic-Verweise"; for id in $(grep -rhoE 'US-[0-9]{2}\.[0-9]' README.md epics | sort -u); do ls stories/$id-* >/dev/null 2>&1 && echo "$id ok" || echo "$id nur Kandidat"; done | sort | uniq; echo "--- Status"; git status --short
```

```
--- Links prüfen
--- Story/Epic-Verweise
(eval):1: no matches found: stories/US-01.2-*
(eval):1: no matches found: stories/US-01.3-*
(eval):1: no matches found: stories/US-02.2-*
(eval):1: no matches found: stories/US-02.3-*
(eval):1: no matches found: stories/US-03.2-*
(eval):1: no matches found: stories/US-04.2-*
(eval):1: no matches found: stories/US-05.2-*
(eval):1: no matches found: stories/US-05.3-*
(eval):1: no matches found: stories/US-06.2-*
(eval):1: no matches found: stories/US-07.1-*
(eval):1: no matches found: stories/US-07.2-*
(eval):1: no matches found: stories/US-07.3-*
(eval):1: no matches found: stories/US-08.2-*
US-01.1 ok
US-01.2 nur Kandidat
US-01.3 nur Kandidat
US-02.1 ok
US-02.2 nur Kandidat
US-02.3 nur Kandidat
US-03.1 ok
US-03.2 nur Kandidat
US-04.1 ok
US-04.2 nur Kandidat
US-05.1 ok
US-05.2 nur Kandidat
US-05.3 nur Kandidat
US-06.1 ok
US-06.2 nur Kandidat
US-07.1 nur Kandidat
US-07.2 nur Kandidat
US-07.3 nur Kandidat
US-08.1 ok
US-08.2 nur Kandidat
--- Status
?? .claude/
?? .gitattributes
?? .gitignore
?? CLAUDE.md
… [gekürzt, 45 Zeilen gesamt]
```

### ● Claude

Links sind alle intakt, die 7 Stories existieren, die übrigen sind wie geplant Kandidaten. Jetzt der erste Commit:

**⏺ Bash**

```bash
cd /home/mschachner/Projects/bestellsystem && git add .gitignore .gitattributes CLAUDE.md README.md docs epics stories .claude/skills && git status --short && git commit -q -m "Add requirements, decisions, epics, stories and roadmap

Co-Authored-By: Claude Opus 5.5 <noreply@anthropic.com>" && git log --oneline && git status --short
```

```
A  .claude/skills/naechster-schritt/SKILL.md
A  .gitattributes
A  .gitignore
A  CLAUDE.md
A  README.md
A  docs/00-anforderung.md
A  docs/01-entscheidungen.md
A  docs/02-domaenenmodell.md
A  docs/fahrplan.md
A  epics/E00-domaenenkern-plattform.md
A  epics/E01-kundenkonto-adressbuch.md
A  epics/E02-produktkatalog-lagerbestand.md
A  epics/E03-merkliste.md
A  epics/E04-bestellung-checkout.md
A  epics/E05-zahlungen-erstattungen.md
A  epics/E06-mahnwesen.md
A  epics/E07-versand-sendungsverfolgung.md
A  epics/E08-gutscheine-aktionen.md
A  stories/US-01.1-registrierung-mit-lieferadresse.md
A  stories/US-02.1-verfuegbarkeit-pruefen.md
A  stories/US-03.1-produkt-merken.md
A  stories/US-04.1-bestellung-aufgeben.md
A  stories/US-05.1-zahlungsstatus-pruefen.md
A  stories/US-06.1-automatische-mahnpruefung.md
A  stories/US-08.1-gutschein-einloesen.md
69d47d6 Add requirements, decisions, epics, stories and roadmap
Shell cwd was reset to /home/mschachner/Projects/Canon
```

### ● Claude

Schritt 0 ist fertig: Repo angelegt, Paketname festgelegt, Werkzeuge geprüft. Der erste Commit ist `69d47d6` auf `main`, nur lokal und nicht gepusht. Das Arbeitsverzeichnis ist sauber.

## Was gemacht ist

- **Git:** Repo mit Branch `main` angelegt. Beim Stagen habe ich jede Datei einzeln angegeben.
- **`.gitignore`:** Schließt Geheimnisse (`.env`, Schlüssel), IDE-Dateien, `backend/target/`, `node_modules`, `dist` und `.angular` aus. Außerdem `.claude/settings.local.json`; die Skills werden dagegen versioniert.
- **`.gitattributes`:** Erzwingt LF-Zeilenenden, sonst bricht `./mvnw` mit Windows-Zeilenenden.
- **Paketname:** `de.bestellsystem` als groupId und Basispaket, artifactId `bestellsystem-backend`. Steht jetzt in der `CLAUDE.md`.
- **Fahrplan:** Schritt 0 ist mit Datum abgehakt. Den Commit-Hash habe ich nicht eingetragen, das hätte einen weiteren Commit gebraucht.

## Prüfung

| Werkzeug | Gefunden | Bewertung |
|---|---|---|
| Java | OpenJDK 21.0.13 | passt |
| Maven | 3.8.7 | zu alt für aktuelle Spring-Boot-Plugins. In der `CLAUDE.md` steht deshalb: nur `./mvnw` benutzen |
| Node / npm | 22.20 / 11.6 | reicht für aktuelles Angular |
| Angular CLI | nicht global installiert | über `npx @angular/cli`, steht in der `CLAUDE.md` |
| Docker | 29.9, Compose 5.7, Docker läuft | Testcontainers sollten damit funktionieren |
| Doku-Links | alle intakt | 7 Stories vorhanden, die übrigen 13 sind wie geplant Kandidaten |

Ich habe meine eigene Regel nicht eingehalten: Hinter dem Commit-Betreff steht eine `Co-Authored-By`-Zeile, obwohl die `CLAUDE.md` nur einen Betreff vorsieht. Willst du die Zeile weg haben, ändere ich den Commit, das geht hier gefahrlos, weil noch nichts gepusht ist.

## Weiter

Neue Sitzung im Ordner `bestellsystem` starten, nicht in `Canon`, damit die richtige `CLAUDE.md` geladen wird:

```bash
cd ~/Projects/bestellsystem && claude
```

Dann `/naechster-schritt` eingeben. Der Befehl startet `feature-dev` für Schritt 1 (Backend-Fundament).

### ❯ Nutzer

> /export

### ❯ Nutzer

> [Ausgabe] Conversation exported to: /home/mschachner/Projects/Canon/2026-11-12-094326-local-command-caveatthe-command-below-was-run-d.txt

