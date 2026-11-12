# ADR 0004: Modularer Monolith, nach Fachlichkeit geschnitten

**Status:** angenommen (2026-11-12)

## Kontext

Das Bestellsystem umfasst neun Fachgebiete (E00 bis E08), die eng zusammenhängen: Der Checkout braucht Kunde, Produkt und Gutschein in einer Transaktion ([D-09](../01-entscheidungen.md#d-09-lagerbestand-und-verfügbarkeit)), die Mahnprüfung liest Bestellungen und Zahlungen ([D-03](../01-entscheidungen.md#d-03-zahlungsrückstand-braucht-eine-fälligkeit), [D-20](../01-entscheidungen.md#d-20-mahnprüfung-im-detail-und-täglicher-mahnlauf)). Ein Team, ein Deployment, eine Datenbank.

## Entscheidung

- **Ein** Spring-Boot-Backend (Java 21, Spring Boot 3.5) mit **einer** PostgreSQL-Datenbank.
- Pakete nach Fachlichkeit: `de.bestellsystem.{kunde, katalog, merkliste, bestellung, zahlung, gutschein, mahnwesen, versand, gemeinsam}`.
- In jedem Fachpaket die Schichten `api` → `application` → `domain`, dazu `persistence` und `infrastruktur`. Abhängigkeiten nur in dieser Richtung.
- `domain` enthält die Fachlogik (reichhaltiges Modell) und kennt außer JPA-Annotationen kein Spring. Services in `application` orchestrieren und schneiden Transaktionen.
- Aggregate nach dem Domänenmodell: `Kunde` (mit Adressen), `Produkt`, `Gutschein`, `Bestellung` (mit Positionen, Zahlungen, Mahnungen, Versandpaketen), `Merklisteneintrag`. Teile eines Aggregats werden nur über die Wurzel geändert; darum nutzen `ZahlungController` und `VersandController` den `BestellungService` ([D-19](../01-entscheidungen.md#d-19-zahlungsstatus-ändern-und-erstattung-buchen), [D-22](../01-entscheidungen.md#d-22-versandpakete-im-detail-übergänge-dienstleister-zeitpunkte)).
- Andere Aggregate werden über ID referenziert (`Bestellung.kundeId`) oder lesend über LAZY-Assoziationen (`Bestellposition.produkt`).
- Zugriff auf externe Systeme (E-Mail) nur über Ports in `application`, Adapter in `infrastruktur` (ADR 0011).

## Konsequenzen

- Eine Transaktion kann mehrere Aggregate umfassen (Checkout senkt Bestand und legt die Bestellung an). Das ist gewollt und mit Microservices nicht ohne Saga zu haben.
- Domain-Logik ist ohne Spring und Datenbank als Unit-Test prüfbar.
- Die Paketgrenzen sind Konvention, kein Compilerzwang. Eine spätere Prüfung (z. B. ArchUnit oder Spring Modulith) ist nachrüstbar.
