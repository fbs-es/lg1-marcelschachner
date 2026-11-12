package de.bestellsystem.gemeinsam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GeldTest {

    @Test
    @DisplayName("E00: 3 × 0,10 € = 0,30 € ohne Gleitkommafehler")
    void multipliziertOhneGleitkommafehler() {
        assertThat(Geld.ausCent(10).multipliziere(3)).isEqualTo(Geld.ausCent(30));
    }

    @Test
    @DisplayName("E00: 0,10 € + 0,20 € = 0,30 € ohne Gleitkommafehler")
    void addiertOhneGleitkommafehler() {
        assertThat(Geld.ausCent(10).addiere(Geld.ausCent(20))).isEqualTo(Geld.ausCent(30));
    }

    @Test
    void subtrahiertUndDarfNegativWerden() {
        Geld ergebnis = Geld.ausCent(500).subtrahiere(Geld.ausCent(700));

        assertThat(ergebnis).isEqualTo(Geld.ausCent(-200));
        assertThat(ergebnis.istNegativ()).isTrue();
    }

    @Test
    void multiplikationMitNullErgibtNull() {
        assertThat(Geld.ausCent(1999).multipliziere(0)).isEqualTo(Geld.NULL);
    }

    @ParameterizedTest(name = "{1} % von {0} Cent = {2} Cent")
    @CsvSource({
        "1999, 15, 300", // 299,85 → 300
        "100, 12.5, 13", // 12,5 → 13 (kaufmännisch auf)
        "5, 10, 1", //  0,5 → 1
        "4, 10, 0", //  0,4 → 0
        "-5, 10, -1", // -0,5 → -1 (vom Betrag her aufgerundet)
        "10000, 100, 10000",
        "10000, 0, 0"
    })
    @DisplayName("D-11: Prozentanteil wird kaufmännisch auf ganze Cent gerundet")
    void rundetProzentanteilKaufmaennisch(long cent, String prozent, long erwartet) {
        assertThat(Geld.ausCent(cent).prozentsatz(new BigDecimal(prozent)))
                .isEqualTo(Geld.ausCent(erwartet));
    }

    @Test
    void ueberlaufWirdNichtStillschweigendAbgeschnitten() {
        assertThatThrownBy(() -> Geld.ausCent(Long.MAX_VALUE).addiere(Geld.ausCent(1)))
                .isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Geld.ausCent(Long.MAX_VALUE).multipliziere(2))
                .isInstanceOf(ArithmeticException.class);
    }

    @Test
    void waehrungIstImmerEuro() {
        assertThat(Geld.ausCent(1).waehrung()).isEqualTo("EUR");
    }

    @Test
    void vergleichtNachBetrag() {
        assertThat(Geld.ausCent(100)).isGreaterThan(Geld.ausCent(99));
        assertThat(Geld.ausCent(100)).isEqualByComparingTo(Geld.ausCent(100));
    }

    @Test
    void zeigtBetragInEuroAn() {
        assertThat(Geld.ausCent(123456)).hasToString("1234,56 EUR");
        assertThat(Geld.ausCent(-5)).hasToString("-0,05 EUR");
    }
}
