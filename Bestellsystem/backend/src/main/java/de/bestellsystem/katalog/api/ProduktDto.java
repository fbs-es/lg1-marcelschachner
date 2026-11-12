package de.bestellsystem.katalog.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Produkt",
        requiredProperties = {"id", "titel", "listenpreis", "lagerbestand", "istVerfuegbar"})
public record ProduktDto(
        UUID id,
        String titel,
        GeldDto listenpreis,
        int lagerbestand,
        @Schema(description = "Lagerbestand > 0 (D-09)") boolean istVerfuegbar) {

    static ProduktDto von(Produkt produkt) {
        return new ProduktDto(
                produkt.getId(),
                produkt.getTitel(),
                GeldDto.von(produkt.getListenpreis()),
                produkt.getLagerbestand(),
                produkt.istVerfuegbar());
    }
}
