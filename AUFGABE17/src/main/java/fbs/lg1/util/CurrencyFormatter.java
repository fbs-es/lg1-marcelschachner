package fbs.lg1.util;

import java.util.Locale;

public class CurrencyFormatter {

    public static String toEuroFormat(int cents) {
        return String.format(Locale.GERMANY, "%.2f €", cents / 100.0);
    }
}
