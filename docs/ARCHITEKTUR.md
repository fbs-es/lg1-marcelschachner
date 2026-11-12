# Architektur

Stand: Schritt 9 (Abschlussdokumentation). Wird mit jedem Schritt im selben Commit nachgezogen.

Überblick und Begründungen: [arc42.md](arc42.md); technische Entscheidungen: [adr/](adr/README.md); Bedienung: [benutzerhandbuch.md](benutzerhandbuch.md). Dieses Dokument ist die Detailreferenz.

## Überblick

```
Browser (Angular 21, :4200, Proxy /api → :8080)
   │  HTTP/JSON, /api/v1
   ▼
Spring Boot 3.5 (Java 21, Gradle)  ──SMTP──▶  Mailpit (lokal) / Mailserver
   │  JPA/Hibernate, JDBC
   ▼
PostgreSQL 16 (Schema per Flyway)
```

- Build: Gradle 9.9.1 über `./gradlew`, Kotlin DSL ([ADR 0001](adr/0001-gradle-als-build-werkzeug.md)).
- `./gradlew build` = kompilieren, alle Tests (Unit + Testcontainers), Spotless-Formatprüfung.
- CI: `.github/workflows/ci.yml` mit zwei Jobs auf `ubuntu-latest`:
  - `backend`: `./gradlew build` (Docker für Testcontainers vorhanden)
  - `frontend`: `npm ci`, `generate:api` plus `git diff --exit-code`, `npm test`, `npm run build`

## Pakete

Schichten je Fachpaket: `api` → `application` → `domain`; `persistence` und `infrastruktur` implementieren Ports aus `application`.

| Paket | Inhalt |
|---|---|
| `de.bestellsystem` | `BestellsystemApplication` (inkl. `@ConfigurationPropertiesScan`) |
| `gemeinsam.domain` | `Geld` (Cent `long`, EUR, `@Embeddable` Record), `Anschrift` (`@Embeddable` Record, ISO-3166-Alpha-2, Längen nach D-16), fachliche Ausnahmen `NichtGefundenException` (404), `KonfliktException` (409), `FachlicheAblehnungException` (422) |
| `gemeinsam.application` | Port `Benachrichtigungsversand` + `Benachrichtigung` (Fehler des Ports: `BenachrichtigungNichtVersendetException`), `BestellnummernGenerator` + Port `BestellnummernZaehler`, `FachlicheEinstellungen` (`bestellsystem.*`) |
| `gemeinsam.persistence` | `SequenzBestellnummernZaehler` (`nextval('bestellnummer_seq')` über `JdbcClient`) |
| `gemeinsam.infrastruktur` | `UhrKonfiguration` (`Clock`-Bean in konfigurierter Zeitzone), `MailBenachrichtigungsversand` (`JavaMailSender`, übersetzt `MailException`), `ZeitplanKonfiguration` (`@EnableScheduling`), `MailEinstellungen` (`bestellsystem.mail.*`), `OpenApiKonfiguration` |
| `gemeinsam.api` | `ZentraleFehlerbehandlung` (`@RestControllerAdvice`, `ResponseEntityExceptionHandler`), `GeldDto` (`{cent, waehrung}`, nur `EUR`), `AnschriftDto` (Schema `Anschrift`, genutzt von Kunde und Bestellung) |
| `katalog.domain` | `Produkt` (Entity, UUID im Konstruktor, `@Version`): `istVerfuegbar()`, `istVerfuegbar(menge)`, `setzeLagerbestand`, `entnehme(menge)` (409 bei zu wenig Bestand); prüft D-15 und I-7 |
| `katalog.persistence` | `ProduktRepository` (Spring Data JPA), `sperreZumEntnehmen` (`PESSIMISTIC_WRITE`, sortiert nach `id`, ADR 0003) |
| `katalog.application` | `ProduktService` (Transaktionen, 404 bei unbekannter ID), `sperreZumEntnehmen` (nur innerhalb einer Transaktion, 404 für unbekannte Produkte) |
| `katalog.api` | `ProduktController`, DTOs `ProduktAnlegenDto`, `ProduktDto`, `LagerbestandDto`, `VerfuegbarkeitDto` |
| `kunde.domain` | `Kunde` (Aggregatwurzel, UUID, `@Version`): Registrierung im Konstruktor, `fuegeAdresseHinzu`, `aendereAdresse`, `entferneAdresse`, `adresse(id)`, `lieferanschrift(id)` (Snapshot, 422 ohne Lieferkennzeichen), `fuehreMahnpruefungDurch(stichtag, bestellungen, mahnintervall)` (US-06.1, D-20: mahnt fällige Bestellungen, liefert `Optional<Mahnbenachrichtigung>`); setzt I-1 (422) und I-2 (400) durch. `Adresse` (Entity, `@Embedded Anschrift`, `istLieferadresse`, `istRechnungsadresse`; nur über `Kunde` änderbar). `Email` (`@Embeddable` Record: getrimmt, kleingeschrieben, Format, ≤ 254) |
| `kunde.persistence` | `KundeRepository`: `existsByEmail`, Liste nach Nachname/Vorname, `findeMitAdressen` (`JOIN FETCH`), `sperreZumMahnen` (`PESSIMISTIC_WRITE`, D-20) |
| `kunde.application` | `KundeService`: `registriere` (409 bei vergebener E-Mail, auch über Unique-Constraint), `alle`, `finde`, `pruefeVorhanden` (404 ohne Laden), `sperreZumMahnen` (404), Adressbuch-Operationen mit `OPTIMISTIC_FORCE_INCREMENT` auf den Kunden (D-16) |
| `kunde.api` | `KundeController`, `AdressbuchController`, DTOs `RegistrierungDto`, `AdresseEingabeDto`, `AdresseDto`, `KundeDto`, `KundeUebersichtDto` |
| `gutschein.domain` | `Gutschein` (Entity, UUID; Fabriken `prozent`, `festbetrag`): `normalisiere(code)` (getrimmt, groß), `istGueltigAm(datum)` (inklusive), `berechneRabatt(warenwert)` (HALF_UP, höchstens Warenwert); `RabattArt` |
| `gutschein.persistence` | `GutscheinRepository.findByCode` |
| `gutschein.application` | `GutscheinService.zumEinloesen(code)` (422 bei unbekanntem Code) |
| `zahlung.domain` | `Zahlung` (Entity, UUID, Teil der Bestellung; Fabriken `ausstehend` für erste Zahlung und für Teilzahlung/Erstattung mit Betrag > 0): `aendereStatus` (nur aus `AUSSTEHEND`, 422; Zeitpunkt nur bei `ERFOLGREICH`, D-19), `istErfolgreich`, `istAusstehend`, `istErstattung`; Enums `Zahlungsart`, `Buchungsart`, `Transaktionsstatus` |
| `zahlung.api` | `ZahlungController` (Statuswechsel, Erstattung, Buchung abrufen; nutzt `BestellungService`, da `Zahlung` Teil des Bestellungs-Aggregats ist), DTOs `ZahlungDto`, `StatusAenderungDto`, `ErstattungBuchenDto` |
| `bestellung.domain` | `Bestellung` (Aggregatwurzel, `bestellnummer` als ID, `@Version`, `kundeId`, `@Embedded lieferanschrift`, Gutschein 0..1 LAZY, eingefrorener `rabattBetrag`, Positionen und Zahlungen `cascade ALL`): Fabrik `gibAuf`, `berechneWarenwert`, `berechneGesamtwert`; Bezahlstatus (US-05.1, D-05) `berechneBezahlteSumme`, `berechneOffenenBetrag`, `istVollstaendigBezahlt`, `berechneErstattbarenBetrag` (D-19); Buchungen `bucheZahlung`, `bucheErstattung` (422 über erstattbar), `aendereZahlungsstatus`, `zahlung(id)` (404 bei fremder Buchung); Mahnwesen (D-03, D-20) `istUeberfaellig(stichtag)`, `istMahnungFaellig(stichtag, intervall)`, `mahne(stichtag, intervall)`, `letzteMahnung`, Mahnungen `cascade ALL`; Versand (E07, D-22) `legePaketAn`, `aendereLieferstatus`, `paket(id)` (404 bei fremdem Paket), Pakete `cascade ALL` mit `@OrderColumn` (Anlagereihenfolge). `Bestellposition` (eingefrorener `einzelpreis`, Produkt LAZY, `berechneZwischenbetrag`). `Bestellwunsch` (Produkt + Menge vor dem Zusammenfassen) |
| `bestellung.persistence` | `BestellungRepository`: `findeMitPositionen` (`JOIN FETCH` Positionen, Produkt, Gutschein), `findeZahlungen` (`JOIN FETCH` Zahlungen), `findeHistorie` (`JOIN FETCH` Positionen, Datum und Nummer absteigend, D-18), `findeZahlungenDesKunden` (Zahlungen der Historie), `findeMitZahlungen` (Kunde und Nummer, für Buchungen), `findePakete` (`LEFT JOIN FETCH` Pakete), `findeMitPaketen` (Kunde und Nummer, für Versand), `findeFaelligeMitPositionen` / `findeZahlungenFaelliger` / `findeMahnungenFaelliger` (fällig vor Stichtag, drei Abfragen), `findeKundenMitFaelligenBestellungen` |
| `bestellung.application` | `BestellungService`: `gibAuf` (eine Transaktion), `finde` (drei Abfragen: Positionen, Zahlungen, Pakete), `historie` (drei Abfragen, US-04.2, D-19), `zahlung`, `aendereZahlungsstatus` (Zeitpunkt aus `Clock`), `bucheErstattung`, `faelligeZumMahnen` (drei Abfragen), `kundenMitFaelligenBestellungen`, `paket`, `legePaketAn` (409 bei vergebener Sendungsnummer, vorab per `exists` und beim `flush` über den Unique-Constraint), `aendereLieferstatus` (Zeitpunkt aus `Clock`); Buchungen und Versand sperren die Bestellung mit `OPTIMISTIC_FORCE_INCREMENT` (D-19); `Positionswunsch` |
| `mahnwesen.domain` | `Mahnung` (Entity, UUID, Teil der Bestellung; eingefroren `erstelltAm`, `gemahnterBetrag` > 0), `Mahnbenachrichtigung` (Empfänger, Stichtag, Positionen, `summe`), `Mahnposition` |
| `mahnwesen.application` | `MahnService.pruefe(kundenId, stichtag)` (`@Transactional`: Kunde sperren, Bestellungen laden, `Kunde.fuehreMahnpruefungDurch`, Versand vor dem Commit), `Mahnlauf` (`@Scheduled`, `fuehreDurch(stichtag)`: je Kunde eigene Transaktion, Fehler gezählt), `Mahnlaufergebnis`, `Mahnschreiben` (Text der E-Mail) |
| `mahnwesen.api` | `MahnwesenController`, DTOs `MahnpruefungDto`, `MahnpositionDto`, `MahnlaufDto` |
| `merkliste.domain` | `Merklisteneintrag` (Entity, Assoziationsklasse Kunde *–* Produkt, D-07): `@EmbeddedId MerklisteneintragId` (Record `kundeId`, `produktId`, I-4), Produkt LAZY (nur lesend auf `produkt_id`), `hinzugefuegtAm` als `Instant`, `merkdatum(zeitzone)` (D-21); wird nie über `persist` angelegt |
| `merkliste.persistence` | `MerklisteneintragRepository`: `merke` (native `INSERT … ON CONFLICT DO NOTHING`, 1 = neu, 0 = schon gemerkt), `findeMerkliste` (`JOIN FETCH` Produkt, Zeitpunkt absteigend, dann Titel), `findeEintrag`, `entferne` (JPQL-Delete, No-op ohne Eintrag) |
| `merkliste.application` | `MerklisteService`: `merke` (404 Kunde/Produkt, Zeitpunkt aus `Clock`, liefert `Merkergebnis(eintrag, istNeu)`), `merkliste`, `eintrag` (404), `entferne` (404 nur für unbekannten Kunden) |
| `merkliste.api` | `MerklisteController`, DTO `MerklisteneintragDto` (`produktId`, `titel`, aktueller `listenpreis`, `istVerfuegbar`, Merkdatum `hinzugefuegtAm` in `bestellsystem.zeitzone`) |
| `versand.domain` | `Versandpaket` (Entity, UUID, Teil der Bestellung; `normalisiere` Sendungsnummer: getrimmt, groß, `[A-Z0-9]{1,50}`; `aendereStatus` 422 bei unerlaubtem Übergang; `angelegtAm`, `statusGeaendertAm`), `Lieferstatus` (`erlaubteFolgestatus`, `kannWechselnZu`; D-22), `Logistikdienstleister` (DHL, DPD, GLS, HERMES, UPS) |
| `versand.persistence` | `VersandpaketRepository.existsByDienstleisterAndSendungsnummer` (I-11) |
| `versand.api` | `VersandController` (nutzt `BestellungService`, da Pakete Teil des Aggregats sind), DTOs `VersandpaketDto` (mit `erlaubteFolgestatus`), `PaketAnlegenDto`, `LieferstatusAenderungDto` |
| `bestellung.api` | `BestellungController`, DTOs `BestellungAufgebenDto`, `PositionswunschDto`, `BestellungDto`, `BestellpositionDto`, `BestellungUebersichtDto` |

## REST-API

OpenAPI unter `/v3/api-docs`, Swagger UI unter `/swagger-ui.html`. Exportiert nach `frontend/openapi.json` (Quelle des Clients) und `docs/api/openapi.yaml` (Dokumentation), beide per `./gradlew aktualisiereOpenApi` ([ADR 0002](adr/0002-openapi-spezifikation-als-datei.md)).

| Methode | Pfad | Ergebnis |
|---|---|---|
| POST | `/api/v1/produkte` | 201 + `Location`, 400 (D-15) |
| GET | `/api/v1/produkte` | alle Produkte nach Titel |
| GET | `/api/v1/produkte/{id}` | Produkt mit `istVerfuegbar`, 404 |
| PUT | `/api/v1/produkte/{id}/lagerbestand` | Bestand absolut setzen, 400 bei < 0, 404, 409 Versionskonflikt |
| GET | `/api/v1/produkte/{id}/verfuegbarkeit?menge=n` | `{produktId, menge, istVerfuegbar}`, 400 bei menge < 1, 404 |
| POST | `/api/v1/kunden` | Registrierung mit erster Lieferadresse: 201 + `Location`, 400, 409 E-Mail vergeben |
| GET | `/api/v1/kunden` | Übersicht ohne Adressbuch, nach Nachname und Vorname |
| GET | `/api/v1/kunden/{kundenId}` | Kunde mit Adressbuch (Anlagereihenfolge), 404 |
| POST | `/api/v1/kunden/{kundenId}/adressen` | 201 + `Location`, 400 (auch I-2), 404, 409 Versionskonflikt |
| GET | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | eine Adresse, 404 auch bei fremder Adresse |
| PUT | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | vollständig ersetzen: 200, 400, 404, 409, 422 (I-1) |
| DELETE | `/api/v1/kunden/{kundenId}/adressen/{adressId}` | 204, 404, 409, 422 letzte Lieferadresse (I-1) |
| POST | `/api/v1/kunden/{kundenId}/bestellungen` | Checkout: 201 + `Location`, 400 (keine Positionen, Menge < 1), 404 (Kunde, Produkt, fremde Adresse), 409 Bestand, 422 (keine Lieferadresse, Gutschein abgelaufen/unbekannt), D-17 |
| GET | `/api/v1/kunden/{kundenId}/bestellungen` | Bestellhistorie (US-04.2): Nummer, Datum, Anzahl Positionen, Gesamtwert, offener Betrag, `istVollstaendigBezahlt`; neueste zuerst; 404 unbekannter Kunde |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}` | Bestellung mit Positionen, Rabatt, Gesamtwert, Zahlungen, `bezahlteSumme`, `offenerBetrag`, `istVollstaendigBezahlt` (US-05.1), `pakete` (US-07.3); 404 auch bei fremder Bestellung |
| POST | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/pakete` | `{dienstleister, sendungsnummer}`: 201 + `Location`, 400, 404, 409 Sendungsnummer beim Dienstleister vergeben oder Versionskonflikt (US-07.1, D-22) |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/pakete/{paketId}` | ein Paket mit `erlaubteFolgestatus`; 404 bei fremder Bestellung oder fremdem Paket |
| PUT | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/pakete/{paketId}/status` | `{lieferstatus}`: 200, 400, 404, 409 Versionskonflikt, 422 unerlaubter Übergang (US-07.2, D-22) |
| GET | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/zahlungen/{zahlungId}` | eine Buchung; 404 bei fremder Bestellung oder Buchung |
| PUT | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/zahlungen/{zahlungId}/status` | `{transaktionsstatus}`: 200, 400, 404, 409 Versionskonflikt, 422 (nicht mehr `AUSSTEHEND` oder Ziel `AUSSTEHEND`, D-19) |
| POST | `/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/erstattungen` | `{zahlungsart, betrag}`: 201 + `Location` (Buchung), 400 (Betrag 0, Pflichtfelder), 404, 409, 422 über erstattbar (D-19) |
| POST | `/api/v1/kunden/{kundenId}/mahnpruefung` | Mahnprüfung, Stichtag heute: 200 `{stichtag, istGemahnt, mahnpositionen[{bestellnummer, faelligAm, offenerBetrag}]}`, 404, 409, 503 Versand gescheitert (D-20) |
| GET | `/api/v1/kunden/{kundenId}/merkliste` | beobachtete Artikel, neueste zuerst, mit Merkdatum, aktuellem Preis und Verfügbarkeit; leere Liste; 404 unbekannter Kunde (US-03.1) |
| PUT | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | merken: 201 + `Location` neu, 200 schon gemerkt (Datum bleibt, I-4), 404 Kunde oder Produkt (D-21) |
| GET | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | ein Eintrag; 404 Kunde unbekannt oder nicht gemerkt |
| DELETE | `/api/v1/kunden/{kundenId}/merkliste/{produktId}` | 204, auch wenn nicht gemerkt; 404 unbekannter Kunde (US-03.2, D-21) |
| POST | `/api/v1/mahnlaeufe` | Mahnlauf über alle Kunden, Stichtag heute: 200 `{stichtag, gemahnteKunden, fehlgeschlageneKunden}` (US-06.2) |

## Datenfluss (Beispiel Produkt anlegen)

```
ProduktAnlegen (Angular) ─ ProdukteService (generiert) ─ POST /api/v1/produkte
  → ProduktController (@Valid ProduktAnlegenDto) → ProduktService.legeAn (@Transactional)
  → new Produkt(...) prüft Invarianten → ProduktRepository.save → INSERT produkt
  ← 201 ProduktDto ← Router zurück zur Liste ← GET /api/v1/produkte
```

## Datenfluss (Checkout im Frontend)

```
Produktliste ─ Warenkorb.lege (nur Browser) ─ /checkout
  → KundenService.listeKunden, findeKunde (Lieferadressen)
  → BestellungenService.gibBestellungAuf ─ POST /api/v1/kunden/{id}/bestellungen
  ← 201: Warenkorb.leere → /kunden/{id}/bestellungen/{nr}?aufgegeben=ja → findeBestellung
  ← 409/422: problemMeldung über dem Bestellknopf, Warenkorb und Eingaben bleiben
```

## Datenfluss (Checkout, US-04.1)

```
POST /kunden/{id}/bestellungen ─ BestellungController (@Valid BestellungAufgebenDto)
  → BestellungService.gibAuf (@Transactional, eine Transaktion)
      KundeService.finde → Kunde.lieferanschrift(adressId)        404 fremd, 422 keine Lieferadresse
      ProduktService.sperreZumEntnehmen → SELECT … FOR UPDATE     404 unbekannt, Sperre bis Commit
      GutscheinService.zumEinloesen (optional)                    422 unbekannt
      BestellnummernGenerator.erzeuge(heute aus Clock)
      Bestellung.gibAuf: zusammenfassen → Gutschein gültig? (422) → alle Bestände prüfen (409)
                         → entnehmen, Preise einfrieren → Rabatt einfrieren → Zahlung AUSSTEHEND
      BestellungRepository.save → INSERT bestellung, bestellposition, zahlung; UPDATE produkt
  ← 201 BestellungDto
```

Jede Ablehnung wirft vor dem Commit; die Transaktion rollt zurück, kein Bestand ändert sich.

## Datenfluss (Zahlungsstatus und Erstattung, D-19)

```
PUT …/zahlungen/{id}/status ─ ZahlungController (@Valid StatusAenderungDto)
  → BestellungService.aendereZahlungsstatus (@Transactional)
      findeMitZahlungen (404) → lock OPTIMISTIC_FORCE_INCREMENT
      Bestellung.aendereZahlungsstatus → Zahlung.aendereStatus(status, Instant aus Clock)   422
  ← 200 ZahlungDto; beim Commit UPDATE zahlung, UPDATE bestellung.version (409 bei Konflikt)

POST …/erstattungen ─ ZahlungController (@Valid ErstattungBuchenDto)
  → BestellungService.bucheErstattung → Bestellung.bucheErstattung (≤ erstattbar, sonst 422)
  ← 201 ZahlungDto + Location …/zahlungen/{id}; INSERT zahlung (AUSSTEHEND)
```

Bezahlte Summe, offener Betrag und `istVollstaendigBezahlt` werden bei jedem Abruf aus den Buchungen berechnet (D-01, D-05).

## Datenfluss (Mahnprüfung und Mahnlauf, D-20)

```
@Scheduled(bestellsystem.mahnlauf.cron) oder POST /mahnlaeufe
  → Mahnlauf.fuehreDurch(heute aus Clock)
      BestellungService.kundenMitFaelligenBestellungen(stichtag)   DISTINCT kunde_id, faellig_am < stichtag
      je Kunde: MahnService.pruefe (eigene Transaktion), Fehler loggen und zählen
  ← Mahnlaufergebnis {stichtag, gemahnteKunden, fehlgeschlageneKunden}

POST /kunden/{id}/mahnpruefung oder Mahnlauf
  → MahnService.pruefe (@Transactional)
      KundeService.sperreZumMahnen → SELECT … FOR UPDATE              404; Prüfungen je Kunde nacheinander
      BestellungService.faelligeZumMahnen → 3 Abfragen (Positionen, Zahlungen, Mahnungen)
      Kunde.fuehreMahnpruefungDurch: je Bestellung istMahnungFaellig → mahne (Mahnung mit offenem Betrag)
      Mahnschreiben → Benachrichtigungsversand.sende                 Fehler: Rollback, 503
  ← beim Commit INSERT mahnung, UPDATE bestellung.version
```

## Datenfluss (Merkliste, D-21)

```
PUT /kunden/{id}/merkliste/{produktId} ─ MerklisteController
  → MerklisteService.merke (@Transactional)
      KundeService.pruefeVorhanden, ProduktService.finde                 404
      MerklisteneintragRepository.merke(…, Instant aus Clock)            INSERT … ON CONFLICT DO NOTHING
      findeEintrag (JOIN FETCH Produkt)
  ← 201 + Location (neu) oder 200 (schon gemerkt, ursprüngliches Datum)

GET /kunden/{id}/merkliste → pruefeVorhanden + findeMerkliste: 2 Abfragen, unabhängig von der Länge
```

Gleichzeitiges doppeltes Merken: das zweite `INSERT` wartet auf den Primärschlüssel-Index und fügt nach dem Commit des ersten nichts ein (200).

## Datenfluss (Versand, D-22)

```
POST …/bestellungen/{nr}/pakete ─ VersandController (@Valid PaketAnlegenDto)
  → BestellungService.legePaketAn (@Transactional)
      findeMitPaketen (404) → lock OPTIMISTIC_FORCE_INCREMENT
      Versandpaket.normalisiere (400) → existsByDienstleisterAndSendungsnummer (409)
      Bestellung.legePaketAn (Zeitpunkt aus Clock) → flush: INSERT versandpaket   Unique-Verletzung → 409
  ← 201 VersandpaketDto + Location …/pakete/{id}

PUT …/pakete/{id}/status → BestellungService.aendereLieferstatus
      findeMitPaketen (404) → lock → Bestellung.aendereLieferstatus → Versandpaket.aendereStatus (422)
  ← 200 VersandpaketDto; beim Commit UPDATE versandpaket, UPDATE bestellung.version
```

Die Existenzprüfung läuft vor dem Anhängen des Pakets, sonst würde der Auto-Flush vor der Abfrage das neue Paket selbst finden.

## Querschnitt

### Geld (D-11)

- Ganze Cent als `long`, Währung fest `EUR`; nie Gleitkomma.
- `addiere`, `subtrahiere`, `multipliziere(menge)`, `prozentsatz(BigDecimal)` mit `RoundingMode.HALF_UP` (kaufmännisch).
- Überlauf wirft `ArithmeticException` (`Math.*Exact`).
- Darf negativ sein (Zwischenergebnisse); Nichtnegativität (I-7) prüfen die Entities.
- In Entities mit `@AttributeOverride` auf eine sprechende Spalte (`..._cent`) abbilden.

### Fehler (RFC 9457)

| Auslöser | Status | `title` |
|---|---|---|
| Bean Validation (`@Valid`) | 400 | Ungültige Eingabe, plus `fehler: [{feld, meldung}]` |
| `IllegalArgumentException` (verletzte Wertobjekt-Regel) | 400 | Ungültige Eingabe |
| unlesbares JSON, unbekannter Pfad, falsche Methode | 400/404/405 | Spring-Standard als ProblemDetail |
| `NichtGefundenException` | 404 | Nicht gefunden |
| `KonfliktException` | 409 | Konflikt |
| `ObjectOptimisticLockingFailureException` | 409 | Versionskonflikt |
| `FachlicheAblehnungException` | 422 | Fachlich abgelehnt |
| alles andere | 500 | Interner Fehler (ohne interne Details, geloggt) |

### Nebenläufigkeit im Adressbuch (D-16)

- `Kunde` trägt `@Version`. Jede Adressbuch-Operation sperrt ihn mit `OPTIMISTIC_FORCE_INCREMENT`, damit auch eine reine Feldänderung an einer `Adresse` die Version erhöht.
- Zwei gleichzeitige Änderungen, die zusammen I-1 verletzen würden (z. B. beide Lieferadressen löschen), scheitern für die zweite mit 409 Versionskonflikt.
- Gleichzeitige Registrierung derselben E-Mail: der Unique-Constraint `kunde_email_eindeutig` greift, `saveAndFlush` übersetzt die Verletzung in 409.

### Nebenläufigkeit bei Buchungen (D-19)

- Statuswechsel und Erstattung sperren die Bestellung mit `OPTIMISTIC_FORCE_INCREMENT`, auch wenn sich nur eine `Zahlung` ändert.
- Zwei gleichzeitige Erstattungen, die zusammen den erstattbaren Betrag übersteigen würden, scheitern für die zweite mit 409 Versionskonflikt.

### Nebenläufigkeit im Versand (D-22)

- Anlegen und Statuswechsel sperren die Bestellung mit `OPTIMISTIC_FORCE_INCREMENT` wie Buchungen.
- Gleiche Sendungsnummer gleichzeitig auf zwei Bestellungen: beide bestehen die Existenzprüfung, das zweite `INSERT` wartet am Unique-Index `versandpaket_sendungsnummer_eindeutig` und scheitert nach dem Commit des ersten; `flush` übersetzt das in 409.

### Nebenläufigkeit im Checkout (D-09, ADR 0003)

- Produkte werden mit `PESSIMISTIC_WRITE` in fester Reihenfolge (`id`) gesperrt; gleichzeitige Checkouts auf dasselbe Produkt laufen nacheinander.
- Der wartende Checkout sieht danach den aktuellen Bestand: reicht er nicht, 409 mit Produktname; reicht er, gelingen beide.
- `@Version` auf `Produkt` bleibt für `PUT lagerbestand`; die Version steigt auch beim Checkout.

### Sprache

- `spring.web.locale=de` mit festem `LocaleResolver`: Bean-Validation-Meldungen sind immer deutsch.

### Zeit

- Einzige Zeitquelle ist die `Clock`-Bean (`Europe/Berlin`, `bestellsystem.zeitzone`).
- Tests legen Zeit per `FesteUhrKonfiguration` (`@Primary Clock.fixed`, 12.11.2026 wie in den Stories) oder direkt mit `Clock.fixed` fest.
- Integrationstests mit Bestellungen leeren die Fachtabellen vorher und nachher (`Datenbestand.leere`), da sie den Testcontainer mit anderen Klassen teilen.

### Bestellnummer (D-13, D-14)

- Format `B-<Jahr des Bestelldatums>-<Zähler, mind. 6-stellig>`, z. B. `B-2026-000123`.
- Zähler global über PostgreSQL-Sequenz, nebenläufig eindeutig, Lücken zulässig.

### Benachrichtigung (D-03)

- Port `Benachrichtigungsversand.sende(Benachrichtigung)`; Adapter verschickt Text-E-Mail per SMTP.
- Lokal landet jede Mail in Mailpit (http://localhost:8025).
- Fehler des Versands meldet der Port als `BenachrichtigungNichtVersendetException` (Antwort 503).
- Mahn-E-Mail: Betreff „Zahlungserinnerung“, je gemahnter Bestellung Nummer, Fälligkeit und offener Betrag, dazu die Summe. Der Versand läuft vor dem Commit (mindestens einmal, D-20).
- Tests ersetzen den Port durch `BenachrichtigungsAttrappe` (merkt Nachrichten, lässt einzelne Empfänger scheitern).

### Konfiguration

| Schlüssel | Standard | Bedeutung |
|---|---|---|
| `bestellsystem.zeitzone` | `Europe/Berlin` | Zeitzone der Uhr |
| `bestellsystem.zahlungsziel` | `14d` | Bestelldatum bis Fälligkeit (D-03) |
| `bestellsystem.mahnintervall` | `7d` | Mindestabstand zweier Mahnungen (D-03) |
| `bestellsystem.mahnlauf.cron` | `0 0 6 * * *` | Zeitplan des Mahnlaufs in `bestellsystem.zeitzone`; `-` schaltet ab (Tests: `src/test/resources/config/application.yml`) |
| `bestellsystem.mail.absender` | `shop@bestellsystem.local` | Absender der E-Mails |

- Zugangsdaten nur über Umgebungsvariablen (`DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_ABSENDER`).
- Lokal: `.env` im Repo-Wurzelverzeichnis (Vorlage `.env.example`), gelesen von Docker Compose und vom Backend (`spring.config.import`).
- `spring.jpa.open-in-view=false`, `ddl-auto=validate`.

## Datenbank

### Tabellen und Sequenzen

| Objekt | Zweck |
|---|---|
| Sequenz `bestellnummer_seq` | Zähler der Bestellnummer |
| Tabelle `produkt` | `id` UUID, `version`, `titel` (≤ 200), `listenpreis_cent` (≥ 0), `lagerbestand` (≥ 0); Index auf `titel` |
| Tabelle `kunde` | `id` UUID, `version`, `vorname`, `nachname` (≤ 100), `email` (≤ 254, normalisiert, UNIQUE `kunde_email_eindeutig`); Index auf `(nachname, vorname)` |
| Tabelle `adresse` | `id` UUID, `kunde_id` (FK, `ON DELETE CASCADE`, Index), `reihenfolge` (`@OrderColumn`, nullable, da von Hibernate nachgetragen), `strasse`, `stadt`, `postleitzahl`, `land`, `ist_lieferadresse`, `ist_rechnungsadresse`; CHECK mindestens ein Kennzeichen (I-2) |
| Tabelle `gutschein` | `id` UUID, `code` (≤ 50, normalisiert, UNIQUE `gutschein_code_eindeutig`), `rabatt_art`, `prozent` NUMERIC(5,2) oder `festbetrag_cent`, `ablaufdatum`; CHECK passend zur Rabattart (D-04) |
| Tabelle `bestellung` | `bestellnummer` PK, `version`, `kunde_id` (FK, Index), `bestelldatum`, `faellig_am`, `liefer_strasse`/`_stadt`/`_postleitzahl`/`_land` (Snapshot, D-08), `gutschein_id` (FK, nullable), `rabatt_betrag_cent` (≥ 0); Index auf `faellig_am` (Mahnlauf); kein gespeicherter Gesamtwert (D-01) |
| Tabelle `bestellposition` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`), `zeilennummer` (≥ 1), `produkt_id` (FK, Index), `menge` (≥ 1), `einzelpreis_cent` (≥ 0); UNIQUE je Bestellung auf Zeilennummer und Produkt (I-6) |
| Tabelle `zahlung` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`, Index), `zahlungsart`, `buchungsart`, `transaktionsstatus` (je CHECK auf Enum-Werte), `betrag_cent` (≥ 0), `zahlungszeitpunkt` (nullable, gesetzt bei `ERFOLGREICH`, D-17, D-19) |
| Tabelle `merklisteneintrag` | PK `(kunde_id, produkt_id)` (I-4), beide FK `ON DELETE CASCADE` (D-07: Produkt entfernt → Eintrag weg), `hinzugefuegt_am` TIMESTAMPTZ (D-21); Index auf `produkt_id` |
| Tabelle `versandpaket` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`, Index), `reihenfolge` (`@OrderColumn`, nullable), `dienstleister`, `lieferstatus` (je CHECK auf Enum-Werte), `sendungsnummer` (CHECK `^[A-Z0-9]{1,50}$`), `angelegt_am`, `status_geaendert_am` (TIMESTAMPTZ); UNIQUE `versandpaket_sendungsnummer_eindeutig` auf `(dienstleister, sendungsnummer)` (I-11) |
| Tabelle `mahnung` | `id` UUID, `bestellnummer` (FK, `ON DELETE CASCADE`, Index), `erstellt_am`, `gemahnter_betrag_cent` (> 0) (D-03, D-20) |

### Migrationen

| Version | Datei | Inhalt |
|---|---|---|
| V1 | `V1__bestellnummer_sequenz.sql` | Sequenz `bestellnummer_seq` |
| V2 | `V2__produkt.sql` | Tabelle `produkt` mit CHECK-Constraints |
| V3 | `V3__kunde.sql` | Tabellen `kunde` und `adresse` |
| V4 | `V4__gutschein_bestellung_zahlung.sql` | Tabellen `gutschein`, `bestellung`, `bestellposition`, `zahlung` |
| V5 | `V5__mahnung.sql` | Tabelle `mahnung`, Index `bestellung_faellig_am_idx` |
| V6 | `V6__merklisteneintrag.sql` | Tabelle `merklisteneintrag` |
| V7 | `V7__versandpaket.sql` | Tabelle `versandpaket` |

## Lokale Infrastruktur

`docker-compose.yml`:

| Dienst | Image | Ports |
|---|---|---|
| `postgres` | `postgres:16-alpine` | 5432 |
| `mailpit` | `axllent/mailpit:v1.33.1` | 1025 (SMTP), 8025 (Web) |

## Frontend

Angular 21: Standalone Components, Signals, Reactive Forms, zoneless; Tests mit Vitest/jsdom.

| Pfad | Inhalt |
|---|---|
| `src/app/app.ts`, `app.routes.ts`, `app.config.ts` | Layout (Kopfzeile, Navigation mit `Warenkorb (n)`), Routen mit `withComponentInputBinding`, `provideApi('')`, `LOCALE_ID de-DE`, `DEFAULT_CURRENCY_CODE EUR` |
| `src/app/api/` | generierter Client (`npm run generate:api`, nie von Hand ändern) |
| `src/app/gemeinsam/` | `GeldPipe` (Cent → `49,99 €`), `euroInCent` + Validator `euroBetrag` (ohne Gleitkomma), `problemMeldung` (ProblemDetail → Text) |
| `src/app/katalog/produkt-liste` | `/produkte`: Laden, Fehler mit Wiederholen, Leerzustand, Tabelle mit Verfügbarkeit, "In den Warenkorb" nur bei `istVerfuegbar` |
| `src/app/katalog/produkt-anlegen` | `/produkte/neu`: Formular, Feld- und Serverfehler, danach zurück zur Liste |
| `src/app/kunde/kunden-liste` | `/kunden`: Laden, Fehler mit Wiederholen, Leerzustand, Tabelle mit Link zum Adressbuch |
| `src/app/kunde/kunde-registrieren` | `/kunden/neu`: Stammdaten, Lieferadresse, "auch Rechnungsadresse", danach zum Kunden |
| `src/app/kunde/kunde-detail` | `/kunden/:kundenId`: Stammdaten und Adressbuch; hinzufügen, bearbeiten, löschen; 422/409 als Meldung; darunter die Bestellhistorie |
| `src/app/kunde/adresse-formular`, `anschrift-felder` | wiederverwendbares Adressformular (Kennzeichen-Validator I-2) und Anschriftfelder mit Regeln aus D-16 |
| `src/app/bestellung/warenkorb.ts` | `Warenkorb` (Signals, nur im Speicher, D-18): `lege` (fasst zusammen), `aendereMenge`, `entferne`, `leere`, `stueckzahl` |
| `src/app/bestellung/bezeichnungen.ts` | deutsche Namen für Zahlungsart und Transaktionsstatus |
| `src/app/bestellung/checkout` | `/checkout`: Warenkorb ohne Summen, Kunde wählen, Lieferadressen des Kunden (`switchMap`), Zahlungsart, Gutscheincode; 409/422 als Meldung, Eingaben bleiben; Erfolg leert den Warenkorb |
| `src/app/bestellung/bestellung-detail` | `/kunden/:kundenId/bestellungen/:bestellnummer`: Bestellansicht mit Bezahlstatus, bezahlter Summe, offenem Betrag und Buchungen (Erstattungen gekennzeichnet; US-05.1, D-19), mit `?aufgegeben=ja` zugleich Bestätigung; darunter `<app-versandpakete>` |
| `src/app/bestellung/bestellhistorie` | `<app-bestellhistorie>`: Liste mit Link zur Bestellansicht und Bezahlstatus („bezahlt“ oder „offen: Betrag“), Laden, Fehler, Leerzustand |
| `src/app/merkliste/merkliste` | `/kunden/:kundenId/merkliste` (Link in der Kundenansicht): Einträge in Backend-Reihenfolge mit Merkdatum, Preis, Verfügbarkeit, „Entfernen“; Produktauswahl + „Merken“; Laden, Fehler mit Wiederholen, Leerzustand, Aktionsfehler als ProblemDetail-Meldung (US-03.1, US-03.2, D-21) |
| `src/app/versand/versandpakete` | `<app-versandpakete>` (Abschnitt „Versand“): Pakete mit Dienstleister, Sendungsnummer, Status, Zeitpunkten; je Paket Auswahl der erlaubten Folgestatus vom Backend und „Status setzen“, im Endzustand „abgeschlossen“; Formular „Paket anlegen“; Leerzustand „Noch kein Paket versandt.“; Fehler als ProblemDetail-Meldung; Liste per `linkedSignal`, aktualisiert aus den Antworten (US-07.1 bis US-07.3) |
| `src/app/versand/bezeichnungen.ts` | deutsche Namen für Lieferstatus und Dienstleister |
| `src/styles.css` | globale Formular- und Tabellenstile (`.formular`, `.feldfehler`, `.aktionen`, `table`, `.zahl`, `.unsichtbar`) |

- `proxy.conf.json` leitet `/api` an `http://localhost:8080` weiter (`npm start`).
- `openapitools.json` pinnt den Generator auf 7.27.0 (`typescript-angular`).

## Tests

| Art | Werkzeug | Beispiele |
|---|---|---|
| Domain-Unit | JUnit 5, AssertJ | `GeldTest`, `AnschriftTest`, `BestellnummernGeneratorTest`, `BenachrichtigungTest` |
| Adapter-Unit | Mockito an der Grenze (`JavaMailSender`) | `MailBenachrichtigungsversandTest` |
| Web | `@WebMvcTest`, MockMvc | `ZentraleFehlerbehandlungTest` (über Test-Controller `FehlerAusloesenderController`) |
| Domain-Unit Katalog | JUnit 5, AssertJ | `ProduktTest` (US-02.1 AK1–5, D-15) |
| Domain-Unit Kunde | JUnit 5, AssertJ | `KundeTest` (US-01.1 bis US-01.3), `EmailTest` (D-13, D-16) |
| API-Integration Kunde | MockMvc, PostgreSQL | `KundeApiIT` (alle AK US-01.1 bis US-01.3 über HTTP) |
| Domain-Unit Checkout | JUnit 5, AssertJ | `BestellungTest` (US-04.1 AK1–8, US-08.1 AK1–9), `GutscheinTest` (US-08.1, D-04, D-17) |
| Domain-Unit Zahlungen | JUnit 5, AssertJ | `BezahlstatusTest` (US-05.1 AK1–7, Statusübergänge und Erstattungsgrenze D-19) |
| API-Integration Zahlungen | MockMvc, PostgreSQL, feste Uhr | `ZahlungApiIT` (US-05.1 AK1/2/4/5/7 über HTTP, Historie mit Bezahlstatus, Statuscodes D-19, `Location`) |
| Nebenläufigkeit Erstattung | `TransactionTemplate`, Latches | `ErstattungNebenlaeufigkeitIT` (zwei Erstattungen über je 60 € auf 100 €: genau eine) |
| API-Integration Checkout | MockMvc, PostgreSQL, feste Uhr 12.11.2026 | `BestellungApiIT` (US-04.1, US-04.2, US-08.1; Historie 3 Abfragen bei 1 und 3 Bestellungen (Kunde, Positionen, Zahlungen); Statuscodes D-17, Rollback; Abfragenzahl beim Abruf über Hibernate-Statistics = 3) |
| API-Integration Merkliste | MockMvc, PostgreSQL, `VerstellbareUhrKonfiguration` (gemeinsam im Testwurzelpaket, Uhr je Test verstellbar) | `MerklisteApiIT` (US-03.1 AK1–5, US-03.2 AK1–5, gleichzeitiges Merken, Zeitzone des Merkdatums, 2 Abfragen beim Abruf, Kaskade D-07) |
| Domain-Unit Versand | JUnit 5, AssertJ | `VersandpaketTest` (US-07.1 AK1/4/6, US-07.2 AK1–7), `VersandTest` (Pakete in der Bestellung, fremdes Paket 404) |
| API-Integration Versand | MockMvc, PostgreSQL, `VerstellbareUhrKonfiguration` | `VersandApiIT` (US-07.1 AK1–7, US-07.2 AK1–7, US-07.3 AK1/3/4: 3 Abfragen unabhängig von der Paketzahl) |
| Nebenläufigkeit Versand | `TransactionTemplate`, Latches, Warten auf `pg_stat_activity` | `VersandpaketNebenlaeufigkeitIT` (gleiche Sendungsnummer auf zwei Bestellungen: genau eine, zweite 409) |
| Nebenläufigkeit Checkout | `TransactionTemplate`, Latches, Warten auf `pg_stat_activity` | `BestellungNebenlaeufigkeitIT` (letzte Lampe: genau einer; genug Bestand: beide) |
| Nebenläufigkeit | zwei Transaktionen per `TransactionTemplate`, Latches | `KundeNebenlaeufigkeitIT` (US-01.3 AK8, gleichzeitige Registrierung) |
| Integration | `@SpringBootTest`, Testcontainers (`TestcontainersKonfiguration`, `@ServiceConnection`) | `BestellsystemApplicationIT`, `SequenzBestellnummernZaehlerIT` |
| API-Integration | `@SpringBootTest` + MockMvc gegen echten Service und PostgreSQL | `ProduktApiIT` (US-02.1 AK1–5 über HTTP, Statuscodes, ProblemDetail) |
| Spec-Drift | `OpenApiSpezifikationIT` (prüft `frontend/openapi.json` und `docs/api/openapi.yaml`); `./gradlew aktualisiereOpenApi` schreibt beide neu | ADR 0002 |
| Frontend | Vitest, `TestBed`, `HttpTestingController` | `produkt-liste.spec.ts`, `produkt-anlegen.spec.ts`, `kunden-liste.spec.ts`, `kunde-registrieren.spec.ts`, `kunde-detail.spec.ts`, `warenkorb.spec.ts`, `checkout.spec.ts` (US-04.1, US-08.1 AK4/6/7), `bestellung-detail.spec.ts` (US-05.1 AK1/2/5, US-07.3 AK1/2), `bestellhistorie.spec.ts` (US-04.2, D-19), `merkliste.spec.ts` (US-03.1, US-03.2), `versandpakete.spec.ts` (US-07.1 bis US-07.3), `geld.pipe.spec.ts`, `euro-eingabe.spec.ts`, `problem-meldung.spec.ts` |
