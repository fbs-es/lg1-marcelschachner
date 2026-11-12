package de.bestellsystem.gemeinsam.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class BenachrichtigungTest {

    @ParameterizedTest
    @NullAndEmptySource
    void verlangtEmpfaengerUndBetreff(String leer) {
        assertThatThrownBy(() -> new Benachrichtigung(leer, "Betreff", "Text"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Benachrichtigung("a@b.de", leer, "Text"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void verlangtText() {
        assertThatThrownBy(() -> new Benachrichtigung("a@b.de", "Betreff", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
