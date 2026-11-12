package de.bestellsystem.kunde.domain;

import jakarta.persistence.Embeddable;
import java.util.Locale;
import java.util.regex.Pattern;

/** E-Mail-Adresse, getrimmt und kleingeschrieben, damit Gleichheit D-13 entspricht. */
@Embeddable
public record Email(String wert) {

    public static final int MAXIMALE_LAENGE = 254;

    private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");

    public Email {
        if (wert == null || wert.isBlank()) {
            throw new IllegalArgumentException("email darf nicht leer sein");
        }
        wert = wert.strip().toLowerCase(Locale.ROOT);
        if (wert.length() > MAXIMALE_LAENGE) {
            throw new IllegalArgumentException(
                    "email darf höchstens " + MAXIMALE_LAENGE + " Zeichen haben");
        }
        if (!FORMAT.matcher(wert).matches()) {
            throw new IllegalArgumentException("email ist keine gültige E-Mail-Adresse: " + wert);
        }
    }
}
