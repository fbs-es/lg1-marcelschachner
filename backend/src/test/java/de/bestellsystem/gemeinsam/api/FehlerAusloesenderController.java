package de.bestellsystem.gemeinsam.api;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.KonfliktException;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Nur für Tests: löst jede Fehlerart aus, die die zentrale Fehlerbehandlung übersetzt. */
@RestController
@RequestMapping("/test/fehler")
class FehlerAusloesenderController {

    record Eingabe(@NotBlank String name, @Positive int menge) {}

    @GetMapping("/nicht-gefunden")
    void nichtGefunden() {
        throw new NichtGefundenException("Kunde 42 existiert nicht.");
    }

    @GetMapping("/konflikt")
    void konflikt() {
        throw new KonfliktException("E-Mail-Adresse ist bereits vergeben.");
    }

    @GetMapping("/versionskonflikt")
    void versionskonflikt() {
        throw new ObjectOptimisticLockingFailureException(Object.class, "id");
    }

    @GetMapping("/abgelehnt")
    void abgelehnt() {
        throw new FachlicheAblehnungException("Gutschein ist abgelaufen.");
    }

    @GetMapping("/ungueltig")
    void ungueltig() {
        throw new IllegalArgumentException("land muss ein ISO-3166-Alpha-2-Code sein");
    }

    @GetMapping("/unerwartet")
    void unerwartet() {
        throw new IllegalStateException("geheimes internes Detail");
    }

    @PostMapping("/validierung")
    void validierung(@Valid @RequestBody Eingabe eingabe) {}
}
