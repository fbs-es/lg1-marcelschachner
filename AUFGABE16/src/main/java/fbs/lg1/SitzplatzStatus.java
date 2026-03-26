package fbs.lg1;

public enum SitzplatzStatus {
    FREI('F'), RESERVIERT('R'), VORGEMERKT('V');

    private final char symbol;

    SitzplatzStatus(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}
