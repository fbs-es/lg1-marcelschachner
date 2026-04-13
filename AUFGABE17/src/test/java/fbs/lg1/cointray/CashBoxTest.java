package fbs.lg1.cointray;

import java.util.EnumMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class CashBoxTest {

    @Test
    void testGetCoinCount() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.EURO_1, 5);
        assertThat(cashBox.getCoinCount(Muenzen.EURO_1)).isEqualTo(5);
    }

    @Test
    void testSetCoinCount() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.CENT_50, 10);
        assertThat(cashBox.getCoinCount(Muenzen.CENT_50)).isEqualTo(10);
    }

    @Test
    void testBerechneKassenstand() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.EURO_2, 1);
        cashBox.setCoinCount(Muenzen.EURO_1, 2);
        cashBox.setCoinCount(Muenzen.CENT_50, 4);
        assertThat(cashBox.berechneKassenstand()).isEqualTo(200 + 200 + 200);
    }

    @Test
    void testBerechneKassenstandEmpty() {
        CashBox cashBox = new CashBox();
        assertThat(cashBox.berechneKassenstand()).isEqualTo(0);
    }

    @Test
    void testKannWechselnTrue() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.EURO_1, 2);
        cashBox.setCoinCount(Muenzen.CENT_50, 1);
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        assertThat(cashBox.kannWechseln(150, einwurf)).isTrue();
    }

    @Test
    void testKannWechselnFalse() {
        CashBox cashBox = new CashBox();
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        assertThat(cashBox.kannWechseln(150, einwurf)).isFalse();
    }

    @Test
    void testKannWechselnWithEinwurf() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.CENT_50, 1);
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_1, 1);
        assertThat(cashBox.kannWechseln(50, einwurf)).isTrue();
    }

    @Test
    void testGebeWechselgeldSuccess() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.EURO_1, 2);
        cashBox.setCoinCount(Muenzen.CENT_50, 1);
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        Map<Muenzen, Integer> wechsel = cashBox.gebeWechselgeld(150, einwurf);
        assertThat(wechsel).isNotNull();
        assertThat(wechsel.get(Muenzen.EURO_1)).isEqualTo(1);
        assertThat(wechsel.get(Muenzen.CENT_50)).isEqualTo(1);
    }

    @Test
    void testGebeWechselgeldFail() {
        CashBox cashBox = new CashBox();
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        Map<Muenzen, Integer> wechsel = cashBox.gebeWechselgeld(150, einwurf);
        assertThat(wechsel).isNull();
    }

    @Test
    void testGebeWechselgeldReducesCoinCount() {
        CashBox cashBox = new CashBox();
        cashBox.setCoinCount(Muenzen.EURO_1, 2);
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        cashBox.gebeWechselgeld(100, einwurf);
        assertThat(cashBox.getCoinCount(Muenzen.EURO_1)).isEqualTo(1);
    }
}
