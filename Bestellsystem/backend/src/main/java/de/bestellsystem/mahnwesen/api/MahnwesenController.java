package de.bestellsystem.mahnwesen.api;

import de.bestellsystem.mahnwesen.application.MahnService;
import de.bestellsystem.mahnwesen.application.Mahnlauf;
import de.bestellsystem.mahnwesen.application.Mahnlaufergebnis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Mahnwesen", description = "Mahnprüfung und Mahnlauf (E06)")
class MahnwesenController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final MahnService mahnService;
    private final Mahnlauf mahnlauf;
    private final Clock uhr;

    MahnwesenController(MahnService mahnService, Mahnlauf mahnlauf, Clock uhr) {
        this.mahnService = mahnService;
        this.mahnlauf = mahnlauf;
        this.uhr = uhr;
    }

    @PostMapping("/kunden/{kundenId}/mahnpruefung")
    @Operation(
            operationId = "pruefeMahnungen",
            summary = "Mahnprüfung für einen Kunden mit heutigem Stichtag (US-06.1, D-20)",
            description =
                    "Mahnt jede überfällige Bestellung, deren letzte Mahnung mindestens ein"
                            + " Mahnintervall zurückliegt, und versendet eine E-Mail mit allen"
                            + " gemahnten Bestellungen.")
    @ApiResponse(responseCode = "200", description = "Geprüft; leere Liste ohne Rückstand")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Versionskonflikt mit einer gleichzeitigen Änderung der Bestellung",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "503",
            description = "E-Mail nicht versendet; keine Mahnung protokolliert (US-06.1 AK7)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    MahnpruefungDto pruefe(@PathVariable UUID kundenId) {
        LocalDate stichtag = LocalDate.now(uhr);
        return MahnpruefungDto.von(stichtag, mahnService.pruefe(kundenId, stichtag));
    }

    @PostMapping("/mahnlaeufe")
    @Operation(
            operationId = "starteMahnlauf",
            summary = "Mahnlauf über alle Kunden mit heutigem Stichtag (US-06.2, D-20)",
            description =
                    "Läuft sonst täglich zeitgesteuert. Fehler bei einzelnen Kunden brechen den"
                            + " Lauf nicht ab und werden gezählt.")
    @ApiResponse(responseCode = "200", description = "Lauf beendet")
    MahnlaufDto starteMahnlauf() {
        Mahnlaufergebnis ergebnis = mahnlauf.fuehreHeuteDurch();
        return MahnlaufDto.von(ergebnis);
    }
}
