package de.bestellsystem.versand.api;

import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.versand.domain.Versandpaket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Versandpakete einer Bestellung; nutzt {@link BestellungService}, da Pakete Teil des Aggregats
 * sind.
 */
@RestController
@RequestMapping(VersandController.PAKETE)
@Tag(name = "Versand", description = "Versandpakete und Lieferstatus (E07, D-12, D-22)")
class VersandController {

    static final String PAKETE = "/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}/pakete";
    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final BestellungService service;

    VersandController(BestellungService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            operationId = "legePaketAn",
            summary = "Versandpaket anlegen (US-07.1)",
            description =
                    "Entsteht mit Lieferstatus ANGELEGT. Sendungsnummer eindeutig je Dienstleister"
                            + " über alle Bestellungen (I-11).")
    @ApiResponse(responseCode = "201", description = "Angelegt, Location zeigt auf das Paket")
    @ApiResponse(
            responseCode = "400",
            description = "Dienstleister fehlt oder unbekannt, Sendungsnummer ungültig",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Bestellung existiert nicht oder gehört einem anderen Kunden",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Sendungsnummer beim Dienstleister vergeben oder Versionskonflikt",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<VersandpaketDto> legeAn(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @Valid @RequestBody PaketAnlegenDto eingabe) {
        Versandpaket paket =
                service.legePaketAn(
                        kundenId, bestellnummer, eingabe.dienstleister(), eingabe.sendungsnummer());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(PAKETE + "/{paketId}")
                        .buildAndExpand(kundenId, bestellnummer, paket.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(VersandpaketDto.von(paket));
    }

    @GetMapping("/{paketId}")
    @Operation(operationId = "findePaket", summary = "Ein Versandpaket der Bestellung (US-07.3)")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Bestellung oder Paket existiert nicht oder gehört nicht dazu",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    VersandpaketDto finde(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @PathVariable UUID paketId) {
        return VersandpaketDto.von(service.paket(kundenId, bestellnummer, paketId));
    }

    @PutMapping("/{paketId}/status")
    @Operation(
            operationId = "aendereLieferstatus",
            summary = "Lieferstatus fortschreiben (US-07.2)",
            description =
                    "Nur vorwärts, Stufen dürfen übersprungen werden; RETOURE und VERLOREN erst"
                            + " ab UEBERGEBEN; ZUGESTELLT, RETOURE, VERLOREN sind Endzustände"
                            + " (D-22).")
    @ApiResponse(responseCode = "200", description = "Geändert")
    @ApiResponse(
            responseCode = "400",
            description = "Status fehlt oder ist unbekannt",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Bestellung oder Paket existiert nicht oder gehört nicht dazu",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Versionskonflikt durch gleichzeitige Änderung",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Übergang nicht erlaubt",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    VersandpaketDto aendereStatus(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @PathVariable UUID paketId,
            @Valid @RequestBody LieferstatusAenderungDto eingabe) {
        return VersandpaketDto.von(
                service.aendereLieferstatus(
                        kundenId, bestellnummer, paketId, eingabe.lieferstatus()));
    }
}
