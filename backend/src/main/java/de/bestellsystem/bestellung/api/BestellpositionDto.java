package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.domain.Bestellposition;
import de.bestellsystem.gemeinsam.api.GeldDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Bestellposition",
        requiredProperties = {
            "zeilennummer",
            "produktId",
            "titel",
            "menge",
            "einzelpreis",
            "zwischenbetrag"
        })
public record BestellpositionDto(
        int zeilennummer,
        UUID produktId,
        String titel,
        int menge,
        @Schema(description = "Listenpreis zum Checkout, eingefroren (D-10)") GeldDto einzelpreis,
        @Schema(description = "menge × einzelpreis") GeldDto zwischenbetrag) {

    static BestellpositionDto von(Bestellposition position) {
        return new BestellpositionDto(
                position.getZeilennummer(),
                position.getProdukt().getId(),
                position.getProdukt().getTitel(),
                position.getMenge(),
                GeldDto.von(position.getEinzelpreis()),
                GeldDto.von(position.berechneZwischenbetrag()));
    }
}
