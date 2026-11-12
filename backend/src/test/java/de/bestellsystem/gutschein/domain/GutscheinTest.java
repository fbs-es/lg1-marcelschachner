package de.bestellsystem.gutschein.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.Geld;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GutscheinTest {

    static final LocalDate ZWOELFTER_NOVEMBER = LocalDate.of(2026, 11, 12);

    static Gutschein herbst10() {
        return Gutschein.prozent("HERBST10", new BigDecimal("10"), LocalDate.of(2026, 11, 30));
    }

    static Gutschein minus15() {
        return Gutschein.festbetrag("MINUS15", Geld.ausCent(1500), LocalDate.of(2026, 12, 31));
    }

    static Gutschein sommer20() {
        return Gutschein.prozent("SOMMER20", new BigDecimal("20"), LocalDate.of(2026, 8, 31));
    }

    @Test
    @DisplayName("US-08.1 AK1: 10 % auf 55,00 € ergibt 5,50 € Rabatt")
    void prozentrabatt() {
        assertThat(herbst10().berechneRabatt(Geld.ausCent(5500))).isEqualTo(Geld.ausCent(550));
    }

    @Test
    @DisplayName("US-08.1 AK2: Festbetrag 15,00 € auf 55,00 € ergibt 15,00 € Rabatt")
    void festbetrag() {
        assertThat(minus15().berechneRabatt(Geld.ausCent(5500))).isEqualTo(Geld.ausCent(1500));
    }

    @Test
    @DisplayName("US-08.1 AK3: Festbetrag über dem Warenwert ist auf den Warenwert begrenzt")
    void festbetragHoechstensWarenwert() {
        assertThat(minus15().berechneRabatt(Geld.ausCent(1000))).isEqualTo(Geld.ausCent(1000));
    }

    @Test
    @DisplayName("US-08.1 AK4: SOMMER20 ist am 12.11.2026 abgelaufen")
    void abgelaufen() {
        assertThat(sommer20().istGueltigAm(ZWOELFTER_NOVEMBER)).isFalse();
    }

    @Test
    @DisplayName("US-08.1 AK5: Ablaufdatum gilt inklusive")
    void ablaufdatumInklusive() {
        assertThat(herbst10().istGueltigAm(LocalDate.of(2026, 11, 30))).isTrue();
        assertThat(herbst10().istGueltigAm(LocalDate.of(2026, 12, 1))).isFalse();
    }

    @Test
    @DisplayName("US-08.1 AK6: Code wird getrimmt und großgeschrieben (D-04, D-17)")
    void codeNormalisiert() {
        assertThat(Gutschein.normalisiere(" herbst10 ")).isEqualTo("HERBST10");
        assertThat(Gutschein.prozent(" herbst10 ", BigDecimal.TEN, ZWOELFTER_NOVEMBER).getCode())
                .isEqualTo("HERBST10");
    }

    @Test
    @DisplayName("US-08.1 AK9: 10 % auf 0,25 € ergibt kaufmännisch gerundet 0,03 €")
    void kaufmaennischGerundet() {
        assertThat(herbst10().berechneRabatt(Geld.ausCent(25))).isEqualTo(Geld.ausCent(3));
    }

    @Test
    @DisplayName("D-04: Prozent muss größer 0 und höchstens 100 sein")
    void prozentGrenzen() {
        assertThatThrownBy(() -> Gutschein.prozent("X", BigDecimal.ZERO, ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(
                        () -> Gutschein.prozent("X", new BigDecimal("100.01"), ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(
                        Gutschein.prozent("X", new BigDecimal("100"), ZWOELFTER_NOVEMBER)
                                .berechneRabatt(Geld.ausCent(999)))
                .isEqualTo(Geld.ausCent(999));
    }

    @Test
    @DisplayName("D-17: Prozent hat höchstens zwei Nachkommastellen")
    void prozentNachkommastellen() {
        assertThatThrownBy(
                        () -> Gutschein.prozent("X", new BigDecimal("12.345"), ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("D-04: Festbetrag muss größer 0 sein")
    void festbetragPositiv() {
        assertThatThrownBy(() -> Gutschein.festbetrag("X", Geld.NULL, ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("D-04: Code und Ablaufdatum sind Pflicht")
    void pflichtfelder() {
        assertThatThrownBy(() -> Gutschein.prozent(" ", BigDecimal.TEN, ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Gutschein.prozent("X", BigDecimal.TEN, null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
