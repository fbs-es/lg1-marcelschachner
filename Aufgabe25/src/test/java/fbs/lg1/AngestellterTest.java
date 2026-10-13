package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AngestellterTest {

    private Angestellter angestellter;

    @BeforeEach
    void prepare() {
        angestellter = new Angestellter("Marcel", "Schachner", 3000.0, 2020, 25);
    }

    @Test
    void testInit() {
        assertThat(angestellter.showVorname()).isEqualTo("Marcel");
        assertThat(angestellter.showNachname()).isEqualTo("Schachner");
        assertThat(angestellter.showGehalt()).isEqualTo(3000.0);
        assertThat(angestellter.showEinstellungsjahr()).isEqualTo(2020);
        assertThat(angestellter.showUrlaubsanspruch()).isEqualTo(25);
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Angestellter(null, "Schachner", 3000.0, 2020, 25))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Angestellter("Marcel", " ", 3000.0, 2020, 25))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Angestellter("Marcel", "Schachner", -1.0, 2020, 25))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Angestellter("Marcel", "Schachner", 3000.0, 0, 25))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Angestellter("Marcel", "Schachner", 3000.0, 2020, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testGehaltAnpassen() {
        angestellter.gehaltAnpassen();

        assertThat(angestellter.showGehalt()).isEqualTo(3090.0);
    }
}
