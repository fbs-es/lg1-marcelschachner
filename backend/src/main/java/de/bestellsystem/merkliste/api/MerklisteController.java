package de.bestellsystem.merkliste.api;

import de.bestellsystem.merkliste.application.MerklisteService;
import de.bestellsystem.merkliste.application.MerklisteService.Merkergebnis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/kunden/{kundenId}/merkliste")
@Tag(name = "Merkliste", description = "Gemerkte Produkte eines Kunden (E03, D-07, D-21)")
class MerklisteController {

    private static final String PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON_VALUE;

    private final MerklisteService service;
    private final Clock uhr;

    MerklisteController(MerklisteService service, Clock uhr) {
        this.service = service;
        this.uhr = uhr;
    }

    @GetMapping
    @Operation(
            operationId = "listeMerkliste",
            summary = "Beobachtete Artikel, neueste zuerst (US-03.1)",
            description =
                    "Liefert Merkdatum, aktuellen Listenpreis und aktuelle Verfügbarkeit; leere"
                            + " Liste, wenn nichts gemerkt ist.")
    @ApiResponse(responseCode = "200", description = "Merkliste, ggf. leer")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    List<MerklisteneintragDto> liste(@PathVariable UUID kundenId) {
        return service.merkliste(kundenId).stream()
                .map(eintrag -> MerklisteneintragDto.von(eintrag, uhr.getZone()))
                .toList();
    }

    @PutMapping("/{produktId}")
    @Operation(
            operationId = "merkeProdukt",
            summary = "Produkt merken (US-03.1)",
            description =
                    "Merkt das Produkt mit heutigem Datum. Ist es schon gemerkt, bleibt der"
                            + " Eintrag mit seinem ursprünglichen Datum unverändert (I-4, D-21).")
    @ApiResponse(responseCode = "201", description = "Neu gemerkt, Location zeigt auf den Eintrag")
    @ApiResponse(responseCode = "200", description = "War schon gemerkt, unverändert")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde oder Produkt existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<MerklisteneintragDto> merke(
            @PathVariable UUID kundenId, @PathVariable UUID produktId) {
        Merkergebnis ergebnis = service.merke(kundenId, produktId);
        MerklisteneintragDto eintrag = MerklisteneintragDto.von(ergebnis.eintrag(), uhr.getZone());
        if (!ergebnis.istNeu()) {
            return ResponseEntity.ok(eintrag);
        }
        return ResponseEntity.created(
                        ServletUriComponentsBuilder.fromCurrentRequest().build().toUri())
                .body(eintrag);
    }

    @GetMapping("/{produktId}")
    @Operation(operationId = "findeMerklisteneintrag", summary = "Ein Eintrag der Merkliste")
    @ApiResponse(responseCode = "200", description = "Gefunden")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht oder Produkt steht nicht auf der Merkliste",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    MerklisteneintragDto finde(@PathVariable UUID kundenId, @PathVariable UUID produktId) {
        return MerklisteneintragDto.von(service.eintrag(kundenId, produktId), uhr.getZone());
    }

    @DeleteMapping("/{produktId}")
    @Operation(
            operationId = "entferneVonMerkliste",
            summary = "Produkt von der Merkliste entfernen (US-03.2)",
            description =
                    "Auch wenn das Produkt nicht gemerkt war: 204, nichts ändert sich (D-21).")
    @ApiResponse(responseCode = "204", description = "Entfernt oder war nicht gemerkt")
    @ApiResponse(
            responseCode = "404",
            description = "Kunde existiert nicht",
            content =
                    @Content(
                            mediaType = PROBLEM_JSON,
                            schema = @Schema(implementation = ProblemDetail.class)))
    ResponseEntity<Void> entferne(@PathVariable UUID kundenId, @PathVariable UUID produktId) {
        service.entferne(kundenId, produktId);
        return ResponseEntity.noContent().build();
    }
}
