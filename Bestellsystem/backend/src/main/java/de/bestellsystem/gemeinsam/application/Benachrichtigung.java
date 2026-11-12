package de.bestellsystem.gemeinsam.application;

/** Nachricht an eine E-Mail-Adresse, unabhängig vom Versandweg. */
public record Benachrichtigung(String empfaenger, String betreff, String text) {

    public Benachrichtigung {
        if (empfaenger == null || empfaenger.isBlank()) {
            throw new IllegalArgumentException("empfaenger darf nicht leer sein");
        }
        if (betreff == null || betreff.isBlank()) {
            throw new IllegalArgumentException("betreff darf nicht leer sein");
        }
        if (text == null) {
            throw new IllegalArgumentException("text darf nicht fehlen");
        }
    }
}
