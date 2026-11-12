package de.bestellsystem.zahlung.api;

import de.bestellsystem.bestellung.application.BestellungService;
import de.bestellsystem.zahlung.domain.Zahlung;
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

@RestController
@RequestMapping(ZahlungController.BESTELLUNG)
@Tag(name = "Zahlungen", description = "Zahlungsstatus und Erstattungen (E05, D-19)")
class ZahlungController {

    static final String BESTELLUNG = "/api/v1/kunden/{kundenId}/bestellungen/{bestellnummer}";
    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final BestellungService service;

    ZahlungController(BestellungService service) {
        this.service = service;
    }

    @GetMapping("/zahlungen/{zahlungId}")
    @Operation(operationId = "findeZahlung", summary = "Eine Buchung der Bestellung")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Bestellung oder Buchung existiert nicht oder gehört nicht dazu",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ZahlungDto finde(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @PathVariable UUID zahlungId) {
        return ZahlungDto.von(service.zahlung(kundenId, bestellnummer, zahlungId));
    }

    @PutMapping("/zahlungen/{zahlungId}/status")
    @Operation(
            operationId = "aendereZahlungsstatus",
            summary = "Status einer Zahlung oder Erstattung ändern (D-19)",
            description =
                    "Nur aus AUSSTEHEND nach ERFOLGREICH, FEHLGESCHLAGEN oder STORNIERT."
                            + " ERFOLGREICH setzt den Zahlungszeitpunkt.")
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
            description = "Bestellung oder Buchung existiert nicht oder gehört nicht dazu",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Versionskonflikt durch gleichzeitige Buchung",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Buchung ist nicht mehr AUSSTEHEND oder Ziel ist AUSSTEHEND",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ZahlungDto aendereStatus(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @PathVariable UUID zahlungId,
            @Valid @RequestBody StatusAenderungDto eingabe) {
        return ZahlungDto.von(
                service.aendereZahlungsstatus(
                        kundenId, bestellnummer, zahlungId, eingabe.transaktionsstatus()));
    }

    @PostMapping("/erstattungen")
    @Operation(
            operationId = "bucheErstattung",
            summary = "Erstattung buchen (D-19)",
            description =
                    "Entsteht mit Status AUSSTEHEND; höchstens bezahlte Summe abzüglich"
                            + " ausstehender Erstattungen.")
    @ApiResponse(responseCode = "201", description = "Angelegt, Location zeigt auf die Buchung")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe, z. B. Betrag 0 oder Zahlungsart fehlt",
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
            description = "Versionskonflikt durch gleichzeitige Buchung",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Betrag übersteigt den erstattbaren Betrag",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ZahlungDto> bucheErstattung(
            @PathVariable UUID kundenId,
            @PathVariable String bestellnummer,
            @Valid @RequestBody ErstattungBuchenDto eingabe) {
        Zahlung erstattung =
                service.bucheErstattung(
                        kundenId, bestellnummer, eingabe.zahlungsart(), eingabe.betrag().alsGeld());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path(BESTELLUNG + "/zahlungen/{zahlungId}")
                        .buildAndExpand(kundenId, bestellnummer, erstattung.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(ZahlungDto.von(erstattung));
    }
}
