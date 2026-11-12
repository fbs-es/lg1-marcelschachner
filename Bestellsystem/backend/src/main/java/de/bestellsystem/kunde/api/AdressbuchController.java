package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Adresse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/kunden/{kundenId}/adressen")
@Tag(name = "Adressbuch", description = "Liefer- und Rechnungsadressen eines Kunden (E01, D-06)")
class AdressbuchController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final KundeService service;

    AdressbuchController(KundeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(operationId = "fuegeAdresseHinzu", summary = "Weitere Adresse hinzufügen (US-01.2)")
    @ApiResponse(responseCode = "201", description = "Hinzugefügt, Location zeigt auf die Adresse")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe oder kein Kennzeichen (I-2)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Adressbuch gleichzeitig geändert (Versionskonflikt)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<AdresseDto> fuegeHinzu(
            @PathVariable UUID kundenId, @Valid @RequestBody AdresseEingabeDto eingabe) {
        Adresse adresse =
                service.fuegeAdresseHinzu(
                        kundenId,
                        eingabe.anschrift().alsAnschrift(),
                        eingabe.istLieferadresse(),
                        eingabe.istRechnungsadresse());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(adresse.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(AdresseDto.von(adresse));
    }

    @GetMapping("/{adressId}")
    @Operation(operationId = "findeAdresse", summary = "Eine Adresse aus dem Adressbuch")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde oder Adresse existiert nicht oder gehört einem anderen Kunden",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    AdresseDto finde(@PathVariable UUID kundenId, @PathVariable UUID adressId) {
        return AdresseDto.von(service.findeAdresse(kundenId, adressId));
    }

    @PutMapping("/{adressId}")
    @Operation(
            operationId = "aendereAdresse",
            summary = "Anschrift und Kennzeichen vollständig ersetzen (US-01.3)")
    @ApiResponse(responseCode = "200", description = "Geändert")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe oder kein Kennzeichen (I-2)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Kunde oder Adresse existiert nicht oder gehört einem anderen Kunden",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Adressbuch gleichzeitig geändert (Versionskonflikt)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Letzter Lieferadresse würde das Kennzeichen entzogen (I-1)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    AdresseDto aendere(
            @PathVariable UUID kundenId,
            @PathVariable UUID adressId,
            @Valid @RequestBody AdresseEingabeDto eingabe) {
        return AdresseDto.von(
                service.aendereAdresse(
                        kundenId,
                        adressId,
                        eingabe.anschrift().alsAnschrift(),
                        eingabe.istLieferadresse(),
                        eingabe.istRechnungsadresse()));
    }

    @DeleteMapping("/{adressId}")
    @Operation(operationId = "entferneAdresse", summary = "Adresse löschen (US-01.3)")
    @ApiResponse(responseCode = "204", description = "Gelöscht")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde oder Adresse existiert nicht oder gehört einem anderen Kunden",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Adressbuch gleichzeitig geändert (Versionskonflikt)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Letzte Lieferadresse ist nicht löschbar (I-1)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<Void> entferne(@PathVariable UUID kundenId, @PathVariable UUID adressId) {
        service.entferneAdresse(kundenId, adressId);
        return ResponseEntity.noContent().build();
    }
}
