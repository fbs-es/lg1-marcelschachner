package fbs.lg1.kosten;

public enum Size {
    S(5.0, 10.5, 40.0, 20.0, 20.0),
    M(10.0, 21.0, 80.0, 40.0, 40.0),
    L(15.0, 31.5, 120.0, 60.0, 60.0);

    private final double basePrice;
    private final double maxWeight;
    private final double maxLength;
    private final double maxWidth;
    private final double maxHeight;

    Size(double basePrice, double maxWeight, double maxLength, double maxWidth, double maxHeight) {
        this.basePrice = basePrice;
        this.maxWeight = maxWeight;
        this.maxLength = maxLength;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public boolean check(double weight, double length, double width, double height) {
        return weight <= maxWeight && length <= maxLength && width <= maxWidth && height <= maxHeight;
    }


}
