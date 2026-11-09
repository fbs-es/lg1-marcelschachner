package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadioprogrammTest {

    private Radioprogramm programm;
    private Radiosendung morgenshow;
    private Radiosendung vormittag;

    @BeforeEach
    void prepare() {
        programm = new Radioprogramm(LocalDate.of(2026, 11, 9));
        morgenshow =
                new Radiosendung(
                        "Morgenshow", "Marcel Schachner", LocalTime.of(8, 0), LocalTime.of(9, 0), 3600);
        vormittag =
                new Radiosendung(
                        "Vormittag", "Max Mustermann", LocalTime.of(9, 0), LocalTime.of(10, 0), 3600);
    }

    @Test
    void testInit() {
        assertThat(programm.showDatum()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(programm.showSendungen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Radioprogramm(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSendungHinzufuegenSortiert() {
        programm.sendungHinzufuegen(vormittag);
        programm.sendungHinzufuegen(morgenshow);

        assertThat(programm.showSendungen()).hasSize(2);
        assertThat(programm.showSendungen().get(0)).isSameAs(morgenshow);
        assertThat(programm.showSendungen().get(1)).isSameAs(vormittag);
    }

    @Test
    void testSendungHinzufuegenUeberschneidung() {
        programm.sendungHinzufuegen(morgenshow);
        Radiosendung ueberlappend =
                new Radiosendung(
                        "Zwischendurch",
                        "Max Mustermann",
                        LocalTime.of(8, 30),
                        LocalTime.of(9, 30),
                        3600);

        assertThatThrownBy(() -> programm.sendungHinzufuegen(ueberlappend))
                .isInstanceOf(IllegalStateException.class);
        assertThat(programm.showSendungen()).hasSize(1);
    }

    @Test
    void testSendungHinzufuegenInvalid() {
        programm.sendungHinzufuegen(morgenshow);

        assertThatThrownBy(() -> programm.sendungHinzufuegen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> programm.sendungHinzufuegen(morgenshow))
                .isInstanceOf(IllegalStateException.class);
    }
}
