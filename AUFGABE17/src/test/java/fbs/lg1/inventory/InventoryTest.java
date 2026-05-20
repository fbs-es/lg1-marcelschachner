package fbs.lg1.inventory;

import fbs.lg1.cointray.Muenzen;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class InventoryTest {

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
    void testGetSetStockQuantity() {
        Inventory inventory = new Inventory();
        inventory.setStockQuantity(Drinks.COLA, 5);
        assertThat(inventory.getStockQuantity(Drinks.COLA)).isEqualTo(5);
    }

    @Test
    void testGetSetCoinCount() {
        Inventory inventory = new Inventory();
        inventory.setCoinCount(Muenzen.EURO_1, 10);
        assertThat(inventory.getCoinCount(Muenzen.EURO_1)).isEqualTo(10);
    }

    @Test
    void testDispense() {
        Inventory inventory = new Inventory();
        inventory.setStockQuantity(Drinks.COLA, 5);
        inventory.dispense(Drinks.COLA);
        assertThat(inventory.getStockQuantity(Drinks.COLA)).isEqualTo(4);
    }

    @Test
    void testBerechneKassenstand() {
        Inventory inventory = new Inventory();
        inventory.setCoinCount(Muenzen.EURO_2, 1);
        inventory.setCoinCount(Muenzen.EURO_1, 1);
        assertThat(inventory.berechneKassenstand()).isEqualTo(300);
    }
}
