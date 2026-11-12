package de.bestellsystem.gemeinsam.domain;

import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.Set;

/** Postanschrift als Wertobjekt, z. B. als Lieferadress-Snapshot einer Bestellung (D-08). */
@Embeddable
public record Anschrift(String strasse, String stadt, String postleitzahl, String land) {

    public static final int STRASSE_MAXIMALE_LAENGE = 200;
    public static final int STADT_MAXIMALE_LAENGE = 100;
    public static final int POSTLEITZAHL_MAXIMALE_LAENGE = 20;

    private static final Set<String> LAENDERCODES = Set.of(Locale.getISOCountries());

    public Anschrift {
        strasse = hoechstens(pflichtfeld(strasse, "strasse"), "strasse", STRASSE_MAXIMALE_LAENGE);
        stadt = hoechstens(pflichtfeld(stadt, "stadt"), "stadt", STADT_MAXIMALE_LAENGE);
        postleitzahl =
                hoechstens(
                        pflichtfeld(postleitzahl, "postleitzahl"),
                        "postleitzahl",
                        POSTLEITZAHL_MAXIMALE_LAENGE);
        land = pflichtfeld(land, "land").toUpperCase(Locale.ROOT);
        if (!LAENDERCODES.contains(land)) {
            throw new IllegalArgumentException(
                    "land muss ein ISO-3166-Alpha-2-Code sein, war: " + land);
        }
    }

    private static String pflichtfeld(String wert, String name) {
        if (wert == null || wert.isBlank()) {
            throw new IllegalArgumentException(name + " darf nicht leer sein");
        }
        return wert.strip();
    }

    private static String hoechstens(String wert, String name, int maximaleLaenge) {
        if (wert.length() > maximaleLaenge) {
            throw new IllegalArgumentException(
                    name + " darf höchstens " + maximaleLaenge + " Zeichen haben");
        }
        return wert;
    }
}
