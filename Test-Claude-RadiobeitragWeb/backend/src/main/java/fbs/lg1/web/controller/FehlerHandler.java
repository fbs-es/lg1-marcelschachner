package fbs.lg1.web.controller;

import fbs.lg1.web.dto.FehlerDto;
import fbs.lg1.web.service.NichtGefundenException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Übersetzt Ausnahmen aus Domäne und Services in HTTP-Antworten mit deutscher Meldung.
 */
@RestControllerAdvice
public class FehlerHandler {

    /**
     * Ungültige Eingaben aus der Domäne werden zu HTTP 400.
     *
     * @param e die Ausnahme aus der Domäne
     * @return die Fehlerantwort
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FehlerDto behandleUngueltigeEingabe(IllegalArgumentException e) {
        return new FehlerDto(400, e.getMessage());
    }

    /**
     * Regelverstöße im aktuellen Zustand werden zu HTTP 409.
     *
     * @param e die Ausnahme aus der Domäne
     * @return die Fehlerantwort
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public FehlerDto behandleKonflikt(IllegalStateException e) {
        return new FehlerDto(409, e.getMessage());
    }

    /**
     * Fehlende Datensätze werden zu HTTP 404.
     *
     * @param e die Ausnahme aus dem Service
     * @return die Fehlerantwort
     */
    @ExceptionHandler(NichtGefundenException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public FehlerDto behandleNichtGefunden(NichtGefundenException e) {
        return new FehlerDto(404, e.getMessage());
    }

    /**
     * Nicht lesbares JSON, zum Beispiel ein unbekannter Typ, wird zu HTTP 400.
     *
     * @param e die Ausnahme von Spring
     * @return die Fehlerantwort
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FehlerDto behandleUnlesbareAnfrage(HttpMessageNotReadableException e) {
        return new FehlerDto(400, "Anfrage ist ungültig oder unvollständig!");
    }

    /**
     * Ungültige Pfadparameter, zum Beispiel keine Zahl als ID, werden zu HTTP 400.
     *
     * @param e die Ausnahme von Spring
     * @return die Fehlerantwort
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public FehlerDto behandleFalschenParameter(MethodArgumentTypeMismatchException e) {
        return new FehlerDto(400, "Parameter '" + e.getName() + "' ist ungültig!");
    }
}
