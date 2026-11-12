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

**Entscheidung:** Die Multiplizität **1..\* bleibt**. Beim Checkout wird sofort eine Zahlung mit Status `AUSSTEHEND` und der gewählten Zahlungsart angelegt. Nur Buchungen mit Status `ERFOLGREICH` zählen als geleistet.

**Begründung:** Erfüllt den Originaltext wörtlich und bildet die Realität ab: Die Zahlungsabsicht existiert ab Bestellung, das Geld kommt später (Rechnung, Vorkasse) oder gar nicht (fehlgeschlagen).

---

## D-03 Zahlungsrückstand braucht eine Fälligkeit

**Problem:** Ohne Fälligkeitsdatum ist jede gerade aufgegebene, noch unbezahlte Bestellung sofort "im Rückstand".

**Entscheidung:**
- Bestellung erhält `faelligAm = bestelldatum + Zahlungsziel`. Das Zahlungsziel ist konfigurierbar, Standard **14 Tage**.
- **Rückstand** liegt vor, wenn `offenerBetrag > 0` **und** `stichtag > faelligAm`.
- Neue Entität **Mahnung** (je Bestellung: Datum, gemahnter Betrag). Sie verhindert, dass derselbe Kunde täglich gemahnt wird.
- Eine Bestellung wird höchstens **einmal pro Mahnintervall** gemahnt (Standard **7 Tage**, konfigurierbar).
- Pro Mahnprüfung erhält der Kunde **eine** E-Mail an seine hinterlegte Adresse, die **jede** überfällige Bestellung mit ihrem jeweils offenen Betrag auflistet ("mit dem jeweils offenen Betrag").

**Nicht im Umfang:** Mahnstufen, Mahngebühren, Verzugszinsen, Inkasso.

---

## D-04 Gutschein: Art des Rabatts und Wirkung

**Problem:** "Rabattwert" ist unklar (Prozent oder Euro?), ebenso, ob und wann der Rabatt in den Gesamtwert einfließt und wann das Ablaufdatum geprüft wird.

**Entscheidung:**
- Gutschein erhält `rabattArt ∈ {PROZENT, FESTBETRAG}`.
  - `PROZENT`: 0 < rabattWert ≤ 100, bezogen auf den Warenwert.
  - `FESTBETRAG`: rabattWert > 0, in Euro.
- Der Rabatt gilt auf den **Warenwert**; der Gesamtwert wird nie negativ.
- Gültigkeit wird **beim Checkout** geprüft: gültig, solange `bestelldatum ≤ ablaufdatum` (Ablaufdatum inklusive).
- Der berechnete Rabatt wird als `rabattBetrag` **in der Bestellung eingefroren**. Spätere Änderungen am Gutschein ändern keine bestehende Bestellung.
- Gutscheincode ist eindeutig und wird ohne Beachtung der Groß-/Kleinschreibung verglichen.
- Ein Gutschein darf beliebig oft verwendet werden (laut Text). Nutzungslimits sind nicht im Umfang.

---

## D-05 Erstattungen und Transaktionsstatus

**Problem:** Erstattungen werden erwähnt, aber nicht modelliert. Unklar, welche Zahlungen zur "Summe aller geleisteten Zahlungen" zählen.

**Entscheidung:**
- Zahlung erhält `buchungsart ∈ {ZAHLUNG, ERSTATTUNG}`. Der Betrag ist **immer positiv**; die Buchungsart bestimmt das Vorzeichen.
- `transaktionsstatus ∈ {AUSSTEHEND, ERFOLGREICH, FEHLGESCHLAGEN, STORNIERT}`.
- Es gilt:

```
bezahlteSumme          = Σ ERFOLGREICH-ZAHLUNG − Σ ERFOLGREICH-ERSTATTUNG
offenerBetrag          = max(0, gesamtwert − bezahlteSumme)
istVollstaendigBezahlt = bezahlteSumme ≥ gesamtwert
```

- Überzahlung ist erlaubt (offenerBetrag = 0), die Rückzahlung erfolgt als Erstattung.

---

## D-06 Liefer- und Rechnungsadressen

**Problem:** Wie wird zwischen Liefer- und Rechnungsadresse unterschieden? Wie wird "mindestens eine Lieferadresse" erzwungen?

**Entscheidung:**
- Adresse erhält zwei Kennzeichen: `istLieferadresse` und `istRechnungsadresse`. Mindestens eines ist wahr; eine Adresse darf beides sein.
- **Invariante:** Ein Kunde hat stets mindestens eine Adresse mit `istLieferadresse = wahr`.
  - Die Registrierung verlangt eine Lieferadresse.
  - Das Löschen der letzten Lieferadresse oder das Entfernen ihres Kennzeichens wird abgelehnt.
- Adressen sind Teil des Kunden (Komposition): Wird der Kunde gelöscht, werden seine Adressen gelöscht.

**Nicht im Umfang:** Rechnungsadresse in der Bestellung (der Text verlangt nur den Lieferadress-Snapshot). Leicht nachrüstbar über D-08.

---

## D-07 Merkliste als Assoziationsklasse

**Entscheidung:**
- Kunde ↔ Produkt ist eine \*-zu-\*-Beziehung mit der Assoziationsklasse **Merklisteneintrag** (`hinzugefuegtAm`, Pflicht).
- Eindeutig pro (Kunde, Produkt). Erneutes Merken ist ein No-op und behält das ursprüngliche Datum.
- "Aktuell beobachtete Artikel abrufen" liefert die Produkte der Merkliste, neueste zuerst, inklusive aktueller Verfügbarkeit.
- Wird ein Produkt aus dem Katalog entfernt, verschwinden seine Merklisteneinträge.

---

## D-08 Lieferadress-Snapshot

**Entscheidung:** Der Snapshot ist ein **Wertobjekt** `Anschrift` (Straße, Stadt, PLZ, Land), das beim Checkout aus der gewählten Lieferadresse **kopiert** und in die Bestellung eingebettet wird. Es gibt **keine Referenz** auf `Adresse`. Ändern oder Löschen einer Adresse im Adressbuch beeinflusst bestehende Bestellungen nicht.

---

## D-09 Lagerbestand und Verfügbarkeit

**Problem:** Wann sinkt der Lagerbestand? Was heißt "verfügbar"?

**Entscheidung:**
- `istVerfuegbar()` ⇔ `lagerbestand > 0`; zusätzlich `istVerfuegbar(menge)` ⇔ `lagerbestand ≥ menge`.
- Der Lagerbestand wird **beim Checkout** reduziert, atomar zusammen mit dem Anlegen der Bestellung (eine Transaktion). Reicht der Bestand für eine Position nicht, wird die gesamte Bestellung abgelehnt.
- Lagerbestand ist nie negativ.

**Nicht im Umfang:** Reservierung im Warenkorb, Storno mit Bestandsrückbuchung (eigenes Epic, wenn benötigt).

---

## D-10 Positionen: Preis, Zeilennummer, Duplikate

**Entscheidung:**
- `einzelpreis` = `listenpreis` des Produkts zum Checkout-Zeitpunkt, danach unveränderlich.
- `zeilennummer` läuft je Bestellung lückenlos ab 1.
- Dasselbe Produkt erscheint höchstens einmal je Bestellung; Mehrfachauswahl wird zu einer Position mit summierter Menge zusammengefasst.
- `menge ≥ 1`.
- `zwischenbetrag = menge × einzelpreis`.

---

## D-11 Geldbeträge und Währung

**Entscheidung:** Alle Beträge als **ganzzahlige Cent** (bzw. Dezimaltyp), **nie Gleitkomma**. Eine Währung: **EUR**. Prozentrabatte werden kaufmännisch auf ganze Cent gerundet.

---

## D-12 Versandpakete

**Entscheidung:**
- `lieferstatus ∈ {ANGELEGT, UEBERGEBEN, IN_ZUSTELLUNG, ZUGESTELLT, RETOURE, VERLOREN}`, Übergänge nur vorwärts (außer `RETOURE`/`VERLOREN` als Endzustände).
- Sendungsverfolgungsnummer ist eindeutig je Logistikdienstleister.
- Pakete verweisen **nicht** auf einzelne Positionen (Text verlangt das nicht).

**Nicht im Umfang:** Zuordnung "welche Position in welchem Paket", Anbindung an Tracking-APIs der Dienstleister.

---

## D-13 Kennungen und Eindeutigkeit

**Entscheidung:**
- Technische IDs (Kunde, Adresse, Produkt, Zahlung) sind UUIDs.
- `bestellnummer` ist eine fachliche, lesbare, eindeutige Nummer (z. B. `B-2026-000123`).
- E-Mail-Adresse ist eindeutig, Vergleich ohne Groß-/Kleinschreibung, getrimmt.

---

## D-14 Zähler der Bestellnummer

**Problem:** D-13 gibt das Format `B-JJJJ-NNNNNN` vor, aber nicht, ob der Zähler je Jahr neu beginnt.

**Entscheidung:**
- Der Zähler ist **global** (eine Datenbanksequenz) und beginnt im neuen Jahr **nicht** wieder bei 1.
- Das Jahr im Präfix ist das Jahr des **Bestelldatums**.
- Der Zähler ist mindestens sechsstellig mit führenden Nullen; größere Werte werden nicht abgeschnitten.
- Lücken (z. B. nach abgebrochenem Checkout) sind zulässig; verlangt ist nur Eindeutigkeit.

**Begründung:** Nebenläufig sicher ohne eigene Sperrlogik, auch über mehrere Instanzen.

---

## D-15 Produktdaten beim Anlegen

**Problem:** Das Domänenmodell nennt `titel`, `listenpreis` und `lagerbestand`, aber keine Regeln für gültige Werte.

**Entscheidung:**
- `titel` wird getrimmt, ist Pflicht und höchstens **200 Zeichen** lang. Er muss **nicht eindeutig** sein.
- `listenpreis` ≥ 0 Cent (0 € ist erlaubt, z. B. Gratisartikel).
- `lagerbestand` ≥ 0 bereits beim Anlegen (I-7).
- Verstöße sind ungültige Eingaben (HTTP 400).

**Nicht im Umfang (Schritt 2):** Titel und Listenpreis ändern (US-02.2), Bestandsbuchungen mit Historie (US-02.3).

---

## D-16 Kundendaten, Adressbuch-Fehler und Nebenläufigkeit

**Problem:** D-06 und D-13 legen weder Feldregeln noch Statuscodes fest, und I-1 kann durch gleichzeitige Änderungen am Adressbuch verletzt werden.

**Entscheidung:**
- `vorname`, `nachname`: Pflicht, getrimmt, höchstens **100 Zeichen**.
- `email`: Pflicht, gültiges Format, höchstens **254 Zeichen**, gespeichert getrimmt und kleingeschrieben (D-13). Eindeutigkeit zusätzlich per Unique-Index in der Datenbank.
- Adresse: `strasse` ≤ 200, `stadt` ≤ 100, `postleitzahl` ≤ 20, `land` ISO-3166-Alpha-2.
- Die erste Adresse der Registrierung ist immer Lieferadresse; optional zugleich Rechnungsadresse.
- Gleiche Anschriften dürfen mehrfach im Adressbuch stehen; keine Höchstzahl.
- Statuscodes:
  - 400: fehlende oder ungültige Felder, keine Adresse bei der Registrierung, kein Kennzeichen gesetzt (I-2).
  - 409: E-Mail bereits vergeben, auch bei gleichzeitiger Registrierung.
  - 422: letzte Lieferadresse löschen oder ihr das Lieferkennzeichen entziehen (I-1).
  - 404: unbekannter Kunde, unbekannte oder fremde Adresse.
- `Kunde` ist Aggregatwurzel mit `@Version`; jede Änderung am Adressbuch erhöht die Version. Gleichzeitige Änderungen enden für den Verlierer mit 409 (Versionskonflikt), I-1 bleibt erhalten.

**Nicht im Umfang (Schritt 3):** Name oder E-Mail ändern, Konto löschen, Standardadresse.

---

## D-17 Checkout: Ablehnungen, Gutscheincode, erste Zahlung

**Problem:** US-04.1 und US-08.1 legen fest, *dass* abgelehnt wird, aber nicht mit welchem Statuscode. Offen sind außerdem die Schreibweise des Gutscheincodes, der Zahlungszeitpunkt einer ausstehenden Zahlung und der Fall Gesamtwert 0 €.

**Entscheidung:**
- Statuscodes beim Checkout:
  - 400: keine Positionen (US-04.1 AK7), `menge < 1`, fehlende Pflichtfelder.
  - 404: unbekannter Kunde, unbekanntes Produkt, Adresse existiert nicht oder gehört einem anderen Kunden (wie D-16).
  - 409: Bestand reicht nicht (US-04.1 AK3). Der Hinweis nennt **alle** Produkte, deren Bestand nicht reicht.
  - 422: Adresse ohne Lieferkennzeichen (US-04.1 AK8), Gutschein abgelaufen oder unbekannt (US-08.1 AK4, AK6).
- Jede Ablehnung lässt keinen Bestand verändert und legt keine Bestellung an.
- Gutscheincode wird getrimmt und **großgeschrieben** gespeichert und gesucht (D-04).
- Prozentrabatt hat höchstens zwei Nachkommastellen.
- Die API nimmt höchstens **einen** Gutscheincode je Checkout an. "Zweiter Code ersetzt den ersten" (US-08.1 AK7) ist Verhalten der Eingabemaske.
- Die erste Zahlung (D-02) hat **keinen** `zahlungszeitpunkt`; er wird erst beim Statuswechsel gesetzt (E05).
- Auch bei Gesamtwert 0 € wird die erste Zahlung über 0,00 € mit Status `AUSSTEHEND` angelegt (I-5).

**Nicht im Umfang (Schritt 4a):** Gutschein anlegen (US-08.2), Bestellhistorie (US-04.2). Tests legen Gutscheine direkt an.

---

## D-18 Checkout im Frontend und Bestellhistorie

**Problem:** Ohne Login ist offen, wer im Frontend bestellt. Ein Warenkorb ist keine Entität (E04), und Gesamtwert und Rabatt darf nur das Backend berechnen. Für US-04.2 fehlten Inhalt und Reihenfolge der Liste.

**Entscheidung:**
- Der Warenkorb lebt nur im Browser-Speicher; Neuladen der Seite leert ihn. Gleiche Produkte werden schon dort zu einem Eintrag zusammengefasst.
- Der Warenkorb zeigt Produkt, Listenpreis und Menge, aber **keine** Zwischenbeträge, Rabatte oder Summen. Diese zeigt erst die Bestellbestätigung mit den Werten des Backends (D-01).
- Nur verfügbare Produkte (`istVerfuegbar`) lassen sich in den Warenkorb legen. Ob der Bestand reicht, prüft erst der Checkout (409).
- Der Kunde wird im Checkout aus der Kundenliste gewählt; angeboten werden nur seine Lieferadressen, die erste ist vorgewählt.
- Das Gutscheinfeld nimmt genau einen Code auf; ein neuer Code ersetzt den alten (US-08.1 AK7). Ablehnungen (409, 422) erscheinen als Meldung; Warenkorb und Eingaben bleiben erhalten, man kann ohne Code weiterbestellen.
- Nach erfolgreichem Checkout wird der Warenkorb geleert und die Bestellansicht als Bestätigung gezeigt.
- Bestellhistorie (US-04.2): `GET /api/v1/kunden/{kundenId}/bestellungen` mit Bestellnummer, Bestelldatum, Anzahl Positionen und Gesamtwert; sortiert nach Bestelldatum absteigend, dann Bestellnummer absteigend (numerisch, D-14); 404 bei unbekanntem Kunden. Sie steht auf der Kundenseite unter dem Adressbuch.

**Nicht im Umfang (Schritt 4b):** Warenkorb über Neuladen hinweg, Vorschau von Rabatt und Gesamtwert vor dem Bestellen, Bezahlstatus in der Historie (Schritt 5), Seitenaufteilung.
