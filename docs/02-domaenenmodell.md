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

  class Produkt {
    produktId: UUID
    titel: String
    lagerbestand: Integer
    listenpreis: Geld
    istVerfuegbar() Boolean
    istVerfuegbar(menge: Integer) Boolean
  }

  class Merklisteneintrag {
    hinzugefuegtAm: Datum
  }

  class Bestellung {
    bestellnummer: String «eindeutig»
    bestelldatum: Datum
    faelligAm: Datum
    lieferanschrift: Anschrift
    rabattBetrag: Geld
    /warenwert: Geld
    /gesamtwert: Geld
    berechneGesamtwert() Geld
    bezahlteSumme() Geld
    offenerBetrag() Geld
    istVollstaendigBezahlt() Boolean
    istUeberfaellig(stichtag: Datum) Boolean
  }

  class Anschrift {
    <<Wertobjekt>>
    strasse: String
    stadt: String
    postleitzahl: String
    land: Laendercode
  }

  class Bestellposition {
    zeilennummer: Integer
    menge: Integer
    einzelpreis: Geld
    berechneZwischenbetrag() Geld
  }

  class Zahlung {
    zahlungsId: UUID
    zahlungsart: Zahlungsart
    buchungsart: Buchungsart
    transaktionsstatus: Transaktionsstatus
    betrag: Geld
    zahlungszeitpunkt: Zeitstempel
  }

  class Versandpaket {
    sendungsnummer: String
    dienstleister: String
    lieferstatus: Lieferstatus
  }

  class Gutschein {
    code: String «eindeutig»
    rabattArt: RabattArt
    rabattWert: Dezimal
    ablaufdatum: Datum
    istGueltigAm(datum: Datum) Boolean
    berechneRabatt(warenwert: Geld) Geld
  }

  class Mahnung {
    mahnungId: UUID
    erstelltAm: Datum
    gemahnterBetrag: Geld
  }

  class Zahlungsart {
    <<enumeration>>
    KREDITKARTE
    PAYPAL
    LASTSCHRIFT
    RECHNUNG
    VORKASSE
  }
  class Buchungsart {
    <<enumeration>>
    ZAHLUNG
    ERSTATTUNG
  }
  class Transaktionsstatus {
    <<enumeration>>
    AUSSTEHEND
    ERFOLGREICH
    FEHLGESCHLAGEN
    STORNIERT
  }
  class Lieferstatus {
    <<enumeration>>
    ANGELEGT
    UEBERGEBEN
    IN_ZUSTELLUNG
    ZUGESTELLT
    RETOURE
    VERLOREN
  }
  class RabattArt {
    <<enumeration>>
    PROZENT
    FESTBETRAG
  }

  Kunde "1" *-- "1..*" Adresse : Adressbuch
  Kunde "1" -- "0..*" Merklisteneintrag
  Merklisteneintrag "0..*" -- "1" Produkt
  Kunde "1" -- "0..*" Bestellung : gibt auf
  Bestellung *-- Anschrift : Snapshot
  Bestellung "1" *-- "1..*" Bestellposition
  Bestellposition "0..*" --> "1" Produkt
  Bestellung "1" *-- "1..*" Zahlung
  Bestellung "1" *-- "0..*" Versandpaket
  Bestellung "0..*" --> "0..1" Gutschein : profitiert von
  Bestellung "1" *-- "0..*" Mahnung
```

> `Merklisteneintrag` ist im UML-Sinne die Assoziationsklasse der Beziehung Kunde \*–\* Produkt. Mermaid kennt keine Assoziationsklassen, daher als Zwischenklasse dargestellt.

## Formeln

```
Bestellposition.zwischenbetrag  = menge × einzelpreis
Bestellung.warenwert            = Σ positionen.zwischenbetrag
Bestellung.gesamtwert           = max(0, warenwert − rabattBetrag)
Bestellung.bezahlteSumme        = Σ ERFOLGREICH-ZAHLUNG.betrag − Σ ERFOLGREICH-ERSTATTUNG.betrag
Bestellung.offenerBetrag        = max(0, gesamtwert − bezahlteSumme)
Bestellung.istVollstaendigBezahlt ⇔ bezahlteSumme ≥ gesamtwert
Bestellung.istUeberfaellig(t)   ⇔ offenerBetrag > 0 ∧ t > faelligAm
Gutschein.berechneRabatt(w)     = PROZENT:    round(w × rabattWert / 100)
                                  FESTBETRAG: min(rabattWert, w)
Produkt.istVerfuegbar()         ⇔ lagerbestand > 0
```

## Invarianten

| # | Invariante | Quelle |
|---|---|---|
| I-1 | Kunde hat ≥ 1 Adresse mit `istLieferadresse` | D-06 |
| I-2 | Jede Adresse hat `istLieferadresse ∨ istRechnungsadresse` | D-06 |
| I-3 | E-Mail eindeutig (case-insensitive) | D-13 |
| I-4 | Merklisteneintrag eindeutig je (Kunde, Produkt) | D-07 |
| I-5 | Bestellung hat ≥ 1 Position und ≥ 1 Zahlung | Text, D-02 |
| I-6 | Zeilennummern lückenlos ab 1, Produkt max. einmal je Bestellung | D-10 |
| I-7 | `menge ≥ 1`, `lagerbestand ≥ 0`, alle Beträge ≥ 0 | D-09, D-10, D-05 |
| I-8 | `einzelpreis`, `rabattBetrag`, `lieferanschrift` nach Checkout unveränderlich | D-04, D-08, D-10 |
| I-9 | Gutschein bei Checkout gültig (`bestelldatum ≤ ablaufdatum`) | D-04 |
| I-10 | Gutscheincode eindeutig (case-insensitive) | D-04 |
| I-11 | Sendungsnummer eindeutig je Dienstleister | D-12 |
