package de.bestellsystem.katalog.api;

import de.bestellsystem.katalog.application.ProduktService;
import de.bestellsystem.katalog.domain.Produkt;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/produkte")
@Tag(name = "Produkte", description = "Produktkatalog und Lagerbestand (E02)")
class ProduktController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final ProduktService service;

    ProduktController(ProduktService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(operationId = "legeProduktAn", summary = "Produkt anlegen")
    @ApiResponse(responseCode = "201", description = "Angelegt, Location zeigt auf das Produkt")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe (D-15)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<ProduktDto> legeAn(@Valid @RequestBody ProduktAnlegenDto eingabe) {
        Produkt produkt =
                service.legeAn(
                        eingabe.titel(), eingabe.listenpreis().alsGeld(), eingabe.lagerbestand());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(produkt.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(ProduktDto.von(produkt));
    }

    @GetMapping
    @Operation(operationId = "listeProdukte", summary = "Alle Produkte, nach Titel sortiert")
    List<ProduktDto> alle() {
        return service.alle().stream().map(ProduktDto::von).toList();
    }

    @GetMapping("/{produktId}")
    @Operation(operationId = "findeProdukt", summary = "Ein Produkt mit Verfügbarkeit")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Produkt existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ProduktDto finde(@PathVariable UUID produktId) {
        return ProduktDto.von(service.finde(produktId));
    }

    @PutMapping("/{produktId}")
    @Operation(
            operationId = "aendereProdukt",
            summary = "Titel und Listenpreis ändern (US-02.2, D-23)",
            description = "Bestehende Bestellungen behalten ihren Einzelpreis (D-10).")
    @ApiResponse(responseCode = "200", description = "Geändert")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe (D-15)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Produkt existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Gleichzeitige Änderung (Versionskonflikt)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ProduktDto aendere(
            @PathVariable UUID produktId, @Valid @RequestBody ProduktAendernDto eingabe) {
        return ProduktDto.von(
                service.aendere(produktId, eingabe.titel(), eingabe.listenpreis().alsGeld()));
    }

    @PutMapping("/{produktId}/lagerbestand")
    @Operation(
            operationId = "setzeLagerbestand",
            summary = "Lagerbestand setzen (Wareneingang, Korrektur)")
    @ApiResponse(responseCode = "200", description = "Bestand gesetzt")
    @ApiResponse(
            responseCode = "400",
            description = "Bestand negativ (US-02.1 AK5)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Produkt existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Gleichzeitige Änderung (Versionskonflikt)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ProduktDto setzeLagerbestand(
            @PathVariable UUID produktId, @Valid @RequestBody LagerbestandDto eingabe) {
        return ProduktDto.von(service.setzeLagerbestand(produktId, eingabe.lagerbestand()));
    }

    @GetMapping("/{produktId}/verfuegbarkeit")
    @Operation(
            operationId = "pruefeVerfuegbarkeit",
            summary = "Prüfen, ob eine Menge lieferbar ist (ändert den Bestand nicht)")
    @ApiResponse(responseCode = "200", description = "Ergebnis der Prüfung")
    @ApiResponse(
            responseCode = "400",
            description = "Menge kleiner als 1 (US-02.1 AK4)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Produkt existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    VerfuegbarkeitDto pruefeVerfuegbarkeit(
            @PathVariable UUID produktId,
            @Parameter(description = "Gewünschte Menge, mindestens 1") @RequestParam int menge) {
        return new VerfuegbarkeitDto(produktId, menge, service.istVerfuegbar(produktId, menge));
    }
}
