package fbs.lg1;

public enum Drink {
    COLA("Cola", "Klassische Cola, 0,33l", 150, 5),
    WATER("Wasser", "Stilles Mineralwasser, 0,5l", 100, 10),
    JUICE("Juice", "Fruchtiger Saft, 0,25l", 120, 3);

    String label;
    String description;
    int priceInCents;
    int minimumStock;

    private Drink(String label, String description, int priceInCents, int minimumStock) {
        this.label = label;
        this.description = description;
        this.priceInCents = priceInCents;
        this.minimumStock = minimumStock;
    }

    // getter
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public int getPriceInCents() {
        return priceInCents;
    }

    public int getMinimumStock() {
        return minimumStock;
    }
}
