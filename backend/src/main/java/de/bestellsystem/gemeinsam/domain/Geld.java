package de.bestellsystem.gemeinsam.domain;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Geldbetrag in ganzen Cent, Währung immer EUR (D-11).
 *
 * <p>Darf negativ sein, damit Zwischenergebnisse (z. B. Zahlungen minus Erstattungen) rechenbar
 * bleiben; "Beträge ≥ 0" (I-7) setzen die Entities durch.
 */
@Embeddable
public record Geld(long cent) implements Comparable<Geld> {

    public static final String WAEHRUNG = "EUR";
    public static final Geld NULL = new Geld(0);

    private static final BigDecimal HUNDERT = BigDecimal.valueOf(100);

    public static Geld ausCent(long cent) {
        return new Geld(cent);
    }

    public String waehrung() {
        return WAEHRUNG;
    }

    public Geld addiere(Geld andere) {
        return new Geld(Math.addExact(cent, andere.cent));
    }

    public Geld subtrahiere(Geld andere) {
        return new Geld(Math.subtractExact(cent, andere.cent));
    }

    public Geld multipliziere(int menge) {
        return new Geld(Math.multiplyExact(cent, menge));
    }

    /** Anteil in Prozent, kaufmännisch auf ganze Cent gerundet (D-11). */
    public Geld prozentsatz(BigDecimal prozent) {
        Objects.requireNonNull(prozent, "prozent");
        BigDecimal anteil =
                BigDecimal.valueOf(cent).multiply(prozent).divide(HUNDERT, 0, RoundingMode.HALF_UP);
        return new Geld(anteil.longValueExact());
    }

    public boolean istNegativ() {
        return cent < 0;
    }

    @Override
    public int compareTo(Geld andere) {
        return Long.compare(cent, andere.cent);
    }

    @Override
    public String toString() {
        String vorzeichen = cent < 0 ? "-" : "";
        long betrag = Math.abs(cent);
        return "%s%d,%02d %s".formatted(vorzeichen, betrag / 100, betrag % 100, WAEHRUNG);
    }
}
