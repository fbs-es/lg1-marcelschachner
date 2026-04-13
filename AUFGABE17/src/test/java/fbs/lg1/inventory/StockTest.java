package fbs.lg1.inventory;

import java.util.EnumMap;
import java.util.Map;
import fbs.lg1.Drinks;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class StockTest {

    @Test
    void testGetQuantity() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 5);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(5);
    }

    @Test
    void testSetQuantity() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 10);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(10);
    }

    @Test
    void testDispense() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 5);
        stock.dispense(Drinks.COLA);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(4);
    }

    @Test
    void testDispenseMultiple() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 5);
        stock.dispense(Drinks.COLA);
        stock.dispense(Drinks.COLA);
        stock.dispense(Drinks.COLA);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(2);
    }

    @Test
    void testPruefeNachfuellbedarfBelowMinimum() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 0);
        var result = stock.pruefeNachfuellbedarf();
        assertThat(result).containsKey(Drinks.COLA);
    }

    @Test
    void testPruefeNachfuellbedarfAboveMinimum() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 10);
        var result = stock.pruefeNachfuellbedarf();
        assertThat(result).doesNotContainKey(Drinks.COLA);
    }

    @Test
    void testErmittleBedarfExactMinimum() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, Drinks.COLA.getMinimumStock() - 1);
        var result = stock.ermittleBedarf();
        assertThat(result).containsKey(Drinks.COLA);
        assertThat(result.get(Drinks.COLA))
            .isEqualTo(Drinks.COLA.getMaximumStock() - (Drinks.COLA.getMinimumStock() - 1));
    }

    @Test
    void testErmittleBedarfNoNeed() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, Drinks.COLA.getMinimumStock());
        var result = stock.ermittleBedarf();
        assertThat(result).doesNotContainKey(Drinks.COLA);
    }

    @Test
    void testAuffuellen() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 0);
        Map<Drinks, Integer> lieferung = new EnumMap<>(Drinks.class);
        lieferung.put(Drinks.COLA, 10);
        stock.auffuellen(lieferung);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(10);
    }

    @Test
    void testAuffuellenUeberMaximum() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 0);
        Map<Drinks, Integer> lieferung = new EnumMap<>(Drinks.class);
        lieferung.put(Drinks.COLA, 999);
        stock.auffuellen(lieferung);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(Drinks.COLA.getMaximumStock());
    }

    @Test
    void testAuffuellenMultipleDrinks() {
        Stock stock = new Stock();
        stock.setQuantity(Drinks.COLA, 0);
        stock.setQuantity(Drinks.WATER, 0);
        Map<Drinks, Integer> lieferung = new EnumMap<>(Drinks.class);
        lieferung.put(Drinks.COLA, 5);
        lieferung.put(Drinks.WATER, 10);
        stock.auffuellen(lieferung);
        assertThat(stock.getQuantity(Drinks.COLA)).isEqualTo(5);
        assertThat(stock.getQuantity(Drinks.WATER)).isEqualTo(10);
    }
}
