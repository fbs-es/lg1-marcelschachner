package fbs.lg1.cointray;

public enum Muenzen {
    EURO_2(200, "2 Euro"),
    EURO_1(100, "1 Euro"),
    CENT_50(50, "50 Cent"),
    CENT_20(20, "20 Cent"),
    CENT_10(10, "10 Cent"),
    CENT_5(5, "5 Cent"),
    CENT_2(2, "2 Cent"),
    CENT_1(1, "1 Cent");

    private final int valueInCents;
    private final String description;

    Muenzen(int valueInCents, String description) {
        this.valueInCents = valueInCents;
        this.description = description;
    }

    public int getValueInCents() {
        return valueInCents;
    }

    public String getDescription() {
        return description;
    }

    public static boolean isValid(int cents) {
        return fromCents(cents) != null;
    }

    public static Muenzen fromCents(int cents) {
        for (Muenzen m : values()) {
            if (m.valueInCents == cents)
                return m;
        }
        return null;
    }
}
