package fbs.lg1.kosten;

public class BulkDiscount {

    private static final double PROCESSING_FEE_PER_PACKAGE = 2.50;

    private static final int[] THRESHOLDS = {1, 11, 51, 101};
    private static final double[] RATES = {0.0, 0.05, 0.10, 0.20};

    public double getDiscountRate(int quantity) {
        double rate = 0.0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (quantity >= THRESHOLDS[i]) {
                rate = RATES[i];
            }
        }
        return rate;
    }

    public double calculateProcessingFee(int quantity) {
        double discount = getDiscountRate(quantity);
        return quantity * PROCESSING_FEE_PER_PACKAGE * (1 - discount);
    }
}
