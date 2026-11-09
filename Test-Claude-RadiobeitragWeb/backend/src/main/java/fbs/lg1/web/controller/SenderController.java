package fbs.lg1.web.controller;

import fbs.lg1.web.dto.FehlerDto;
import fbs.lg1.web.dto.SenderDto;
import fbs.lg1.web.dto.SenderRequest;
import fbs.lg1.web.service.SenderService;
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
 * REST-Endpunkte für Radiosender.
 */
@RestController
@RequestMapping("/api/sender")
@Tag(name = "Sender", description = "Radiosender mit ihrem Tagesprogramm")
public class SenderController {

    private final SenderService senderService;

    /**
     * Legt den Controller an.
     *
     * @param senderService der Service für Sender
     */
    public SenderController(SenderService senderService) {
        this.senderService = senderService;
    }

    /**
     * Legt einen Sender samt Programm an.
     *
     * @param request Name, Frequenz und Datum
     * @return der neue Sender
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Sender anlegen", description = "Legt einen Sender mit leerem Programm für einen Tag an.")
    @ApiResponse(responseCode = "201", description = "Sender wurde angelegt")
    @ApiResponse(
            responseCode = "400",
            description = "Ungültige Werte",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SenderDto senderAnlegen(@RequestBody SenderRequest request) {
        return senderService.senderAnlegen(request);
    }

    /**
     * @return alle Sender
     */
    @GetMapping
    @Operation(summary = "Sender auflisten", description = "Liefert alle gespeicherten Sender.")
    @ApiResponse(responseCode = "200", description = "Liste der Sender")
    public List<SenderDto> senderAuflisten() {
        return senderService.senderAuflisten();
    }

    /**
     * Liest einen Sender.
     *
     * @param id die ID des Senders
     * @return der Sender
     */
    @GetMapping("/{id}")
    @Operation(summary = "Sender lesen", description = "Liefert einen Sender mit seiner Programm-ID.")
    @ApiResponse(responseCode = "200", description = "Der Sender")
    @ApiResponse(
            responseCode = "404",
            description = "Sender nicht gefunden",
            content = @Content(schema = @Schema(implementation = FehlerDto.class)))
    public SenderDto senderLesen(@PathVariable Long id) {
        return senderService.senderLesen(id);
    }
}
