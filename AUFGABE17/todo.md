## Bugs

- [x] **`verkaufe` – Reihenfolge falsch**
      `einwurf` wird in die Kasse gelegt (Z. 69) _bevor_ `gebeWechselgeld` aufgerufen wird (Z. 71).
      Laut Spec: erst Wechselgeld herausgeben → dann Einwurf in Kasse legen → dann Getränk ausgeben.
      Aktuell funktioniert es zufällig (weil `gebeWechselgeld` dann auf Kasse + Einwurf zugreift),
      aber der Ablauf widerspricht der Spezifikation.

- [x] **`gebeWechselgeld` gibt `null` zurück** wenn Wechselgeld nicht aufgeht (Z. 55).
      In `verkaufe` wird `change.forEach(...)` direkt aufgerufen — NullPointerException wenn
      `kannWechseln` und `gebeWechselgeld` aus dem Takt geraten.

- [x] **`run()` – Münzen werden bei Abbruch nicht zurückgegeben**
      Drückt der Kunde 0 zum Abbruch, ist `einwurf` bereits befüllt — es gibt keine Ausgabe
      welche Münzen zurückgegeben werden.

- [x] **`run()` – Tippfehler** Z. 102: `"Ungütige Eingabe."` → sollte `"Ungültige Eingabe."` sein
      (alle anderen Meldungen sind korrekt).

## Fehlende Funktionalität

- [x] **`run()` – kein Wechselgeld-Retry**
      Laut Aufgabe soll der Prozess erst nach mehreren Versuchen (z.B. 3) abgebrochen werden,
      wenn kein Wechselgeld möglich ist. Aktuell wird sofort abgebrochen.

- [x] **`auffuellen` prüft `maximumStock` nicht**
      Man kann über das Maximum hinaus auffüllen. `Stock.auffuellen` sollte den neuen Bestand
      auf `drink.getMaximumStock()` deckeln.

## Encapsulation / Design

- [ ] **`getInventory()` bricht Kapselung**
      `Getraenkautomat.getInventory()` gibt das interne `Inventory`-Objekt direkt raus.
      Externe Klassen (auch Tests) können so den Bestand ohne jede Prüfung manipulieren.
      Besser: gezielte Setter/Getter im `Getraenkautomat` selbst; Tests über die öffentliche API.

- [x] **`Stock` und `CashBox` deklarieren Felder als `Map`, initialisieren aber `EnumMap`**
      `private final Map<...> drinks` / `coins` — Typ könnte direkt `EnumMap` sein, was die
      Absicht klarer macht.

- [x] **`berechneEinwurf` verwendet keine Instanzvariablen** → könnte `static` sein.

## Fehlende Tests

- [ ] **`Drinks.getMaximumStock()`** wird in `testDrinksFelder` nicht geprüft.
- [ ] **`Muenzen.getDescription()`** ist nie getestet.
- [ ] **`kannWechseln` direkt testen** (nicht nur indirekt über `verkaufe`).
- [ ] **`auffuellen` überschreitet Maximum** — sobald die Deckelung implementiert ist, testen.
