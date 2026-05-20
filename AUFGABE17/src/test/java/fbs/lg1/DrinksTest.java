package fbs.lg1;

import fbs.lg1.inventory.Drinks;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class DrinksTest {

    @Test
    void testDrinksFelder() {
        assertThat(Drinks.COLA.getName()).isEqualTo("Cola");
        assertThat(Drinks.COLA.getPriceInCents()).isGreaterThan(0);
        assertThat(Drinks.COLA.getMinimumStock()).isGreaterThan(0);
    }

    @Test
    void testAllDrinksHaveValidProperties() {
        for (Drinks drink : Drinks.values()) {
            assertThat(drink.getName()).isNotEmpty();
            assertThat(drink.getDescription()).isNotEmpty();
            assertThat(drink.getPriceInCents()).isGreaterThan(0);
            assertThat(drink.getMinimumStock()).isGreaterThan(0);
            assertThat(drink.getMaximumStock()).isGreaterThan(drink.getMinimumStock());
        }
    }

    @Test
    void testDrinksDescription() {
        assertThat(Drinks.WATER.getDescription()).isEqualTo("Mineralwasser");
        assertThat(Drinks.ORANGE_JUICE.getDescription()).isEqualTo("Frag nicht was für Saft, einfach Orangensaft");
    }
}
