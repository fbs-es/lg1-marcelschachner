// TODO Drinks refactoren in inventory
package fbs.lg1;

public enum Drinks {
    COLA("Cola", "Coca Cola", 150, 5, 10),
    WATER("Wasser", "Mineralwasser", 100, 10, 20),
    ORANGE_JUICE("Orangensaft", "Frag nicht was für Saft, einfach Orangensaft", 200, 5, 10),
    ENERGY_DRINK("Energy Drink", "Energetische Flüssigkeit", 250, 3, 8),
    LEMONADE("Limonade", "Zitronenlimo", 130, 5, 10);

    private final String name;
    private final String description;
    private final int priceInCents;
    private final int minimumStock;
    private final int maximumStock;

    Drinks(String name, String description, int priceInCents, int minimumStock, int maximumStock) {
        this.name = name;
        this.description = description;
        this.priceInCents = priceInCents;
        this.minimumStock = minimumStock;
        this.maximumStock = maximumStock;
    }

    public String getName() {
        return name;
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

    public int getMaximumStock() {
        return maximumStock;
    }
}
