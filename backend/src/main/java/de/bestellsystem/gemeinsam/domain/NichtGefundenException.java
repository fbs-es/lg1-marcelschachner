package de.bestellsystem.gemeinsam.domain;

/** Gesuchter Datensatz existiert nicht (HTTP 404). */
public class NichtGefundenException extends RuntimeException {

    public NichtGefundenException(String meldung) {
        super(meldung);
    }
}
