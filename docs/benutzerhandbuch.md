# Benutzerhandbuch

Für Shop-Mitarbeiter und Tester des Bestellsystems (MVP). Start der Anwendung: siehe [README](../README.md#schnellstart). Die Oberfläche läuft unter http://localhost:4200.

Im MVP gibt es **kein Login**. Wer die Oberfläche bedient, wählt den Kunden aus einer Liste und handelt in seinem Namen. Fachliche Regeln stehen in [01-entscheidungen.md](01-entscheidungen.md) (`D-xx`).

## Überblick

Die Kopfzeile führt zu drei Bereichen:

| Menüpunkt | Inhalt |
|---|---|
| **Katalog** | Produkte mit Preis und Verfügbarkeit, Produkt anlegen und bearbeiten, in den Warenkorb legen |
| **Kunden** | Kundenliste, Registrierung; je Kunde Adressbuch, Bestellungen, Merkliste |
| **Gutscheine** | Gutscheine mit Rabatt, Ablaufdatum und Gültigkeit; Gutschein anlegen |
| **Warenkorb (n)** | Inhalt des Warenkorbs und Kasse; `n` ist die Stückzahl |

Jede Liste zeigt beim Laden einen Hinweis, bei einem Fehler eine Meldung mit **Erneut versuchen** und bei leerem Ergebnis einen Leerhinweis. Abgelehnte Aktionen erscheinen als Meldung in Klartext, z. B. „Gutschein ist abgelaufen“.

Beträge werden in Euro angezeigt (`49,99 €`); intern rechnet das System in ganzen Cent.

## 1 Produkt anlegen

1. **Katalog** → **Produkt anlegen**.
2. **Titel** (Pflicht, höchstens 200 Zeichen), **Listenpreis (€)** (0 oder mehr, höchstens zwei Nachkommastellen), **Lagerbestand** (0 oder mehr) eingeben.
3. **Speichern**. Die Produktliste zeigt das neue Produkt.

In der Produktliste ist jedes Produkt als **verfügbar** (Bestand > 0) oder **nicht verfügbar** gekennzeichnet. Titel dürfen sich wiederholen ([D-15](01-entscheidungen.md#d-15-produktdaten-beim-anlegen)).

### Produkt bearbeiten

1. **Katalog** → beim Produkt **Bearbeiten**.
2. Unter **Stammdaten** Titel und Listenpreis ändern, **Stammdaten speichern**.
3. Unter **Lagerbestand** den neuen Bestand eintragen (absolut, z. B. nach Wareneingang), **Lagerbestand speichern**.

Ein neuer Preis gilt nur für künftige Bestellungen; bestehende Bestellungen behalten ihren Einzelpreis. Die Merkliste zeigt sofort den neuen Preis ([D-23](01-entscheidungen.md#d-23-produkt-und-gutschein-pflegen)). Hat jemand das Produkt gleichzeitig geändert, erscheint „Versionskonflikt“: Seite neu laden und erneut speichern.

## 2 Kunde registrieren

1. **Kunden** → **Kunde registrieren**.
2. **Vorname**, **Nachname**, **E-Mail** eingeben. Die E-Mail muss eindeutig sein; Groß-/Kleinschreibung zählt nicht.
3. Unter **Lieferadresse**: **Straße und Hausnummer**, **Postleitzahl**, **Stadt**, **Land** als ISO-Code (`DE`, `AT`, …). Optional **Auch als Rechnungsadresse verwenden** ankreuzen.
4. **Registrieren**. Danach öffnet sich die Kundenansicht.

Ist die E-Mail schon vergeben, erscheint eine Meldung; die Eingaben bleiben stehen.

## 3 Adressbuch pflegen

In der Kundenansicht (**Kunden** → Kunde anklicken), Abschnitt **Adressbuch**:

- **Adresse hinzufügen**: Anschrift eingeben, mindestens eines der Kennzeichen **Lieferadresse** oder **Rechnungsadresse** ankreuzen, **Adresse speichern**.
- **Bearbeiten**: Anschrift oder Kennzeichen ändern, **Adresse speichern**.
- **Löschen**: entfernt die Adresse sofort.

Regeln ([D-06](01-entscheidungen.md#d-06-liefer--und-rechnungsadressen), [D-16](01-entscheidungen.md#d-16-kundendaten-adressbuch-fehler-und-nebenläufigkeit)):

- Jeder Kunde hat immer mindestens eine Lieferadresse. Die letzte Lieferadresse lässt sich weder löschen noch ihr Kennzeichen entfernen; das System lehnt mit einer Meldung ab.
- Änderungen am Adressbuch wirken nicht auf bestehende Bestellungen; diese tragen eine Kopie der Lieferanschrift ([D-08](01-entscheidungen.md#d-08-lieferadress-snapshot)).
- Haben zwei Personen das Adressbuch gleichzeitig geändert, scheitert die zweite Änderung mit „Versionskonflikt“. Seite neu laden und erneut versuchen.

## 4 Bestellen

### 4.1 Warenkorb füllen

1. **Katalog**: bei verfügbaren Produkten **In den Warenkorb**. Mehrfaches Klicken erhöht die Menge.
2. **Warenkorb (n)** öffnen: Menge ändern oder **Entfernen**.

Der Warenkorb liegt nur im Browser. Neuladen der Seite leert ihn ([D-18](01-entscheidungen.md#d-18-checkout-im-frontend-und-bestellhistorie)). Er zeigt bewusst keine Summen: Zwischenbeträge, Rabatt und Gesamtwert berechnet das Backend erst beim Bestellen.

### 4.2 Kasse

Unter dem Warenkorb, Abschnitt **Kasse**:

1. **Kunde** wählen. Danach erscheinen seine **Lieferadressen**; die erste ist vorgewählt.
2. **Zahlungsart** wählen: Rechnung, Lastschrift, Kreditkarte, PayPal oder Vorkasse.
3. Optional **Gutscheincode** eingeben (Groß-/Kleinschreibung egal, genau ein Code je Bestellung).
4. **Zahlungspflichtig bestellen**.

Bei Erfolg wird der Warenkorb geleert und die **Bestellansicht** als Bestätigung gezeigt: Bestellnummer (`B-2026-000123`), Positionen mit eingefrorenen Einzelpreisen, Rabatt, Gesamtwert, Fälligkeit (Bestelldatum + 14 Tage) und eine erste Zahlung im Status *ausstehend*.

### 4.3 Wenn die Bestellung abgelehnt wird

| Meldung sinngemäß | Ursache | Was tun |
|---|---|---|
| Bestand reicht nicht für … | ein oder mehrere Produkte nicht mehr in der gewünschten Menge da; alle betroffenen werden genannt | Menge verringern oder Produkt entfernen |
| Gutschein ist abgelaufen / unbekannt | Ablaufdatum überschritten (das Ablaufdatum selbst gilt noch) oder Code existiert nicht | Code korrigieren oder Feld leeren und ohne Gutschein bestellen |
| Adresse ist keine Lieferadresse | gewählte Adresse hat kein Lieferkennzeichen | andere Adresse wählen |

Bei jeder Ablehnung bleiben Warenkorb und Eingaben erhalten; es wird nichts gebucht und kein Bestand verändert ([D-17](01-entscheidungen.md#d-17-checkout-ablehnungen-gutscheincode-erste-zahlung)).

## 5 Bestellungen einsehen

- Kundenansicht, Abschnitt **Bestellungen**: alle Bestellungen des Kunden, neueste zuerst, mit Datum, Anzahl Positionen, Gesamtwert und Bezahlstatus („bezahlt“ oder „offen: Betrag“).
- Klick auf die Bestellnummer öffnet die **Bestellansicht** mit:
  - Positionen, Warenwert, Rabatt, Gesamtwert, Lieferanschrift,
  - **Zahlung**: Bezahlstatus („vollständig bezahlt“ oder „offen“), bezahlte Summe, offener Betrag und alle Buchungen (Erstattungen gekennzeichnet),
  - **Versand**: Pakete der Bestellung.

Bezahlt ist eine Bestellung, sobald erfolgreiche Zahlungen minus erfolgreiche Erstattungen den Gesamtwert erreichen ([D-05](01-entscheidungen.md#d-05-erstattungen-und-transaktionsstatus)).

## 6 Versand

In der Bestellansicht, Abschnitt **Versand**:

1. Unter **Paket anlegen** den **Dienstleister** (DHL, DPD, GLS, Hermes, UPS) wählen und die **Sendungsnummer** eingeben (nur Buchstaben und Ziffern, höchstens 50 Zeichen; wird großgeschrieben gespeichert).
2. **Paket anlegen**. Das Paket erscheint mit Status *angelegt*.
3. Je Paket den neuen Status aus der Auswahl wählen und **Status setzen**.

Erlaubte Übergänge ([D-22](01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)):

```
ANGELEGT → UEBERGEBEN → IN_ZUSTELLUNG → ZUGESTELLT
              │               │
              └──────┬────────┘
                     ▼
            RETOURE oder VERLOREN
```

- Stufen dürfen übersprungen werden (z. B. *angelegt* → *zugestellt*).
- *Zugestellt*, *Retoure* und *verloren* sind Endzustände; das Paket zeigt dann „abgeschlossen“.
- Die Auswahl bietet nur erlaubte Folgestatus an.
- Eine Sendungsnummer darf je Dienstleister nur einmal vorkommen, über alle Bestellungen.

## 7 Merkliste

Kundenansicht → **Merkliste**:

1. Produkt aus der Auswahl wählen, **Merken**.
2. Die Liste zeigt neueste Einträge zuerst, mit Merkdatum, aktuellem Preis und Verfügbarkeit.
3. **Entfernen** nimmt ein Produkt von der Liste.

Ein schon gemerktes Produkt erneut zu merken ändert nichts; das ursprüngliche Datum bleibt ([D-07](01-entscheidungen.md#d-07-merkliste-als-assoziationsklasse), [D-21](01-entscheidungen.md#d-21-merkliste-im-detail-zeitpunkt-merken-entfernen)).

## 8 Mahnwesen

Das System prüft **täglich um 06:00 Uhr** alle Kunden mit fälligen Bestellungen ([D-03](01-entscheidungen.md#d-03-zahlungsrückstand-braucht-eine-fälligkeit), [D-20](01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)):

- Überfällig ist eine Bestellung, wenn ein Betrag offen ist und die Fälligkeit (Bestelldatum + 14 Tage) überschritten ist.
- Der Kunde erhält **eine** E-Mail „Zahlungserinnerung“ mit jeder gemahnten Bestellung, ihrer Fälligkeit und ihrem offenen Betrag, dazu die Summe.
- Dieselbe Bestellung wird höchstens alle **7 Tage** gemahnt. Ein zweiter Lauf am selben Tag verschickt nichts.
- Bezahlte Bestellungen werden nicht gemahnt.

Lokal landen alle E-Mails in **Mailpit**: http://localhost:8025.

Manuell auslösen (Stichtag ist immer heute), siehe [Abschnitt 9](#9-funktionen-ohne-oberfläche):

- ein Kunde: `POST /api/v1/kunden/{kundenId}/mahnpruefung`
- alle Kunden: `POST /api/v1/mahnlaeufe`

Ist der Mailserver nicht erreichbar, wird nichts als gemahnt vermerkt; die manuelle Prüfung meldet 503, der nächste Lauf versucht es erneut.

## 9 Funktionen ohne Oberfläche

Einige Abläufe hat das MVP nur in der API. Sie lassen sich in der **Swagger UI** unter http://localhost:8080/swagger-ui.html ausprobieren („Try it out“). Die Kunden-ID und die Bestellnummer stehen in der Adresszeile des Browsers (`/kunden/<kundenId>/bestellungen/<bestellnummer>`); die ID einer Zahlung liefert `GET …/bestellungen/{bestellnummer}` im Feld `zahlungen[].id`.

| Aufgabe | Endpunkt | Hinweis |
|---|---|---|
| Zahlung als erfolgreich, fehlgeschlagen oder storniert melden | `PUT /api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/zahlungen/{zahlungId}/status` | nur aus *ausstehend*, danach endgültig ([D-19](01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen)) |
| Erstattung buchen | `POST /api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/erstattungen` | höchstens der erstattbare Betrag; entsteht *ausstehend* und wird per Statuswechsel abgeschlossen |
| Mahnprüfung / Mahnlauf | siehe [Abschnitt 8](#8-mahnwesen) | |


## 10 Gutscheine

Menüpunkt **Gutscheine**:

- Die Liste zeigt alle Gutscheine nach Code mit Rabatt, „Gültig bis“ und Status *gültig* oder *abgelaufen*.
- **Gutschein anlegen**: **Code** (1–50 Buchstaben, Ziffern oder `-`, wird großgeschrieben gespeichert), **Rabattart** *Prozent* oder *Festbetrag*, dazu der Wert (z. B. `10` oder `12,5` bzw. `5,00`), **Gültig bis (einschließlich)**. Dann **Gutschein anlegen**.
- Abgelehnt wird ein schon vergebener Code (auch in anderer Schreibweise), ein Prozentwert über 100 oder ein Ablaufdatum vor heute ([D-23](01-entscheidungen.md#d-23-produkt-und-gutschein-pflegen)).
- Ein angelegter Gutschein ist sofort im Checkout einlösbar. Ändern oder löschen lässt er sich im MVP nicht.

Der Rabatt gilt auf den Warenwert; der Gesamtwert wird nie negativ ([D-04](01-entscheidungen.md#d-04-gutschein-art-des-rabatts-und-wirkung)).

## 11 Typischer Durchlauf zum Ausprobieren

1. Zwei Produkte anlegen, eines mit Lagerbestand 1.
2. Kunde registrieren, eine zweite Adresse als Rechnungsadresse hinzufügen; versuchen, die Lieferadresse zu löschen (wird abgelehnt).
3. Unter **Gutscheine** `HERBST10` (10 %, bis Jahresende) anlegen. Einen abgelaufenen Gutschein `ALT` gibt es nur, wenn das Ablaufdatum inzwischen verstrichen ist; sonst den Code `GIBTSNICHT` als unbekannten Gutschein nutzen.
4. Beide Produkte in den Warenkorb, Produkt mit Bestand 1 auf Menge 2 setzen, mit `GIBTSNICHT` bestellen (Gutschein unbekannt), dann mit `HERBST10` (Bestand reicht nicht), dann Menge 1 (Erfolg).
5. In der Bestellansicht ein Paket anlegen und über *übergeben* bis *zugestellt* führen.
6. Im Katalog den Preis eines Produkts erhöhen: die Bestellung behält den alten Einzelpreis.
7. Zahlung per Swagger auf `ERFOLGREICH` setzen: Bestellansicht zeigt „vollständig bezahlt“, die Historie „bezahlt“.
8. Für eine unbezahlte Bestellung mit Fälligkeit in der Vergangenheit `POST /api/v1/mahnlaeufe` aufrufen und die E-Mail in Mailpit ansehen. Da die Uhr echt läuft, muss die Fälligkeit dafür in der Datenbank zurückdatiert werden, z. B. `UPDATE bestellung SET faellig_am = DATE '2026-01-01' WHERE bestellnummer = '…';`.
