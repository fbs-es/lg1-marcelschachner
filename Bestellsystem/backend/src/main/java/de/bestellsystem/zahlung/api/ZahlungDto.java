package de.bestellsystem.zahlung.api;

import de.bestellsystem.gemeinsam.api.GeldDto;
import de.bestellsystem.zahlung.domain.Buchungsart;
import de.bestellsystem.zahlung.domain.Transaktionsstatus;
import de.bestellsystem.zahlung.domain.Zahlung;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

@Schema(
        name = "Zahlung",
        requiredProperties = {"id", "zahlungsart", "buchungsart", "transaktionsstatus", "betrag"})
public record ZahlungDto(
        UUID id,
        Zahlungsart zahlungsart,
        Buchungsart buchungsart,
        Transaktionsstatus transaktionsstatus,
        GeldDto betrag,
        @Schema(description = "Leer, solange kein Geld geflossen ist (D-17)")
                Instant zahlungszeitpunkt) {

    public static ZahlungDto von(Zahlung zahlung) {
        return new ZahlungDto(
                zahlung.getId(),
                zahlung.getZahlungsart(),
                zahlung.getBuchungsart(),
                zahlung.getTransaktionsstatus(),
                GeldDto.von(zahlung.getBetrag()),
                zahlung.getZahlungszeitpunkt().orElse(null));
    }
}
