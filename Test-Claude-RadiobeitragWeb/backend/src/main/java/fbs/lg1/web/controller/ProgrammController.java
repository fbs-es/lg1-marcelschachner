package fbs.lg1.web.controller;

import fbs.lg1.web.dto.FehlerDto;
import fbs.lg1.web.dto.ProgrammDto;
import fbs.lg1.web.dto.SendungDto;
import fbs.lg1.web.dto.SendungRequest;
import fbs.lg1.web.service.ProgrammService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Endpunkte für Tagesprogramme und ihre Sendungen.
 */
@RestController
@RequestMapping("/api/programme")
@Tag(name = "Programme", description = "Tagesprogramme und Sendungen")
public class ProgrammController {

    private final ProgrammService programmService;

    /**
     * Legt den Controller an.
     *
     * @param programmService der Service für Programme
     */
    public ProgrammController(ProgrammService programmService) {
        this.programmService = programmService;
    }

    /**
     * Liest ein Programm.
     *
     * @param id die ID des Programms
     * @return das Programm mit seinen Sendungen
     */
    @GetMapping("/{id}")
    @Operation(summary = "Programm lesen", description = "Liefert ein Tagesprogramm mit allen Sendungen.")
    @ApiResponse(responseCode = "200", description = "Das Programm")
    @ApiResponse(
            responseCode = "404",
            description = "Programm nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public ProgrammDto programmLesen(@PathVariable Long id) {
        return programmService.programmLesen(id);
    }

    /**
     * Listet die Sendungen eines Programms auf.
     *
     * @param id die ID des Programms
     * @return die Sendungen nach Startzeit
     */
    @GetMapping("/{id}/sendungen")
    @Operation(summary = "Sendungen auflisten", description = "Liefert alle Sendungen des Programms nach Startzeit sortiert.")
    @ApiResponse(responseCode = "200", description = "Liste der Sendungen")
    @ApiResponse(
            responseCode = "404",
            description = "Programm nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public List<SendungDto> sendungenAuflisten(@PathVariable Long id) {
        return programmService.sendungenAuflisten(id);
    }

    /**
     * Legt eine Sendung im Programm an.
     *
     * @param id die ID des Programms
     * @param request die Daten der Sendung
     * @return die neue Sendung
     */
    @PostMapping("/{id}/sendungen")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sendung anlegen", description = "Nimmt eine Sendung in das Programm auf. Zeitblöcke dürfen sich nicht überschneiden.")
    @ApiResponse(responseCode = "201", description = "Sendung wurde angelegt")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Werte",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Programm nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Zeitblock überschneidet sich mit einer anderen Sendung",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendungDto sendungAnlegen(@PathVariable Long id, @RequestBody SendungRequest request) {
        return programmService.sendungAnlegen(id, request);
    }
}
