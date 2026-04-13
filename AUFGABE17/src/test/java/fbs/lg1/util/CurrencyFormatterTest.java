package fbs.lg1.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CurrencyFormatterTest {

    @Test
    void testCurrencyFormatter() {
        assertThat(CurrencyFormatter.toEuroFormat(100)).isEqualTo("1,00 €");
        assertThat(CurrencyFormatter.toEuroFormat(150)).isEqualTo("1,50 €");
    }

    @Test
    void testFormatZero() {
        assertThat(CurrencyFormatter.toEuroFormat(0)).isEqualTo("0,00 €");
    }

    @Test
    void testFormatCents() {
        assertThat(CurrencyFormatter.toEuroFormat(1)).isEqualTo("0,01 €");
        assertThat(CurrencyFormatter.toEuroFormat(50)).isEqualTo("0,50 €");
        assertThat(CurrencyFormatter.toEuroFormat(99)).isEqualTo("0,99 €");
    }

    @Test
    void testFormatMultipleEuros() {
        assertThat(CurrencyFormatter.toEuroFormat(200)).isEqualTo("2,00 €");
        assertThat(CurrencyFormatter.toEuroFormat(500)).isEqualTo("5,00 €");
        assertThat(CurrencyFormatter.toEuroFormat(999)).isEqualTo("9,99 €");
    }

    @Test
    void testFormatLargeAmount() {
        assertThat(CurrencyFormatter.toEuroFormat(10000)).isEqualTo("100,00 €");
    }
}
