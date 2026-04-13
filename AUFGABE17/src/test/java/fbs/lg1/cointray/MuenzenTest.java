package fbs.lg1.cointray;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class MuenzenTest {

    @Test
    void testMuenzenWerte() {
        assertThat(Muenzen.CENT_1.getValueInCents()).isEqualTo(1);
        assertThat(Muenzen.CENT_50.getValueInCents()).isEqualTo(50);
        assertThat(Muenzen.EURO_1.getValueInCents()).isEqualTo(100);
        assertThat(Muenzen.EURO_2.getValueInCents()).isEqualTo(200);
    }

    @Test
    void testGetDescription() {
        assertThat(Muenzen.EURO_2.getDescription()).isEqualTo("2 Euro");
        assertThat(Muenzen.CENT_1.getDescription()).isEqualTo("1 Cent");
    }

    @Test
    void testFromCentsValidValues() {
        assertThat(Muenzen.fromCents(200)).isEqualTo(Muenzen.EURO_2);
        assertThat(Muenzen.fromCents(100)).isEqualTo(Muenzen.EURO_1);
        assertThat(Muenzen.fromCents(50)).isEqualTo(Muenzen.CENT_50);
        assertThat(Muenzen.fromCents(1)).isEqualTo(Muenzen.CENT_1);
    }

    @Test
    void testFromCentsInvalidValue() {
        assertThat(Muenzen.fromCents(99)).isNull();
        assertThat(Muenzen.fromCents(150)).isNull();
        assertThat(Muenzen.fromCents(0)).isNull();
    }

    @Test
    void testIsValidTrue() {
        assertThat(Muenzen.isValid(200)).isTrue();
        assertThat(Muenzen.isValid(50)).isTrue();
        assertThat(Muenzen.isValid(1)).isTrue();
    }

    @Test
    void testIsValidFalse() {
        assertThat(Muenzen.isValid(99)).isFalse();
        assertThat(Muenzen.isValid(0)).isFalse();
        assertThat(Muenzen.isValid(300)).isFalse();
    }
}
