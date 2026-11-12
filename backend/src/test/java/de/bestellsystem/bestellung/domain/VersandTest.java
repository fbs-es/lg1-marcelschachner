package de.bestellsystem.bestellung.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Anschrift;
import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import de.bestellsystem.gemeinsam.domain.NichtGefundenException;
import de.bestellsystem.katalog.domain.Produkt;
import de.bestellsystem.versand.domain.Lieferstatus;
import de.bestellsystem.versand.domain.Logistikdienstleister;
import de.bestellsystem.versand.domain.Versandpaket;
import de.bestellsystem.zahlung.domain.Zahlungsart;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Versandpakete als Teil der Bestellung (US-07.1 bis US-07.3, D-22). */
class VersandTest {

    private static final Instant AM_12_11 = Instant.parse("2026-11-12T09:30:00Z");
    private static final Instant AM_13_11 = Instant.parse("2026-11-13T08:00:00Z");

    private final Bestellung bestellung =
            Bestellung.gibAuf(
                    "B-2026-000001",
                    UUID.randomUUID(),
                    LocalDate.of(2026, 11, 12),
                    Period.ofDays(14),
                    new Anschrift("Hauptstr. 1", "Berlin", "10115", "DE"),
                    List.of(new Bestellwunsch(new Produkt("Artikel", Geld.ausCent(1_000), 5), 1)),
                    Zahlungsart.RECHNUNG,
                    null);

    @Test
    @DisplayName("US-07.1 AK2: neue Bestellung hat keine Pakete")
    void ohnePaket() {
        assertThat(bestellung.getPakete()).isEmpty();
    }

    @Test
    @DisplayName("US-07.1 AK1/AK3: Pakete gehören zur Bestellung, in Anlagereihenfolge")
    void mehrerePakete() {
        Versandpaket dhl =
                bestellung.legePaketAn(Logistikdienstleister.DHL, "00340434161094042557", AM_12_11);
        Versandpaket dpd =
                bestellung.legePaketAn(Logistikdienstleister.DPD, "01234567890123", AM_12_11);

        assertThat(bestellung.getPakete()).containsExactly(dhl, dpd);
        assertThat(dhl.getLieferstatus()).isEqualTo(Lieferstatus.ANGELEGT);
        assertThat(bestellung.paket(dpd.getId())).isSameAs(dpd);
    }

    @Test
    @DisplayName("US-07.2 AK1: Statuswechsel über die Bestellung")
    void statuswechsel() {
        Versandpaket paket = bestellung.legePaketAn(Logistikdienstleister.DHL, "AB123DE", AM_12_11);

        bestellung.aendereLieferstatus(paket.getId(), Lieferstatus.UEBERGEBEN, AM_13_11);

        assertThat(paket.getLieferstatus()).isEqualTo(Lieferstatus.UEBERGEBEN);
        assertThat(paket.getStatusGeaendertAm()).isEqualTo(AM_13_11);
        assertThatThrownBy(() ->
                        bestellung.aendereLieferstatus(
                                paket.getId(), Lieferstatus.ANGELEGT, AM_13_11))
                .isInstanceOf(FachlicheAblehnungException.class);
    }

    @Test
    @DisplayName("US-07.2 AK7, US-07.3 AK3: fremdes Paket ist nicht gefunden")
    void fremdesPaket() {
        bestellung.legePaketAn(Logistikdienstleister.DHL, "AB123DE", AM_12_11);
        UUID fremd = UUID.randomUUID();

        assertThatThrownBy(() -> bestellung.paket(fremd))
                .isInstanceOf(NichtGefundenException.class);
        assertThatThrownBy(() ->
                        bestellung.aendereLieferstatus(fremd, Lieferstatus.UEBERGEBEN, AM_13_11))
                .isInstanceOf(NichtGefundenException.class);
    }
}
