package de.bestellsystem.merkliste.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.merkliste.domain.Merklisteneintrag;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Schema(
        name = "Merklisteneintrag",
        description = "Gemerktes Produkt mit aktuellem Preis und aktueller Verfügbarkeit (D-07)",
        requiredProperties = {
            "produktId",
            "titel",
            "listenpreis",
            "istVerfuegbar",
            "hinzugefuegtAm"
        })
record MerklisteneintragDto(
        UUID produktId,
        String titel,
        @Schema(description = "Aktueller Listenpreis") GeldDto listenpreis,
        @Schema(description = "Lagerbestand > 0 (D-09)") boolean istVerfuegbar,
        @Schema(description = "Merkdatum in der Zeitzone des Shops (D-21)")
                LocalDate hinzugefuegtAm) {

    static MerklisteneintragDto von(Merklisteneintrag eintrag, ZoneId zeitzone) {
        Produkt produkt = eintrag.getProdukt();
        return new MerklisteneintragDto(
                produkt.getId(),
                produkt.getTitel(),
                GeldDto.von(produkt.getListenpreis()),
                produkt.istVerfuegbar(),
                eintrag.merkdatum(zeitzone));
    }
}
