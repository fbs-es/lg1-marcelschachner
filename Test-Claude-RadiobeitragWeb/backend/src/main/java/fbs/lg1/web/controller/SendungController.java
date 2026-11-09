package fbs.lg1.web.controller;

import fbs.lg1.web.dto.BeitragRequest;
import fbs.lg1.web.dto.EilmeldungEinfuegenRequest;
import fbs.lg1.web.dto.EilmeldungErgebnisDto;
import fbs.lg1.web.dto.FehlerDto;
import fbs.lg1.web.dto.KuerzenRequest;
import fbs.lg1.web.dto.SendeplanDto;
import fbs.lg1.web.dto.VerschiebenRequest;
import fbs.lg1.web.service.SendungService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-Endpunkte für den Sendeplan einer Sendung.
 */
@RestController
@RequestMapping("/api/sendungen")
@Tag(name = "Sendungen", description = "Sendeplan einer Sendung bearbeiten")
public class SendungController {

    private final SendungService sendungService;

    /**
     * Legt den Controller an.
     *
     * @param sendungService der Service für Sendungen
     */
    public SendungController(SendungService sendungService) {
        this.sendungService = sendungService;
    }

    /**
     * Liest den Sendeplan.
     *
     * @param id die ID der Sendung
     * @return der Sendeplan
     */
    @GetMapping("/{id}")
    @Operation(summary = "Sendeplan lesen", description = "Liefert die Sendung mit allen Beiträgen, Startzeitpunkten, Gesamtdauer und Restzeit.")
    @ApiResponse(responseCode = "200", description = "Der Sendeplan")
    @ApiResponse(
            responseCode = "404",
            description = "Sendung nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendeplanDto sendeplanLesen(@PathVariable Long id) {
        return sendungService.sendeplanLesen(id);
    }

    /**
     * Hängt einen Beitrag an.
     *
     * @param id die ID der Sendung
     * @param request der Beitrag mit Typ
     * @return der neue Sendeplan
     */
    @PostMapping("/{id}/beitraege")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Beitrag hinzufügen", description = "Hängt einen Beitrag an das Ende des Sendeplans an. Das Feld typ ist MUSIK, WERBUNG, MODERATION oder EILMELDUNG.")
    @ApiResponse(responseCode = "201", description = "Beitrag wurde hinzugefügt")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Werte oder unbekannter Typ",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Sendung nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Maximaldauer würde überschritten",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendeplanDto beitragHinzufuegen(
            @PathVariable Long id, @RequestBody BeitragRequest request) {
        return sendungService.beitragHinzufuegen(id, request);
    }

    /**
     * Kürzt einen Beitrag.
     *
     * @param id die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @param request die neue Dauer
     * @return der neue Sendeplan
     */
    @PostMapping("/{id}/beitraege/{beitragId}/kuerzen")
    @Operation(summary = "Beitrag kürzen", description = "Kürzt einen Beitrag, alle folgenden Beiträge rücken nach vorne.")
    @ApiResponse(responseCode = "200", description = "Beitrag wurde gekürzt")
    @ApiResponse(
            responseCode = "400",
            description = "Neue Dauer ist ungültig",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Sendung oder Beitrag nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendeplanDto beitragKuerzen(
            @PathVariable Long id, @PathVariable Long beitragId, @RequestBody KuerzenRequest request) {
        return sendungService.beitragKuerzen(id, beitragId, request.neueDauer());
    }

    /**
     * Verschiebt einen Beitrag.
     *
     * @param id die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @param request die neue Position
     * @return der neue Sendeplan
     */
    @PostMapping("/{id}/beitraege/{beitragId}/verschieben")
    @Operation(summary = "Beitrag verschieben", description = "Verschiebt einen Beitrag an eine neue Position, beginnend bei 0.")
    @ApiResponse(responseCode = "200", description = "Beitrag wurde verschoben")
    @ApiResponse(
            responseCode = "400",
            description = "Index liegt außerhalb des Sendeplans",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Sendung oder Beitrag nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendeplanDto beitragVerschieben(
            @PathVariable Long id,
            @PathVariable Long beitragId,
            @RequestBody VerschiebenRequest request) {
        return sendungService.beitragVerschieben(id, beitragId, request.neuerIndex());
    }

    /**
     * Löscht einen Beitrag.
     *
     * @param id die ID der Sendung
     * @param beitragId die ID des Beitrags
     * @return der neue Sendeplan
     */
    @DeleteMapping("/{id}/beitraege/{beitragId}")
    @Operation(summary = "Beitrag löschen", description = "Entfernt einen Beitrag, alle folgenden Beiträge rücken nach vorne.")
    @ApiResponse(responseCode = "200", description = "Beitrag wurde gelöscht")
    @ApiResponse(
            responseCode = "404",
            description = "Sendung oder Beitrag nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SendeplanDto beitragLoeschen(@PathVariable Long id, @PathVariable Long beitragId) {
        return sendungService.beitragLoeschen(id, beitragId);
    }

    /**
     * Fügt eine Eilmeldung an der Laufstelle ein.
     *
     * @param id die ID der Sendung
     * @param request die Eilmeldung mit Laufzeit
     * @return der neue Sendeplan und die Warnung oder null
     */
    @PostMapping("/{id}/eilmeldung")
    @Operation(summary = "Eilmeldung einfügen", description = "Fügt eine Eilmeldung an der Laufstelle ein. Mitten in einem Beitrag wird dieser geteilt und als Fortsetzung weitergeführt. Das Zeitlimit darf überschritten werden, dann ist das Feld warnung gesetzt.")
    @ApiResponse(responseCode = "200", description = "Eilmeldung wurde eingefügt")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Werte",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    @ApiResponse(
            responseCode = "404",
            description = "Sendung nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public EilmeldungErgebnisDto eilmeldungEinfuegen(
            @PathVariable Long id, @RequestBody EilmeldungEinfuegenRequest request) {
        return sendungService.eilmeldungEinfuegen(id, request);
    }
}
