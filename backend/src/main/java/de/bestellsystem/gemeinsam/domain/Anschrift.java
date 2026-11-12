package de.bestellsystem.gemeinsam.domain;

import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.Set;

/** Postanschrift als Wertobjekt, z. B. als Lieferadress-Snapshot einer Bestellung (D-08). */
@Embeddable
public record Anschrift(String strasse, String stadt, String postleitzahl, String land) {

    private static final Set<String> LAENDERCODES = Set.of(Locale.getISOCountries());

    public Anschrift {
        strasse = pflichtfeld(strasse, "strasse");
        stadt = pflichtfeld(stadt, "stadt");
        postleitzahl = pflichtfeld(postleitzahl, "postleitzahl");
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
}
