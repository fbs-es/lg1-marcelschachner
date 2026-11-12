package de.bestellsystem.gemeinsam.domain;

/** Zustand verhindert die Aktion, z. B. eindeutiger Wert schon vergeben (HTTP 409). */
public class KonfliktException extends RuntimeException {

    public KonfliktException(String meldung) {
        super(meldung);
    }
}
