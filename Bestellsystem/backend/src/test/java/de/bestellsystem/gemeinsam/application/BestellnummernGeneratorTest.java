package de.bestellsystem.gemeinsam.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BestellnummernGeneratorTest {

    private final AtomicLong zaehlerstand = new AtomicLong(122);
    private final BestellnummernGenerator generator =
            new BestellnummernGenerator(zaehlerstand::incrementAndGet);

    @Test
    @DisplayName("D-13: Bestellnummer hat das Format B-JJJJ-NNNNNN mit Jahr des Bestelldatums")
    void erzeugtLesbareNummer() {
        assertThat(generator.erzeuge(LocalDate.of(2026, 3, 15))).isEqualTo("B-2026-000123");
    }

    @Test
    void jahrKommtVomBestelldatumZaehlerLaeuftWeiter() {
        assertThat(generator.erzeuge(LocalDate.of(2026, 12, 31))).isEqualTo("B-2026-000123");
        assertThat(generator.erzeuge(LocalDate.of(2027, 1, 1))).isEqualTo("B-2027-000124");
    }

    @Test
    void groessereZaehlerWerdenNichtAbgeschnitten() {
        zaehlerstand.set(1_234_566);

        assertThat(generator.erzeuge(LocalDate.of(2026, 1, 1))).isEqualTo("B-2026-1234567");
    }
}
