package de.bestellsystem.zahlung.domain;

/** Nur {@code ERFOLGREICH} zählt als geleistet (D-02, D-05). */
public enum Transaktionsstatus {
    AUSSTEHEND,
    ERFOLGREICH,
    FEHLGESCHLAGEN,
    STORNIERT
}
