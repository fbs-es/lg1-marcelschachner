package fbs.lg1.web.service;

/**
 * Wird geworfen, wenn ein angefragter Datensatz nicht existiert. Die REST-Schicht antwortet
 * darauf mit HTTP 404.
 */
public class NichtGefundenException extends RuntimeException {

    /**
     * Legt die Ausnahme mit einer deutschen Fehlermeldung an.
     *
     * @param message die Fehlermeldung
     */
    public NichtGefundenException(String message) {
        super(message);
    }
}
