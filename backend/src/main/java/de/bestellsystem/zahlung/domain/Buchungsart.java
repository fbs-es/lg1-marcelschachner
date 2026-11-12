package de.bestellsystem.zahlung.domain;

/** Bestimmt das Vorzeichen einer Buchung; der Betrag selbst ist immer positiv (D-05). */
public enum Buchungsart {
    ZAHLUNG,
    ERSTATTUNG
}
