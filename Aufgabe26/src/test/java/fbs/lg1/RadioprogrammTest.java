package fbs.lg1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RadioprogrammTest {

    private Radioprogramm programm;
    private Radiosendung sendung;

    @BeforeEach
    void prepare() {
        programm = new Radioprogramm(LocalDate.of(2026, 10, 20));
        Moderator moderator = new Moderator("Marcel Schachner");
        sendung =
                new Radiosendung(
                        "Morgenshow", LocalTime.of(8, 0), LocalTime.of(9, 0), 3600, moderator);
    }

    @Test
    void testInit() {
        assertThat(programm.showDatum()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(programm.showSendungen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Radioprogramm(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testSendungHinzufuegen() {
        programm.sendungHinzufuegen(sendung);

        assertThat(programm.showSendungen()).hasSize(1);
        assertThat(programm.showSendungen().get(0)).isSameAs(sendung);
    }

    @Test
    void testSendungHinzufuegenInvalid() {
        programm.sendungHinzufuegen(sendung);

        assertThatThrownBy(() -> programm.sendungHinzufuegen(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> programm.sendungHinzufuegen(sendung))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testSendungEntfernen() {
        programm.sendungHinzufuegen(sendung);

        programm.sendungEntfernen(sendung);

        assertThat(programm.showSendungen()).isEmpty();
    }

    @Test
    void testSendungEntfernenNichtVorhanden() {
        assertThatThrownBy(() -> programm.sendungEntfernen(sendung))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testShowSendungenNichtVeraenderbar() {
        programm.sendungHinzufuegen(sendung);

        assertThatThrownBy(() -> programm.showSendungen().clear())
                .isInstanceOf(UnsupportedOperationException.class);
        assertThat(programm.showSendungen()).hasSize(1);
    }
}
