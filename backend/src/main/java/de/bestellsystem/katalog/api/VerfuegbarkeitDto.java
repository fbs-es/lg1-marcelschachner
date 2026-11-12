package de.bestellsystem.katalog.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(
        name = "Verfuegbarkeit",
        requiredProperties = {"produktId", "menge", "istVerfuegbar"},
        description = "Ergebnis der Prüfung Lagerbestand ≥ menge (D-09)")
public record VerfuegbarkeitDto(UUID produktId, int menge, boolean istVerfuegbar) {}
