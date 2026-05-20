package fbs.lg1;

import java.util.EnumMap;
import java.util.Map;
import fbs.lg1.cointray.Muenzen;
import fbs.lg1.inventory.Drinks;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class GetraekautoamtTest {

    @Test
    void testBerechneEinwurfSingleCoin() {
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_1, 1);
        assertThat(Getraenkautomat.berechneEinwurf(einwurf)).isEqualTo(100);
    }

    @Test
    void testBerechneEinwurfMultipleCoins() {
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_1, 1);
        einwurf.put(Muenzen.CENT_50, 1);
        assertThat(Getraenkautomat.berechneEinwurf(einwurf)).isEqualTo(150);
    }

    @Test
    void testBerechneEinwurfMultipleSameCoin() {
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_2, 3);
        assertThat(Getraenkautomat.berechneEinwurf(einwurf)).isEqualTo(600);
    }

    @Test
    void testBerechneEinwurfEmpty() {
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        assertThat(Getraenkautomat.berechneEinwurf(einwurf)).isEqualTo(0);
    }

    @Test
    void testBerechneEinwurfComplex() {
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_2, 1);
        einwurf.put(Muenzen.EURO_1, 2);
        einwurf.put(Muenzen.CENT_50, 2);
        assertThat(Getraenkautomat.berechneEinwurf(einwurf)).isEqualTo(500);
    }

    @Test
    void testPruefeNachfuellbedarf() {
        Getraenkautomat automat = new Getraenkautomat();
        automat.setStockQuantity(Drinks.COLA, 0);
        assertThat(automat.pruefeNachfuellbedarf()).containsKey(Drinks.COLA);
        assertThat(automat.pruefeNachfuellbedarf()).doesNotContainKey(Drinks.ENERGY_DRINK);
    }

    @Test
    void testErmittleBedarf() {
        Getraenkautomat automat = new Getraenkautomat();
        automat.setStockQuantity(Drinks.COLA, 0);
        assertThat(automat.ermittleBedarf()).containsKey(Drinks.COLA);
        assertThat(automat.ermittleBedarf().get(Drinks.COLA)).isEqualTo(Drinks.COLA.getMaximumStock());
        assertThat(automat.ermittleBedarf()).doesNotContainKey(Drinks.ENERGY_DRINK);
    }

    @Test
    void testAuffuellen() {
        Getraenkautomat automat = new Getraenkautomat();
        automat.setStockQuantity(Drinks.COLA, 0);
        Map<Drinks, Integer> lieferung = new EnumMap<>(Drinks.class);
        lieferung.put(Drinks.COLA, 10);
        automat.auffuellen(lieferung);
        assertThat(automat.getStockQuantity(Drinks.COLA)).isEqualTo(10);
    }

    @Test
    void testAuffuellenUeberMaximum() {
        Getraenkautomat automat = new Getraenkautomat();
        automat.setStockQuantity(Drinks.COLA, 0);
        Map<Drinks, Integer> lieferung = new EnumMap<>(Drinks.class);
        lieferung.put(Drinks.COLA, 999);
        automat.auffuellen(lieferung);
        assertThat(automat.getStockQuantity(Drinks.COLA)).isEqualTo(Drinks.COLA.getMaximumStock());
    }

    @Test
    void testBerechneKassenstand() {
        Getraenkautomat automat = new Getraenkautomat();
        assertThat(automat.berechneKassenstand()).isEqualTo(3880);
    }

    @Test
    void testVerkaufeErfolg() {
        Getraenkautomat automat = new Getraenkautomat();
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_1, 1);
        einwurf.put(Muenzen.CENT_50, 1);
        assertThat(automat.verkaufe(Drinks.COLA, einwurf)).isTrue();
        assertThat(automat.getStockQuantity(Drinks.COLA)).isEqualTo(4);
    }

    @Test
    void testVerkaufeMitWechselgeld() {
        Getraenkautomat automat = new Getraenkautomat();
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_2, 1);
        assertThat(automat.verkaufe(Drinks.COLA, einwurf)).isTrue();
        assertThat(automat.getStockQuantity(Drinks.COLA)).isEqualTo(4);
        assertThat(automat.getCoinCount(Muenzen.CENT_50)).isEqualTo(9);
    }

    @Test
    void testVerkaufeKeinWechselgeld() {
        Getraenkautomat automat = new Getraenkautomat();
        for (Muenzen m : Muenzen.values()) {
            automat.setCoinCount(m, 0);
        }
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_2, 1);
        assertThat(automat.verkaufe(Drinks.COLA, einwurf)).isFalse();
        assertThat(automat.getStockQuantity(Drinks.COLA)).isEqualTo(5);
    }

    @Test
    void testVerkaufeZuWenig() {
        Getraenkautomat automat = new Getraenkautomat();
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.CENT_50, 1);
        assertThat(automat.verkaufe(Drinks.COLA, einwurf)).isFalse();
    }

    @Test
    void testVerkaufeAusverkauft() {
        Getraenkautomat automat = new Getraenkautomat();
        automat.setStockQuantity(Drinks.COLA, 0);
        Map<Muenzen, Integer> einwurf = new EnumMap<>(Muenzen.class);
        einwurf.put(Muenzen.EURO_2, 1);
        assertThat(automat.verkaufe(Drinks.COLA, einwurf)).isFalse();
    }
}
