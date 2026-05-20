package fbs.lg1;

import java.util.EnumMap;
import java.util.Map;

import fbs.lg1.cointray.Muenzen;
import fbs.lg1.inventory.Drinks;
import fbs.lg1.inventory.Inventory;
import fbs.lg1.util.CurrencyFormatter;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class AppTest {

    @Test
    void testMuenzenWerte() {
        assertThat(Muenzen.CENT_1.getValueInCents()).isEqualTo(1);
        assertThat(Muenzen.CENT_50.getValueInCents()).isEqualTo(50);
        assertThat(Muenzen.EURO_1.getValueInCents()).isEqualTo(100);
        assertThat(Muenzen.EURO_2.getValueInCents()).isEqualTo(200);
    }

    @Test
    void testDrinksFelder() {
        assertThat(Drinks.COLA.getName()).isEqualTo("Cola");
        assertThat(Drinks.COLA.getPriceInCents()).isGreaterThan(0);
        assertThat(Drinks.COLA.getMinimumStock()).isGreaterThan(0);
    }

    @Test
    void testCurrencyFormatter() {
        assertThat(CurrencyFormatter.toEuroFormat(100)).isEqualTo("1,00 €");
        assertThat(CurrencyFormatter.toEuroFormat(150)).isEqualTo("1,50 €");
    }

    @Test
    void testInventoryInitialisierung() {
        Inventory inventory = new Inventory();
        for (Drinks drink : Drinks.values()) {
            assertThat(inventory.getStockQuantity(drink)).isEqualTo(0);
        }
        for (Muenzen coin : Muenzen.values()) {
            assertThat(inventory.getCoinCount(coin)).isEqualTo(0);
        }
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
