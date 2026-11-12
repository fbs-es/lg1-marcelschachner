package de.bestellsystem.bestellung.domain;

import de.bestellsystem.katalog.domain.Produkt;

/** Ein vom Kunden ausgewähltes Produkt mit Menge, vor dem Zusammenfassen (D-10). */
public record Bestellwunsch(Produkt produkt, int menge) {

    public Bestellwunsch {
        if (produkt == null) {
            throw new IllegalArgumentException("produkt ist erforderlich");
        }
        if (menge < 1) {
            throw new IllegalArgumentException("menge muss mindestens 1 sein, war: " + menge);
        }
    }
}
