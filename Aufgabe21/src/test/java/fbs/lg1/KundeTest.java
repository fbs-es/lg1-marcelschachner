package fbs.lg1;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KundeTest {

    private FakeClock clock;
    private Scooter scooter;
    private Kunde kunde;

    @BeforeEach
    void prepare() {
        clock = FakeClock.start();
        scooter = new Scooter("S-001");
        kunde = new Kunde("K-001", "Marcel Schachner", "schachner.marcel@icloud.com", 10.00, clock);
    }

    private void fahre(int minuten) {
        kunde.fahrtStarten(scooter);
        clock.advance(Duration.ofMinutes(minuten));
        kunde.fahrtBeenden();
    }

    @Test
    void testInit() {
        assertThat(kunde.showKundenId()).isEqualTo("K-001");
        assertThat(kunde.showName()).isEqualTo("Marcel Schachner");
        assertThat(kunde.showEmail()).isEqualTo("schachner.marcel@icloud.com");
        assertThat(kunde.showGuthaben()).isEqualTo(10.00);
        assertThat(kunde.istKundeGesperrt()).isFalse();
        assertThat(kunde.istMahnungOffen()).isFalse();
        assertThat(kunde.showAusleihungen()).isEmpty();
    }

    @Test
    void testInitInvalid() {
        assertThatThrownBy(() -> new Kunde(null, "Marcel", "schachner.marcel@icloud.com", 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde(" ", "Marcel", "schachner.marcel@icloud.com", 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", null, "schachner.marcel@icloud.com", 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", "", "schachner.marcel@icloud.com", 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", "Marcel", null, 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Kunde("K-1", "Marcel", "keine-email", 10, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testStartguthabenTooLow() {
        assertThatThrownBy(() -> new Kunde("K-1", "Marcel", "schachner.marcel@icloud.com", 9.99, clock))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testFahrtStarten() {
        assertThat(kunde.fahrtStarten(scooter)).isTrue();
        assertThat(kunde.showAusleihungen()).hasSize(1);
        assertThat(kunde.showAusleihungen().get(0).showScooter()).isSameAs(scooter);
        assertThat(kunde.showAusleihungen().get(0).istBeendet()).isFalse();
        assertThat(scooter.checkIstGesperrt()).isFalse();
    }

    @Test
    void testFahrtStartenNullScooter() {
        assertThat(kunde.fahrtStarten(null)).isFalse();
    }

    @Test
    void testFahrtStartenAlreadyHasScooter() {
        kunde.fahrtStarten(scooter);
        Scooter other = new Scooter("S-002");
        assertThat(kunde.fahrtStarten(other)).isFalse();
        assertThat(other.checkIstGesperrt()).isTrue();
        assertThat(kunde.showAusleihungen()).hasSize(1);
    }

    @Test
    void testFahrtStartenScooterInUse() {
        Kunde other = new Kunde("K-002", "Anna Muster", "anna@example.com", 20.00, clock);
        other.fahrtStarten(scooter);
        assertThat(kunde.fahrtStarten(scooter)).isFalse();
        assertThat(kunde.showAusleihungen()).isEmpty();
    }

    @Test
    void testFahrtStartenAkkuTooLow() {
        scooter.akkuVerringern(85);
        assertThat(kunde.fahrtStarten(scooter)).isFalse();
        assertThat(scooter.checkIstGesperrt()).isTrue();
    }

    @Test
    void testFahrtStartenAkkuJustEnough() {
        scooter.akkuVerringern(84);
        assertThat(kunde.fahrtStarten(scooter)).isTrue();
    }

    @Test
    void testFahrtStartenGuthabenTooLow() {
        fahre(46);
        assertThat(kunde.showGuthaben()).isEqualTo(0.80);
        assertThat(kunde.fahrtStarten(scooter)).isFalse();
    }

    @Test
    void testFahrtStartenGuthabenExactlyOne() {
        fahre(45);
        assertThat(kunde.showGuthaben()).isEqualTo(1.00);
        assertThat(kunde.fahrtStarten(scooter)).isTrue();
    }

    @Test
    void testFahrtStartenKundeGesperrt() {
        fahre(60);
        kunde.kontoAusgleichen(1.00);
        assertThat(kunde.istKundeGesperrt()).isTrue();
        assertThat(kunde.fahrtStarten(scooter)).isFalse();
    }

    @Test
    void testFahrtBeenden() {
        fahre(10);
        assertThat(kunde.showGuthaben()).isEqualTo(8.00);
        assertThat(kunde.showAusleihungen().get(0).istBeendet()).isTrue();
        assertThat(scooter.checkIstGesperrt()).isTrue();
        assertThat(scooter.checkAkkustand()).isEqualTo(90);
        assertThat(kunde.istKundeGesperrt()).isFalse();
        assertThat(kunde.istMahnungOffen()).isFalse();
    }

    @Test
    void testFahrtBeendenWithoutScooter() {
        assertThatThrownBy(kunde::fahrtBeenden).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void testFahrtBeendenZeroMinutes() {
        kunde.fahrtStarten(scooter);
        assertThatThrownBy(kunde::fahrtBeenden).isInstanceOf(IllegalStateException.class);
        assertThat(kunde.showGuthaben()).isEqualTo(10.00);
        assertThat(kunde.showAusleihungen().get(0).istBeendet()).isFalse();
    }

    @Test
    void testFahrtBeendenGuthabenExactlyZero() {
        fahre(50);
        assertThat(kunde.showGuthaben()).isEqualTo(0.00);
        assertThat(kunde.istKundeGesperrt()).isFalse();
        assertThat(kunde.istMahnungOffen()).isFalse();
    }

    @Test
    void testFahrtBeendenGuthabenNegative() {
        fahre(60);
        assertThat(kunde.showGuthaben()).isEqualTo(-2.00);
        assertThat(kunde.istKundeGesperrt()).isTrue();
        assertThat(kunde.istMahnungOffen()).isTrue();
    }

    @Test
    void testHistorie() {
        fahre(5);
        fahre(10);
        assertThat(kunde.showAusleihungen()).hasSize(2);
        assertThat(kunde.showAusleihungen().get(0).showKosten()).isEqualTo(1.00);
        assertThat(kunde.showAusleihungen().get(1).showKosten()).isEqualTo(2.00);
        assertThat(kunde.showAusleihungen().get(0).showEndzeit())
                .isBefore(kunde.showAusleihungen().get(1).showStartzeit().plusSeconds(1));
    }

    @Test
    void testKontoAusgleichen() {
        kunde.kontoAusgleichen(5.50);
        assertThat(kunde.showGuthaben()).isEqualTo(15.50);
    }

    @Test
    void testKontoAusgleichenInvalid() {
        assertThatThrownBy(() -> kunde.kontoAusgleichen(0)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> kunde.kontoAusgleichen(-5)).isInstanceOf(IllegalArgumentException.class);
        assertThat(kunde.showGuthaben()).isEqualTo(10.00);
    }

    @Test
    void testKontoAusgleichenUnlocks() {
        fahre(60);
        kunde.kontoAusgleichen(5.00);
        assertThat(kunde.showGuthaben()).isEqualTo(3.00);
        assertThat(kunde.istKundeGesperrt()).isFalse();
        assertThat(kunde.istMahnungOffen()).isFalse();
        assertThat(kunde.fahrtStarten(scooter)).isTrue();
    }

    @Test
    void testKontoAusgleichenToExactlyZeroStaysLocked() {
        fahre(60);
        kunde.kontoAusgleichen(2.00);
        assertThat(kunde.showGuthaben()).isEqualTo(0.00);
        assertThat(kunde.istKundeGesperrt()).isTrue();
        assertThat(kunde.istMahnungOffen()).isTrue();
    }

    private String captureOutput(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return output.toString();
    }

    @Test
    void testMahnungSentWhenGuthabenNegative() {
        String output = captureOutput(() -> fahre(60));
        assertThat(output).contains("Mahnung").contains("schachner.marcel@icloud.com").contains("-2.0");
    }

    @Test
    void testNoMahnungWhenGuthabenNotNegative() {
        String output = captureOutput(() -> fahre(50));
        assertThat(output).doesNotContain("Mahnung");
    }
}
