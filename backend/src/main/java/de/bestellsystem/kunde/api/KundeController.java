package de.bestellsystem.kunde.api;

import de.bestellsystem.kunde.application.KundeService;
import de.bestellsystem.kunde.domain.Kunde;
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
@RequestMapping("/api/v1/kunden")
@Tag(name = "Kunden", description = "Kundenkonto (E01)")
class KundeController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final KundeService service;

    KundeController(KundeService service) {
        this.service = service;
    }

    @PostMapping
    @Operation(
            operationId = "registriereKunde",
            summary = "Kunde mit erster Lieferadresse registrieren")
    @ApiResponse(responseCode = "201", description = "Registriert, Location zeigt auf den Kunden")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Eingabe, z. B. fehlende Lieferadresse (US-01.1 AK2, AK4)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "E-Mail bereits vergeben (US-01.1 AK3)",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<KundeDto> registriere(@Valid @RequestBody RegistrierungDto eingabe) {
        Kunde kunde =
                service.registriere(
                        eingabe.vorname(),
                        eingabe.nachname(),
                        eingabe.email(),
                        eingabe.lieferadresse().alsAnschrift(),
                        eingabe.istAuchRechnungsadresse());
        URI ort =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(kunde.getId())
                        .toUri();
        return ResponseEntity.created(ort).body(KundeDto.von(kunde));
    }

    @GetMapping
    @Operation(operationId = "listeKunden", summary = "Alle Kunden ohne Adressbuch, nach Name")
    List<KundeUebersichtDto> alle() {
        return service.alle().stream().map(KundeUebersichtDto::von).toList();
    }

    @GetMapping("/{kundenId}")
    @Operation(operationId = "findeKunde", summary = "Ein Kunde mit Adressbuch")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    KundeDto finde(@PathVariable UUID kundenId) {
        return KundeDto.von(service.finde(kundenId));
    }
}
