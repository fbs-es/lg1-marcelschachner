package fbs.lg1;

public enum Coin {
    EURO_2(200, "2 Euro"),
    EURO_1(100, "1 Euro"),
    CENT_50(50, "50 Cent"),
    CENT_20(20, "20 Cent"),
    CENT_10(10, "10 Cent"),
    CENT_5(5, "5 Cent"),
    CENT_2(2, "2 Cent"),
    CENT_1(1, "1 Cent");

    int valueInCents;
    String label;

    private Coin(int valueInCents, String label) {
        this.valueInCents = valueInCents;
        this.label = label;
    }

    public String toEuroFormat() {
        return String.format("%d,%02d €", valueInCents / 100, valueInCents % 100);
    }

    // getter methode fehlt
    public int getValueInCents() {
        return valueInCents;
    }

    public String getLabel() {
        return label;
    }
}
