package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.bestellung.domain.Bestellung;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/kunden/{kundenId}/bestellungen")
@Tag(name = "Bestellungen", description = "Checkout und Bestellabruf (E04, E08)")
class BestellungController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final BestellungService service;

    BestellungController(BestellungService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            operationId = "gibBestellungAuf",
            summary = "Bestellung aufgeben (US-04.1), optional mit Gutschein (US-08.1)")
    @ApiResponse(responseCode = "201", description = "Angelegt, Location zeigt auf die Bestellung")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe, z. B. keine Positionen oder Menge < 1",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description =
                    "Kunde oder Produkt existiert nicht, Adresse gehört nicht zum Kunden (D-17)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Bestand reicht nicht; Hinweis nennt die Produkte (US-04.1 AK3)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description =
                    "Adresse ist keine Lieferadresse, Gutschein abgelaufen oder unbekannt (D-17)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<BestellungDto> gibAuf(
            @PathVariable UUID kundenId, @Valid @RequestBody BestellungAufgebenDto eingabe) {
        Bestellung bestellung =
                service.gibAuf(
                        kundenId,
                        eingabe.positionen().stream()
                                .map(PositionswunschDto::alsPositionswunsch)
                                .toList(),
                        eingabe.lieferadresseId(),
                        eingabe.zahlungsart(),
                        eingabe.gutscheincode());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{bestellnummer}")
                        .buildAndExpand(bestellung.getBestellnummer())
                        .toUri();
        return ResponseEntity.created(ort).body(BestellungDto.von(bestellung));
    }

    @GetMapping
    @Operation(
            operationId = "listeBestellungen",
            summary = "Bestellhistorie des Kunden, neueste zuerst (US-04.2)")
    @ApiResponse(responseCode = "200", description = "Liste, leer ohne Bestellungen")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    List<BestellungUebersichtDto> historie(@PathVariable UUID kundenId) {
        return service.historie(kundenId).stream().map(BestellungUebersichtDto::von).toList();
    }

    @GetMapping("/{bestellnummer}")
    @Operation(operationId = "findeBestellung", summary = "Eine Bestellung des Kunden")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Bestellung existiert nicht oder gehört einem anderen Kunden",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    BestellungDto finde(@PathVariable UUID kundenId, @PathVariable String bestellnummer) {
        return BestellungDto.von(service.finde(kundenId, bestellnummer));
    }
}
