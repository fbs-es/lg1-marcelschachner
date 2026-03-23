package fbs.lg1.kosten;

public class DeliveryCost {

    public Size determineSize(double weight, double length, double width, double height) {
        for (Size s : Size.values()) {
            if (s.check(weight, length, width, height))
                return s;
        }
        return Size.L;
    }

    public double calculateBasePrice(Size size) {
        return size.getBasePrice();
    }

    public double calculateAdditionalCost(ShippingDestination destination, ShippingType type) {
        if (destination == ShippingDestination.INLAND && type == ShippingType.NORMAL)
            return 0.0;
        if (destination == ShippingDestination.INLAND && type == ShippingType.EXPRESS)
            return 10.0;
        if (destination == ShippingDestination.AUSLAND && type == ShippingType.NORMAL)
            return 10.0;
        return 25.0;
    }
}
