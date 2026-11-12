package de.bestellsystem.gutschein.api;

import de.bestellsystem.gutschein.application.GutscheinService;
import de.bestellsystem.gutschein.domain.Gutschein;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
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
@RequestMapping("/api/v1/gutscheine")
@Tag(name = "Gutscheine", description = "Gutscheine anlegen und ansehen (E08, D-23)")
class GutscheinController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final GutscheinService service;

    GutscheinController(GutscheinService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(operationId = "legeGutscheinAn", summary = "Gutschein anlegen (US-08.2)")
    @ApiResponse(responseCode = "201", description = "Angelegt, Location zeigt auf den Gutschein")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültiger Code oder Rabattwert (D-04, D-23)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Code bereits vergeben",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "422",
            description = "Ablaufdatum liegt vor heute",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<GutscheinDto> legeAn(@Valid @RequestBody GutscheinAnlegenDto eingabe) {
        Gutschein gutschein =
                service.legeAn(
                        eingabe.code(),
                        eingabe.rabattArt(),
                        eingabe.prozent(),
                        eingabe.festbetrag() == null ? null : eingabe.festbetrag().alsGeld(),
                        eingabe.ablaufdatum());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(gutschein.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(GutscheinDto.von(gutschein, service.heute()));
    }

    @GetMapping
    @Operation(operationId = "listeGutscheine", summary = "Alle Gutscheine, nach Code sortiert")
    List<GutscheinDto> alle() {
        LocalDate heute = service.heute();
        return service.alle().stream().map(g -> GutscheinDto.von(g, heute)).toList();
    }

    @GetMapping("/{gutscheinId}")
    @Operation(operationId = "findeGutschein", summary = "Ein Gutschein")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Gutschein existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    GutscheinDto finde(@PathVariable UUID gutscheinId) {
        return GutscheinDto.von(service.finde(gutscheinId), service.heute());
    }
}
