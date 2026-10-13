package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuszubildenderTest {

    private Auszubildender auszubildender;

    @BeforeEach
    void prepare() {
        auszubildender = new Auszubildender("Marcel", "Schachner", 900.0, "Informatiker", 2);
    }

    @Test
    void testInit() {
        assertThat(auszubildender.showVorname()).isEqualTo("Marcel");
        assertThat(auszubildender.showNachname()).isEqualTo("Schachner");
        assertThat(auszubildender.showGehalt()).isEqualTo(900.0);
        assertThat(auszubildender.showAusbildungsberuf()).isEqualTo("Informatiker");
        assertThat(auszubildender.showAusbildungsjahr()).isEqualTo(2);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Auszubildender("Marcel", "Schachner", 900.0, null, 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Auszubildender("Marcel", "Schachner", 900.0, " ", 2))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Auszubildender("Marcel", "Schachner", 900.0, "Informatiker", 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Auszubildender("Marcel", "Schachner", 900.0, "Informatiker", 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGehaltAnpassen() {
        auszubildender.gehaltAnpassen();

        assertThat(auszubildender.showGehalt()).isEqualTo(1000.0);
    }
}
