package de.bestellsystem.gutschein.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.bestellsystem.gemeinsam.domain.FachlicheAblehnungException;
import de.bestellsystem.gemeinsam.domain.Geld;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    private static final LocalDate SILVESTER = LocalDate.of(2026, 12, 31);

    @Test
    @DisplayName("US-08.2 AK1: Prozentgutschein, Code getrimmt und großgeschrieben")
    void prozentgutscheinAnlegen() {
        Gutschein gutschein =
                Gutschein.legeAn(
                        " herbst10 ",
                        RabattArt.PROZENT,
                        new BigDecimal("10"),
                        null,
                        SILVESTER,
                        ZWOELFTER_NOVEMBER);

        assertThat(gutschein.getCode()).isEqualTo("HERBST10");
        assertThat(gutschein.getRabattArt()).isEqualTo(RabattArt.PROZENT);
        assertThat(gutschein.getProzent()).isEqualByComparingTo("10");
        assertThat(gutschein.getFestbetrag()).isNull();
        assertThat(gutschein.getAblaufdatum()).isEqualTo(SILVESTER);
    }

    @Test
    @DisplayName("US-08.2 AK2: Festbetrag 5,00 €")
    void festbetragAnlegen() {
        Gutschein gutschein =
                Gutschein.legeAn(
                        "MINUS5",
                        RabattArt.FESTBETRAG,
                        null,
                        Geld.ausCent(500),
                        SILVESTER,
                        ZWOELFTER_NOVEMBER);

        assertThat(gutschein.getFestbetrag()).isEqualTo(Geld.ausCent(500));
        assertThat(gutschein.getProzent()).isNull();
        assertThat(gutschein.berechneRabatt(Geld.ausCent(2000))).isEqualTo(Geld.ausCent(500));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "100.01", "10.555"})
    @DisplayName("US-08.2 AK3: Prozent 0, negativ, über 100 oder mit drei Nachkommastellen: 400")
    void ungueltigesProzent(String prozent) {
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X",
                                RabattArt.PROZENT,
                                new BigDecimal(prozent),
                                null,
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-08.2 AK3: Festbetrag 0 € oder Rabattwert passt nicht zur Rabattart: 400")
    void rabattwertPasstNicht() {
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X",
                                RabattArt.FESTBETRAG,
                                null,
                                Geld.NULL,
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X",
                                RabattArt.PROZENT,
                                BigDecimal.TEN,
                                Geld.ausCent(500),
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X",
                                RabattArt.FESTBETRAG,
                                BigDecimal.TEN,
                                null,
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X", null, BigDecimal.TEN, null, SILVESTER, ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HERBST 10", "HERBST_10", "RABATT!", "ÄPFEL"})
    @DisplayName("US-08.2 AK4: Code nur aus Buchstaben, Ziffern und Bindestrich")
    void ungueltigeZeichenImCode(String code) {
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                code,
                                RabattArt.PROZENT,
                                BigDecimal.TEN,
                                null,
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-08.2 AK4: Code mit Bindestrich und 50 Zeichen ist erlaubt, 51 nicht")
    void codeLaenge() {
        String fuenfzig = "WINTER-" + "A".repeat(43);

        assertThat(
                        Gutschein.legeAn(
                                        fuenfzig,
                                        RabattArt.PROZENT,
                                        BigDecimal.TEN,
                                        null,
                                        SILVESTER,
                                        ZWOELFTER_NOVEMBER)
                                .getCode())
                .isEqualTo(fuenfzig);
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                fuenfzig + "B",
                                RabattArt.PROZENT,
                                BigDecimal.TEN,
                                null,
                                SILVESTER,
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("US-08.2 AK5: Ablaufdatum gestern wird fachlich abgelehnt, heute ist erlaubt")
    void ablaufdatumNichtInDerVergangenheit() {
        assertThatThrownBy(() ->
                        Gutschein.legeAn(
                                "X",
                                RabattArt.PROZENT,
                                BigDecimal.TEN,
                                null,
                                ZWOELFTER_NOVEMBER.minusDays(1),
                                ZWOELFTER_NOVEMBER))
                .isInstanceOf(FachlicheAblehnungException.class);

        Gutschein heute =
                Gutschein.legeAn(
                        "X",
                        RabattArt.PROZENT,
                        BigDecimal.TEN,
                        null,
                        ZWOELFTER_NOVEMBER,
                        ZWOELFTER_NOVEMBER);
        assertThat(heute.istGueltigAm(ZWOELFTER_NOVEMBER)).isTrue();
    }
}
