package de.bestellsystem.gemeinsam.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class AnschriftTest {

    @Test
    void normalisiertEingaben() {
        Anschrift anschrift = new Anschrift("  Hauptstraße 1 ", " Berlin ", " 10115 ", " de ");

        assertThat(anschrift.strasse()).isEqualTo("Hauptstraße 1");
        assertThat(anschrift.stadt()).isEqualTo("Berlin");
        assertThat(anschrift.postleitzahl()).isEqualTo("10115");
        assertThat(anschrift.land()).isEqualTo("DE");
    }

    @Test
    void istWertobjekt() {
        assertThat(new Anschrift("Weg 2", "Wien", "1010", "AT"))
                .isEqualTo(new Anschrift("Weg 2", "Wien", "1010", "at"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void verlangtAlleFelder(String leer) {
        assertThatThrownBy(() -> new Anschrift(leer, "Berlin", "10115", "DE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strasse");
        assertThatThrownBy(() -> new Anschrift("Weg 1", leer, "10115", "DE"))
                .hasMessageContaining("stadt");
        assertThatThrownBy(() -> new Anschrift("Weg 1", "Berlin", leer, "DE"))
                .hasMessageContaining("postleitzahl");
        assertThatThrownBy(() -> new Anschrift("Weg 1", "Berlin", "10115", leer))
                .hasMessageContaining("land");
    }

    @ParameterizedTest
    @ValueSource(strings = {"XX", "DEU", "Deutschland"})
    void lehntUnbekanntenLaendercodeAb(String land) {
        assertThatThrownBy(() -> new Anschrift("Weg 1", "Berlin", "10115", land))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ISO-3166");
    }

    @Test
    void begrenztFeldlaengen() {
        String strasse = "s".repeat(Anschrift.STRASSE_MAXIMALE_LAENGE);
        String stadt = "s".repeat(Anschrift.STADT_MAXIMALE_LAENGE);
        String postleitzahl = "1".repeat(Anschrift.POSTLEITZAHL_MAXIMALE_LAENGE);
        new Anschrift(strasse, stadt, postleitzahl, "DE");

        assertThatThrownBy(() -> new Anschrift(strasse + "s", "Berlin", "10115", "DE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("strasse");
        assertThatThrownBy(() -> new Anschrift("Weg 1", stadt + "s", "10115", "DE"))
                .hasMessageContaining("stadt");
        assertThatThrownBy(() -> new Anschrift("Weg 1", "Berlin", postleitzahl + "1", "DE"))
                .hasMessageContaining("postleitzahl");
    }
}
