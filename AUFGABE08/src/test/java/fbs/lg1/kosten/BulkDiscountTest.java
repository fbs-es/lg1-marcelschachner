package fbs.lg1.kosten;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BulkDiscountTest {

    private static final double DELTA = 0.01;

    private final BulkDiscount discount = new BulkDiscount();

    @Test
    void testDiscountRateMinimum() {
        assertEquals(0.0, discount.getDiscountRate(1));
    }

    @Test
    void testDiscountRateUpperBoundRule1() {
        assertEquals(0.0, discount.getDiscountRate(10));
    }

    @Test
    void testDiscountRateLowerBoundRule2() {
        assertEquals(0.05, discount.getDiscountRate(11));
    }

    @Test
    void testDiscountRateUpperBoundRule2() {
        assertEquals(0.05, discount.getDiscountRate(50));
    }

    @Test
    void testDiscountRateLowerBoundRule3() {
        assertEquals(0.10, discount.getDiscountRate(51));
    }

    @Test
    void testDiscountRateUpperBoundRule3() {
        assertEquals(0.10, discount.getDiscountRate(100));
    }

    @Test
    void testDiscountRateLowerBoundRule4() {
        assertEquals(0.20, discount.getDiscountRate(101));
    }

    @Test
    void testDiscountRateMidRule1() {
        assertEquals(0.0, discount.getDiscountRate(5));
    }

    @Test
    void testDiscountRateMidRule2() {
        assertEquals(0.05, discount.getDiscountRate(30));
    }

    @Test
    void testDiscountRateMidRule3() {
        assertEquals(0.10, discount.getDiscountRate(75));
    }

    @Test
    void testDiscountRateMidRule4() {
        assertEquals(0.20, discount.getDiscountRate(200));
    }

    @Test
    void testProcessingFeeT01() {
        assertEquals(2.50, discount.calculateProcessingFee(1), DELTA);
    }

    @Test
    void testProcessingFeeT02() {
        assertEquals(12.50, discount.calculateProcessingFee(5), DELTA);
    }

    @Test
    void testProcessingFeeT03() {
        assertEquals(25.00, discount.calculateProcessingFee(10), DELTA);
    }

    @Test
    void testProcessingFeeT04() {
        assertEquals(26.13, discount.calculateProcessingFee(11), DELTA);
    }

    @Test
    void testProcessingFeeT05() {
        assertEquals(71.25, discount.calculateProcessingFee(30), DELTA);
    }

    @Test
    void testProcessingFeeT06() {
        assertEquals(118.75, discount.calculateProcessingFee(50), DELTA);
    }

    @Test
    void testProcessingFeeT07() {
        assertEquals(114.75, discount.calculateProcessingFee(51), DELTA);
    }

    @Test
    void testProcessingFeeT08() {
        assertEquals(168.75, discount.calculateProcessingFee(75), DELTA);
    }

    @Test
    void testProcessingFeeT09() {
        assertEquals(225.00, discount.calculateProcessingFee(100), DELTA);
    }

    @Test
    void testProcessingFeeT10() {
        assertEquals(202.00, discount.calculateProcessingFee(101), DELTA);
    }

    @Test
    void testProcessingFeeT11() {
        assertEquals(400.00, discount.calculateProcessingFee(200), DELTA);
    }
}
