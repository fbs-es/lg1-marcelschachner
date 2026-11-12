package de.bestellsystem.kunde.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @Test
    @DisplayName("D-13: E-Mail wird getrimmt und kleingeschrieben")
    void normalisiert() {
        assertThat(new Email(" Anna@Example.DE ").wert()).isEqualTo("anna@example.de");
    }

    @Test
    @DisplayName("US-01.1 AK3: Gleichheit unabhängig von Groß-/Kleinschreibung")
    void gleichOhneGrossKleinschreibung() {
        assertThat(new Email(" Anna@Example.DE ")).isEqualTo(new Email("anna@example.de"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(
            strings = {"   ", "anna", "anna@", "@example.de", "anna@example", "an na@example.de"})
    @DisplayName("D-16: ungültige E-Mail wird abgelehnt")
    void lehntUngueltigeAb(String email) {
        assertThatThrownBy(() -> new Email(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("D-16: E-Mail höchstens 254 Zeichen")
    void hoechstens254Zeichen() {
        String domain = "@example.de";
        String grenze = "a".repeat(Email.MAXIMALE_LAENGE - domain.length()) + domain;

        assertThat(new Email(grenze).wert()).hasSize(Email.MAXIMALE_LAENGE);
        assertThatThrownBy(() -> new Email("a" + grenze))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("254");
    }
}
