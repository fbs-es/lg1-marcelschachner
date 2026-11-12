package de.bestellsystem.bestellung.api;

import de.bestellsystem.bestellung.domain.Bestellung;
import de.bestellsystem.gemeinsam.api.GeldDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
        name = "BestellungUebersicht",
        description = "Eintrag der Bestellhistorie (US-04.2)",
        requiredProperties = {"bestellnummer", "bestelldatum", "anzahlPositionen", "gesamtwert"})
public record BestellungUebersichtDto(
        @Schema(example = "B-2026-000123") String bestellnummer,
        LocalDate bestelldatum,
        @Schema(description = "Anzahl der Positionen, nicht der Stückzahl") int anzahlPositionen,
        @Schema(description = "max(0, warenwert − rabattBetrag), berechnet (D-01)")
                GeldDto gesamtwert) {

    static BestellungUebersichtDto von(Bestellung bestellung) {
        return new BestellungUebersichtDto(
                bestellung.getBestellnummer(),
                bestellung.getBestelldatum(),
                bestellung.getPositionen().size(),
                GeldDto.von(bestellung.berechneGesamtwert()));
    }
}
