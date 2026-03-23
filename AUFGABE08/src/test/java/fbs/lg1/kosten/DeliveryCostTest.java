package fbs.lg1.kosten;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class DeliveryCostTest {

    private final DeliveryCost cost = new DeliveryCost();

    @Test
    void testBasePriceS() {
        assertEquals(5.0, cost.calculateBasePrice(Size.S));
    }

    @Test
    void testBasePriceM() {
        assertEquals(10.0, cost.calculateBasePrice(Size.M));
    }

    @Test
    void testBasePriceL() {
        assertEquals(15.0, cost.calculateBasePrice(Size.L));
    }

    @Test
    void testAdditionalCostInlandNormal() {
        assertEquals(0.0, cost.calculateAdditionalCost(ShippingDestination.INLAND, ShippingType.NORMAL));
    }

    @Test
    void testAdditionalCostInlandExpress() {
        assertEquals(10.0, cost.calculateAdditionalCost(ShippingDestination.INLAND, ShippingType.EXPRESS));
    }

    @Test
    void testAdditionalCostAuslandNormal() {
        assertEquals(10.0, cost.calculateAdditionalCost(ShippingDestination.AUSLAND, ShippingType.NORMAL));
    }

    @Test
    void testAdditionalCostAuslandExpress() {
        assertEquals(25.0, cost.calculateAdditionalCost(ShippingDestination.AUSLAND, ShippingType.EXPRESS));
    }
}
