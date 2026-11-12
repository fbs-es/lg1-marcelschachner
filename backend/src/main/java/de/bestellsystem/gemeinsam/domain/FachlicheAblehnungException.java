package de.bestellsystem.gemeinsam.domain;

/** Fachregel lehnt die Aktion ab, z. B. abgelaufener Gutschein (HTTP 422). */
public class FachlicheAblehnungException extends RuntimeException {

    public FachlicheAblehnungException(String meldung) {
        super(meldung);
    }
}
