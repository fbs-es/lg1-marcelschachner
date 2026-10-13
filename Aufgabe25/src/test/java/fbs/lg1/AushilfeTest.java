package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AushilfeTest {

    private Aushilfe aushilfe;

    @BeforeEach
    void prepare() {
        aushilfe = new Aushilfe("Marcel", "Schachner", 0.0);
    }

    @Test
    void testInit() {
        assertThat(aushilfe.showVorname()).isEqualTo("Marcel");
        assertThat(aushilfe.showNachname()).isEqualTo("Schachner");
        assertThat(aushilfe.showGehalt()).isEqualTo(0.0);
        assertThat(aushilfe.showArbeitszeiten()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Aushilfe("", "Schachner", 0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Aushilfe("Marcel", null, 0.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Aushilfe("Marcel", "Schachner", -5.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testArbeitszeitErfassen() {
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 120);

        assertThat(aushilfe.showArbeitszeiten()).hasSize(1);
        assertThat(aushilfe.showArbeitszeiten().get(0).showMinuten()).isEqualTo(120);
    }

    @Test
    void testArbeitszeitErfassenInvalid() {
        assertThatThrownBy(() -> aushilfe.arbeitszeitErfassen(null, 120))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testShowArbeitszeitenNichtVeraenderbar() {
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 120);

        assertThatThrownBy(() -> aushilfe.showArbeitszeiten().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(aushilfe.showArbeitszeiten()).hasSize(1);
    }

    @Test
    void testGehaltAnpassen() {
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 120);
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 2), 60);

        aushilfe.gehaltAnpassen();

        assertThat(aushilfe.showGehalt()).isEqualTo(45.0);
        assertThat(aushilfe.showArbeitszeiten().get(0).istAbgerechnet()).isTrue();
        assertThat(aushilfe.showArbeitszeiten().get(1).istAbgerechnet()).isTrue();
    }

    @Test
    void testGehaltAnpassenNurNichtAbgerechnet() {
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 1), 120);
        aushilfe.gehaltAnpassen();
        aushilfe.arbeitszeitErfassen(LocalDate.of(2026, 10, 2), 60);

        aushilfe.gehaltAnpassen();

        assertThat(aushilfe.showGehalt()).isEqualTo(45.0);
    }
}
