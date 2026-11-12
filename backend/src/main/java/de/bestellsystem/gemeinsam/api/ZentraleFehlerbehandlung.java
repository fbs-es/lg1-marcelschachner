package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Übersetzt alle Fehler in ProblemDetail (RFC 9457) mit den Statuscodes aus CLAUDE.md. */
@RestControllerAdvice
public class ZentraleFehlerbehandlung extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ZentraleFehlerbehandlung.class);

    @ExceptionHandler(NichtGefundenException.class)
    ProblemDetail nichtGefunden(NichtGefundenException fehler) {
        return problem(HttpStatus.NOT_FOUND, "Nicht gefunden", fehler.getMessage());
    }

    @ExceptionHandler(KonfliktException.class)
    ProblemDetail konflikt(KonfliktException fehler) {
        return problem(HttpStatus.CONFLICT, "Konflikt", fehler.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail versionskonflikt(ObjectOptimisticLockingFailureException fehler) {
        return problem(
                HttpStatus.CONFLICT,
                "Versionskonflikt",
                "Der Datensatz wurde zwischenzeitlich geändert. Bitte neu laden und erneut"
                        + " versuchen.");
    }

    @ExceptionHandler(FachlicheAblehnungException.class)
    ProblemDetail fachlicheAblehnung(FachlicheAblehnungException fehler) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Fachlich abgelehnt", fehler.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail ungueltigeEingabe(IllegalArgumentException fehler) {
        return problem(HttpStatus.BAD_REQUEST, "Ungültige Eingabe", fehler.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail unerwartet(Exception fehler) {
        LOG.error("Unerwarteter Fehler", fehler);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Interner Fehler",
                "Ein unerwarteter Fehler ist aufgetreten.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException fehler,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ProblemDetail problem =
                problem(HttpStatus.BAD_REQUEST, "Ungültige Eingabe", "Die Eingabe ist ungültig.");
        List<Map<String, String>> feldfehler =
                fehler.getBindingResult().getFieldErrors().stream()
                        .map(feld ->
                                Map.of(
                                        "feld", feld.getField(),
                                        "meldung", String.valueOf(feld.getDefaultMessage())))
                        .toList();
        problem.setProperty("fehler", feldfehler);
        return handleExceptionInternal(fehler, problem, headers, status, request);
    }

    private static ProblemDetail problem(HttpStatus status, String titel, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(titel);
        return problem;
    }
}
