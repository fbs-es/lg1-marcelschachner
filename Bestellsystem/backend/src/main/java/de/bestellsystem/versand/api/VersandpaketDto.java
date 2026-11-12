package de.bestellsystem.versand.api;

import de.bestellsystem.versand.domain.Lieferstatus;
import de.bestellsystem.versand.domain.Logistikdienstleister;
import de.bestellsystem.versand.domain.Versandpaket;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(
        name = "Versandpaket",
        requiredProperties = {
            "id",
            "dienstleister",
            "sendungsnummer",
            "lieferstatus",
            "angelegtAm",
            "statusGeaendertAm",
            "erlaubteFolgestatus"
        })
public record VersandpaketDto(
        UUID id,
        Logistikdienstleister dienstleister,
        @Schema(description = "Getrimmt und großgeschrieben (D-22)", example = "AB123DE")
                String sendungsnummer,
        Lieferstatus lieferstatus,
        Instant angelegtAm,
        @Schema(description = "Zeitpunkt des letzten Statuswechsels, anfangs gleich angelegtAm")
                Instant statusGeaendertAm,
        @Schema(description = "Status, in die das Paket wechseln darf; leer im Endzustand (D-22)")
                List<Lieferstatus> erlaubteFolgestatus) {

    public static VersandpaketDto von(Versandpaket paket) {
        return new VersandpaketDto(
                paket.getId(),
                paket.getDienstleister(),
                paket.getSendungsnummer(),
                paket.getLieferstatus(),
                paket.getAngelegtAm(),
                paket.getStatusGeaendertAm(),
                paket.erlaubteFolgestatus());
    }
}
