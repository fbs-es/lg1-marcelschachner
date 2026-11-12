package de.bestellsystem.mahnwesen.domain;

import de.bestellsystem.gemeinsam.domain.Geld;
import java.time.LocalDate;
import java.util.List;

/** Ergebnis einer Mahnprüfung mit Rückstand: eine Nachricht an den Kunden (D-03, D-20). */
public record Mahnbenachrichtigung(
        String empfaenger, LocalDate stichtag, List<Mahnposition> positionen) {

    public Mahnbenachrichtigung {
        if (positionen == null || positionen.isEmpty()) {
            throw new IllegalArgumentException("Eine Mahnung braucht mindestens eine Bestellung");
        }
        positionen = List.copyOf(positionen);
    }

    public Geld summe() {
        return positionen.stream()
                .map(Mahnposition::offenerBetrag)
                .reduce(Geld.NULL, Geld::addiere);
    }
}
